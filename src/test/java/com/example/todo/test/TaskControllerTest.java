package com.example.todo.test;

import com.example.todo.controller.TaskController;
import com.example.todo.dto.TaskDTO;
import com.example.todo.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    public void testGetTasksForSuperUser() throws Exception {
        // Mock data for Super User
        List<TaskDTO> mockTasks = Arrays.asList(
                new TaskDTO(1L, "Task 1", "Description 1", 2L, 1L),
                new TaskDTO(2L, "Task 2", "Description 2", 3L, 1L)
        );
        Mockito.when(taskService.getTasksForUser(1L, null)).thenReturn(mockTasks);

        // Perform GET request
        mockMvc.perform(get("/tasks")
                        .header("requesterUserId", 1L)) // Super User ID
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Task 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Task 2")));
    }

    @Test
    public void testGetTasksForCompanyAdmin() throws Exception {
        // Mock data for Company Admin
        List<TaskDTO> mockTasks = Arrays.asList(
                new TaskDTO(3L, "Company Task 1", "Description 3", 4L, 2L)
        );
        Mockito.when(taskService.getTasksForUser(2L, null)).thenReturn(mockTasks);

        // Perform GET request
        mockMvc.perform(get("/tasks")
                        .header("requesterUserId", 2L)) // Company Admin ID
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(3)))
                .andExpect(jsonPath("$[0].title", is("Company Task 1")));
    }

    @Test
    public void testGetTasksForStandardUser() throws Exception {
        // Mock data for Standard User
        List<TaskDTO> mockTasks = Arrays.asList(
                new TaskDTO(4L, "Standard Task", "Description 4", 5L, 3L)
        );
        Mockito.when(taskService.getTasksForUser(3L, null)).thenReturn(mockTasks);

        // Perform GET request
        mockMvc.perform(get("/tasks")
                        .header("requesterUserId", 3L)) // Standard User ID
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(4)))
                .andExpect(jsonPath("$[0].title", is("Standard Task")));
    }

    @Test
    public void testSuperUserCanAccessAnotherUsersTasks() throws Exception {
        // Mock data
        List<TaskDTO> mockTasks = Arrays.asList(
                new TaskDTO(6L, "Another User's Task", "Description 6", 7L, 2L)
        );
        Mockito.when(taskService.getTasksForUser(1L, 7L)).thenReturn(mockTasks);

        // Perform GET request as Super User
        mockMvc.perform(get("/tasks")
                        .header("requesterUserId", 1L) // Super User
                        .param("userId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(6)))
                .andExpect(jsonPath("$[0].title", is("Another User's Task")));
    }

    @Test
    public void testCompanyAdminCannotAccessTasksOutsideTheirCompany() throws Exception {
        // Mock unauthorized access
        Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied"))
                .when(taskService).getTasksForUser(2L, 8L); // Company Admin accessing another company's user

        // Perform GET request
        mockMvc.perform(get("/tasks")
                        .header("requesterUserId", 2L) // Company Admin
                        .param("userId", "8")) // Another user's ID
                .andExpect(status().isForbidden());
    }

    @Test
    public void testStandardUserCannotAccessAnotherUsersTasks() throws Exception {
        // Mock unauthorized access
        Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied"))
                .when(taskService).getTasksForUser(3L, 5L); // Standard User trying to access another user's tasks

        // Perform GET request
        mockMvc.perform(get("/tasks")
                        .header("requesterUserId", 3L) // Standard User
                        .param("userId", "5")) // Another user's ID
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateTaskWithProperPermissions() throws Exception {
        // Mock data
        TaskDTO inputTask = new TaskDTO(null, "New Task", "Description", 4L, 2L);
        TaskDTO createdTask = new TaskDTO(7L, "New Task", "Description", 4L, 2L);
        Mockito.when(taskService.createTask(Mockito.eq(2L), Mockito.any(TaskDTO.class))).thenReturn(createdTask);

        // Perform POST request
        mockMvc.perform(post("/tasks")
                        .header("requesterUserId", 2L) // Company Admin
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Task\",\"description\":\"Description\",\"userId\":4,\"companyId\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(7)))
                .andExpect(jsonPath("$.title", is("New Task")));
    }

    @Test
    public void testUnauthorizedTaskCreation() throws Exception {
        // Mock unauthorized task creation
        Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied"))
                .when(taskService).createTask(Mockito.eq(3L), Mockito.any(TaskDTO.class)); // Standard User trying to create a task

        // Perform POST request
        mockMvc.perform(post("/tasks")
                        .header("requesterUserId", 3L) // Standard User
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Task\",\"description\":\"Description\",\"userId\":4,\"companyId\":2}"))
                .andExpect(status().isForbidden());
    }
}
