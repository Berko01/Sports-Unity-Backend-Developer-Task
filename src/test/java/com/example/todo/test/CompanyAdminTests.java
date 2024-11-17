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
public class CompanyAdminTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryRepository repository;

    @BeforeEach
    public void setup() {
        repository.clearAll();
        repository.saveUser(new User(2L, "jane", 1L, User.Role.COMPANY_ADMIN));
    }

    @Test
    public void testGetTasksForCompanyAdmin() throws Exception {
        repository.saveUser(new User(3L, "employee", 1L, User.Role.STANDARD));
        repository.saveTask(new Task(3L, "Task for Employee", "Description", 3L, 1L));

        mockMvc.perform(get("/tasks?userId=3")
                        .header("requesterUserId", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Task for Employee"));
    }

    @Test
    public void testCompanyAdminCannotAccessTasksOfOtherCompany() throws Exception {
        repository.saveUser(new User(5L, "otherCompanyUser", 2L, User.Role.STANDARD));
        repository.saveTask(new Task(5L, "Other Company Task", "Description", 5L, 2L));

        mockMvc.perform(get("/tasks?userId=5")
                        .header("requesterUserId", 2L))
                .andExpect(status().isForbidden());
    }
}

