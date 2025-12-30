package de.angr2301.genericllmadapter.domain.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory for creating LLM clients for different providers.
 * Centralizes provider configuration and client instantiation.
 */
@Component
@Slf4j
public class LlmProviderFactory {

    @org.springframework.beans.factory.annotation.Value("${vllm.url:http://localhost:8000}")
    private String vllmUrl;

    public LlmClient createChatModel(String provider, String apiKey) {
        if (provider == null || provider.isBlank()) {
            log.error("Provider name cannot be null or empty");
            throw new IllegalArgumentException("Provider name cannot be empty");
        }

        // Validate API key for providers that require it
        if (apiKey == null || apiKey.isBlank()) {
            if (!"LOCAL_VLLM".equalsIgnoreCase(provider)) {
                log.error("API Key is missing for provider: {}", provider);
                throw new IllegalArgumentException("API Key cannot be empty for provider: " + provider);
            }
            // LOCAL_VLLM doesn't require an API key
            apiKey = "dummy";
        }

        log.debug("Creating LlmClient for provider: {}", provider);

        return switch (provider.toUpperCase()) {
            case "OPENAI" ->
                new OpenAiCompatibleClient("https://api.openai.com/v1", "gpt-4o", apiKey);

            case "DEEPSEEK" ->
                new OpenAiCompatibleClient("https://api.deepseek.com/v1", "deepseek-chat", apiKey);

            case "LOCAL_VLLM" ->
                new OpenAiCompatibleClient(vllmUrl + "/v1", "Qwen/Qwen2.5-0.5B-Instruct", apiKey);

            case "HUGGINGFACE" -> {
                //String modelId = "mistralai/Mistral-7B-Instruct-v0.3";
                String modelId = "deepseek-ai/DeepSeek-R1:fastest";
                yield new HuggingFaceClient(modelId, apiKey);
            }

            default -> {
                log.error("Unsupported provider: {}", provider);
                throw new IllegalArgumentException("Unsupported provider: " + provider);
            }
        };
    }
}
