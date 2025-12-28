package de.angr2301.genericllmadapter.api.controller;

import de.angr2301.genericllmadapter.domain.user.User;
import de.angr2301.genericllmadapter.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        return userRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me/keys")
    public ResponseEntity<User> updateApiKeys(@RequestBody User updatedUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        return userRepository.findByEmail(email)
                .map(user -> {
                    user.setOpenaiKey(updatedUser.getOpenaiKey());
                    user.setHuggingfaceKey(updatedUser.getHuggingfaceKey());
                    user.setDeepseekKey(updatedUser.getDeepseekKey());
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
