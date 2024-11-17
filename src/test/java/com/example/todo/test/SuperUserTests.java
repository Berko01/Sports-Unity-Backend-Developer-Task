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
public class SuperUserTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryRepository repository;

    @BeforeEach
    public void setup() {
        repository.clearAll();

        // Önce kullanıcıları ekliyoruz
        repository.saveUser(new User(1L, "john", 1L, User.Role.STANDARD));
        repository.saveUser(new User(4L, "superUser", null, User.Role.SUPER_USER));

        // Sonra görevleri ekliyoruz
        repository.saveTask(new Task(1L, "Task 1", "Description for Task 1", 1L, 1L));
        repository.saveTask(new Task(2L, "Task 2", "Description for Task 2", 1L, 1L));
    }


    @Test
    public void testGetTasksForSuperUser() throws Exception {
        mockMvc.perform(get("/tasks")
                        .header("requesterUserId", 4L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}

