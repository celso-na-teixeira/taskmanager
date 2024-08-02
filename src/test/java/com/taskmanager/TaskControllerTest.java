package com.taskmanager;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskUser;
import net.minidev.json.JSONArray;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskControllerTest {

    @Autowired
    TestRestTemplate restTemplate;
    
    private TaskUser[] taskUsers;

    @BeforeEach
    void setUp() {
        taskUsers = Arrays.array(
                new TaskUser(200L, "leonardo", "password123", "leonardo@taskmanager.com", "TASK-OWNER"),
                new TaskUser(201L, "michelangelo", "password123", "michelangelo@taskmanager.com", "TASK-OWNER"));
    }

    @Test
    void shouldReturnTask() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(taskUsers[0].username(), taskUsers[0].password())
                .getForEntity("/api/v1/taskmanager/100", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        LocalDateTime sampleDate = LocalDateTime.of(2024, 8, 15, 0,0);
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(100);

        String title = documentContext.read("$.title");
        assertThat(title).isEqualTo("Wash the dishes");

        LocalDateTime dueDate = LocalDateTime.parse(documentContext.read("$.dueDate"));
        assertThat(dueDate).isEqualTo(sampleDate);

        Boolean completed = documentContext.read("$.completed");
        assertThat(completed).isEqualTo(false);

    }

    @Test
    void shouldNotReturnTaskWithUnknownId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(taskUsers[0].username(), taskUsers[0].password())
                .getForEntity("/api/v1/taskmanager/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    @Test
    void shouldReturnAllTasks() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(taskUsers[0].username(), taskUsers[0].password())
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
        Task newTask = new Task(null, "Wash the car", "Description to wash the car", sampleDate,false, taskUsers[0].id());
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth(taskUsers[0].username(), taskUsers[0].password())
                .postForEntity("/api/v1/taskmanager", newTask, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewTask = createResponse.getHeaders().getLocation();
        System.out.println(locationOfNewTask);
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth(taskUsers[0].username(), taskUsers[0].password())
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
        Task updatedTask = new Task(100L, "Wash the car", "Description to wash the dishes", LocalDateTime.parse("2024-08-16T00:00:00"),false, taskUsers[0].id());
        HttpEntity<Task> request = new HttpEntity<>(updatedTask);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth(taskUsers[0].username(), taskUsers[0].password())
                .exchange("/api/v1/taskmanager/100", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth(taskUsers[0].username(), taskUsers[0].password())
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
        Task unknownTask = new Task(null, "Wash the car", "Description to wash the dishes", LocalDateTime.parse("2024-08-16T00:00:00"),false, taskUsers[1].id());
        HttpEntity<Task> request = new HttpEntity<>(unknownTask);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth(taskUsers[1].username(), taskUsers[1].password())
                .exchange("/api/v1/taskmanager/10001", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldDeleteAnExistingTask() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth(taskUsers[0].username(), taskUsers[0].password())
                .exchange("/api/v1/taskmanager/100", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth(taskUsers[0].username(), taskUsers[0].password())
                .getForEntity("/api/v1/taskmanager/100", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotDeleteTaskThatDoesNotExist() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth(taskUsers[1].username(), taskUsers[1].password())
                .exchange("/api/v1/taskmanager/10001", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
