package com.electronic.store.controllers;

import com.electronic.store.dtos.*;
import com.electronic.store.entities.Order;
import com.electronic.store.repositories.OrderRepository;
import com.electronic.store.services.OrderService;
import com.razorpay.RazorpayException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@SecurityRequirement(name = "scheme1")
public class OrderController {

    @Autowired
    private OrderService orderService;

    //create
    @PreAuthorize("hasAnyRole('NORMAL','ADMIN')")
    @PostMapping
    public ResponseEntity<PaymentInitResponse> createOrder(@Valid @RequestBody OrderRequest request) throws RazorpayException {
        PaymentInitResponse response = orderService.createOrderAndInitPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //  Verify payment after frontend callback
    @PreAuthorize("hasAnyRole('NORMAL','ADMIN')")
    @PostMapping("/verify")
    public ResponseEntity<ApiResponseMessage> verifyPayment(@RequestBody PaymentVerifyRequest verifyRequest) {
        ApiResponseMessage response = orderService.verifyAndCompleteOrder(verifyRequest);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    //remove order
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponseMessage> removeOrder(@PathVariable String orderId){
        orderService.removeOrder(orderId);
        ApiResponseMessage responseMessage = ApiResponseMessage.builder()
                .status(HttpStatus.OK)
                .message("order is removed !!")
                .success(true)
                .build();
        return new ResponseEntity<>(responseMessage,HttpStatus.OK);
    }

    //get orders of the User
    @PreAuthorize("hasAnyRole('NORMAL','ADMIN')")
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<OrderDto>> getOrderOfUser(@PathVariable String userId){
        List<OrderDto> orderOfUser = orderService.getOrderOfUser(userId);
        return new ResponseEntity<>(orderOfUser,HttpStatus.OK);
    }

    //get all orders
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PageableResponse<OrderDto>> getOrders(
        @RequestParam(value = "pageNumber",defaultValue = "0",required = false) int pageNumber,
        @RequestParam(value = "pageSize",defaultValue = "10",required = false) int pageSize,
        @RequestParam(value = "sortBy",defaultValue = "orderDate",required = false) String sortBy,
        @RequestParam(value = "sortDir",defaultValue = "desc",required = false) String sortDir
    ){

        PageableResponse<OrderDto> orders = orderService.getOrders(pageNumber,pageSize,sortBy,sortDir);
        return new ResponseEntity<>(orders,HttpStatus.OK);
    }

    //update order
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDto> updateOrder(
            @PathVariable String orderId,
            @RequestBody UpdateOrderRequest request
    ){
        OrderDto order = orderService.updateOrder(orderId,request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    //cancel order
    @PreAuthorize("hasAnyRole('NORMAL','ADMIN')")
    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<OrderDto> cancelOrder(
            @PathVariable String orderId){

        OrderDto cancelledOrder = orderService.cancelOrder(orderId);

        return ResponseEntity.ok(cancelledOrder);
    }

    @PutMapping("/return/{orderId}")
    public ResponseEntity<OrderDto> requestReturn(@PathVariable String orderId){

        OrderDto order = orderService.requestReturn(orderId);

        return ResponseEntity.ok(order);
    }
}
