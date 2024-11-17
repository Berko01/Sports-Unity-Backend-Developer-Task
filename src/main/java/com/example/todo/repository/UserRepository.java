package com.example.todo.repository;

import com.example.todo.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);

    List<User> findAll();

    User save(User user);

    boolean existsById(Long id);

    void deleteById(Long id);
}
