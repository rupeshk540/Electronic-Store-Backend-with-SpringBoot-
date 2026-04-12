package com.electronic.store.dtos;

import com.electronic.store.entities.enums.PaymentMethod;
import com.electronic.store.entities.enums.PaymentStatus;
import com.electronic.store.entities.enums.ShippingMethod;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotBlank(message = "userId required..!!")
    private String userId;
    @NotBlank(message = "addressId required..!!")
    private String addressId;
    private String phone;
    private String email;
    private List<OrderItemRequest> orderItems;

    private PaymentMethod paymentMethod;   // COD, RAZORPAY, PAYPAL
    private String transactionId;   // optional for online
    private String gatewayOrderId;  // optional for Razorpay
   // private PaymentStatus paymentStatus;   // SUCCESS, FAILED, PENDING

    private Double subtotal;
    private Double shippingFee;
    private Double discount;
    private Double totalAmount;
    private ShippingMethod shippingMethod;
    private String notes;
    private Boolean fromCart;
}
