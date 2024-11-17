package com.example.todo.test;

import com.example.todo.model.Task;
import com.example.todo.model.User;
import com.example.todo.repository.InMemoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StandardUserTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryRepository repository;

    @BeforeEach
    public void setup() {
        repository.clearAll();
        repository.saveUser(new User(1L, "john", 1L, User.Role.STANDARD));
        repository.saveTask(new Task(1L, "Task 1", "Description for Task 1", 1L, 1L));
    }

    @Test
    public void testGetTasksForStandardUser() throws Exception {
        mockMvc.perform(get("/tasks?userId=1")
                        .header("requesterUserId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[0].userId").value(1L));
    }

    @Test
    public void testForbiddenAccessForStandardUser() throws Exception {
        repository.saveUser(new User(7L, "anotherUser", 2L, User.Role.STANDARD));
        repository.saveTask(new Task(4L, "Task 4", "Description for Task 4", 7L, 2L));

        mockMvc.perform(get("/tasks?userId=7")
                        .header("requesterUserId", 1L))
                .andExpect(status().isForbidden());
    }
}

