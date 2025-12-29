package de.angr2301.genericllmadapter.domain.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests für LlmProviderFactory
 * Testet die Erstellung von LLM-Clients für verschiedene Provider
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LlmProviderFactory - Unit Tests")
class LlmProviderFactoryTest {

    @InjectMocks
    private LlmProviderFactory factory;

    // ===== POSITIVE TESTS: Client-Erstellung =====

    @Test
    @DisplayName("Should create OpenAI client successfully")
    void shouldCreateOpenAiClient() {
        // Given
        String provider = "OPENAI";
        String apiKey = "sk-test-12345";

        // When
        LlmClient client = factory.createChatModel(provider, apiKey);

        // Then
        assertThat(client)
                .isNotNull()
                .isInstanceOf(OpenAiCompatibleClient.class);
    }

    @Test
    @DisplayName("Should create DeepSeek client successfully")
    void shouldCreateDeepSeekClient() {
        // Given
        String provider = "DEEPSEEK";
        String apiKey = "deepseek-test-key-xyz";

        // When
        LlmClient client = factory.createChatModel(provider, apiKey);

        // Then
        assertThat(client)
                .isNotNull()
                .isInstanceOf(OpenAiCompatibleClient.class);
    }

    @Test
    @DisplayName("Should create HuggingFace client successfully")
    void shouldCreateHuggingFaceClient() {
        // Given
        String provider = "HUGGINGFACE";
        String apiKey = "hf_test_key_abcdef123456";

        // When
        LlmClient client = factory.createChatModel(provider, apiKey);

        // Then
        assertThat(client)
                .isNotNull()
                .isInstanceOf(HuggingFaceClient.class);
    }

    @Test
    @DisplayName("Should create Local vLLM client without API key")
    void shouldCreateLocalVllmClientWithoutApiKey() {
        // Given
        String provider = "LOCAL_VLLM";
        String apiKey = null;

        // When
        LlmClient client = factory.createChatModel(provider, apiKey);

        // Then
        assertThat(client)
                .isNotNull()
                .isInstanceOf(OpenAiCompatibleClient.class);
    }

    @Test
    @DisplayName("Should create Local vLLM client with empty API key")
    void shouldCreateLocalVllmClientWithEmptyApiKey() {
        // Given
        String provider = "LOCAL_VLLM";
        String apiKey = "";

        // When
        LlmClient client = factory.createChatModel(provider, apiKey);

        // Then
        assertThat(client).isNotNull();
    }

    @Test
    @DisplayName("Should create Local vLLM client with blank API key")
    void shouldCreateLocalVllmClientWithBlankApiKey() {
        // Given
        String provider = "LOCAL_VLLM";
        String apiKey = "   ";

        // When
        LlmClient client = factory.createChatModel(provider, apiKey);

        // Then
        assertThat(client).isNotNull();
    }

    // ===== POSITIVE TESTS: Case-Insensitivity =====

    @ParameterizedTest
    @ValueSource(strings = {"openai", "OPENAI", "OpenAI", "OpenAI", "oPeNaI"})
    @DisplayName("Should handle case-insensitive provider names")
    void shouldHandleCaseInsensitiveProviderNames(String provider) {
        // Given
        String apiKey = "test-key";

        // When
        LlmClient client = factory.createChatModel(provider, apiKey);

        // Then
        assertThat(client).isInstanceOf(OpenAiCompatibleClient.class);
    }

    // ===== NEGATIVE TESTS: Null Provider =====

    @Test
    @DisplayName("Should throw IllegalArgumentException for null provider")
    void shouldThrowExceptionForNullProvider() {
        // Given
        String provider = null;
        String apiKey = "test-key";

        // When & Then
        assertThatThrownBy(() -> factory.createChatModel(provider, apiKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider name cannot be empty");
    }

    // ===== NEGATIVE TESTS: Empty/Blank Provider =====

    @Test
    @DisplayName("Should throw IllegalArgumentException for empty provider")
    void shouldThrowExceptionForEmptyProvider() {
        // Given
        String provider = "";
        String apiKey = "test-key";

        // When & Then
        assertThatThrownBy(() -> factory.createChatModel(provider, apiKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider name cannot be empty");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for blank provider")
    void shouldThrowExceptionForBlankProvider() {
        // Given
        String provider = "   ";
        String apiKey = "test-key";

        // When & Then
        assertThatThrownBy(() -> factory.createChatModel(provider, apiKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider name cannot be empty");
    }

    // ===== NEGATIVE TESTS: Unsupported Provider =====

    @Test
    @DisplayName("Should throw IllegalArgumentException for unsupported provider")
    void shouldThrowExceptionForUnsupportedProvider() {
        // Given
        String provider = "INVALID_PROVIDER";
        String apiKey = "test-key";

        // When & Then
        assertThatThrownBy(() -> factory.createChatModel(provider, apiKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported provider");
    }

    @Test
    @DisplayName("Should throw exception for provider 'CLAUDE'")
    void shouldThrowExceptionForClaudeProvider() {
        // Given
        String provider = "CLAUDE";
        String apiKey = "test-key";

        // When & Then
        assertThatThrownBy(() -> factory.createChatModel(provider, apiKey))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ===== NEGATIVE TESTS: Missing API Key (required providers) =====

    @Test
    @DisplayName("Should throw IllegalArgumentException for null API key (OpenAI)")
    void shouldThrowExceptionForNullApiKeyOpenAi() {
        // Given
        String provider = "OPENAI";
        String apiKey = null;

        // When & Then
        assertThatThrownBy(() -> factory.createChatModel(provider, apiKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API Key cannot be empty");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for empty API key (DeepSeek)")
    void shouldThrowExceptionForEmptyApiKeyDeepSeek() {
        // Given
        String provider = "DEEPSEEK";
        String apiKey = "";

        // When & Then
        assertThatThrownBy(() -> factory.createChatModel(provider, apiKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API Key cannot be empty");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for blank API key (HuggingFace)")
    void shouldThrowExceptionForBlankApiKeyHuggingFace() {
        // Given
        String provider = "HUGGINGFACE";
        String apiKey = "   ";

        // When & Then
        assertThatThrownBy(() -> factory.createChatModel(provider, apiKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API Key cannot be empty");
    }

    // ===== EDGE CASES =====

    @Test
    @DisplayName("Should handle very long API keys")
    void shouldHandleVeryLongApiKeys() {
        // Given
        String provider = "OPENAI";
        String apiKey = "a".repeat(1000); // 1000 character API key

        // When
        LlmClient client = factory.createChatModel(provider, apiKey);

        // Then
        assertThat(client).isNotNull();
    }

    @Test
    @DisplayName("Should handle special characters in API key")
    void shouldHandleSpecialCharactersInApiKey() {
        // Given
        String provider = "DEEPSEEK";
        String apiKey = "sk-proj-abc!@#$%^&*()_+-=[]{}|;':\"<>?,./";

        // When
        LlmClient client = factory.createChatModel(provider, apiKey);

        // Then
        assertThat(client).isNotNull();
    }
}

