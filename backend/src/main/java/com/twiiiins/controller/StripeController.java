package com.twiiiins.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.twiiiins.entity.Order;
import com.twiiiins.service.OrderService;
import com.twiiiins.service.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final StripeService stripeService;
    private final OrderService orderService;

    @Value("${app.stripe.webhook-secret}")
    private String webhookSecret;

    // Payment Intent 생성 (Payment Element 방식)
    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Long> request) {
        try {
            Long orderId = request.get("orderId");
            Order order = orderService.getOrderById(orderId);

            PaymentIntent paymentIntent = stripeService.createPaymentIntent(order);
            
            // Payment Intent ID를 주문에 저장
            order.setStripePaymentIntentId(paymentIntent.getId());

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("paymentIntentId", paymentIntent.getId());

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Stripe error: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Checkout Session 생성 (Stripe Checkout 방식)
    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestBody Map<String, Object> request) {
        try {
            Long orderId = Long.valueOf(request.get("orderId").toString());
            String successUrl = request.get("successUrl").toString();
            String cancelUrl = request.get("cancelUrl").toString();

            Order order = orderService.getOrderById(orderId);
            Session session = stripeService.createCheckoutSession(order, successUrl, cancelUrl);

            Map<String, String> response = new HashMap<>();
            response.put("sessionId", session.getId());
            response.put("url", session.getUrl());

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Stripe error: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Stripe Webhook 핸들러
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            log.error("Webhook signature verification failed: ", e);
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        // 이벤트 타입별 처리
        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            default:
                log.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Success");
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (paymentIntent != null) {
            log.info("Payment succeeded for PaymentIntent: {}", paymentIntent.getId());
            orderService.updateOrderStatus(paymentIntent.getId(), Order.OrderStatus.PAID);
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (paymentIntent != null) {
            log.warn("Payment failed for PaymentIntent: {}", paymentIntent.getId());
            orderService.updateOrderStatus(paymentIntent.getId(), Order.OrderStatus.CANCELLED);
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (session != null && session.getPaymentIntent() != null) {
            log.info("Checkout completed for Session: {}", session.getId());
            orderService.updateOrderStatus(session.getPaymentIntent(), Order.OrderStatus.PAID);
        }
    }
}

