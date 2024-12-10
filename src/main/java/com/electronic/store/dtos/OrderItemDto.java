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

    private int orderItemId;
    private int quantity;
    private int totalPrice;
    private ProductDto product;
}
