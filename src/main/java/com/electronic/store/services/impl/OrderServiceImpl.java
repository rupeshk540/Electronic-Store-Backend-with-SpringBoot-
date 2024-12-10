package com.electronic.store.services.impl;

import com.electronic.store.dtos.CreateOrderRequest;
import com.electronic.store.dtos.OrderDto;
import com.electronic.store.dtos.PageableResponse;
import com.electronic.store.entities.*;
import com.electronic.store.exceptions.BadApiRequestException;
import com.electronic.store.exceptions.ResourceNotFoundException;
import com.electronic.store.helper.Helper;
import com.electronic.store.repositories.CartRepository;
import com.electronic.store.repositories.OrderRepository;
import com.electronic.store.repositories.UserRepository;
import com.electronic.store.services.OrderService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ModelMapper modalMapper;


    @Override
    public OrderDto createOrder(CreateOrderRequest orderDto) {
        String userId = orderDto.getUserId();
        String cartId = orderDto.getCartId();
        //fetch user
        User user = userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User not found with given id !!"));
        //fetch cart
        Cart cart = cartRepository.findById(cartId).orElseThrow(()->new ResourceNotFoundException("Cart not found in DB !!"));

        List<CartItem> cartItems = cart.getItems();
        if(cartItems.size() <= 0){
            throw new BadApiRequestException("Cart is Empty !!");
        }

        Order order = Order.builder()
                .billingName(orderDto.getBillingName())
                .billingPhone(orderDto.getBillingPhone())
                .billingAddress(orderDto.getBillingAddress())
                .orderedDate(new Date())
                .deliveredDate(null)
                .paymentStatus(orderDto.getPaymentStatus())
                .orderStatus(orderDto.getOrderStatus())
                .orderId(UUID.randomUUID().toString())
                .user(user)
                .build();

        AtomicReference<Integer> orderAmount=new AtomicReference<>(0);
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            //cartItem->OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .quantity(cartItem.getQuantity())
                    .product(cartItem.getProduct())
                    .totalPrice(cartItem.getQuantity()*cartItem.getProduct().getDiscountedPrice())
                    .order(order)
                    .build();
            orderAmount.set(orderAmount.get()+orderItem.getTotalPrice());
            return orderItem;
        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);
        order.setOrderAmount(orderAmount.get());

        cart.getItems().clear();
        cartRepository.save(cart);
        Order savedOrder = orderRepository.save(order);
        return modalMapper.map(savedOrder,OrderDto.class);
    }

    @Override
    public void removeOrder(String orderId) {
            Order order = orderRepository.findById(orderId).orElseThrow(()->new ResourceNotFoundException("Order not found !!"));
            orderRepository.delete(order);
    }

    @Override
    public List<OrderDto> getOrderOfUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User not found !!"));
        List<Order> orders = orderRepository.findByUser(user);
        List<OrderDto> orderDtos = orders.stream().map(order -> modalMapper.map(order,OrderDto.class)).collect(Collectors.toList());
        return orderDtos;
    }

    @Override
    public PageableResponse<OrderDto> getOrders(int pageNumber, int pageSize, String sortBy, String sortDir) {
        Sort sort = (sortDir.equalsIgnoreCase("desc"))?(Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());
        Pageable pageable = PageRequest.of(pageNumber,pageSize,sort);
        Page<Order> page = orderRepository.findAll(pageable);
        return Helper.getPageableResponse(page,OrderDto.class);
    }

    @Override
    public OrderDto updateOrder(String orderId,CreateOrderRequest request) {
        Order order = orderRepository.findById(orderId).orElseThrow(()->new ResourceNotFoundException("Order not found !!"));
            order.setBillingName(request.getBillingName());
            order.setBillingAddress(request.getBillingAddress());
            order.setBillingPhone(request.getBillingPhone());

        Order savedOrder = orderRepository.save(order);
        return modalMapper.map(savedOrder,OrderDto.class);
    }
}
