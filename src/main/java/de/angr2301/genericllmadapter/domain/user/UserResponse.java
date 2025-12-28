package de.angr2301.genericllmadapter.domain.user;

import lombok.Data;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String email;
    private Role role;
    private String openaiKey;
    private String huggingfaceKey;
    private String deepseekKey;

    public static UserResponse fromUser(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setOpenaiKey(user.getOpenaiKey());
        response.setHuggingfaceKey(user.getHuggingfaceKey());
        response.setDeepseekKey(user.getDeepseekKey());
        return response;
    }
}
