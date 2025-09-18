package com.electronic.store.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PaymentRequest {

        @NotBlank(message = "order id is required !!")
        private String orderId;
        @NotBlank(message = "amount is required !!")
        private int amount;


}
