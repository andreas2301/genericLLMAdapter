package de.angr2301.genericllmadapter.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Message DTO for LLM communication
 * Standardized format independent of provider
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmMessage {
    private String role;  // "user", "assistant", "model"
    private List<Part> parts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
    }

    /**
     * Factory method to create a user message
     */
    public static LlmMessage user(String text) {
        return new LlmMessage("user", List.of(new Part(text)));
    }

    /**
     * Factory method to create an assistant message
     */
    public static LlmMessage assistant(String text) {
        return new LlmMessage("assistant", List.of(new Part(text)));
    }

    /**
     * Get all text content from parts
     */
    public String getText() {
        if (parts == null || parts.isEmpty()) {
            return "";
        }
        return parts.stream()
                .map(Part::getText)
                .filter(text -> text != null && !text.isEmpty())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }
}

