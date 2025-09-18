package com.electronic.store.controllers;

import com.electronic.store.dtos.PaymentRequest;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@SecurityRequirement(name = "scheme1")
public class PaymentController {

    @PostMapping("/user/createOrder")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody PaymentRequest request) throws RazorpayException {


        RazorpayClient razorpayClient = new RazorpayClient("rzp_test_1MavwWrVN7zWZ5", "Qv0lEvvfQm3BepCAfoQgNkCL");

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", request.getAmount()*100); // amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "order_rcptid_11");

        Order order = razorpayClient.orders.create(orderRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "created");
        response.put("razorpayOrderId", order.get("id"));
        response.put("amount", order.get("amount"));
        return ResponseEntity.ok(response );
    }
}
