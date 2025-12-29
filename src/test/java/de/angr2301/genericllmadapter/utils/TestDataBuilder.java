package de.angr2301.genericllmadapter.utils;

import de.angr2301.genericllmadapter.domain.user.Role;
import de.angr2301.genericllmadapter.domain.user.User;
import de.angr2301.genericllmadapter.domain.chat.LlmMessage;
import de.angr2301.genericllmadapter.domain.chat.LlmResponse;
import de.angr2301.genericllmadapter.dto.auth.AuthenticationRequest;
import de.angr2301.genericllmadapter.dto.auth.RegisterRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Test Data Builder für Test-Objekte mit Fluent API
 * Ermöglicht einfache und lesbare Test-Datenerstellung
 *
 * Verwendungsbeispiel:
 * User user = TestDataBuilder.aUser()
 *     .email("custom@example.com")
 *     .openaiKey("custom-key")
 *     .build();
 */
public class TestDataBuilder {

    /**
     * Erstellt einen Test-User mit Standard-Werten
     */
    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    /**
     * Erstellt einen RegisterRequest mit Standard-Werten
     */
    public static RegisterRequestBuilder aRegisterRequest() {
        return new RegisterRequestBuilder();
    }

    /**
     * Erstellt einen AuthenticationRequest mit Standard-Werten
     */
    public static AuthenticationRequestBuilder anAuthenticationRequest() {
        return new AuthenticationRequestBuilder();
    }

    /**
     * Erstellt eine User-Message
     */
    public static LlmMessage aUserMessage(String text) {
        return new LlmMessage("user", List.of(new LlmMessage.Part(text)));
    }

    /**
     * Erstellt eine Assistant-Message
     */
    public static LlmMessage anAssistantMessage(String text) {
        return new LlmMessage("assistant", List.of(new LlmMessage.Part(text)));
    }

    /**
     * Erstellt eine LLM-Response
     */
    public static LlmResponse anLlmResponse(String text) {
        return new LlmResponse(
                List.of(new LlmResponse.Candidate(
                        new LlmResponse.Content("model",
                                List.of(new LlmResponse.Part(text)))
                ))
        );
    }

    /**
     * Builder-Klasse für User
     */
    public static class UserBuilder {
        private UUID id = UUID.randomUUID();
        private String email = "test@example.com";
        private String passwordHash = "hashed-password";
        private Role role = Role.USER;
        private boolean enabled = true;
        private String openaiKey = "test-openai-key";
        private String huggingfaceKey = "test-hf-key";
        private String deepseekKey = "test-deepseek-key";
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public UserBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public UserBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UserBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public UserBuilder openaiKey(String openaiKey) {
            this.openaiKey = openaiKey;
            return this;
        }

        public UserBuilder huggingfaceKey(String huggingfaceKey) {
            this.huggingfaceKey = huggingfaceKey;
            return this;
        }

        public UserBuilder deepseekKey(String deepseekKey) {
            this.deepseekKey = deepseekKey;
            return this;
        }

        public User build() {
            User user = new User();
            user.setId(id);
            user.setEmail(email);
            user.setPasswordHash(passwordHash);
            user.setRole(role);
            user.setEnabled(enabled);
            user.setOpenaiKey(openaiKey);
            user.setHuggingfaceKey(huggingfaceKey);
            user.setDeepseekKey(deepseekKey);
            user.setCreatedAt(createdAt);
            user.setUpdatedAt(updatedAt);
            return user;
        }
    }

    /**
     * Builder-Klasse für RegisterRequest
     */
    public static class RegisterRequestBuilder {
        private String email = "test@example.com";
        private String password = "SecurePassword123!";

        public RegisterRequestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public RegisterRequestBuilder password(String password) {
            this.password = password;
            return this;
        }

        public RegisterRequest build() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail(email);
            request.setPassword(password);
            return request;
        }
    }

    /**
     * Builder-Klasse für AuthenticationRequest
     */
    public static class AuthenticationRequestBuilder {
        private String email = "test@example.com";
        private String password = "SecurePassword123!";

        public AuthenticationRequestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public AuthenticationRequestBuilder password(String password) {
            this.password = password;
            return this;
        }

        public AuthenticationRequest build() {
            AuthenticationRequest request = new AuthenticationRequest();
            request.setEmail(email);
            request.setPassword(password);
            return request;
        }
    }
}

