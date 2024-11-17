package com.example.todo.repository;

import com.example.todo.model.Task;
import com.example.todo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryRepository {
    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong taskIdCounter = new AtomicLong(1);


    public List<Task> findAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Task> findTasksByUser(Long userId) {
        return tasks.values().stream()
                .filter(task -> task.getUserId().equals(userId))
                .collect(Collectors.toList());
    }


    public List<Task> findTasksByCompany(Long companyId) {
        return tasks.values().stream()
                .filter(task -> task.getCompanyId().equals(companyId))
                .collect(Collectors.toList());
    }

    public Optional<Task> findTaskById(Long taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    public Task saveTask(Task task) {
        if (task.getId() == null) {
            task.setId(taskIdCounter.incrementAndGet());
        }

        if (tasks.containsKey(task.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task with ID " + task.getId() + " already exists");
        }

        if (!users.containsKey(task.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + task.getUserId() + " not found");
        }

        tasks.put(task.getId(), task);
        return task;
    }
    public void deleteTask(Long taskId) {
        tasks.remove(taskId);
    }

    public Optional<User> findUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public User saveUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        users.put(user.getId(), user);
        return user;
    }

    public void clearAll() {
        tasks.clear();
        users.clear();
    }

}

