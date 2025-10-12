package com.twiiiins.service;

import com.twiiiins.dto.CreateOrderRequest;
import com.twiiiins.dto.GuestOrderRequest;
import com.twiiiins.entity.Order;
import com.twiiiins.entity.OrderItem;
import com.twiiiins.entity.Product;
import com.twiiiins.entity.User;
import com.twiiiins.repository.OrderRepository;
import com.twiiiins.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public List<Order> getUserOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));
    }

    @Transactional
    public Order createOrder(String email, CreateOrderRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        // 주문 생성
        Order order = Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .status(Order.OrderStatus.PENDING)
                .build();

        // 주문 항목 추가 및 총액 계산
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productService.getProductById(itemReq.getProductId());
            
            // 재고 확인
            if (product.getStock() < itemReq.getQuantity()) {
                throw new RuntimeException("재고가 부족합니다: " + product.getName());
            }

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();

            order.addItem(orderItem);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    @Transactional
    public Order createGuestOrder(GuestOrderRequest request) {
        // 게스트 주문 생성 (User 없이)
        Order order = Order.builder()
                .guestEmail(request.getEmail())
                .guestName(request.getName())
                .guestPhone(request.getPhone())
                .shippingAddress(request.getShippingAddress())
                .status(Order.OrderStatus.PENDING)
                .build();

        // 주문 항목 추가 및 총액 계산
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (GuestOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productService.getProductById(itemReq.getProductId());
            
            // 재고 확인
            if (product.getStock() < itemReq.getQuantity()) {
                throw new RuntimeException("재고가 부족합니다: " + product.getName());
            }

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();

            order.addItem(orderItem);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    @Transactional
    public void updateOrderStatus(String paymentIntentId, Order.OrderStatus status) {
        Order order = orderRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));
        
        order.setStatus(status);
        orderRepository.save(order);

        // 결제 완료 시 재고 차감
        if (status == Order.OrderStatus.PAID) {
            for (OrderItem item : order.getItems()) {
                productService.updateStock(item.getProduct().getId(), item.getQuantity());
            }
        }
    }
}

