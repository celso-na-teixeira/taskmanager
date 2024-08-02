package com.taskmanager.service;

import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.exception.UserTaskNotFoundException;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskUser;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;


@Slf4j
@Service
public class TaskServiceImpl implements TaskService{
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Task getTask(Long taskId, Principal principal) {
        log.debug("Fetching task with id: {} for user: {}", taskId, principal.getName());
        return getTaskByIdAndUser(taskId, principal.getName());
    }

    public TaskUser getUserByUsername(final String username) {
        log.debug("Fetching user with username: {}", username);
        return userRepository.findByUsername(username).orElseThrow(() -> new UserTaskNotFoundException("UserTask not found"));
    }

    public Task getTaskByIdAndUser(Long taskId, String username) {
        log.debug("Fetching task with id: {} for username: {}", taskId, username);
        TaskUser user = getUserByUsername(username);
        return taskRepository.findByIdAndUserId(taskId, user.id()).orElseThrow(() -> new TaskNotFoundException("Task not found"));
    }
}
