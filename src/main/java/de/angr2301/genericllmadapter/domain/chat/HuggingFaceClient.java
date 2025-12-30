package de.angr2301.genericllmadapter.domain.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * HuggingFace Inference API LLM client
 * Converts standardized LlmMessage format to HuggingFace API format
 */
@Slf4j
@RequiredArgsConstructor
public class HuggingFaceClient implements LlmClient {

    private final String modelId; // e.g. "deepseek-ai/DeepSeek-R1:fastest"
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public LlmResponse generateContent(List<LlmMessage> messages) {
        try {
            // Convert your internal messages to OpenAI-style messages
            ArrayNode msgArray = objectMapper.createArrayNode();
            for (LlmMessage m : messages) {
                ObjectNode msg = objectMapper.createObjectNode();
                msg.put("role", m.getRole());
                msg.put("content", m.getText());
                msgArray.add(msg);
            }

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", modelId);
            requestBody.set("messages", msgArray);

            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://router.huggingface.co/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("HF API error {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("HF API error: " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String text = root.path("choices").get(0).path("message").path("content").asText();

            return new LlmResponse(
                    List.of(new LlmResponse.Candidate(
                            new LlmResponse.Content("model",
                                    List.of(new LlmResponse.Part(text))))));

        } catch (Exception e) {
            throw new RuntimeException("HF call failed: " + e.getMessage(), e);
        }
    }
}
