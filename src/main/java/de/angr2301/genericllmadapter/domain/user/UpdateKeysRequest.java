package de.angr2301.genericllmadapter.domain.user;

import lombok.Data;

@Data
public class UpdateKeysRequest {
    private String openaiKey;
    private String huggingfaceKey;
    private String deepseekKey;
}
