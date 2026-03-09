package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.LoginRequest;
import com.ecommerce.cart.dto.RegisterRequest;
import com.ecommerce.cart.dto.UserDto;
import jakarta.servlet.http.HttpSession;

public interface UserService {

    UserDto register(RegisterRequest request);

    UserDto login(LoginRequest request, HttpSession session);

    void logout(HttpSession session);

    UserDto getCurrentUser(HttpSession session);
}
