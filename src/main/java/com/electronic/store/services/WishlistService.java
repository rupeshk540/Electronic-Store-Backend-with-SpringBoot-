package com.electronic.store.services;

import com.electronic.store.dtos.WishlistDto;

public interface WishlistService {

    WishlistDto getWishlistOfUser(String userId);
    WishlistDto addProductToWishlist(String userId, String productId);
    WishlistDto removeProductFromWishlist(String userId, String productId);
    void clearWishlist(String userId);
}
