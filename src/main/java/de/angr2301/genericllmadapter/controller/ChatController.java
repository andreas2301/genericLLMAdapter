package de.angr2301.genericllmadapter.controller;

import de.angr2301.genericllmadapter.domain.chat.ChatService;
import de.angr2301.genericllmadapter.domain.chat.InteractionLog;
import de.angr2301.genericllmadapter.domain.chat.LlmHealthCheckService;
import de.angr2301.genericllmadapter.domain.chat.ChatSession;
import de.angr2301.genericllmadapter.domain.chat.ChatSessionRepository; // Adding repository if needed, though controller uses service
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import de.angr2301.genericllmadapter.dto.chat.ChatReply;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final LlmHealthCheckService llmHealthCheckService;

    @PostMapping("/sessions")
    public ChatSession createSession() {
        String email = getCurrentUserEmail();
        log.debug("Creating new chat session for user: {}", email);
        return chatService.createSession(email);
    }

    @GetMapping("/sessions")
    public List<ChatSession> getSessions() {
        String email = getCurrentUserEmail();
        return chatService.getUserSessions(email);
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<de.angr2301.genericllmadapter.dto.chat.ChatReply> sendMessage(
            @PathVariable UUID sessionId,
            @RequestBody de.angr2301.genericllmadapter.controller.ChatRequest request,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                chatService.sendMessage(sessionId, request.prompt(), request.provider(), userDetails.getUsername()));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public List<InteractionLog> getMessages(@PathVariable UUID sessionId) {
        String email = getCurrentUserEmail();
        return chatService.getMessages(sessionId, email);
    }

    /**
     * Get list of available LLM providers for frontend dropdown/selection.
     * Allows dynamic provider selection without hardcoding provider options.
     *
     * Supported providers:
     * - OPENAI: GPT-4o via https://api.openai.com/v1
     * - DEEPSEEK: deepseek-chat via https://api.deepseek.com/v1
     * - HUGGINGFACE: Mistral-7B Instruct via https://router.huggingface.co
     * - LOCAL_VLLM: Self-hosted LLM on http://localhost:8000/v1
     *
     * @return List of available provider names
     */
    @GetMapping("/providers")
    public List<de.angr2301.genericllmadapter.dto.chat.ProviderStatus> getAvailableProviders() {
        log.debug("Fetching available LLM providers");
        List<de.angr2301.genericllmadapter.dto.chat.ProviderStatus> providers = new java.util.ArrayList<>();

        providers.add(new de.angr2301.genericllmadapter.dto.chat.ProviderStatus("OPENAI", true));
        providers.add(new de.angr2301.genericllmadapter.dto.chat.ProviderStatus("DEEPSEEK", true));
        providers.add(new de.angr2301.genericllmadapter.dto.chat.ProviderStatus("HUGGINGFACE", true));

        boolean isVllmAlive = llmHealthCheckService.isVllmAlive();
        providers.add(new de.angr2301.genericllmadapter.dto.chat.ProviderStatus("LOCAL_VLLM", isVllmAlive));

        return providers;
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }
}
