package com.electronic.store.controllers;

import com.electronic.store.dtos.ChatAiRequestDto;
import com.electronic.store.dtos.ChatAiResponseDto;
import com.electronic.store.services.ChatAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/chat")
public class ChatAiController {

    @Autowired
    private ChatAIService chatAIService;

    @PostMapping
    public ChatAiResponseDto chat(@RequestBody ChatAiRequestDto request) {

        String reply = chatAIService.getAIResponse(request.getMessage());

        return new ChatAiResponseDto(reply);
    }
}
