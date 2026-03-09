package com.ecommerce.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResponse {

    private String orderNumber;
    private UUID cartId;
    private String status;
}
