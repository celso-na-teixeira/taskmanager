package com.taskmanager.controller;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskUser;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/taskmanager")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;


    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTask(@PathVariable Long taskId, Principal principal) {
        log.debug("Fetching task with ID: {}", taskId);
        Optional<TaskUser> user = userRepository.findByUsername(principal.getName());
        if (!user.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Optional<Task> task = taskRepository.findByIdAndUserId(taskId, user.get().id());
        if (task.isPresent()) {
            return ResponseEntity.ok(task.get());
        }
        log.warn("Task with ID {} not found", taskId);
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasks(Pageable pageable, Principal principal) {
        log.debug("Fetching all tasks");
        try {
            Optional<TaskUser> user = userRepository.findByUsername(principal.getName());
            if (!user.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            Page<Task> taskPage = taskRepository.findByUserId(user.get().id(),
                    PageRequest.of(
                            pageable.getPageNumber(),
                            pageable.getPageSize(),
                            pageable.getSortOr(Sort.by(Sort.Direction.ASC, "dueDate"))
                    ));
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
            Optional<TaskUser> user = userRepository.findByUsername(principal.getName());
            if (!user.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            Task newTask = new Task(null, newTaskRequest.title(), newTaskRequest.description(), newTaskRequest.dueDate(), newTaskRequest.completed(), user.get().id());
            Task savedTask = taskRepository.save(newTask);
            URI locationOfNewTask = ucb
                    .path("/api/v1/taskmanager/{id}")
                    .buildAndExpand(savedTask.id())
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
        Optional<TaskUser> user = userRepository.findByUsername(principal.getName());
        if (!user.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Optional<Task> oldTask = taskRepository.findByIdAndUserId(taskId, user.get().id());
        if (oldTask.isPresent()) {
            Task updatedTask = new Task(taskId, taskRequest.title(), taskRequest.description(), taskRequest.dueDate(), taskRequest.completed(), user.get().id());
            taskRepository.save(updatedTask);
            return ResponseEntity.noContent().build();
        }
        log.warn("Task with ID {} not found for update", taskId);
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        log.debug("Deleting task with ID: {}", taskId);
        Optional<Task> deleteTask = taskRepository.findById(taskId);
        if (deleteTask.isPresent()) {
            taskRepository.delete(deleteTask.get());
            return ResponseEntity.noContent().build();
        }
        log.warn("Task with ID {} not found for deletion", taskId);
        return ResponseEntity.notFound().build();
    }


}
