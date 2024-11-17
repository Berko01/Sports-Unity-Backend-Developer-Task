package com.example.todo.test;

import com.example.todo.controller.UserController;
import com.example.todo.dto.UserDTO;
import com.example.todo.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testGetUserById() throws Exception {
        // Mock data
        UserDTO mockUser = new UserDTO(1L, "john.doe", 1L, "STANDARD");
        Mockito.when(userService.getUserById(1L)).thenReturn(mockUser);

        // Perform GET request
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("john.doe")))
                .andExpect(jsonPath("$.companyId", is(1)))
                .andExpect(jsonPath("$.role", is("STANDARD")));
    }

    @Test
    public void testGetAllUsers() throws Exception {
        // Mock data
        List<UserDTO> mockUsers = Arrays.asList(
                new UserDTO(1L, "john.doe", 1L, "STANDARD"),
                new UserDTO(2L, "jane.doe", 1L, "COMPANY_ADMIN")
        );
        Mockito.when(userService.getAllUsers()).thenReturn(mockUsers);

        // Perform GET request
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("john.doe")))
                .andExpect(jsonPath("$[0].companyId", is(1)))
                .andExpect(jsonPath("$[0].role", is("STANDARD")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].username", is("jane.doe")))
                .andExpect(jsonPath("$[1].companyId", is(1)))
                .andExpect(jsonPath("$[1].role", is("COMPANY_ADMIN")));
    }

    @Test
    public void testCreateUser() throws Exception {
        // Mock data
        UserDTO inputUser = new UserDTO(null, "john.doe", 1L, "STANDARD");
        UserDTO mockUser = new UserDTO(1L, "john.doe", 1L, "STANDARD");
        Mockito.when(userService.createUser(Mockito.any(UserDTO.class))).thenReturn(mockUser);

        // Perform POST request
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john.doe\",\"companyId\":1,\"role\":\"STANDARD\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("john.doe")))
                .andExpect(jsonPath("$.companyId", is(1)))
                .andExpect(jsonPath("$.role", is("STANDARD")));
    }

    @Test
    public void testUpdateUser() throws Exception {
        // Mock data
        UserDTO inputUser = new UserDTO(null, "john.doe.updated", 1L, "STANDARD");
        UserDTO mockUser = new UserDTO(1L, "john.doe.updated", 1L, "STANDARD");
        Mockito.when(userService.updateUser(Mockito.eq(1L), Mockito.any(UserDTO.class))).thenReturn(mockUser);

        // Perform PUT request
        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john.doe.updated\",\"companyId\":1,\"role\":\"STANDARD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("john.doe.updated")))
                .andExpect(jsonPath("$.companyId", is(1)))
                .andExpect(jsonPath("$.role", is("STANDARD")));
    }

    @Test
    public void testDeleteUser() throws Exception {
        // Perform DELETE request
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
        Mockito.verify(userService, Mockito.times(1)).deleteUser(1L);
    }
}
