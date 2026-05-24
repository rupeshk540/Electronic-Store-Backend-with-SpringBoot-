package com.electronic.store.services.impl;

import com.electronic.store.services.ChatAIService;
import org.springframework.stereotype.Service;

@Service
public class ChatAIServiceImpl implements ChatAIService {

    @Override
    public String getAIResponse(String message) {

        return "AI response";
    }
}
