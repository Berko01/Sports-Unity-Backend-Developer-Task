package com.example.todo.service;

import com.example.todo.model.Task;
import com.example.todo.model.User;
import com.example.todo.repository.InMemoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class TaskService {
    private final InMemoryRepository repository;

    public TaskService(InMemoryRepository repository) {
        this.repository = repository;
    }

    public List<Task> getTasksForUser(User user) {
        List<Task> tasks = switch (user.getRole()) {
            case SUPER_USER -> repository.findAllTasks();
            case COMPANY_ADMIN -> repository.findTasksByCompany(user.getCompanyId());
            case STANDARD -> repository.findTasksByUser(user.getId());
            default -> throw new IllegalStateException("Unexpected value: " + user.getRole());
        };

        tasks.sort(Comparator.comparing(Task::getId));
        return tasks;
    }


    public Task createTask(Task task) {
        System.out.println("Creating task: " + task);

        repository.findUserById(task.getUserId())
                .orElseThrow(() -> {
                    System.out.println("User not found: " + task.getUserId());
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User with ID " + task.getUserId() + " not found");
                });

        return repository.saveTask(task);
    }



    public void deleteTask(Long taskId) {
        repository.deleteTask(taskId);
    }
}
