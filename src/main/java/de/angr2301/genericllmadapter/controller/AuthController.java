package de.angr2301.genericllmadapter.controller;

import de.angr2301.genericllmadapter.domain.user.Role;
import de.angr2301.genericllmadapter.domain.user.User;
import de.angr2301.genericllmadapter.domain.user.UserRepository;
import de.angr2301.genericllmadapter.dto.auth.AuthenticationRequest;
import de.angr2301.genericllmadapter.dto.auth.AuthenticationResponse;
import de.angr2301.genericllmadapter.dto.auth.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for user authentication and registration.
 * Refactored for Session-based authentication with high security headers.
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final org.springframework.security.web.csrf.CsrfTokenRepository csrfTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request,
            HttpServletRequest httpRequest, jakarta.servlet.http.HttpServletResponse httpResponse) {
        try {
            // 1. Validate input: null check
            if (request == null || request.getEmail() == null || request.getPassword() == null) {
                log.warn("Invalid registration request: missing email or password");
                return ResponseEntity.badRequest().build();
            }

            String email = request.getEmail().trim();
            String password = request.getPassword();

            // 2. Validate email format (Simple check for non-empty)
            if (email.length() < 3) {
                log.debug("Registration attempt with invalid username/email: {}", email);
                return ResponseEntity.badRequest().build();
            }

            // 3. Validate password strength (Minimum 4 characters for dev)
            if (password.length() < 4) {
                log.debug("Registration attempt with weak password for email: {}", email);
                return ResponseEntity.badRequest().build();
            }

            // 4. Check if user already exists (409 Conflict instead of 400 Bad Request)
            if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
                log.warn("Registration attempt with existing email: {}", email);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            log.info("Registering new user: {}", email);

            // 5. Create and save new user with hashed password
            User user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .role(Role.USER)
                    .enabled(true)
                    .build();

            userRepository.save(user);
            log.debug("User registered successfully: {} (ID: {})", email, user.getId());

            // Auto-login after registration
            authenticateUser(email, password, httpRequest, httpResponse);
            log.info("User auto-logged in after registration: {}", email);

            return ResponseEntity.ok(new AuthenticationResponse("Registration successful"));

        } catch (Exception e) {
            log.error("Unexpected error during registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request,
            HttpServletRequest httpRequest, jakarta.servlet.http.HttpServletResponse httpResponse) {
        try {
            // 1. Validate input
            if (request == null || request.getEmail() == null || request.getPassword() == null) {
                log.warn("Invalid authentication request: missing email or password");
                return ResponseEntity.badRequest().build();
            }

            String email = request.getEmail().trim();
            log.debug("Authentication attempt for user: {}", email);

            // 2. Attempt authentication (BadCredentialsException might be thrown)
            try {
                authenticateUser(email, request.getPassword(), httpRequest, httpResponse);
                log.debug("Authentication successful for user: {}", email);
            } catch (BadCredentialsException e) {
                log.warn("Authentication failed - invalid credentials for user: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            log.info("User logged in successfully: {}", email);

            return ResponseEntity.ok(new AuthenticationResponse("Login successful"));

        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            log.info("User session invalidated.");
        }
        SecurityContextHolder.clearContext();
        log.info("Security context cleared.");
        return ResponseEntity.ok().build();
    }

    private void authenticateUser(String email, String password, HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Force session creation
        request.getSession(true);

        // Manually seed CSRF token since /auth is ignored by filter
        org.springframework.security.web.csrf.CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(csrfToken, request, response);
    }
}
