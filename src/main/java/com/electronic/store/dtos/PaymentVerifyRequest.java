package com.electronic.store.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentVerifyRequest {

    private String orderId;           // DB orderId (merchant order id)
    private String gatewayOrderId;   // gateway order id
    private String paymentId; // payment id returned by razorpay in handler
    private String signature; // signature to verify
}
