package com.electronic.store.controllers;

import com.electronic.store.dtos.WishlistDto;
import com.electronic.store.services.WishlistService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wishlist")
@SecurityRequirement(name = "scheme1")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @PreAuthorize("hasAnyRole('NORMAL','ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<WishlistDto> getWishlist(@PathVariable String userId) {
        return ResponseEntity.ok(wishlistService.getWishlistOfUser(userId));
    }

    @PreAuthorize("hasAnyRole('NORMAL','ADMIN')")
    @PostMapping("/{userId}/products/{productId}")
    public ResponseEntity<WishlistDto> addProduct(
            @PathVariable String userId,
            @PathVariable String productId) {
        return ResponseEntity.ok(wishlistService.addProductToWishlist(userId, productId));
    }

    @PreAuthorize("hasAnyRole('NORMAL','ADMIN')")
    @DeleteMapping("/{userId}/products/{productId}")
    public ResponseEntity<WishlistDto> removeProduct(
            @PathVariable String userId,
            @PathVariable String productId) {
        return ResponseEntity.ok(wishlistService.removeProductFromWishlist(userId, productId));
    }

    @PreAuthorize("hasAnyRole('NORMAL','ADMIN')")
    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<Void> clearWishlist(@PathVariable String userId) {
        wishlistService.clearWishlist(userId);
        return ResponseEntity.noContent().build();
    }
}
