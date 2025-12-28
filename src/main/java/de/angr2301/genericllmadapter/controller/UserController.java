package de.angr2301.genericllmadapter.controller;

import de.angr2301.genericllmadapter.domain.user.UpdateKeysRequest;
import de.angr2301.genericllmadapter.domain.user.User;
import de.angr2301.genericllmadapter.domain.user.UserResponse;
import de.angr2301.genericllmadapter.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }

    @PutMapping("/me/keys")
    public ResponseEntity<UserResponse> updateKeys(
            Authentication authentication,
            @RequestBody UpdateKeysRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.getUserByEmail(userDetails.getUsername());
        User updatedUser = userService.updateApiKeys(user.getId(), request);
        return ResponseEntity.ok(UserResponse.fromUser(updatedUser));
    }
}
