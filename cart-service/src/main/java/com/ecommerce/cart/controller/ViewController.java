package com.ecommerce.cart.controller;

import com.ecommerce.cart.client.CheckoutServiceClient;
import com.ecommerce.cart.dto.*;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.cart.service.UserService;
import com.ecommerce.cart.service.UserServiceImpl;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ViewController {

    private static final List<ProductController.ProductDto> PRODUCTS = List.of(
        ProductController.ProductDto.builder().productId("P001").productName("Luxury Sofa").price(new BigDecimal("499.99")).imageUrl("https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400").stock(100).build(),
        ProductController.ProductDto.builder().productId("P002").productName("TV Stand").price(new BigDecimal("149.99")).imageUrl("https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=400").stock(50).build(),
        ProductController.ProductDto.builder().productId("P003").productName("Coffee Table").price(new BigDecimal("89.99")).imageUrl("https://images.unsplash.com/photo-1565791380713-1756b9a05343?w=400").stock(75).build(),
        ProductController.ProductDto.builder().productId("P004").productName("Bookshelf").price(new BigDecimal("119.99")).imageUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400").stock(30).build(),
        ProductController.ProductDto.builder().productId("P005").productName("Dining Table Set").price(new BigDecimal("699.99")).imageUrl("https://images.unsplash.com/photo-1617806118233-18e1de247200?w=400").stock(20).build()
    );

    private final CartService cartService;
    private final UserService userService;
    private final CheckoutServiceClient checkoutServiceClient;

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void addCommonAttrs(Model model, HttpSession session) {
        CartDto cart = cartService.getOrCreateCart(session);
        model.addAttribute("cartCount", cart.getTotalItems());
        model.addAttribute("userEmail", session.getAttribute(UserServiceImpl.SESSION_USER_EMAIL));
        model.addAttribute("loggedIn", session.getAttribute(UserServiceImpl.SESSION_USER_ID) != null);
    }

    // ── Home ───────────────────────────────────────────────────────────────────

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        addCommonAttrs(model, session);
        model.addAttribute("products", PRODUCTS);
        return "index";
    }

    // ── Cart ───────────────────────────────────────────────────────────────────

    @GetMapping("/cart-page")
    public String cartPage(Model model, HttpSession session) {
        addCommonAttrs(model, session);
        CartDto cart = cartService.getOrCreateCart(session);
        model.addAttribute("cart", cart);
        return "cart";
    }

    @PostMapping("/cart-page/add")
    public String addToCart(@RequestParam String productId,
                            @RequestParam String productName,
                            @RequestParam BigDecimal price,
                            @RequestParam(defaultValue = "1") int quantity,
                            @RequestParam(required = false) String imageUrl,
                            HttpSession session,
                            RedirectAttributes ra) {
        try {
            AddItemRequest req = new AddItemRequest();
            req.setProductId(productId);
            req.setProductName(productName);
            req.setPrice(price);
            req.setQuantity(quantity);
            req.setImageUrl(imageUrl);
            cartService.addItem(session, req);
            ra.addFlashAttribute("success", productName + " added to cart!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not add item: " + e.getMessage());
        }
        return "redirect:/cart-page";
    }

    @PostMapping("/cart-page/update/{productId}")
    public String updateCartItem(@PathVariable String productId,
                                 @RequestParam int quantity,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        try {
            UpdateItemRequest req = new UpdateItemRequest();
            req.setQuantity(quantity);
            cartService.updateItem(session, productId, req);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not update item: " + e.getMessage());
        }
        return "redirect:/cart-page";
    }

    @PostMapping("/cart-page/remove/{productId}")
    public String removeCartItem(@PathVariable String productId,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        try {
            cartService.removeItem(session, productId);
            ra.addFlashAttribute("success", "Item removed.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not remove item: " + e.getMessage());
        }
        return "redirect:/cart-page";
    }

    // ── Auth ───────────────────────────────────────────────────────────────────

    @GetMapping("/register-page")
    public String registerPage(Model model, HttpSession session) {
        addCommonAttrs(model, session);
        return "register";
    }

    @PostMapping("/register-page")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           HttpSession session,
                           RedirectAttributes ra) {
        try {
            RegisterRequest req = new RegisterRequest();
            req.setUsername(username);
            req.setEmail(email);
            req.setPassword(password);
            userService.register(req);
            ra.addFlashAttribute("success", "Account created! Please log in.");
            return "redirect:/login-page";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/register-page";
        }
    }

    @GetMapping("/login-page")
    public String loginPage(Model model, HttpSession session) {
        addCommonAttrs(model, session);
        return "login";
    }

    @PostMapping("/login-page")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes ra) {
        try {
            LoginRequest req = new LoginRequest();
            req.setUsername(username);
            req.setPassword(password);
            userService.login(req, session);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/login-page";
        }
    }

    @PostMapping("/logout-page")
    public String logout(HttpSession session) {
        userService.logout(session);
        return "redirect:/";
    }

    // ── Checkout ───────────────────────────────────────────────────────────────

    @GetMapping("/checkout-page")
    public String checkoutPage(Model model, HttpSession session) {
        addCommonAttrs(model, session);
        CartDto cart = cartService.getOrCreateCart(session);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/cart-page";
        }
        model.addAttribute("cart", cart);
        return "checkout";
    }

    @PostMapping("/checkout-page")
    public String initiateCheckout(@RequestParam String shippingAddress,
                                   @RequestParam String paymentMethod,
                                   @RequestParam(required = false) List<String> productIds,
                                   @RequestParam(required = false) List<Integer> quantities,
                                   HttpSession session,
                                   RedirectAttributes ra) {
        try {
            // Apply any quantity changes from the checkout form
            if (productIds != null && quantities != null) {
                for (int i = 0; i < productIds.size(); i++) {
                    UpdateItemRequest upd = new UpdateItemRequest();
                    upd.setQuantity(quantities.get(i));
                    cartService.updateItem(session, productIds.get(i), upd);
                }
            }

            CartDto cart = cartService.getOrCreateCart(session);
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                ra.addFlashAttribute("error", "Your cart is empty.");
                return "redirect:/cart-page";
            }

            CheckoutServiceClient.CheckoutRequest req = new CheckoutServiceClient.CheckoutRequest();
            req.setSessionId(session.getId());
            req.setShippingAddress(shippingAddress);
            req.setPaymentMethod(paymentMethod);
            req.setUserEmail((String) session.getAttribute(UserServiceImpl.SESSION_USER_EMAIL));
            req.setTotalAmount(cart.getTotalAmount());
            req.setTotalItems(cart.getTotalItems());
            req.setItems(CheckoutServiceClient.fromDb(cart.getItems()));

            UUID cartId = checkoutServiceClient.initiateCheckout(req);
            cartService.storePendingCartId(session, cartId.toString());

            return "redirect:/place-order-page";
        } catch (Exception e) {
            log.error("Checkout initiation failed", e);
            ra.addFlashAttribute("error", "Checkout failed: " + e.getMessage());
            return "redirect:/checkout-page";
        }
    }

    @GetMapping("/place-order-page")
    public String placeOrderPage(Model model, HttpSession session, RedirectAttributes ra) {
        String cartIdStr = cartService.getPendingCartId(session);
        if (cartIdStr == null) {
            return "redirect:/cart-page";
        }
        addCommonAttrs(model, session);
        model.addAttribute("cartId", cartIdStr);
        return "place-order";
    }

    @PostMapping("/place-order-page")
    public String placeOrder(HttpSession session, RedirectAttributes ra) {
        String cartIdStr = cartService.getPendingCartId(session);
        if (cartIdStr == null) {
            ra.addFlashAttribute("error", "No pending checkout. Please start checkout again.");
            return "redirect:/cart-page";
        }
        try {
            CheckoutServiceClient.PlaceOrderRequest req = new CheckoutServiceClient.PlaceOrderRequest();
            req.setCartId(UUID.fromString(cartIdStr));
            req.setSessionId(session.getId());
            req.setUserEmail((String) session.getAttribute(UserServiceImpl.SESSION_USER_EMAIL));

            String orderNumber = checkoutServiceClient.placeOrder(req);

            cartService.clearPendingCartId(session);
            cartService.clearCart(session);
            session.setAttribute("ORDER_NUMBER", orderNumber);

            return "redirect:/order-confirmation-page";
        } catch (Exception e) {
            log.error("Place order failed", e);
            ra.addFlashAttribute("error", "Order failed: " + e.getMessage());
            return "redirect:/place-order-page";
        }
    }

    @GetMapping("/order-confirmation-page")
    public String orderConfirmation(Model model, HttpSession session) {
        String orderNumber = (String) session.getAttribute("ORDER_NUMBER");
        addCommonAttrs(model, session);
        model.addAttribute("orderNumber", orderNumber != null ? orderNumber : "N/A");
        session.removeAttribute("ORDER_NUMBER");
        return "order-confirmation";
    }
}
