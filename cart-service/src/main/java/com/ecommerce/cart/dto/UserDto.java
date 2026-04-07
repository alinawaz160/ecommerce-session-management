package com.ecommerce.cart.dto;

import com.ecommerce.cart.entity.User;
import lombok.Data;

@Data
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private String role;

    public static UserDto from(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        return dto;
    }
}
