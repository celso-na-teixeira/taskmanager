package com.taskmanager;

import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.exception.UserTaskNotFoundException;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskUser;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Principal principal;
    private TaskUser taskUser;
    private Task task;

    @BeforeEach
    public void setUp() {
        principal = () -> "username";
        taskUser = new TaskUser(200L, "leonardo", "password123", "leonardo@taskmanager.com", "TASK-OWNER");
        task = new Task(null, "Wash the car", "Description to wash the car", LocalDateTime.parse("2024-08-16T00:00:00"),false, taskUser.id());
    }

    @Test
    public void testGetTask_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(task));

        Task result = taskService.getTask(1L, principal);
        assertEquals(task, result);
    }

    @Test
    public void testGetTask_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UserTaskNotFoundException.class, () -> {
            taskService.getTask(1L, principal);
        });
    }

    @Test
    public void testGetTask_TaskNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTask(1L, principal);
        });
    }

    @Test
    public void testGetUserByUsername_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));

        TaskUser result = taskService.getUserByUsername("username");
        assertEquals(taskUser, result);
    }

    @Test
    public void testGetUserByUsername_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UserTaskNotFoundException.class, () -> {
            taskService.getUserByUsername("username");
        });
    }

    @Test
    public void testGetTaskByIdAndUser_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(task));

        Task result = taskService.getTaskByIdAndUser(1L, "username");
        assertEquals(task, result);
    }

    @Test
    public void testGetTaskByIdAndUser_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UserTaskNotFoundException.class, () -> {
            taskService.getTaskByIdAndUser(1L, "username");
        });
    }

    @Test
    public void testGetTaskByIdAndUser_TaskNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTaskByIdAndUser(1L, "username");
        });
    }
}

