package de.angr2301.genericllmadapter.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserByEmail(String email) {
        if (email == null)
            throw new IllegalArgumentException("Email cannot be null");
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserById(UUID id) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User updateApiKeys(UUID userId, UpdateKeysRequest request) {
        User user = getUserById(userId);

        if (request.getOpenaiKey() != null) {
            user.setOpenaiKey(request.getOpenaiKey());
        }
        if (request.getHuggingfaceKey() != null) {
            user.setHuggingfaceKey(request.getHuggingfaceKey());
        }
        if (request.getDeepseekKey() != null) {
            user.setDeepseekKey(request.getDeepseekKey());
        }

        return userRepository.save(user);
    }
}
