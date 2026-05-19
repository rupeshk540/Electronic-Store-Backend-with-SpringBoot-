package com.electronic.store.services;

import com.electronic.store.dtos.ReviewDto;

import java.util.List;

public interface ReviewService {

    ReviewDto createReview(ReviewDto reviewDto, String userId);

    List<ReviewDto> getReviewsOfProduct(String productId);
}
