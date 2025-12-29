package de.angr2301.genericllmadapter.domain.chat;

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
 * OpenAI-compatible LLM client
 * Supports OpenAI, DeepSeek, and local vLLM endpoints
 */
@Slf4j
@RequiredArgsConstructor
public class OpenAiCompatibleClient implements LlmClient {

    private final String baseUrl;
    private final String modelName;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public LlmResponse generateContent(List<LlmMessage> contents) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", modelName);
            ArrayNode messages = requestBody.putArray("messages");

            for (LlmMessage content : contents) {
                ObjectNode message = messages.addObject();
                // Map role: "user" stays user, "assistant" or "model" → "assistant"
                String role = content.getRole();
                if (role == null || role.isBlank()) {
                    role = "user";
                } else if ("model".equalsIgnoreCase(role)) {
                    role = "assistant";
                }
                message.put("role", role.toLowerCase());
                message.put("content", content.getText());
            }

            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            log.debug("Sending request to OpenAI-compatible endpoint: {}", baseUrl);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Error from {} API: {} - {}", modelName, response.statusCode(), response.body());
                throw new RuntimeException("API error: " + response.statusCode() + " - " + response.body());
            }

            var rootNode = objectMapper.readTree(response.body());

            // Safely extract the assistant reply
            if (rootNode.path("choices").isEmpty()) {
                log.error("No choices in response from {} API", modelName);
                throw new RuntimeException("Empty choices in API response");
            }

            String assistantReply = rootNode.path("choices").get(0).path("message").path("content").asText();
            if (assistantReply == null || assistantReply.isEmpty()) {
                log.error("Empty content in response from {} API", modelName);
                throw new RuntimeException("Empty content in API response");
            }

            log.debug("Successfully received response from {} with {} characters", modelName, assistantReply.length());

            // Wrap in LlmResponse
            return new LlmResponse(
                    List.of(new LlmResponse.Candidate(
                            new LlmResponse.Content("model",
                                    List.of(new LlmResponse.Part(assistantReply)))
                    ))
            );

        } catch (RuntimeException e) {
            log.error("Failed to call OpenAI-compatible API: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to call OpenAI-compatible API", e);
            throw new RuntimeException("LLM call failed: " + e.getMessage(), e);
        }
    }
}
