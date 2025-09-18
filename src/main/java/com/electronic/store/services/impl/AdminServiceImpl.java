package com.electronic.store.services.impl;

import com.electronic.store.dtos.AdminStatsResponse;
import com.electronic.store.repositories.*;
import com.electronic.store.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CollectionRepository collectionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;


    @Override
    public AdminStatsResponse getDashboardStats() {
        long products = productRepository.count();
        long categories = categoryRepository.count();
        long collections = collectionRepository.count();
        long users = userRepository.count();
        long orders = orderRepository.count();
        long pendingOrders = orderRepository.countByorderStatus("PENDING");
        long lowStock = productRepository.countByStockLessThan(10);

        return new AdminStatsResponse(products, categories, collections,
                users, orders, pendingOrders, lowStock);
    }
}
