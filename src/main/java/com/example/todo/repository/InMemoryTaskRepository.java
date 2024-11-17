package com.example.todo.repository;

import com.example.todo.model.Task;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryTaskRepository implements TaskRepository {
    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong taskIdCounter = new AtomicLong(1);

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<Task> findByUserId(Long userId) {
        return tasks.values().stream()
                .filter(task -> task.getUserId().equals(userId))
                .toList();
    }

    @Override
    public List<Task> findByCompanyId(Long companyId) {
        return tasks.values().stream()
                .filter(task -> task.getCompanyId().equals(companyId))
                .toList();
    }

    @Override
    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(taskIdCounter.incrementAndGet());
        }
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public void delete(Long id) {
        tasks.remove(id);
    }
}
