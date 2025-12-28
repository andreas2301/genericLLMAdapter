package de.angr2301.genericllmadapter.controller;

import de.angr2301.genericllmadapter.domain.chat.ChatService;
import de.angr2301.genericllmadapter.domain.chat.Session;
import de.angr2301.genericllmadapter.dto.ChatReply;
import de.angr2301.genericllmadapter.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/sessions")
    public Session createSession() {
        String email = getCurrentUserEmail();
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
        String reply = chatService.sendMessage(sessionId, request.prompt(), request.provider(), email);
        return new ChatReply(request.prompt(), reply);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }
}
