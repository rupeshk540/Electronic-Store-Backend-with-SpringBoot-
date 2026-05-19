package com.electronic.store.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {

    private String reviewId;

    private Integer rating;

    private String review;

    private LocalDateTime createdAt;

    private String userId;

    private String userName;

    private String productId;

    private String orderId;
}
