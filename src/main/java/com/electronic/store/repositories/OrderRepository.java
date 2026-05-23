package com.electronic.store.repositories;

import com.electronic.store.entities.Order;
import com.electronic.store.entities.User;
import com.electronic.store.entities.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,String> {

    List<Order> findByUserId(String userId);
    long count();
    long countByOrderStatus(OrderStatus status);
    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    Double getTotalRevenue();
}
