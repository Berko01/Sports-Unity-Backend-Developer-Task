package com.example.todo.test;

import com.example.todo.controller.TaskController;
import com.example.todo.dto.TaskDTO;
import com.example.todo.model.Role;
import com.example.todo.model.User;
import com.example.todo.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
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

    private User superUser;
    private User companyAdmin;
    private User standardUser;

    @BeforeEach
    public void setup() {
        // Kullanıcı rollerini açıkça tanımla
        superUser = new User(1L, "Super User", 1L, Role.SUPER_USER);
        companyAdmin = new User(2L, "Company Admin", 1L, Role.COMPANY_ADMIN);
        standardUser = new User(3L, "Standard User", 1L, Role.STANDARD);
    }

    // Super User Tests
    @Test
    public void testSuperUserCanAccessAllTasks() throws Exception {
        List<TaskDTO> mockTasks = Arrays.asList(
                new TaskDTO(1L, "Task 1", "Description 1", 2L, 1L),
                new TaskDTO(2L, "Task 2", "Description 2", 3L, 1L)
        );
        Mockito.when(taskService.getTasksForUser(superUser.getId(), null)).thenReturn(mockTasks);

        mockMvc.perform(get("/tasks")
                        .header("requesterUserId", superUser.getId())) // Super User ID
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    public void testSuperUserCanAccessSpecificUserTasks() throws Exception {
        List<TaskDTO> mockTasks = Arrays.asList(
                new TaskDTO(3L, "User Task", "Description", 4L, 1L)
        );
        Mockito.when(taskService.getTasksForUser(superUser.getId(), 4L)).thenReturn(mockTasks);

        mockMvc.perform(get("/tasks")
                        .header("requesterUserId", superUser.getId()) // Super User ID
                        .param("userId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(3)));
    }

    // Company Admin Tests
    @Test
    public void testCompanyAdminCanAccessCompanyTasks() throws Exception {
        List<TaskDTO> mockTasks = Arrays.asList(
                new TaskDTO(4L, "Company Task", "Description", 5L, companyAdmin.getCompanyId())
        );
        Mockito.when(taskService.getTasksForUser(companyAdmin.getId(), null)).thenReturn(mockTasks);

        mockMvc.perform(get("/tasks")
                        .header("requesterUserId", companyAdmin.getId())) // Company Admin ID
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(4)));
    }

    @Test
    public void testCompanyAdminCannotAccessTasksOutsideTheirCompany() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied"))
                .when(taskService).getTasksForUser(companyAdmin.getId(), 8L);

        mockMvc.perform(get("/tasks")
                        .header("requesterUserId", companyAdmin.getId()) // Company Admin ID
                        .param("userId", "8")) // Another company's user
                .andExpect(status().isForbidden());
    }

    // Standard User Tests
    @Test
    public void testStandardUserCanAccessOwnTasks() throws Exception {
        List<TaskDTO> mockTasks = Arrays.asList(
                new TaskDTO(5L, "User Task", "Description", standardUser.getId(), 1L)
        );
        Mockito.when(taskService.getTasksForUser(standardUser.getId(), null)).thenReturn(mockTasks);

        mockMvc.perform(get("/tasks")
                        .header("requesterUserId", standardUser.getId())) // Standard User ID
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(5)));
    }

    @Test
    public void testStandardUserCannotAccessAnotherUsersTasks() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied"))
                .when(taskService).getTasksForUser(standardUser.getId(), 7L);

        mockMvc.perform(get("/tasks")
                        .header("requesterUserId", standardUser.getId()) // Standard User ID
                        .param("userId", "7")) // Another user's ID
                .andExpect(status().isForbidden());
    }

    // Task Creation Tests
    @Test
    public void testCompanyAdminCanCreateTaskInTheirCompany() throws Exception {
        TaskDTO inputTask = new TaskDTO(null, "New Task", "Description", 4L, companyAdmin.getCompanyId());
        TaskDTO createdTask = new TaskDTO(6L, "New Task", "Description", 4L, companyAdmin.getCompanyId());
        Mockito.when(taskService.createTask(Mockito.eq(companyAdmin.getId()), Mockito.any(TaskDTO.class))).thenReturn(createdTask);

        mockMvc.perform(post("/tasks")
                        .header("requesterUserId", companyAdmin.getId()) // Company Admin ID
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Task\",\"description\":\"Description\",\"userId\":4,\"companyId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(6)))
                .andExpect(jsonPath("$.title", is("New Task")));
    }

    @Test
    public void testStandardUserCannotCreateTask() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied"))
                .when(taskService).createTask(Mockito.eq(standardUser.getId()), Mockito.any(TaskDTO.class));

        mockMvc.perform(post("/tasks")
                        .header("requesterUserId", standardUser.getId()) // Standard User ID
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Task\",\"description\":\"Description\",\"userId\":4,\"companyId\":1}"))
                .andExpect(status().isForbidden());
    }
}
