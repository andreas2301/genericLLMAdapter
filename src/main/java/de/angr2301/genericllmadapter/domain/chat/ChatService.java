package de.angr2301.genericllmadapter.domain.chat;

import de.angr2301.genericllmadapter.domain.user.User;
import de.angr2301.genericllmadapter.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final SessionRepository sessionRepository;
    private final InteractionLogRepository interactionLogRepository;
    private final UserRepository userRepository;
    private final LlmProviderFactory llmProviderFactory;

    @Transactional
    public Session createSession(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Session session = Session.builder()
                .user(user)
                .build();

        return sessionRepository.save(session);
    }

    public List<Session> getUserSessions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return sessionRepository.findByUserIdOrderByStartedAtDesc(user.getId());
    }

    public List<InteractionLog> getMessages(UUID sessionId, String email) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!session.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("Unauthorized access to session");
        }

        return interactionLogRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }

    @Transactional
    public String sendMessage(UUID sessionId, String content, String provider, String email) {
        Session session = sessionRepository.findById(sessionId)
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
        if (response == null) {
            log.error("Received null response from LLM provider: {}", provider);
            throw new RuntimeException("LLM returned null response");
        }

        if (response.getCandidates() == null || response.getCandidates().isEmpty()) {
            log.error("No candidates in LLM response from provider: {}", provider);
            throw new RuntimeException("LLM returned empty candidates");
        }

        LlmResponse.Candidate firstCandidate = response.getCandidates().get(0);
        if (firstCandidate == null || firstCandidate.getContent() == null) {
            log.error("Invalid candidate structure in LLM response from provider: {}", provider);
            throw new RuntimeException("Invalid LLM response structure");
        }

        LlmResponse.Content responseContent = firstCandidate.getContent();
        if (responseContent.getParts() == null || responseContent.getParts().isEmpty()) {
            log.error("No parts in LLM response content from provider: {}", provider);
            throw new RuntimeException("LLM response content has no parts");
        }

        LlmResponse.Part firstPart = responseContent.getParts().get(0);
        String reply = firstPart.getText();

        if (reply == null || reply.isEmpty()) {
            log.error("Empty text in LLM response from provider: {}", provider);
            throw new RuntimeException("LLM returned empty response text");
        }

        log.debug("LLM responded with content length: {}", reply.length());

        // 4. Save Assistant Message
        InteractionLog botLog = InteractionLog.builder()
                .session(session)
                .role("ASSISTANT")
                .content(reply)
                .provider(provider)
                .build();
        interactionLogRepository.save(botLog);

        // Update session timestamp
        session.setLastInteractionAt(java.time.LocalDateTime.now());
        sessionRepository.save(session);

        return reply;
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
