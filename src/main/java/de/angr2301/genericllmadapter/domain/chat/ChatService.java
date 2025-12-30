package de.angr2301.genericllmadapter.domain.chat;

import de.angr2301.genericllmadapter.dto.analysis.AnalysisRequest;
import de.angr2301.genericllmadapter.dto.analysis.AnalysisResponse;
import de.angr2301.genericllmadapter.dto.chat.ChatReply;
import de.angr2301.genericllmadapter.domain.user.User;
import de.angr2301.genericllmadapter.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final InteractionLogRepository interactionLogRepository;
    private final UserRepository userRepository;
    private final LlmProviderFactory llmProviderFactory;
    private final de.angr2301.genericllmadapter.feign.AnalysisClient analysisClient;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @Transactional
    public ChatSession createSession(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ChatSession session = ChatSession.builder()
                .user(user)
                .build();

        return chatSessionRepository.save(session);
    }

    public List<ChatSession> getUserSessions(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return chatSessionRepository.findByUserIdOrderByStartedAtDesc(user.getId());
    }

    public List<InteractionLog> getMessages(UUID sessionId, String email) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!session.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("Unauthorized access to session");
        }

        return interactionLogRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }

    @Transactional
    public ChatReply sendMessage(UUID sessionId, String content, String provider, String email) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!session.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("Unauthorized access to session");
        }

        User user = session.getUser();
        String apiKey = getApiKeyForProvider(user, provider);
        log.debug("Setting up LLM call for provider: {}", provider);

        // 1. Save User Message
        InteractionLog userLog = InteractionLog.builder()
                .session(session)
                .role("USER")
                .content(content)
                .provider(provider)
                .build();
        interactionLogRepository.save(userLog);

        // 2. Prepare Context (History) using standardized LlmMessage format
        List<InteractionLog> history = interactionLogRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        List<LlmMessage> messages = new ArrayList<>();

        for (InteractionLog logEntry : history) {
            String role = "USER".equalsIgnoreCase(logEntry.getRole()) ? "user" : "assistant";
            messages.add(new LlmMessage(role, List.of(new LlmMessage.Part(logEntry.getContent()))));
        }

        // 3. Call LLM using LlmClient
        log.debug("Creating chat model for provider: {}", provider);
        LlmClient llmClient = llmProviderFactory.createChatModel(provider, apiKey);
        log.debug("Calling LLM...");

        LlmResponse response = llmClient.generateContent(messages);

        // Validate response
        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            throw new RuntimeException("Invalid LLM response");
        }

        LlmResponse.Candidate firstCandidate = response.getCandidates().get(0);
        String fullReply = firstCandidate.getContent().getParts().get(0).getText();

        log.debug("LLM responded with content length: {}", fullReply.length());

        // 4. Extract Reasoning (Thinking)
        String reasoning = null;
        String contentOnly = fullReply;

        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("<think>(.*?)</think>", java.util.regex.Pattern.DOTALL).matcher(fullReply);
        if (matcher.find()) {
            reasoning = matcher.group(1).trim();
            contentOnly = fullReply.replace(matcher.group(0), "").trim();
        }

        // 5. Call Analysis Service
        log.debug("Triggering analysis for session: {}", sessionId);
        Map<String, Object> metrics = null;
        try {
            AnalysisRequest analysisRequest = new AnalysisRequest(
                    sessionId.toString(), content, contentOnly, "guide");
            AnalysisResponse analysisResponse = analysisClient.analyze(analysisRequest);
            metrics = analysisResponse.getMetrics();
        } catch (Exception e) {
            log.error("Analysis service call failed", e);
        }

        String metricsJson = null;
        if (metrics != null) {
            try {
                metricsJson = objectMapper.writeValueAsString(metrics);
            } catch (Exception e) {
                log.error("Failed to serialize metrics", e);
            }
        }

        // 6. Save Assistant Message
        InteractionLog botLog = InteractionLog.builder()
                .session(session)
                .role("ASSISTANT")
                .content(contentOnly)
                .reasoning(reasoning)
                .provider(provider)
                .metrics(metricsJson)
                .build();
        interactionLogRepository.save(botLog);

        // Update session timestamp
        session.setLastInteractionAt(java.time.LocalDateTime.now());
        chatSessionRepository.save(session);

        return new ChatReply(content, contentOnly, reasoning, metrics);
    }

    private String getApiKeyForProvider(User user, String provider) {
        return switch (provider.toUpperCase()) {
            case "OPENAI" -> user.getOpenaiKey();
            case "DEEPSEEK" -> user.getDeepseekKey();
            case "HUGGINGFACE" -> user.getHuggingfaceKey();
            case "LOCAL_VLLM" -> "dummy"; // Usually not needed for local vLLM
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };
    }
}
