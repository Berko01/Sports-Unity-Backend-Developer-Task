package com.example.todo.repository;

import com.example.todo.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    List<Task> findAll();
    Optional<Task> findById(Long id);
    List<Task> findByUserId(Long userId);
    List<Task> findByCompanyId(Long companyId);
    Task save(Task task);
    void delete(Long id);
}
