package com.example.todo.util;

import com.example.todo.dto.TaskDTO;
import com.example.todo.dto.UserDTO;
import com.example.todo.model.Task;
import com.example.todo.model.User;

public class Mapper {
    public static UserDTO toUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getCompanyId(),
                user.getRole().name()
        );
    }

    public static TaskDTO toTaskDTO(Task task) {
        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getUserId(),
                task.getCompanyId()
        );
    }
}
