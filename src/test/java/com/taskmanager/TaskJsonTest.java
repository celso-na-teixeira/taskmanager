package com.taskmanager;

import com.taskmanager.model.TaskStatus;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import com.taskmanager.model.Task;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@JsonTest
public class TaskJsonTest {

    @Autowired
    private JacksonTester<Task> taskJson;

    @Autowired
    private JacksonTester<Task[]> taskJsonList;

    private Task[] tasks;

    @BeforeEach
    void setUp() {
        LocalDateTime sampleDate = LocalDateTime.of(2024, 7, 30, 0,0);
        tasks = Arrays.array(
                new Task(100L, "Wash the dishes", "Description to wash the dishes", sampleDate, TaskStatus.TODO, 200L),
                new Task(101L, "Throw the garbage", "Description to throw the garbage", sampleDate,TaskStatus.TODO, 200L),
                new Task(102L, "Do groceries", "Description to do groceries", sampleDate,TaskStatus.COMPLETED, 200L)
        );
    }

    @Test
    void taskSerializationTest() throws IOException {
        Task task = tasks[0];
        assertThat(taskJson.write(task)).isStrictlyEqualToJson("expectedtask.json");

        assertThat(taskJson.write(task)).hasJsonPathNumberValue("@.id");
        assertThat(taskJson.write(task)).extractingJsonPathNumberValue("@.id")
                .isEqualTo(100);

        assertThat(taskJson.write(task)).hasJsonPathStringValue("@.title");
        assertThat(taskJson.write(task)).extractingJsonPathStringValue("@.title")
                .isEqualTo("Wash the dishes");

        assertThat(taskJson.write(task)).hasJsonPathStringValue("@.dueDate");
        assertThat(taskJson.write(task)).extractingJsonPathStringValue("@.dueDate")
                .isEqualTo("2024-07-30T00:00:00");

        assertThat(taskJson.write(task)).hasJsonPathStringValue("@.status");
        assertThat(taskJson.write(task)).extractingJsonPathStringValue("@.status")
                .isEqualTo("TODO");

    }

    @Test
    void taskDeserializationTest() throws IOException {
        String expectedTask = """
                {
                    "id": 101,
                    "title": "Throw the garbage",
                    "description": "Description to throw the garbage",
                    "dueDate": "2024-07-30T00:00:00",
                    "status": "TODO",
                    "userId": 200
                  }
                """;
        Task task = tasks[1];
        LocalDateTime sampleDate = LocalDateTime.of(2024, 7, 30, 0,0);

        assertThat(taskJson.parse(expectedTask)).isEqualTo(task);
        assertThat(taskJson.parseObject(expectedTask).getId()).isEqualTo(101L);
        assertThat(taskJson.parseObject(expectedTask).getTitle()).isEqualTo("Throw the garbage");
        assertThat(taskJson.parseObject(expectedTask).getDueDate()).isEqualTo(sampleDate);
        assertThat(taskJson.parseObject(expectedTask).getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void taskListSerializationTest() throws IOException {
        assertThat(taskJsonList.write(tasks)).isStrictlyEqualToJson("expectedtasklist.json");
    }

    @Test
    void taskListDeserializationTest() throws IOException {
        String expected = """
                [
                    {
                     "id": 100,
                     "title": "Wash the dishes",
                     "description": "Description to wash the dishes",
                     "dueDate": "2024-07-30T00:00:00",
                     "status": "TODO",
                     "userId": 200
                   },
                   {
                     "id": 101,
                     "title": "Throw the garbage",
                     "description": "Description to throw the garbage",
                     "dueDate": "2024-07-30T00:00:00",
                     "status": "TODO",
                     "userId": 200
                   },
                   {
                     "id": 102,
                     "title": "Do groceries",
                     "description": "Description to do groceries",
                     "dueDate": "2024-07-30T00:00:00",
                     "status": "COMPLETED",
                     "userId": 200
                   }
                ]
                """;
        assertThat(taskJsonList.parse(expected)).isEqualTo(tasks);
    }


}
