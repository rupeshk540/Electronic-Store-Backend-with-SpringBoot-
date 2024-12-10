package com.electronic.store.controllers;

import com.electronic.store.dtos.ApiResponseMessage;
import com.electronic.store.dtos.CreateOrderRequest;
import com.electronic.store.dtos.OrderDto;
import com.electronic.store.dtos.PageableResponse;
import com.electronic.store.entities.Order;
import com.electronic.store.repositories.OrderRepository;
import com.electronic.store.services.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    //create
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request){
        OrderDto order = orderService.createOrder(request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    //remove order
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
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<OrderDto>> getOrderOfUser(@PathVariable String userId){
        List<OrderDto> orderOfUser = orderService.getOrderOfUser(userId);
        return new ResponseEntity<>(orderOfUser,HttpStatus.OK);
    }

    //get all orders
    @GetMapping
    public ResponseEntity<PageableResponse<OrderDto>> getOrders(
        @RequestParam(value = "pageNumber",defaultValue = "0",required = false) int pageNumber,
        @RequestParam(value = "pageSize",defaultValue = "10",required = false) int pageSize,
        @RequestParam(value = "sortBy",defaultValue = "orderedDate",required = false) String sortBy,
        @RequestParam(value = "sortDir",defaultValue = "desc",required = false) String sortDir
    ){

        PageableResponse<OrderDto> orders = orderService.getOrders(pageNumber,pageSize,sortBy,sortDir);
        return new ResponseEntity<>(orders,HttpStatus.OK);
    }

    //update order
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDto> updateOrder(
            @PathVariable String orderId,
            @RequestBody CreateOrderRequest request
    ){
        OrderDto order = orderService.updateOrder(orderId,request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
}
