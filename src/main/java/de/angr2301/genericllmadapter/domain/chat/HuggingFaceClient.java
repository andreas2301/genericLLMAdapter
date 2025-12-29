package de.angr2301.genericllmadapter.domain.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final String modelId;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public LlmResponse generateContent(List<LlmMessage> contents) {
        try {
            // HF Inference API usually expects a single string for text-generation
            // Using [INST] tags for Instruct models (e.g., Mistral/Llama)
            StringBuilder promptBuilder = new StringBuilder("<s>");

            for (int i = 0; i < contents.size(); i++) {
                LlmMessage content = contents.get(i);
                String role = content.getRole();
                String text = content.getText();

                if ("user".equalsIgnoreCase(role)) {
                    promptBuilder.append("[INST] ").append(text).append(" [/INST]");
                } else if ("assistant".equalsIgnoreCase(role) || "model".equalsIgnoreCase(role)) {
                    promptBuilder.append(" ").append(text).append(" </s>");
                    if (i < contents.size() - 1) {
                        promptBuilder.append("<s>");
                    }
                }
            }

            // If the last message was from the user, the model should now respond
            String prompt = promptBuilder.toString();
            if (prompt.isEmpty()) {
                log.error("Generated empty prompt from contents");
                throw new RuntimeException("Empty prompt generated");
            }

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("inputs", prompt);

            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://router.huggingface.co/models/" + modelId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            log.debug("Sending request to HuggingFace model: {}", modelId);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Error from HuggingFace API: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("API error: " + response.statusCode() + " - " + response.body());
            }

            var rootNode = objectMapper.readTree(response.body());

            if (!rootNode.isArray() || rootNode.isEmpty()) {
                log.error("Invalid response format from HuggingFace API: expected non-empty array, got {}",
                        rootNode.getNodeType());
                throw new RuntimeException("Invalid response format from HuggingFace API");
            }

            var firstItem = rootNode.get(0);
            String generatedText = firstItem.path("generated_text").asText();

            if (generatedText == null || generatedText.isEmpty()) {
                log.warn("Empty generated_text in HuggingFace response");
                throw new RuntimeException("Empty response from HuggingFace API");
            }

            // Strip the input prompt if returned
            if (generatedText.startsWith(prompt)) {
                generatedText = generatedText.substring(prompt.length()).trim();
            }

            log.debug("Successfully received response from HuggingFace with {} characters", generatedText.length());

            // Wrap in LlmResponse
            return new LlmResponse(
                    List.of(new LlmResponse.Candidate(
                            new LlmResponse.Content("model",
                                    List.of(new LlmResponse.Part(generatedText))))));

        } catch (RuntimeException e) {
            log.error("Failed to call HuggingFace API: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to call HuggingFace API", e);
            throw new RuntimeException("LLM call failed: " + e.getMessage(), e);
        }
    }
}
