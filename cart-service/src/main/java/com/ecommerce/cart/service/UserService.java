package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.AuthResponse;
import com.ecommerce.cart.dto.LoginRequest;
import com.ecommerce.cart.dto.RegisterRequest;
import com.ecommerce.cart.dto.UserDto;

public interface UserService {

    UserDto register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String refreshToken);

    AuthResponse refresh(String refreshToken);

    UserDto getCurrentUser(Long userId);
}
