package com.example.todo.test;

import com.example.todo.model.Task;
import com.example.todo.model.User;
import com.example.todo.repository.InMemoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GeneralTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        repository.clearAll();
        repository.saveUser(new User(1L, "john", 1L, User.Role.STANDARD));
    }

    @Test
    public void testCreateTaskWithExistingId() throws Exception {
        repository.saveTask(new Task(453L, "Existing Task", "Already Exists", 1L, 1L));

        Task duplicateTask = new Task(453L, "Another Task", "Description", 1L, 1L);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateTask)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Task with ID 453 already exists"));
    }

    @Test
    public void testCreateTaskForNonExistentUser() throws Exception {
        Task task = new Task(null, "Invalid Task", "Invalid User Test", 99L, 1L);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with ID 99 not found"));
    }
}

