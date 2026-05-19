package com.electronic.store.repositories;

import com.electronic.store.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {

    List<Review> findByProduct_ProductId(String productId);

    boolean existsByUser_UserIdAndProduct_ProductId(String userId, String productId);
}
