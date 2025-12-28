package de.angr2301.genericllmadapter.controller;

import de.angr2301.genericllmadapter.domain.chat.ChatService;
import de.angr2301.genericllmadapter.domain.chat.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/sessions")
    public Session createSession() {
        String email = getCurrentUserEmail();
        log.debug("Creating new chat session for user: {}", email);
        return chatService.createSession(email);
    }

    @GetMapping("/sessions")
    public List<Session> getSessions() {
        String email = getCurrentUserEmail();
        return chatService.getUserSessions(email);
    }

    @PostMapping("/sessions/{sessionId}/message")
    public ChatReply sendMessage(@PathVariable UUID sessionId, @RequestBody ChatRequest request) {
        String email = getCurrentUserEmail();
        log.debug("Received message for session {}: {} using provider {}", sessionId, request.prompt(),
                request.provider());
        String reply = chatService.sendMessage(sessionId, request.prompt(), request.provider(), email);
        return new ChatReply(reply);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }
}
