package com.example.todo.controller;

import com.example.todo.dto.TaskDTO;
import com.example.todo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getTasksForUser(
            @RequestHeader Long requesterUserId,
            @RequestParam(required = false) Long userId) {
        List<TaskDTO> tasks = taskService.getTasksForUser(requesterUserId, userId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(
            @RequestHeader Long requesterUserId,
            @Valid @RequestBody TaskDTO taskDTO) {
        TaskDTO createdTask = taskService.createTask(requesterUserId, taskDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long taskId,
            @RequestHeader Long requesterUserId,
            @Valid @RequestBody TaskDTO taskDTO) {
        TaskDTO updatedTask = taskService.updateTask(requesterUserId, taskId, taskDTO);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long taskId,
            @RequestHeader Long requesterUserId) {
        taskService.deleteTask(requesterUserId, taskId);
        return ResponseEntity.noContent().build();
    }
}
