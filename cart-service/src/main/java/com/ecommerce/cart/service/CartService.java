package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.*;
import jakarta.servlet.http.HttpSession;

public interface CartService {

    /** Get or create a cart. Guest → session; logged-in → DB. */
    CartDto getOrCreateCart(HttpSession session);

    /** Add a new item or increment quantity. */
    CartDto addItem(HttpSession session, AddItemRequest request);

    /** Update quantity of an existing item (quantity = 0 removes the item). */
    CartDto updateItem(HttpSession session, String productId, UpdateItemRequest request);

    /** Remove a single item from the cart. */
    CartDto removeItem(HttpSession session, String productId);

    /** Remove all items and clear the cart. */
    void clearCart(HttpSession session);
}
