package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.LoginRequest;
import com.ecommerce.cart.dto.RegisterRequest;
import com.ecommerce.cart.dto.UserDto;
import com.ecommerce.cart.entity.User;
import com.ecommerce.cart.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    static final String SESSION_USER_ID = "USER_ID";
    static final String SESSION_USER_EMAIL = "USER_EMAIL";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();

        User saved = userRepository.save(user);
        log.info("Registered new user: {}", saved.getUsername());
        return UserDto.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto login(LoginRequest request, HttpSession session) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        session.setAttribute(SESSION_USER_ID, user.getId());
        session.setAttribute(SESSION_USER_EMAIL, user.getEmail());
        log.info("User logged in: {}", user.getUsername());
        return UserDto.from(user);
    }

    @Override
    public void logout(HttpSession session) {
        session.invalidate();
        log.info("User session invalidated");
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            throw new IllegalStateException("No user logged in");
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found"));
        return UserDto.from(user);
    }
}
