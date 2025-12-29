package de.angr2301.genericllmadapter.domain.chat;

import java.util.List;

/**
 * Abstraction layer for all LLM providers (OpenAI, DeepSeek, HuggingFace, Local vLLM)
 * Standardizes the request/response format across different LLM services
 */
public interface LlmClient {
    /**
     * Generate content response from LLM provider
     * @param contents List of content/messages to send to LLM
     * @return LlmResponse containing generated text
     */
    LlmResponse generateContent(List<LlmMessage> contents);
}
