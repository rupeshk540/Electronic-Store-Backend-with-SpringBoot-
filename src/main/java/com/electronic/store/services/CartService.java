package com.electronic.store.services;

import com.electronic.store.dtos.AddItemToCartRequest;
import com.electronic.store.dtos.CartDto;

public interface CartService {

    //Add item to cart:
    //case1: cart for user is not available - we will create cart and then add
    //case2: cart available - add the items to cart
    CartDto addItemToCart(String userId, AddItemToCartRequest request);

    //remove item from cart
    void removeItemFromCart(String userId,int cartItem);

    //remove all items from cart
    void clearCart(String userId);

    //get cart
    CartDto getCartByUser(String userId);
}
