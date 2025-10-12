package com.twiiiins.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class GuestOrderRequest {

    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
    private List<OrderItemRequest> items;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 형식이어야 합니다")
    private String email;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    private String phone;

    @NotBlank(message = "배송 주소는 필수입니다")
    private String shippingAddress;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}

