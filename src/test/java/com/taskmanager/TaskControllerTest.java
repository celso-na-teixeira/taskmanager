package com.taskmanager;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.taskmanager.model.Role;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.model.User;
import net.minidev.json.JSONArray;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskControllerTest {

    @Autowired
    TestRestTemplate restTemplate;
    
    private User[] users;
    private List<Role> roles;

    private User unknownUser;

    private static final String BASE_URL = "/api/v1/taskmanager";

    @BeforeEach
    void setUp() {
        roles = Collections.singletonList(new Role(400L, "TASK-OWNER"));

        users = Arrays.array(
                new User(200L, "leonardo", "password123", "leonardo@taskmanager.com", roles),
                new User(201L, "michelangelo", "password123", "michelangelo@taskmanager.com", roles));
        unknownUser = new User(20001L, "unknownUser", "password123", "michelangelo@taskmanager.com", roles);
    }

    @Test
    void shouldReturnTask() {
        String taskId = "100";
        String url = UriComponentsBuilder.fromPath(BASE_URL)
                .pathSegment(taskId)
                .toUriString();
        System.out.println(url);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth(users[0].getUsername(), users[0].getPassword())
                .getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        LocalDateTime sampleDate = LocalDateTime.of(2024, 8, 15, 0,0);
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(100);

        String title = documentContext.read("$.title");
        assertThat(title).isEqualTo("Wash the dishes");

        LocalDateTime dueDate = LocalDateTime.parse(documentContext.read("$.dueDate"));
        assertThat(dueDate).isEqualTo(sampleDate);

        TaskStatus status = TaskStatus.valueOf(documentContext.read("$.status"));
        assertThat(status).isEqualTo(TaskStatus.TODO);

    }

    @Test
    void shouldNotReturnTaskWithUnknownId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(users[0].getUsername(), users[0].getPassword())
                .getForEntity("/api/v1/taskmanager/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    @Test
    void shouldReturnAllTasks() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(users[0].getUsername(), users[0].getPassword())
                .getForEntity("/api/v1/taskmanager", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int taskCount = documentContext.read("$.length()");
        assertThat(taskCount).isEqualTo(2);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(100, 101);

        JSONArray titles = documentContext.read("$..title");
        assertThat(titles).containsExactlyInAnyOrder("Wash the dishes", "Throw the garbage");

        JSONArray dueDates = documentContext.read("$..dueDate");
        assertThat(dueDates).containsExactlyInAnyOrder("2024-08-15T00:00:00", "2024-08-20T00:00:00");
    }

    @Test
    @DirtiesContext
    void shouldCreateANewTask() {
        LocalDateTime sampleDate = LocalDateTime.of(2024, 8, 16, 0,0);
        Task newTask = new Task(null, "Wash the car", "Description to wash the car", sampleDate,TaskStatus.TODO, users[0].getId());
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth(users[0].getUsername(), users[0].getPassword())
                .postForEntity("/api/v1/taskmanager", newTask, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewTask = createResponse.getHeaders().getLocation();
        System.out.println(locationOfNewTask);
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth(users[0].getUsername(), users[0].getPassword())
                .getForEntity(locationOfNewTask, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        String title = documentContext.read("$.title");
        LocalDateTime dueDate = LocalDateTime.parse(documentContext.read("$.dueDate"));

        assertThat(id).isNotNull();
        assertThat(title).isEqualTo("Wash the car");
        assertThat(dueDate).isEqualTo(LocalDateTime.parse("2024-08-16T00:00:00"));
    }

    @Test
    @DirtiesContext
    void shouldUpdateExistingTask() {
        Task updatedTask = new Task(100L, "Wash the car", "Description to wash the dishes", LocalDateTime.parse("2024-08-16T00:00:00"), TaskStatus.TODO, users[0].getId());
        HttpEntity<Task> request = new HttpEntity<>(updatedTask);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth(users[0].getUsername(), users[0].getPassword())
                .exchange("/api/v1/taskmanager/100", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth(users[0].getUsername(), users[0].getPassword())
                .getForEntity("/api/v1/taskmanager/100", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        String title = documentContext.read("$.title");
        String description = documentContext.read("$.description");
        LocalDateTime dueDate = LocalDateTime.parse(documentContext.read("$.dueDate"));

        assertThat(id).isEqualTo(100);
        assertThat(title).isEqualTo("Wash the car");
        assertThat(description).isEqualTo("Description to wash the dishes");
        assertThat(dueDate).isEqualTo(LocalDateTime.parse("2024-08-16T00:00:00"));
    }

    @Test
    @DirtiesContext
    void shouldNotUpdateTaskThatDoesNotExist() {
        Task unknownTask = new Task(null, "Wash the car", "Description to wash the dishes", LocalDateTime.parse("2024-08-16T00:00:00"),TaskStatus.TODO, users[1].getId());
        HttpEntity<Task> request = new HttpEntity<>(unknownTask);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth(users[1].getUsername(), users[1].getPassword())
                .exchange("/api/v1/taskmanager/10001", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldDeleteAnExistingTask() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth(users[0].getUsername(), users[0].getPassword())
                .exchange("/api/v1/taskmanager/100", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth(users[0].getUsername(), users[0].getPassword())
                .getForEntity("/api/v1/taskmanager/100", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotDeleteTaskThatDoesNotExist() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth(users[1].getUsername(), users[1].getPassword())
                .exchange("/api/v1/taskmanager/10001", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotDeleteTaskThatUserNotExist() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth(unknownUser.getUsername(), unknownUser.getPassword())
                .exchange("/api/v1/taskmanager/10001", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
