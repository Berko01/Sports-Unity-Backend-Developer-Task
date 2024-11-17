package com.example.todo;

import com.example.todo.model.User;
import com.example.todo.repository.InMemoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.example.todo.model.Task;


@SpringBootApplication
public class TodoApplication {
	public static void main(String[] args) {
		SpringApplication.run(TodoApplication.class, args);
	}

	@Bean
	CommandLineRunner runner(InMemoryRepository repository) {
		return args -> {
			// Kullanıcı ve görevleri ekleyin
			repository.saveUser(new User(1L, "john", 1L, User.Role.STANDARD));
			repository.saveUser(new User(2L, "jane", 1L, User.Role.STANDARD));
			repository.saveUser(new User(4L, "superUser", null, User.Role.SUPER_USER)); // Süper kullanıcı
			repository.saveTask(new Task(1L, "Task 1", "Description for Task 1", 1L, 1L));
			repository.saveTask(new Task(2L, "Task 2", "Description for Task 2", 2L, 1L));
			repository.saveTask(new Task(3L, "Task 3", "Description for Task 3", 1L, 1L));
		};
	}

}

