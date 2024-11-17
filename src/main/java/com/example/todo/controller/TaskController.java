package com.example.todo.controller;

import com.example.todo.service.TaskService;
import com.example.todo.model.Task;
import com.example.todo.model.User;
import com.example.todo.repository.InMemoryRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;
    private final InMemoryRepository repository;

    public TaskController(TaskService taskService, InMemoryRepository repository) {
        this.taskService = taskService;
        this.repository = repository;
    }

    @GetMapping
    public List<Task> getTasks(@RequestHeader Long requesterUserId, @RequestParam(required = false) Long userId) {
        User requester = repository.findUserById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requester User not found"));

        if (userId != null) {
            User targetUser = repository.findUserById(userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User with ID " + userId + " not found"));

            if (!canAccessUserTasks(requester, targetUser)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this user's tasks");
            }

            return taskService.getTasksForUser(targetUser);
        }

        return taskService.getTasksForUser(requester);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        Task savedTask = taskService.createTask(task);
        return ResponseEntity.ok(savedTask);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Task> deleteTask(@PathVariable Long taskId) {
        Task task = repository.findTaskById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        taskService.deleteTask(taskId);
        return ResponseEntity.ok(task);
    }

    private boolean canAccessUserTasks(User requester, User targetUser) {
        if (requester.getRole() == User.Role.SUPER_USER) {
            return true;
        }

        if (requester.getRole() == User.Role.COMPANY_ADMIN &&
                requester.getCompanyId().equals(targetUser.getCompanyId())) {
            return true;
        }

        return requester.getId().equals(targetUser.getId());
    }
}


