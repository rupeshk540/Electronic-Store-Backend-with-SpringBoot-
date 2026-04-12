package com.electronic.store.services;

import com.electronic.store.dtos.OrderDto;
import com.electronic.store.dtos.PaymentInitResponse;
import com.electronic.store.dtos.PaymentVerifyRequest;
import com.electronic.store.entities.Order;
import com.electronic.store.entities.Payment;
import com.electronic.store.entities.enums.PaymentMethod;
import com.razorpay.RazorpayException;

import java.util.Map;

public interface PaymentService {


    //Create gateway order for an existing DB order and return gateway data.
    PaymentInitResponse initializePayment(OrderDto orderDto) throws RazorpayException;



    //verify payment signature
    boolean verifyPaymentSignature(String gatewayOrderId, String paymentId, String signature);


    // Optional: handle webhook payload verification for idempotent processing.
    void handleRazorpayWebhook(Map<String, Object> payload, String signature);
}
