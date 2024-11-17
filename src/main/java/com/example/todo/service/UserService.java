package com.example.todo.service;

import com.example.todo.dto.UserDTO;
import com.example.todo.model.Role;
import com.example.todo.model.User;
import com.example.todo.repository.UserRepository;
import com.example.todo.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO) // Delegates to UserMapper
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return userMapper.toDTO(user); // Delegates to UserMapper
    }

    public UserDTO createUser(UserDTO userDTO) {
        validateUserDTO(userDTO);

        User user = userMapper.toEntity(userDTO); // Delegates to UserMapper
        User savedUser = userRepository.save(user);

        return userMapper.toDTO(savedUser); // Delegates to UserMapper
    }

    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        validateUserDTO(userDTO);

        existingUser.setUsername(userDTO.getUsername());
        existingUser.setCompanyId(userDTO.getCompanyId());
        existingUser.setRole(Role.valueOf(userDTO.getRole()));

        User updatedUser = userRepository.save(existingUser);

        return userMapper.toDTO(updatedUser); // Delegates to UserMapper
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(userId);
    }

    private void validateUserDTO(UserDTO userDTO) {
        if (userDTO.getUsername() == null || userDTO.getUsername().length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be at least 3 characters long");
        }
        if (userDTO.getCompanyId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company ID cannot be null");
        }
        if (userDTO.getRole() == null || !isValidRole(userDTO.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
        }
    }

    private boolean isValidRole(String role) {
        try {
            Role.valueOf(role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
