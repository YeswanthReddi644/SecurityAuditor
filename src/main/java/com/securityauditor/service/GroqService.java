package com.securityauditor.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestClient restClient =
            RestClient.builder()
                    .baseUrl("https://api.groq.com/openai/v1")
                    .build();

    public String generateExplanation(String prompt) {

        Map<String, Object> requestBody = Map.of(
        		"model", "openai/gpt-oss-120b",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        Map response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        List choices = (List) response.get("choices");

        Map firstChoice = (Map) choices.get(0);
        Map message = (Map) firstChoice.get("message");

        return (String) message.get("content");
    }
}