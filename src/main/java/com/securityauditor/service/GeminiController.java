package com.securityauditor.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gemini")
public class GeminiController {

    private final GroqService groqService;

    public GeminiController(GroqService groqService) {
        this.groqService = groqService;
    }

    @GetMapping("/test")
    public String testGemini() {
        return groqService.generateExplanation(
                "Explain SQL injection in simple terms."
        );
    }
}