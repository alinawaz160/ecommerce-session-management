package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.LoginRequest;
import com.ecommerce.cart.dto.RegisterRequest;
import com.ecommerce.cart.dto.UserDto;
import com.ecommerce.cart.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UserDto> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session) {
        log.debug("POST login - username: {}", request.getUsername());
        UserDto user = userService.login(request, session);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        log.debug("POST logout");
        userService.logout(session);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(HttpSession session) {
        UserDto user = userService.getCurrentUser(session);
        return ResponseEntity.ok(user);
    }
}
