package com.electronic.store.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitResponse {

    private String orderId;           // DB orderId (merchant order id)
    private String gatewayOrderId;    // Razorpay / gateway order id
    private String gatewayKey;        // publishable key (frontend)
    private Double amount;
    private String currency;
    private String message;
    private boolean success;
}
