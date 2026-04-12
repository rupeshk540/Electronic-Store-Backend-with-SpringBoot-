package com.electronic.store.controllers;

import com.electronic.store.dtos.PaymentRequest;
import com.electronic.store.services.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@SecurityRequirement(name = "scheme1")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Optional: if you handle Razorpay webhooks directly
    @PostMapping("/webhook/razorpay")
    public ResponseEntity<Void> handleRazorpayWebhook(@RequestBody Map<String, Object> payload,
                                                      @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handleRazorpayWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

//    @PostMapping("/user/createOrder")
//    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody PaymentRequest request) throws RazorpayException {
//
//
//        RazorpayClient razorpayClient = new RazorpayClient("rzp_test_1MavwWrVN7zWZ5", "Qv0lEvvfQm3BepCAfoQgNkCL");
//
//        JSONObject orderRequest = new JSONObject();
//        orderRequest.put("amount", request.getAmount()*100); // amount in paise
//        orderRequest.put("currency", "INR");
//        orderRequest.put("receipt", "order_rcptid_11");
//
//        Order order = razorpayClient.orders.create(orderRequest);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", "created");
//        response.put("razorpayOrderId", order.get("id"));
//        response.put("amount", order.get("amount"));
//        return ResponseEntity.ok(response );
//    }
}
