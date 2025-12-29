package de.angr2301.genericllmadapter.domain.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing user-related operations.
 * Features:
 * - User lookup by email and ID
 * - API key management
 * - Comprehensive logging and error handling
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get user by email.
     *
     * @param email User email
     * @return User object
     * @throws IllegalArgumentException if email is null
     * @throws RuntimeException if user not found
     */
    public User getUserByEmail(String email) {
        if (email == null) {
            log.warn("Attempted to get user with null email");
            throw new IllegalArgumentException("Email cannot be null");
        }

        log.debug("Looking up user by email: {}", maskEmail(email));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for email: {}", maskEmail(email));
                    return new RuntimeException("User not found: " + email);
                });
    }

    /**
     * Get user by ID.
     *
     * @param id User ID
     * @return User object
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if user not found
     */
    public User getUserById(UUID id) {
        if (id == null) {
            log.warn("Attempted to get user with null ID");
            throw new IllegalArgumentException("ID cannot be null");
        }

        log.debug("Looking up user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for ID: {}", id);
                    return new RuntimeException("User not found with ID: " + id);
                });
    }

    /**
     * Update user's API keys.
     *
     * @param userId User ID
     * @param request API key request with OpenAI, HuggingFace, and DeepSeek keys
     * @return Updated user object
     */
    @Transactional
    public User updateApiKeys(UUID userId, UpdateKeysRequest request) {
        log.info("Updating API keys for user ID: {}", userId);

        User user = getUserById(userId);

        if (request.getOpenaiKey() != null) {
            user.setOpenaiKey(request.getOpenaiKey());
            log.debug("OpenAI key updated for user: {}", userId);
        }

        if (request.getHuggingfaceKey() != null) {
            user.setHuggingfaceKey(request.getHuggingfaceKey());
            log.debug("HuggingFace key updated for user: {}", userId);
        }

        if (request.getDeepseekKey() != null) {
            user.setDeepseekKey(request.getDeepseekKey());
            log.debug("DeepSeek key updated for user: {}", userId);
        }

        user = userRepository.save(user);
        log.info("API keys updated successfully for user ID: {}", userId);

        return user;
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
