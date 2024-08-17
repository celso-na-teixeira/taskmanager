package com.taskmanager.service;

import com.taskmanager.exception.*;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.security.Principal;


@Slf4j
@Service
public class TaskServiceImpl implements TaskService{
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskUserService userService;

    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository, TaskUserService userService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public Task getTask(Long taskId, Principal principal) {
        log.debug("Fetching task with id: {} for user: {}", taskId, principal.getName());
        return getTaskByIdAndUser(taskId, principal.getName());
    }

    @Override
    public Page<Task> getTasks(Pageable pageable, Principal principal) {
        log.debug("Fetching all tasks for user: {}", principal.getName());
        User user = userService.getUserByUsername(principal.getName());
        String username = user.getUsername();
        Sort sort = pageable.getSortOr(Sort.by(Sort.Direction.ASC, "dueDate"));
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        try {
            Page<Task> tasks = taskRepository.findByUserId(user.getId(), pageRequest);
            if (tasks.isEmpty()) {
                log.warn("No tasks found for user: {}", username);
                throw new TaskNotFoundException("No tasks found for user: " + username);
            }
            log.info("Successfully fetched {} tasks for user: {}", tasks.getTotalElements(), username);
            return tasks;
        } catch (Exception e) {
            log.error("Error fetching tasks for user: {}: {}", user.getUsername(), e.getMessage());
            throw new TaskNotFoundException("An error occurred while fetching tasks for user: " + username, e);
        }
    }

    @Override
    public Task createTask(Task newTaskRequest, Principal principal) {
        String username = principal.getName();
        log.debug("Creating new task for user: {}", username);
        User user = userService.getUserByUsername(username);
        Task newTask = new Task(
                null,
                newTaskRequest.getTitle(),
                newTaskRequest.getDescription(),
                newTaskRequest.getDueDate(),
                newTaskRequest.getStatus(),
                user.getId()
        );
        try {
            Task savedTask = taskRepository.save(newTask);
            log.info("Task created successfully for user: {}, task ID: {}", username, savedTask.getId());
            return savedTask;
        } catch (Exception e) {
            log.error("Error creating task for user: {}, task: {}", username, newTaskRequest, e);
            throw new TaskCreationException("Error creating task for user: " + username, e);
        }
    }

    @Override
    public void updateTask(Long taskId, Task taskRequest, Principal principal) {
        String username = principal.getName();
        log.debug("Updating new task for user: {}", username);
            Task oldTask = getTaskByIdAndUser(taskId, username);
        try {
            Task updatedTask = new Task(taskId, taskRequest.getTitle(), taskRequest.getDescription(), taskRequest.getDueDate(), taskRequest.getStatus(), oldTask.getUserId());
            taskRepository.save(updatedTask);
            log.info("Task with ID: {} updated successfully for user: {}", taskId, username);
        }catch (Exception e) {
            log.error("Error updating task with ID: {} for user: {}", taskId, username, e);
            throw new TaskUpdateException("Error updating task with ID: " + taskId + " for user: " + username, e);
        }
    }

    @Override
    public void deleteTask(Long taskId, Principal principal) {
        log.debug("Deleting task with ID: {}", taskId);
        String username = principal.getName();
        Task deleteTask = getTaskByIdAndUser(taskId, username);
        try {
            taskRepository.delete(deleteTask);
        }catch (Exception e) {
            log.error("Error deleting task with ID: {} for user: {}", taskId, username, e);
            throw new TaskDeleteException("Error deleting task with ID: " + taskId + " for user: " + username, e);
        }
    }

    public Task getTaskByIdAndUser(Long taskId, String username) {
        log.debug("Fetching task with id: {} for username: {}", taskId, username);
        User user = userService.getUserByUsername(username);
        return taskRepository.findByIdAndUserId(taskId, user.getId()).orElseThrow(() -> {
            log.warn("Task with id: {} not found for user: {}", taskId, username);
            return new TaskNotFoundException("Task not found for id: " + taskId + " and user: " + username);
        });
    }


}
