package com.twiiiins.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // 게스트 주문 시 null 가능

    private String guestEmail;  // 게스트 이메일
    private String guestName;   // 게스트 이름
    private String guestPhone;  // 게스트 전화번호

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(unique = true)
    private String stripePaymentIntentId;  // Stripe Payment Intent ID

    @Column(columnDefinition = "TEXT")
    private String shippingAddress;

    private String trackingNumber;  // 배송 추적 번호

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING,        // 결제 대기
        PAID,           // 결제 완료
        PROCESSING,     // 처리 중
        SHIPPED,        // 배송 중
        DELIVERED,      // 배송 완료
        CANCELLED,      // 취소됨
        REFUNDED        // 환불됨
    }

    // 연관관계 편의 메서드
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}

