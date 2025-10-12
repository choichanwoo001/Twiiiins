package com.twiiiins.controller;

import com.twiiiins.dto.GuestOrderRequest;
import com.twiiiins.entity.Order;
import com.twiiiins.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/guest")
    public ResponseEntity<Order> createGuestOrder(@Valid @RequestBody GuestOrderRequest request) {
        Order order = orderService.createGuestOrder(request);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}

