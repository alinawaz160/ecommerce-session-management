package com.ecommerce.cart.service.cart;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartCreationService {

    private final CartRepository cartRepository;

    /**
     * Creates a cart for a logged-in user (REQUIRES_NEW transaction).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Cart createCartForUser(Long userId) {
        log.info("Creating new DB cart for userId: {}", userId);
        return cartRepository.save(Cart.builder().userId(userId).build());
    }

    /**
     * Creates a cart for a guest session (REQUIRES_NEW transaction).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Cart createCart(String sessionId) {
        log.info("Creating new DB cart for session: {}", sessionId);
        return cartRepository.save(Cart.builder().sessionId(sessionId).build());
    }
}
