package de.angr2301.genericllmadapter.controller;

import de.angr2301.genericllmadapter.config.JwtService;
import de.angr2301.genericllmadapter.domain.user.Role;
import de.angr2301.genericllmadapter.domain.user.User;
import de.angr2301.genericllmadapter.domain.user.UserRepository;
import de.angr2301.genericllmadapter.dto.auth.AuthenticationRequest;
import de.angr2301.genericllmadapter.dto.auth.AuthenticationResponse;
import de.angr2301.genericllmadapter.dto.auth.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        @PostMapping("/register")
        public ResponseEntity<AuthenticationResponse> register(
                        @RequestBody RegisterRequest request) {
                // Simple check if user exists
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                        return ResponseEntity.badRequest().build();
                }

                User user = User.builder()
                                .email(request.getEmail())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .role(Role.USER) // Default role
                                .enabled(true)
                                .build();

                userRepository.save(user);

                // Map to UserDetails for token generation
                org.springframework.security.core.userdetails.UserDetails userDetails = org.springframework.security.core.userdetails.User
                                .builder()
                                .username(user.getEmail())
                                .password(user.getPasswordHash()) // Not used in token gen but good measure
                                .authorities("ROLE_" + user.getRole().name())
                                .build();

                String jwtToken = jwtService.generateToken(userDetails);

                return ResponseEntity.ok(AuthenticationResponse.builder()
                                .token(jwtToken)
                                .build());
        }

        @PostMapping("/login")
        public ResponseEntity<AuthenticationResponse> authenticate(
                        @RequestBody AuthenticationRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));

                // If we get here, authentication was successful
                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow();

                org.springframework.security.core.userdetails.UserDetails userDetails = org.springframework.security.core.userdetails.User
                                .builder()
                                .username(user.getEmail())
                                .password(user.getPasswordHash())
                                .authorities("ROLE_" + user.getRole().name())
                                .build();

                String jwtToken = jwtService.generateToken(userDetails);

                return ResponseEntity.ok(AuthenticationResponse.builder()
                                .token(jwtToken)
                                .build());
        }
}
