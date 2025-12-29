package de.angr2301.genericllmadapter.controller;

import de.angr2301.genericllmadapter.config.JwtService;
import de.angr2301.genericllmadapter.domain.user.Role;
import de.angr2301.genericllmadapter.domain.user.User;
import de.angr2301.genericllmadapter.domain.user.UserRepository;
import de.angr2301.genericllmadapter.dto.auth.AuthenticationRequest;
import de.angr2301.genericllmadapter.dto.auth.AuthenticationResponse;
import de.angr2301.genericllmadapter.dto.auth.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for user authentication and registration.
 * Features:
 * - Input validation (email format, password strength)
 * - Proper HTTP status codes (409 Conflict, 401 Unauthorized, 403 Forbidden, etc.)
 * - Email masking in logs for privacy
 * - Comprehensive error logging
 * - Exception handling for all edge cases
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        try {
            // 1. Validate input: null check
            if (request == null || request.getEmail() == null || request.getPassword() == null) {
                log.warn("Invalid registration request: missing email or password");
                return ResponseEntity.badRequest().build();
            }

            String email = request.getEmail().trim();
            String password = request.getPassword();

            // 2. Validate email format
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                log.debug("Registration attempt with invalid email format: {}", maskEmail(email));
                return ResponseEntity.badRequest().build();
            }

            // 3. Validate password strength (minimum 8 characters)
            if (password.length() < 8) {
                log.debug("Registration attempt with weak password for email: {}", maskEmail(email));
                return ResponseEntity.badRequest().build();
            }

            // 4. Check if user already exists (409 Conflict instead of 400 Bad Request)
            if (userRepository.findByEmail(email).isPresent()) {
                log.warn("Registration attempt with existing email: {}", maskEmail(email));
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            log.info("Registering new user: {}", maskEmail(email));

            // 5. Create and save new user with hashed password
            User user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .role(Role.USER)
                    .enabled(true)
                    .build();

            user = userRepository.save(user);
            log.debug("User registered successfully: {} (ID: {})", maskEmail(email), user.getId());

            // 6. Generate JWT token
            org.springframework.security.core.userdetails.UserDetails userDetails = createUserDetails(user);
            String jwtToken = jwtService.generateToken(userDetails);
            log.debug("JWT token generated for registered user: {}", maskEmail(email));

            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build());

        } catch (Exception e) {
            log.error("Unexpected error during registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            // 1. Validate input
            if (request == null || request.getEmail() == null || request.getPassword() == null) {
                log.warn("Invalid authentication request: missing email or password");
                return ResponseEntity.badRequest().build();
            }

            String email = request.getEmail().trim();
            log.debug("Authentication attempt for user: {}", maskEmail(email));

            // 2. Attempt authentication (BadCredentialsException might be thrown)
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(email, request.getPassword()));
                log.debug("Authentication successful for user: {}", maskEmail(email));

            } catch (BadCredentialsException e) {
                log.warn("Authentication failed - invalid credentials for user: {}", maskEmail(email));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 3. Fetch user (should exist after successful authentication)
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("User not found after successful authentication: {}", maskEmail(email));
                        return new IllegalStateException("User authentication succeeded but user not found");
                    });

            // 4. Check if user is enabled
            if (!user.isEnabled()) {
                log.warn("Login attempt for disabled user: {}", maskEmail(email));
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            log.info("User logged in successfully: {}", maskEmail(email));

            // 5. Generate JWT token
            org.springframework.security.core.userdetails.UserDetails userDetails = createUserDetails(user);
            String jwtToken = jwtService.generateToken(userDetails);
            log.debug("JWT token generated for user: {}", maskEmail(email));

            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build());

        } catch (IllegalStateException e) {
            log.error("State error during authentication", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create Spring Security UserDetails from domain User object.
     * Helper method to remove code duplication between register() and authenticate().
     */
    private org.springframework.security.core.userdetails.UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }

    /**
     * Mask email for safe logging to prevent exposure of sensitive user data.
     * Example: user@example.com → u***@example.com
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }

        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            return username + "***@" + domain;
        }

        return username.charAt(0) + "***@" + domain;
    }
}
