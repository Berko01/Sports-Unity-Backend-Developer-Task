package com.example.todo.service;

import com.example.todo.dto.TaskDTO;
import com.example.todo.model.Task;
import com.example.todo.model.User;
import com.example.todo.repository.TaskRepository;
import com.example.todo.repository.UserRepository;
import com.example.todo.util.TaskMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository,
                       AuthorizationService authorizationService, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
        this.taskMapper = taskMapper;
    }

    public List<TaskDTO> getTasksForUser(Long requesterUserId, Long userId) {
        // If userId is provided, check access and fetch tasks for the specific user
        if (userId != null) {
            authorizationService.checkAccess(requesterUserId, userId);
            return taskRepository.findByUserId(userId)
                    .stream().map(taskMapper::toDTO).collect(Collectors.toList());
        }

        // If userId is not provided, determine tasks based on requester's role
        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requester User not found"));

        List<Task> tasks = authorizationService.getAccessibleTasks(requester, taskRepository);
        return tasks.stream().map(taskMapper::toDTO).collect(Collectors.toList());
    }

    public TaskDTO createTask(Long requesterUserId, TaskDTO taskDTO) {
        validateTaskDTO(taskDTO);

        // Ensure requester has permission to create the task
        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requester User not found"));

        // Convert TaskDTO to Task
        Task task = taskMapper.toEntity(taskDTO);

        // Check permissions with the Task entity
        authorizationService.checkTaskCreationPermission(requester, task);

        // Save and return the task
        Task savedTask = taskRepository.save(task);
        return taskMapper.toDTO(savedTask);
    }



    public TaskDTO updateTask(Long requesterUserId, Long taskId, TaskDTO taskDTO) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        authorizationService.checkAccess(requesterUserId, existingTask.getUserId());

        existingTask.setTitle(taskDTO.getTitle());
        existingTask.setDescription(taskDTO.getDescription());
        existingTask.setUserId(taskDTO.getUserId());
        existingTask.setCompanyId(taskDTO.getCompanyId());

        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toDTO(updatedTask);
    }

    public void deleteTask(Long requesterUserId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        authorizationService.checkAccess(requesterUserId, task.getUserId());

        taskRepository.delete(taskId);
    }

    private void validateTaskDTO(TaskDTO taskDTO) {
        if (taskDTO.getTitle() == null || taskDTO.getTitle().length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title must be at least 3 characters long");
        }
        if (taskDTO.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID cannot be null");
        }
        if (taskDTO.getCompanyId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company ID cannot be null");
        }
    }
}
