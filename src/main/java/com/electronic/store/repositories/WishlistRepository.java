package com.electronic.store.repositories;

import com.electronic.store.entities.User;
import com.electronic.store.entities.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist,String> {

    Optional<Wishlist> findByUser(User user);
}
