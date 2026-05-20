package com.electronic.store.services.impl;

import com.electronic.store.dtos.*;
import com.electronic.store.entities.*;
import com.electronic.store.entities.enums.OrderStatus;
import com.electronic.store.entities.enums.PaymentMethod;
import com.electronic.store.entities.enums.PaymentStatus;
import com.electronic.store.exceptions.BadApiRequestException;
import com.electronic.store.exceptions.ResourceNotFoundException;
import com.electronic.store.helper.Helper;
import com.electronic.store.repositories.*;
import com.electronic.store.services.CartService;
import com.electronic.store.services.OrderService;
import com.electronic.store.services.PaymentService;
import com.razorpay.RazorpayException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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
    private AddressRepository addressRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private CartService cartService;
    @Autowired
    private ModelMapper modelMapper;
    private Logger logger = LoggerFactory.getLogger(OrderService.class);


    @Override
    @Transactional
    public PaymentInitResponse createOrderAndInitPayment(OrderRequest request) throws RazorpayException {
        //1.Validate user and address
        if (!userRepository.existsById(request.getUserId())) {
            throw new ResourceNotFoundException("User not found with ID: " + request.getUserId());
        }

        if (!addressRepository.existsById(request.getAddressId())) {
            throw new ResourceNotFoundException("Address not found with ID: " + request.getAddressId());
        }


        // ORDER STATUS
        OrderStatus orderStatus =
                request.getPaymentMethod() == PaymentMethod.COD
                        ? OrderStatus.PLACED
                        : OrderStatus.PENDING;

        // 2.Create and build Payment (not yet persisted)
        Payment payment = Payment.builder()
                .paymentMethod(request.getPaymentMethod())
                .amount(request.getTotalAmount())
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .build();

        //3. Create and build Order
        Order order = Order.builder()
                .orderId(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .addressId(request.getAddressId())
                .phone(request.getPhone())
                .email(request.getEmail())
                .subtotal(request.getSubtotal())
                .shippingFee(request.getShippingFee())
                .discount(request.getDiscount())
                .totalAmount(request.getTotalAmount())
                .orderStatus(orderStatus)
                .shippingMethod(request.getShippingMethod())
                .notes(request.getNotes())
                .payment(payment)   // link payment
                .build();

        // link order ↔ payment bidirectionally
        payment.setOrder(order);

        // 4. Map and attach OrderItems
        List<OrderItem> orderItems = request.getOrderItems().stream()
                .map(itemReq -> OrderItem.builder()
                        .productId(itemReq.getProductId())
                        .productTitle(itemReq.getProductTitle())
                        .price(itemReq.getPrice())
                        .quantity(itemReq.getQuantity())
                        .subtotal(itemReq.getPrice() * itemReq.getQuantity())
                        .build())
                .collect(Collectors.toList());

        orderItems.forEach(item -> item.setOrder(order));
        order.setOrderItems(orderItems);

        //5. save order (will cascade items + payment)
        Order order1 = orderRepository.save(order);

        logger.info("Order created with ID: {}", order1.getOrderId());

        // If order came from cart, clear it
        if (request.getFromCart()) {
            cartService.clearCart(request.getUserId());
            logger.info("Cart cleared for user: {}", request.getUserId());
        }

        // FOR COD
        if (request.getPaymentMethod() == PaymentMethod.COD) {

            PaymentInitResponse response = new PaymentInitResponse();

            response.setOrderId(order1.getOrderId());
            response.setSuccess(true);
            response.setMessage("COD Order placed successfully");

            return response;
        }

        //6. Initialize payment (Razorpay etc.)
        OrderDto orderDto = modelMapper.map(order1, OrderDto.class);
        PaymentInitResponse paymentInit = paymentService.initializePayment(orderDto);

        //Update DB (gatewayOrderId)
        if (request.getPaymentMethod() != PaymentMethod.COD && paymentInit.getGatewayOrderId() != null) {
            payment.setGatewayOrderId(paymentInit.getGatewayOrderId());
            paymentRepository.save(payment);
        }

        return paymentInit;
    }

    @Override
    public ApiResponseMessage verifyAndCompleteOrder(PaymentVerifyRequest request) {
        logger.info("Verifying payment for orderId: {}", request.getOrderId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Payment payment = order.getPayment();
        if (payment == null)
            throw new ResourceNotFoundException("No payment linked to order");

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            logger.warn(" Payment already verified for {}", order.getOrderId());
            ApiResponseMessage response = ApiResponseMessage.builder()
                    .message("Payment verified successfully and order placed.")
                    .success(true)
                    .status(HttpStatus.OK)
                    .build();
            return response;
        }

        // verify signature securely
        boolean isValid = paymentService.verifyPaymentSignature(
                request.getGatewayOrderId(),
                request.getPaymentId(),
                request.getSignature()
        );

        if (!isValid)
            throw new BadApiRequestException("Invalid payment signature!");

        // update payment + order
        payment.setTransactionId(request.getPaymentId());
        payment.setGatewayOrderId(request.getGatewayOrderId());
        payment.setSignature(request.getSignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        order.setOrderStatus(OrderStatus.PLACED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        logger.info("Order placed successfully: {}", order.getOrderId());
        ApiResponseMessage response = ApiResponseMessage.builder()
                .message("Payment verified successfully and order placed.")
                .success(true)
                .status(HttpStatus.OK)
                .build();
        return response;
    }

    @Override
    public void removeOrder(String orderId) {
            Order order = orderRepository.findById(orderId).orElseThrow(()->new ResourceNotFoundException("Order not found !!"));
            orderRepository.delete(order);
    }

    @Override
    public List<OrderDto> getOrderOfUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User not found !!"));
        List<Order> orders = orderRepository.findByUserId(userId);
        List<OrderDto> orderDtos = orders.stream().map(order -> modelMapper.map(order,OrderDto.class)).collect(Collectors.toList());
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
    public OrderDto updateOrder(String orderId,UpdateOrderRequest request) {
        // Fetch existing order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found !!"));

        // Update order status if provided
        if (request.getOrderStatus() != null) {
            order.setOrderStatus(request.getOrderStatus());
        }

        // Update payment status if provided
        if (request.getPaymentStatus() != null) {
            Payment payment = order.getPayment();
            if (payment != null) {
                payment.setStatus(request.getPaymentStatus());
                payment.setPaymentDate(LocalDateTime.now()); // optional, update timestamp
            } else {
                throw new BadApiRequestException("Payment not found for this order !!");
            }
        }

        // Update timestamp
        order.setUpdatedAt(LocalDateTime.now());

        // Save the updated order
        Order updatedOrder = orderRepository.save(order);

        // Return mapped DTO
        return modelMapper.map(updatedOrder, OrderDto.class);
    }

    @Override
    public OrderDto cancelOrder(String orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if(order.getOrderStatus() == OrderStatus.SHIPPED ||
                order.getOrderStatus() == OrderStatus.DELIVERED){

            throw new BadApiRequestException(
                    "Order cannot be cancelled now"
            );
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);

        return modelMapper.map(savedOrder, OrderDto.class);
    }
}
