package de.angr2301.genericllmadapter.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Mock-Responses für verschiedene LLM-Provider
 * Wird für WireMock Integration Tests verwendet
 *
 * Beispiel-Verwendung in WireMock:
 * wireMockServer.stubFor(post(urlPathEqualTo("/v1/chat/completions"))
 *     .willReturn(aResponse()
 *         .withBody(MockLlmResponses.openAiResponse("Hello!"))
 *         .withStatus(200)));
 */
public class MockLlmResponses {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Mock-Response für OpenAI API (Chat Completions)
     * Simuliert: https://api.openai.com/v1/chat/completions
     */
    public static String openAiResponse(String content) {
        try {
            ObjectNode response = objectMapper.createObjectNode();

            // Build message object
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "assistant");
            message.put("content", content);

            // Build choice object
            ObjectNode choice = objectMapper.createObjectNode();
            choice.set("message", message);
            choice.put("finish_reason", "stop");
            choice.put("index", 0);

            // Build root response
            ArrayNode choices = objectMapper.createArrayNode();
            choices.add(choice);

            response.put("id", "chatcmpl-" + System.nanoTime());
            response.put("object", "chat.completion");
            response.put("created", System.currentTimeMillis() / 1000);
            response.put("model", "gpt-4o");
            response.set("choices", choices);
            response.put("usage", objectMapper.createObjectNode()
                    .put("prompt_tokens", 10)
                    .put("completion_tokens", content.split(" ").length)
                    .put("total_tokens", 10 + content.split(" ").length));

            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create OpenAI mock response", e);
        }
    }

    /**
     * Mock-Response für DeepSeek API (OpenAI-kompatibel)
     * Simuliert: https://api.deepseek.com/v1/chat/completions
     */
    public static String deepSeekResponse(String content) {
        try {
            ObjectNode response = objectMapper.createObjectNode();

            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "assistant");
            message.put("content", content);

            ObjectNode choice = objectMapper.createObjectNode();
            choice.set("message", message);
            choice.put("finish_reason", "stop");
            choice.put("index", 0);

            ArrayNode choices = objectMapper.createArrayNode();
            choices.add(choice);

            response.put("id", "deepseek-" + System.nanoTime());
            response.put("object", "chat.completion");
            response.put("created", System.currentTimeMillis() / 1000);
            response.put("model", "deepseek-chat");
            response.set("choices", choices);

            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create DeepSeek mock response", e);
        }
    }

    /**
     * Mock-Response für HuggingFace Inference API
     * Simuliert: https://api-inference.huggingface.co/models/{modelId}
     */
    public static String huggingFaceResponse(String content) {
        try {
            ObjectNode responseItem = objectMapper.createObjectNode();
            responseItem.put("generated_text", content);

            ArrayNode response = objectMapper.createArrayNode();
            response.add(responseItem);

            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create HuggingFace mock response", e);
        }
    }

    /**
     * Mock-Response für Local vLLM (OpenAI-kompatibel)
     * Simuliert: http://localhost:8000/v1/chat/completions
     */
    public static String localVllmResponse(String content) {
        try {
            ObjectNode response = objectMapper.createObjectNode();

            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "assistant");
            message.put("content", content);

            ObjectNode choice = objectMapper.createObjectNode();
            choice.set("message", message);
            choice.put("finish_reason", "stop");
            choice.put("index", 0);

            ArrayNode choices = objectMapper.createArrayNode();
            choices.add(choice);

            response.put("id", "local-" + System.nanoTime());
            response.put("object", "chat.completion");
            response.put("created", System.currentTimeMillis() / 1000);
            response.put("model", "Qwen/Qwen2.5-7B-Instruct");
            response.set("choices", choices);

            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Local vLLM mock response", e);
        }
    }

    /**
     * Mock-Error-Response (401 Unauthorized)
     * Simuliert ungültigen API-Key
     */
    public static String unauthorizedErrorResponse() {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("error", "Unauthorized");
            response.put("message", "Invalid API key provided");
            response.put("type", "invalid_request_error");

            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create error response", e);
        }
    }

    /**
     * Mock-Error-Response (429 Too Many Requests)
     * Simuliert Rate-Limiting
     */
    public static String rateLimitErrorResponse() {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("error", "Too Many Requests");
            response.put("message", "Rate limit exceeded");
            response.put("retry_after", 60);

            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create rate limit error response", e);
        }
    }

    /**
     * Mock-Error-Response (500 Internal Server Error)
     * Simuliert Server-Fehler
     */
    public static String serverErrorResponse(String errorMessage) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("error", "Internal Server Error");
            response.put("message", errorMessage);
            response.put("type", "server_error");

            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create server error response", e);
        }
    }

    /**
     * Mock-Error-Response (400 Bad Request)
     * Simuliert ungültigen Request
     */
    public static String badRequestErrorResponse(String message) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("error", "Bad Request");
            response.put("message", message);
            response.put("type", "invalid_request_error");

            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bad request error response", e);
        }
    }

    /**
     * HuggingFace Model Loading Response
     * Simuliert die Antwort wenn das Modell noch geladen wird
     */
    public static String huggingFaceLoadingResponse() {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("error", "Model is loading");
            response.put("estimated_time", 120);

            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create HuggingFace loading response", e);
        }
    }
}

