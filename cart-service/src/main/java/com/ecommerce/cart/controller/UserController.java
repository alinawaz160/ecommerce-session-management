package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.AuthResponse;
import com.ecommerce.cart.dto.LoginRequest;
import com.ecommerce.cart.dto.RefreshTokenRequest;
import com.ecommerce.cart.dto.RegisterRequest;
import com.ecommerce.cart.dto.UserDto;
import com.ecommerce.cart.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("POST register - username: {}", request.getUsername());
        UserDto user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("POST login - username: {}", request.getUsername());
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request, HttpSession session) {
        log.debug("POST logout");
        userService.logout(request.getRefreshToken());
        session.invalidate();  // clear session cart and pendingCartId so guest session starts clean
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("POST refresh");
        AuthResponse response = userService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        UserDto user = userService.getCurrentUser(userId);
        return ResponseEntity.ok(user);
    }
}
