package com.example.todo.util;

import com.example.todo.dto.UserDTO;
import com.example.todo.model.Role;
import com.example.todo.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getCompanyId(),
                user.getRole().name()
        );
    }

    public User toEntity(UserDTO userDTO) {
        return new User(
                null, // ID is assigned by the database
                userDTO.getUsername(),
                userDTO.getCompanyId(),
                Role.valueOf(userDTO.getRole())
        );
    }
}
