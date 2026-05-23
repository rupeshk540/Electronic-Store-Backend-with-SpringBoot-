package com.electronic.store.services;

import com.electronic.store.dtos.*;
import com.electronic.store.entities.enums.OrderStatus;
import com.razorpay.RazorpayException;

import java.util.List;

public interface OrderService {

    //create oder and initialize payment
    PaymentInitResponse createOrderAndInitPayment(OrderRequest request) throws RazorpayException;

    //verify payment
    ApiResponseMessage verifyAndCompleteOrder(PaymentVerifyRequest verifyRequest);

    //remove order
    void removeOrder(String orderId);

    //get orders of user
    List<OrderDto> getOrderOfUser(String userId);

    //get orders
    PageableResponse<OrderDto> getOrders(int pageNumber,int pageSize,String sortBy,String sortDir);

    //update order
    OrderDto updateOrder(String orderId, UpdateOrderRequest request);

    //update order status
    OrderDto updateOrderStatus(String orderId, String status);

    //cancel order
    OrderDto cancelOrder(String orderId);

    //request for return
    OrderDto requestReturn(String orderId);

    //order stats
    OrderStatsDto getOrderStats();
}
