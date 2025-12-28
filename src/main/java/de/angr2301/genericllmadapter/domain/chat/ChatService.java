package de.angr2301.genericllmadapter.domain.chat;

import de.angr2301.genericllmadapter.domain.user.User;
import de.angr2301.genericllmadapter.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        return sessionRepository.findByUserId(user.getId());
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

        // 2. Prepare Context (History)
        List<InteractionLog> history = interactionLogRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        List<Message> messages = new ArrayList<>();
        // Add System Prompt if needed (skipped for now)
        for (InteractionLog log : history) {
            if ("USER".equalsIgnoreCase(log.getRole())) {
                messages.add(new UserMessage(log.getContent()));
            } else if ("ASSISTANT".equalsIgnoreCase(log.getRole())) {
                messages.add(new AssistantMessage(log.getContent()));
            }
        }

        // 3. Call LLM
        log.debug("Creating chat model for provider: {}", provider);
        ChatModel chatModel = llmProviderFactory.createChatModel(provider, apiKey);
        log.debug("Calling LLM...");
        ChatResponse response = chatModel.call(new Prompt(messages));
        String reply = response.getResult().toString();
        log.debug("LLM responded with content length: {}", reply.length());
        /*
         * String reply = response.getResults().stream()
         * .map(result -> {
         * // Case 1: Generation → Output → AssistantMessage
         * if (result.getOutput() instanceof AssistantMessage msg) {
         * return msg.getContent();
         * }
         * 
         * // Case 2: Generation → Output → something with getContent()
         * if (result.getOutput() != null && result.getOutput().getContent() != null) {
         * return result.getOutput().getContent();
         * }
         * 
         * // Case 3: Direct AssistantMessage in results
         * if (result instanceof AssistantMessage msg) {
         * return msg.getContent();
         * }
         * 
         * // Case 4: Direct content field
         * if (result.getContent() != null) {
         * return result.getContent();
         * }
         * 
         * return null;
         * })
         * .filter(Objects::nonNull)
         * .findFirst()
         * .orElseThrow(() -> new RuntimeException("No content returned by LLM"));
         */

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
        switch (provider.toUpperCase()) {
            case "OPENAI":
                return user.getOpenaiKey();
            case "DEEPSEEK":
                return user.getDeepseekKey();
            case "HUGGINGFACE":
                return user.getHuggingfaceKey();
            default:
                throw new IllegalArgumentException("Unknown provider: " + provider);
        }
    }
}
