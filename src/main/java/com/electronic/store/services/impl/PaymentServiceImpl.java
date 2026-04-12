package com.electronic.store.services.impl;

import com.electronic.store.controllers.UserController;
import com.electronic.store.dtos.OrderDto;
import com.electronic.store.dtos.PaymentInitResponse;
import com.electronic.store.dtos.PaymentVerifyRequest;
import com.electronic.store.entities.Order;
import com.electronic.store.entities.Payment;
import com.electronic.store.entities.enums.PaymentMethod;
import com.electronic.store.exceptions.ResourceNotFoundException;
import com.electronic.store.repositories.OrderRepository;
import com.electronic.store.repositories.PaymentRepository;
import com.electronic.store.services.PaymentService;
import com.electronic.store.services.RazorpayPaymentGatewayService;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private RazorpayPaymentGatewayService razorpayGateway;

    private Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Override
    @Transactional
    public PaymentInitResponse initializePayment(OrderDto orderDto) {
        Order order = orderRepository.findById(orderDto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getPayment().getPaymentMethod() == PaymentMethod.COD) {
            // No online init for COD
            return PaymentInitResponse.builder()
                    .orderId(order.getOrderId())
                    .gatewayOrderId(null)
                    .gatewayKey(null)
                    .amount(order.getTotalAmount())
                    .currency("INR")
                    .message("COD order created successfully")
                    .build();
        }

        // Delegate to Razorpay gateway
        PaymentInitResponse response = razorpayGateway.createRazorpayOrder(order);

        // persist Razorpay orderId
        Payment payment = order.getPayment();
        payment.setGatewayOrderId(response.getGatewayOrderId());
        paymentRepository.save(payment);

        return response;
    }

    @Override
    public boolean verifyPaymentSignature(String gatewayOrderId, String paymentId, String signature) {
        return razorpayGateway.verifySignature(gatewayOrderId, paymentId, signature);
    }

    @Override
    public void handleRazorpayWebhook(Map<String, Object> payload, String signature) {
        // (future-ready for automatic confirmation)
    }
}
