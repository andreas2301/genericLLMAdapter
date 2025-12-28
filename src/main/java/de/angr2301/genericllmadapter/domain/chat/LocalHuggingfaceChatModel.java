package de.angr2301.genericllmadapter.domain.chat;

import de.angr2301.genericllmadapter.feignClients.HuggingfaceFeignClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;

public class LocalHuggingfaceChatModel implements ChatModel {

    private final HuggingfaceFeignClient api;
    private final String apiKey;
    private final String modelId;

    public LocalHuggingfaceChatModel(HuggingfaceFeignClient api, String apiKey, String modelId) {
        this.api = api;
        this.apiKey = apiKey;
        this.modelId = modelId;
    }

    @Override
    public ChatResponse call(Prompt prompt) {

        List<Map<String, Object>> response = api.generate(
                modelId,
                Map.of(
                        "inputs", prompt.getContents(),
                        "options", Map.of("use_cache", false)));

        String text = (String) response.get(0).get("generated_text");

        return new ChatResponse(
                List.of(new Generation(new AssistantMessage(text))));
    }
}
