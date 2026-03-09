package com.ecommerce.checkout.service;

import com.ecommerce.checkout.dto.*;

import java.util.List;
import java.util.UUID;

public interface CheckoutService {

    /**
     * Step 1 — Store cart snapshot in Cassandra, return cartId.
     * Cart data is received directly from the Main (cart) service.
     */
    CheckoutResponse initiateCheckout(CheckoutRequest request);

    /**
     * Step 2 — Place order by calling Order service.
     * Uses the previously stored cart snapshot identified by cartId.
     */
    PlaceOrderResponse placeOrder(PlaceOrderRequest request);

    /** Retrieve a single cart snapshot by cartId. */
    CheckoutResponse getCartSnapshot(UUID cartId);

    /** Retrieve all snapshots for a given session. */
    List<CheckoutResponse> getSnapshotsBySession(String sessionId);
}
