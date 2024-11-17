package com.example.todo.service;

import com.example.todo.model.Role;
import com.example.todo.model.Task;
import com.example.todo.model.User;
import com.example.todo.repository.TaskRepository;
import com.example.todo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthorizationService {

    private final UserRepository userRepository;

    public AuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void checkAccess(Long requesterUserId, Long targetUserId) {
        if (!canAccessUserTasks(requesterUserId, targetUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
    }

    public boolean canAccessUserTasks(Long requesterUserId, Long targetUserId) {
        // Fetch requester and target user details from the repository
        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requester User not found"));

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target User not found"));

        // Super User can access anyone's tasks
        if (requester.getRole() == Role.SUPER_USER) {
            return true;
        }

        // Company Admin can access tasks of users in their company
        if (requester.getRole() == Role.COMPANY_ADMIN &&
                requester.getCompanyId().equals(targetUser.getCompanyId())) {
            return true;
        }

        // Standard User can only access their own tasks
        return requesterUserId.equals(targetUserId);
    }

    public void checkTaskCreationPermission(User requester, Task task) {
        if (requester.getRole() != Role.SUPER_USER &&
                !requester.getCompanyId().equals(task.getCompanyId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot create task for another company");
        }
    }

    public List<Task> getAccessibleTasks(User requester, TaskRepository taskRepository) {
        if (requester.getRole() == Role.SUPER_USER) {
            return taskRepository.findAll();
        } else if (requester.getRole() == Role.COMPANY_ADMIN) {
            return taskRepository.findByCompanyId(requester.getCompanyId());
        } else {
            return taskRepository.findByUserId(requester.getId());
        }
    }
}
