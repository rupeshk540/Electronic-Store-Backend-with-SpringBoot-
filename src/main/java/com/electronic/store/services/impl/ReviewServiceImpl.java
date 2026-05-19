package com.electronic.store.services.impl;

import com.electronic.store.dtos.ReviewDto;
import com.electronic.store.entities.*;
import com.electronic.store.entities.enums.OrderStatus;
import com.electronic.store.exceptions.BadApiRequestException;
import com.electronic.store.exceptions.ResourceNotFoundException;
import com.electronic.store.repositories.*;
import com.electronic.store.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    @Override
    public ReviewDto createReview(ReviewDto reviewDto, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(reviewDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Order order = orderRepository.findById(reviewDto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));


        if(order.getOrderStatus() != OrderStatus.DELIVERED){
            throw new BadApiRequestException("Review allowed only after delivery");
        }

        boolean alreadyReviewed = reviewRepository.existsByUser_UserIdAndProduct_ProductId(user.getUserId(),product.getProductId());

        if(alreadyReviewed){
            throw new BadApiRequestException("You already reviewed this product");
        }


        Review review = Review.builder()
                .reviewId(UUID.randomUUID().toString())
                .rating(reviewDto.getRating())
                .review(reviewDto.getReview())
                .createdAt(LocalDateTime.now())
                .user(user)
                .product(product)
                .order(order)
                .build();

        Review savedReview =
                reviewRepository.save(review);


        // Update Product Rating
        List<Review> productReviews = reviewRepository.findByProduct_ProductId(product.getProductId());

        double averageRating =
                productReviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0);

        product.setAverageRating(averageRating);

        product.setTotalReviews(productReviews.size());

        productRepository.save(product);

        return convertToDto(savedReview);
    }

    @Override
    public List<ReviewDto> getReviewsOfProduct(String productId) {

        List<Review> reviews = reviewRepository.findByProduct_ProductId(productId);

        return reviews.stream()
                .map(this::convertToDto)
                .toList();
    }

    // Helper
    private ReviewDto convertToDto(Review review){
        ReviewDto dto = modelMapper.map(review, ReviewDto.class);

        dto.setUserId(review.getUser().getUserId());

        dto.setUserName(review.getUser().getName());

        dto.setProductId(review.getProduct().getProductId());

        dto.setOrderId(review.getOrder().getOrderId());

        return dto;
    }
}