package com.irenewhub.backend.controller;

import com.irenewhub.backend.dto.CreateOrderRequest;
import com.irenewhub.backend.dto.OrderResponse;
import com.irenewhub.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        OrderResponse response = orderService.createOrder(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            Authentication authentication) {

        String userEmail = authentication.getName();
        return ResponseEntity.ok(orderService.getMyOrders(userEmail));
    }
}