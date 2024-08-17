package com.taskmanager.controller;

import com.taskmanager.exception.TaskDeleteException;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.exception.UserTaskNotFoundException;
import com.taskmanager.model.Task;
import com.taskmanager.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
public class TaskController {

    private final TaskService taskService;


    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTask(@PathVariable final Long taskId, final Principal principal) {
        log.debug("Fetching task with ID: {} for user: {}", taskId, principal.getName());
        try {
            Task task = taskService.getTask(taskId, principal);
            return ResponseEntity.ok(task);
        } catch (UserTaskNotFoundException | TaskNotFoundException e) {
            log.warn("Error fetching task with ID: {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Unexpected error fetching task with ID: {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasks(Pageable pageable, Principal principal) {
        log.debug("Fetching all tasks");
        try {
            Page<Task> taskPage = taskService.getTasks(pageable, principal);
            if (taskPage.getContent().isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(taskPage.getContent());
        } catch (Exception e) {
            log.error("Error fetching tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Void> createTask(@RequestBody Task newTaskRequest, UriComponentsBuilder ucb, Principal principal) {
        log.debug("Creating new task: {}", newTaskRequest);
        try {
           Task savedTask = taskService.createTask(newTaskRequest, principal);
            URI locationOfNewTask = ucb
                    .path("/api/v1/taskmanager/{id}")
                    .buildAndExpand(savedTask.getId())
                    .toUri();
            return ResponseEntity.created(locationOfNewTask).build();
        } catch (Exception e) {
            log.error("Error creating task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<Void> updateTask(@PathVariable Long taskId, @RequestBody Task taskRequest, Principal principal) {
        log.debug("Updating task with ID: {}", taskId);
        try {
            taskService.updateTask(taskId, taskRequest, principal);
            return ResponseEntity.noContent().build();
        }catch (UserTaskNotFoundException | TaskNotFoundException e) {
            log.warn("Error updating task with ID: {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Unexpected error updating task with ID: {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId, Principal principal) {
        log.debug("Deleting task with ID: {}", taskId);
        try {
            taskService.deleteTask(taskId, principal);
            return ResponseEntity.noContent().build();
        }catch (UserTaskNotFoundException | TaskNotFoundException e) {
            log.warn("Error deleting task with ID: {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (TaskDeleteException e) {
            log.error("Unexpected error deleting task with ID: {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
