package de.angr2301.genericllmadapter.domain.chat;

import de.angr2301.genericllmadapter.feignClients.HuggingfaceFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LlmProviderFactory {

    @Autowired
    private HuggingfaceFeignClient huggingfaceFeignClient;

    public ChatModel createChatModel(String provider, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("API Key is missing for provider: {}", provider);
            throw new IllegalArgumentException("API Key cannot be empty for provider: " + provider);
        }

        log.debug("Creating ChatModel for provider: {}", provider);

        switch (provider.toUpperCase()) {
            case "OPENAI":
                return new OpenAiChatModel(new OpenAiApi(apiKey));

            case "DEEPSEEK":
                // DeepSeek is OpenAI compatible
                return new OpenAiChatModel(new OpenAiApi("https://api.deepseek.com", apiKey));

            case "HUGGINGFACE":
                log.debug("Initializing HuggingfaceChatModel with provided API key");
                String modelId = "mistralai/Mistral-7B-Instruct-v0.3";
                return new LocalHuggingfaceChatModel(huggingfaceFeignClient, apiKey, modelId);

            default:
                log.error("Unsupported provider: {}", provider);
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }
}
