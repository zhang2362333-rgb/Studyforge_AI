package com.studyforge.voice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyforge.system.service.IntegrationSettingService;
import com.studyforge.voice.service.VoiceService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SiliconFlowVoiceServiceImpl implements VoiceService {
    private static final String DEFAULT_BASE_URL = "https://api.siliconflow.cn/v1";
    private static final String DEFAULT_MODEL = "FunAudioLLM/CosyVoice2-0.5B";
    private static final String DEFAULT_VOICE = "FunAudioLLM/CosyVoice2-0.5B:alex";
    private static final Duration VOICE_TIMEOUT = Duration.ofSeconds(200);

    private final IntegrationSettingService integrationSettingService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final LocalFallbackVoiceServiceImpl fallback = new LocalFallbackVoiceServiceImpl();

    public SiliconFlowVoiceServiceImpl(IntegrationSettingService integrationSettingService) {
        this.integrationSettingService = integrationSettingService;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(VOICE_TIMEOUT)
                .build();
    }

    @Override
    public String textToSpeech(String text, String language) {
        String apiKey = integrationSettingService.getValue("voice.api_key", "");
        if (apiKey.isBlank() || text == null || text.isBlank()) {
            return fallback.textToSpeech(text, language);
        }

        try {
            String baseUrl = trimSlash(integrationSettingService.getValue("voice.base_url", DEFAULT_BASE_URL));
            String model = integrationSettingService.getValue("voice.model", DEFAULT_MODEL);
            String voice = integrationSettingService.getValue("voice.name", DEFAULT_VOICE);
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "input", text,
                    "voice", voice,
                    "response_format", "mp3",
                    "stream", false
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/audio/speech"))
                    .timeout(VOICE_TIMEOUT)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300 || response.body().length == 0) {
                return fallback.textToSpeech(text, language);
            }
            return "data:audio/mpeg;base64," + Base64.getEncoder().encodeToString(response.body());
        } catch (Exception exception) {
            return fallback.textToSpeech(text, language);
        }
    }

    @Override
    public String speechToText(String audioFilePath, String language) {
        return fallback.speechToText(audioFilePath, language);
    }

    @Override
    public String detectSpeechLanguage(String audioFilePath) {
        return fallback.detectSpeechLanguage(audioFilePath);
    }

    private String trimSlash(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_BASE_URL;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
