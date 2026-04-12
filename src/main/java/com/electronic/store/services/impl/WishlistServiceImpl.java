package com.electronic.store.services.impl;

import com.electronic.store.dtos.WishlistDto;
import com.electronic.store.entities.Product;
import com.electronic.store.entities.User;
import com.electronic.store.entities.Wishlist;
import com.electronic.store.exceptions.ResourceNotFoundException;
import com.electronic.store.repositories.ProductRepository;
import com.electronic.store.repositories.UserRepository;
import com.electronic.store.repositories.WishlistRepository;
import com.electronic.store.services.WishlistService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public WishlistDto getWishlistOfUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found !!"));
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseGet(() -> wishlistRepository.save(Wishlist.builder().user(user).build()));
        return modelMapper.map(wishlist, WishlistDto.class);
    }

    @Override
    public WishlistDto addProductToWishlist(String userId, String productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found !!"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found !!"));

        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseGet(() -> wishlistRepository.save(Wishlist.builder().user(user).build()));

        wishlist.getProducts().add(product);
        wishlistRepository.save(wishlist);

        return modelMapper.map(wishlist, WishlistDto.class);
    }

    @Override
    public WishlistDto removeProductFromWishlist(String userId, String productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found !!"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found !!"));

        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found !!"));

        wishlist.getProducts().remove(product);
        wishlistRepository.save(wishlist);

        return modelMapper.map(wishlist, WishlistDto.class);
    }

    @Override
    public void clearWishlist(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found !!"));
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found !!"));

        wishlist.getProducts().clear();
        wishlistRepository.save(wishlist);
    }

}
