package com.electronic.store.dtos;

import com.electronic.store.entities.enums.OrderStatus;
import com.electronic.store.entities.enums.PaymentMethod;
import com.electronic.store.entities.enums.PaymentStatus;
import com.electronic.store.entities.enums.ShippingMethod;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OrderDto {

    private String orderId;
    private String userId;
    private String addressId;

    private List<OrderItemDto> orderItems;

    private Double subtotal;
    private Double shippingFee;
    private Double discount;
    private Double totalAmount;

    private PaymentMethod paymentMethod; // COD, RAZORPAY, PAYPAL
    private PaymentStatus paymentStatus; // PENDING, SUCCESS, FAILED
    private ShippingMethod shippingMethod;
    private OrderStatus orderStatus;

    private String notes;
    private LocalDateTime orderDate;
    private LocalDateTime updatedAt;
}

