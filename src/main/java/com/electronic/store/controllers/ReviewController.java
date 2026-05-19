package com.electronic.store.controllers;

import com.electronic.store.dtos.ReviewDto;
import com.electronic.store.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Create Review
    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            @RequestBody ReviewDto reviewDto,
            Principal principal
    ) {
        ReviewDto dto =
                reviewService.createReview(reviewDto, principal.getName());

        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    // Get Product Reviews
    @GetMapping("/product/{productId}")
    public List<ReviewDto> getReviewsOfProduct(
            @PathVariable String productId
    ){

        return reviewService.getReviewsOfProduct(productId);
    }
}
