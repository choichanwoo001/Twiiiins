package com.twiiiins.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.twiiiins.entity.Order;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripeService {

    @Value("${app.stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    // Payment Intent 생성 (Payment Element 사용 시)
    public PaymentIntent createPaymentIntent(Order order) throws StripeException {
        // 유로 단위로 변환 (센트 단위)
        long amount = order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency("eur")
                .addPaymentMethodType("card")
                .addPaymentMethodType("eps")  // 오스트리아 은행 송금
                .addPaymentMethodType("sepa_debit")  // SEPA 직불
                .putMetadata("orderId", String.valueOf(order.getId()))
                .putMetadata("guestEmail", order.getGuestEmail() != null ? order.getGuestEmail() : "")
                .setDescription("Order #" + order.getId())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        
        return paymentIntent;
    }

    // Checkout Session 생성 (Stripe Checkout 사용 시)
    public Session createCheckoutSession(Order order, String successUrl, String cancelUrl) throws StripeException {
        long amount = order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(amount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Order #" + order.getId())
                                                                .build()
                                                )
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .putMetadata("orderId", String.valueOf(order.getId()))
                .setCustomerEmail(order.getGuestEmail())
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.EPS)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.SEPA_DEBIT)
                .build();

        return Session.create(params);
    }

    // Payment Intent 조회
    public PaymentIntent getPaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }
}

