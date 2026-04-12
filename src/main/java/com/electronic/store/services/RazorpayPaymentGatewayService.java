package com.electronic.store.services;

import com.electronic.store.dtos.PaymentInitResponse;
import com.electronic.store.entities.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RazorpayPaymentGatewayService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public PaymentInitResponse createRazorpayOrder(Order order) {
        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject options = new JSONObject();
            options.put("amount", Math.round(order.getTotalAmount() * 100));  // in paise
            options.put("currency", "INR");
            options.put("receipt", order.getOrderId());
            options.put("payment_capture", 1);

            com.razorpay.Order razorpayOrder = client.orders.create(options);

            return new PaymentInitResponse(
                    order.getOrderId(),
                    razorpayOrder.get("id"),
                    razorpayKeyId,
                    order.getTotalAmount(),
                    "INR",
                    "Payment initialized successfully"
            );
        } catch (Exception e) {
            throw new RuntimeException("Razorpay order creation failed", e);
        }
    }

    public boolean verifySignature(String gatewayOrderId, String paymentId, String signature) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("razorpay_order_id", gatewayOrderId);
            payload.put("razorpay_payment_id", paymentId);
            payload.put("razorpay_signature", signature);

            Utils.verifyPaymentSignature(payload, razorpayKeySecret);

            return true; // valid
        } catch (Exception e) {
            return false; // invalid
        }
    }
}
