package com.electronic.store.services.impl;

import com.electronic.store.services.ChatAIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;


@Service
public class ChatAIServiceImpl implements ChatAIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getAIResponse(String userMessage) {

        String prompt = """
        You are an AI shopping assistant for an e-commerce website.

        Store Policies:
        - Delivery takes 3 to 5 days.
        - Return is available within 7 days of delivery.
        - Refund takes 5 to 7 business days after return approval.
        - Cash on Delivery is available on eligible products.
        - Order cancellation is allowed before the product is shipped.
        - For payment failure or order issues, ask the user to contact support.

        Rules:
        - Answer only related to shopping, products, orders, payment, shipping, return, refund, and support.
        - Keep answers short and helpful.
        - Do not invent store policies.
        - If product recommendation is asked, say: "I can help you find products from our store. Please tell me your budget and category."

        User message: %s
        """.formatted(userMessage);

        String requestBody = """
                {
                  "contents": [
                    {
                      "parts": [
                        {
                          "text": "%s"
                        }
                      ]
                    }
                  ]
                }
                """.formatted(escapeJson(prompt));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(geminiApiUrl, HttpMethod.POST, entity, String.class);

            return extractTextFromGeminiResponse(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I am unable to respond right now. Please try again later.";
        }
    }

    private String extractTextFromGeminiResponse(String responseBody) {
        try {
            int textIndex = responseBody.indexOf("\"text\":");
            if (textIndex == -1) {
                return "Sorry, I could not understand the response.";
            }

            int start = responseBody.indexOf("\"", textIndex + 7) + 1;
            int end = responseBody.indexOf("\"", start);

            return responseBody.substring(start, end)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");

        } catch (Exception e) {
            return "Sorry, I could not process the AI response.";
        }
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}

