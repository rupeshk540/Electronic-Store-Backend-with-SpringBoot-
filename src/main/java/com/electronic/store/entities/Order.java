package com.electronic.store.entities;

import com.electronic.store.entities.enums.OrderStatus;
import com.electronic.store.entities.enums.PaymentStatus;
import com.electronic.store.entities.enums.ShippingMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "orders")
public class Order {

    @Id
    private String orderId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;
    private String userId;
    private String phone;
    private String email;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

//    @ManyToOne
//    @JoinColumn(name = "address_id")
//    private Address address;
    private String addressId; // fetch from saved Address entity

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private Double subtotal;
    private Double shippingFee;
    private Double discount;
    private Double totalAmount;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; // PROCESSING, SHIPPED, DELIVERED
    @Enumerated(EnumType.STRING)
    private ShippingMethod shippingMethod; // STANDARD, EXPRESS
    private String notes;

    private LocalDateTime orderDate;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        orderDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
