package de.angr2301.genericllmadapter.domain.chat;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

@Component
public class LlmProviderFactory {

    public ChatModel createChatModel(String provider, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API Key cannot be empty for provider: " + provider);
        }

        switch (provider.toUpperCase()) {
            case "OPENAI":
                var openAiApi = new OpenAiApi(apiKey);
                return new OpenAiChatModel(openAiApi);

            case "DEEPSEEK":
                // DeepSeek is OpenAI compatible
                var deepSeekApi = new OpenAiApi("https://api.deepseek.com", apiKey);
                return new OpenAiChatModel(deepSeekApi);

            // Placeholder for HuggingFace - typically requires different setup
            // case "HUGGINGFACE":
            // return new HuggingFaceChatModel(...);

            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }
}
