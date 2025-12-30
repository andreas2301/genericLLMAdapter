package de.angr2301.genericllmadapter.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatReply {
    private String prompt;
    private String content;
    private String reasoning;
    private Map<String, Object> metrics;
}
