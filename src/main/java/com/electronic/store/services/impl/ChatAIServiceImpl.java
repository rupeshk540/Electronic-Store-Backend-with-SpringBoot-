package com.electronic.store.services.impl;

import com.electronic.store.dtos.PriceFilterDto;
import com.electronic.store.entities.Product;
import com.electronic.store.repositories.ProductRepository;
import com.electronic.store.services.ChatAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;


@Service
public class ChatAIServiceImpl implements ChatAIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Autowired
    private final ProductRepository productRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public ChatAIServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    @Override
    public String getAIResponse(String userMessage) {

        System.out.println("AI USER MESSAGE: " + userMessage);
        System.out.println("IS PRODUCT QUERY: " + isProductRecommendationQuery(userMessage));

        if (isProductRecommendationQuery(userMessage)) {

            String keyword = extractSearchKeyword(userMessage);
            boolean generalProductSearch =
                    keyword.equals("product")
                            || keyword.equals("products")
                            || keyword.isBlank();
            PriceFilterDto priceFilter = extractPriceFilter(userMessage);


            if (keyword == null || keyword.isBlank() || priceFilter == null) {
                return "Please tell me the product name and price range. Example: show shirt under 1000 or show shirt above 1000.";
            }


            List<Product> products;

            if (generalProductSearch) {

                if (priceFilter.getType().equals("UNDER")) {
                    products = productRepository.searchAllProductsUnderBudget(priceFilter.getPrice());
                } else {
                    products = productRepository.searchAllProductsAboveBudget(priceFilter.getPrice());
                }

            } else {

                if (priceFilter.getType().equals("UNDER")) {
                    products = productRepository.searchProductsUnderBudget(keyword, priceFilter.getPrice());
                } else {
                    products = productRepository.searchProductsAboveBudget(keyword, priceFilter.getPrice());
                }
            }

            if (products.isEmpty()) {
                return "Sorry, I couldn't find matching products in our store right now.";
            }

            StringBuilder reply = new StringBuilder("Here are some products from our store:\n\n");

            reply.append("Found ")
                    .append(products.size())
                    .append(" matching products.\n\n");
            int count = Math.min(products.size(), 5);

            for (int i = 0; i < count; i++) {
                Product p = products.get(i);

                reply.append(i + 1)
                        .append(". ")
                        .append(p.getTitle())
                        .append("\n")
                        .append("Price: ₹")
                        .append(p.getDiscountedPrice())
                        .append("\n")
                        .append("Rating: ⭐ ")
                        .append(p.getAverageRating())
                        .append("\n")
                        .append("View Product: http://localhost:3000/products/")
                        .append(p.getProductId())
                        .append("\n\n");
            }

            return reply.toString();
        }

        // existing Gemini FAQ/general code below


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

    private boolean isProductRecommendationQuery(String message) {
        String lower = message.toLowerCase();

        return lower.contains("suggest")
                || lower.contains("recommend")
                || lower.contains("show")
                || lower.contains("find")
                || lower.contains("under")
                || lower.contains("below")
                || lower.contains("best")
                || lower.contains("buy");
    }

    private PriceFilterDto extractPriceFilter(String message) {
        String lower = message.toLowerCase();

        Pattern underPattern = Pattern.compile("(under|below|less than)\\s*₹?\\s*(\\d+)");
        Matcher underMatcher = underPattern.matcher(lower);

        if (underMatcher.find()) {
            return new PriceFilterDto(Integer.parseInt(underMatcher.group(2)), "UNDER");
        }

        Pattern abovePattern = Pattern.compile("(above|over|more than|greater than)\\s*₹?\\s*(\\d+)");
        Matcher aboveMatcher = abovePattern.matcher(lower);

        if (aboveMatcher.find()) {
            return new PriceFilterDto(Integer.parseInt(aboveMatcher.group(2)), "ABOVE");
        }

        return null;
    }

    private String extractSearchKeyword(String message) {
        String lower = message.toLowerCase();

        lower = lower.replace("suggest", "");
        lower = lower.replace("recommend", "");
        lower = lower.replace("show", "");
        lower = lower.replace("find", "");
        lower = lower.replace("best", "");
        lower = lower.replace("buy", "");
        lower = lower.replace("under", "");
        lower = lower.replace("below", "");
        lower = lower.replace("less than", "");
        lower = lower.replace("₹", "");
        lower = lower.replaceAll("\\d+", "");

        String cleaned = lower.trim();

        String[] words = cleaned.split("\\s+");

        if (words.length > 0) {
            return words[0];
        }

        return cleaned;
    }


}


