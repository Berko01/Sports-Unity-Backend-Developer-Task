package com.example.todo.util;

import com.example.todo.dto.TaskDTO;
import com.example.todo.model.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskDTO toDTO(Task task) {
        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getUserId(),
                task.getCompanyId()
        );
    }

    public Task toEntity(TaskDTO taskDTO) {
        return new Task(
                null,
                taskDTO.getTitle(),
                taskDTO.getDescription(),
                taskDTO.getUserId(),
                taskDTO.getCompanyId()
        );
    }
}

