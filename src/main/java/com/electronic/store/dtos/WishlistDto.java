package com.electronic.store.dtos;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishlistDto {
    private String wishlistId;
    private String userId;
    private Set<ProductDto> products = new HashSet<>();
}
