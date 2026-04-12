package com.electronic.store.dtos;

import com.electronic.store.entities.Order;
import com.electronic.store.entities.Product;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderItemDto {

    private Long orderItemId;
    private String productId;
    private String productTitle;
    private Double price;
    private Integer quantity;
    private Double subtotal;
}
