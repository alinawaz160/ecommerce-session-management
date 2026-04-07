package com.ecommerce.cart.controller;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/ollama")
@RequiredArgsConstructor
public class OllamaAiController {

    private final OllamaChatModel chatModel;

    @GetMapping("/{message}")
    public ResponseEntity<String> getAnswer(@PathVariable String message){
        String response = chatModel.call(message);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-description")
    public ResponseEntity<String> generateDescription(@RequestBody AiRequest request) {
        String response = chatModel.call(
                "Write a professional e-commerce product description (max 120 words) for: "
                        + request.getPrompt()
        );
        return ResponseEntity.ok(response);
    }
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class AiRequest {
    private String prompt;
}
