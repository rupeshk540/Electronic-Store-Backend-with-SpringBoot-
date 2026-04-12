package com.electronic.store.entities;

import com.electronic.store.entities.enums.PaymentMethod;
import com.electronic.store.entities.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;        // COD, RAZORPAY, PAYPAL

    private String transactionId;  // Razorpay/PayPal payment_id
    private String gatewayOrderId;        // Gateway's order ID
    private String signature;

    private Double amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;         // PENDING, SUCCESS, FAILED

    private LocalDateTime paymentDate;

    @OneToOne(mappedBy = "payment")
    @JsonIgnore
    private Order order;
}
