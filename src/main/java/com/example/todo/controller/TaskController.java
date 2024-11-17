package com.example.todo.controller;

import com.example.todo.dto.TaskDTO;
import com.example.todo.util.Mapper;
import com.example.todo.model.User;
import com.example.todo.service.TaskService;
import com.example.todo.model.Task;
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
    public List<TaskDTO> getTasks(@RequestHeader Long requesterUserId, @RequestParam(required = false) Long userId) {
        User requester = repository.findUserById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requester User not found"));

        List<Task> tasks;

        if (userId != null) {
            User targetUser = repository.findUserById(userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User with ID " + userId + " not found"));

            if (!canAccessUserTasks(requester, targetUser)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this user's tasks");
            }

            tasks = taskService.getTasksForUser(targetUser);
        } else {
            tasks = taskService.getTasksForUser(requester);
        }

        // Map tasks to TaskDTOs
        return tasks.stream().map(Mapper::toTaskDTO).toList();
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody Task task) {
        Task savedTask = taskService.createTask(task);
        TaskDTO taskDTO = Mapper.toTaskDTO(savedTask);
        return ResponseEntity.ok(taskDTO);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<TaskDTO> deleteTask(@PathVariable Long taskId) {
        Task task = repository.findTaskById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        taskService.deleteTask(taskId);
        TaskDTO taskDTO = Mapper.toTaskDTO(task);
        return ResponseEntity.ok(taskDTO);
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
