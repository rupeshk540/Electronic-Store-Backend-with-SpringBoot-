package com.electronic.store.dtos;

import com.electronic.store.entities.enums.OrderStatus;
import com.electronic.store.entities.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderRequest {

    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
}
