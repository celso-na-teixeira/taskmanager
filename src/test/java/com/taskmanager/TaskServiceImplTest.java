package com.taskmanager;

import com.taskmanager.exception.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private Task savedTask;
    private Task updatedTask;

    @BeforeEach
    public void setUp() {
        principal = () -> "username";
        taskUser = new TaskUser(200L, "leonardo", "password123", "leonardo@taskmanager.com", "TASK-OWNER");
        task = new Task(null, "Wash the car", "Description to wash the car", LocalDateTime.parse("2024-08-16T00:00:00"),false, taskUser.id());
        savedTask = new Task(103L, "Wash the car", "Description to wash the car", LocalDateTime.parse("2024-08-16T00:00:00"),false, taskUser.id());
        updatedTask = new Task(103L, "Wash the dishes", "Description to wash the dishes", LocalDateTime.parse("2024-08-17T00:00:00"),true, taskUser.id());
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

    @Test
    public void testGetTasks_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Task> taskList = Collections.singletonList(task);
        Page<Task> taskPage = new PageImpl<>(taskList, pageable, taskList.size());

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.findByUserId(anyLong(), any(PageRequest.class))).thenReturn(taskPage);

        Page<Task> result = taskService.getTasks(pageable, principal);
        assertEquals(taskPage, result);
    }

    @Test
    public void testGetTasks_NoTasksFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> emptyTaskPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.findByUserId(anyLong(), any(PageRequest.class))).thenReturn(emptyTaskPage);

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTasks(pageable, principal);
        });
    }

    @Test
    public void testGetTasks_Exception() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.findByUserId(anyLong(), any(PageRequest.class))).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTasks(pageable, principal);
        });

        assertEquals("An error occurred while fetching tasks for user: leonardo", exception.getMessage());
    }

    @Test
    public void testCreateTask_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        Task result = taskService.createTask(task, principal);
        assertEquals(savedTask, result);

        verify(userRepository).findByUsername(principal.getName());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    public void testCreateTask_Exception() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.save(any(Task.class))).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(TaskCreationException.class, () -> {
            taskService.createTask(task, principal);
        });

        assertEquals("Error creating task for user: username", exception.getMessage());

        verify(userRepository).findByUsername(principal.getName());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    public void testUpdateTask_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(savedTask));

        taskService.updateTask(1L, updatedTask, principal);

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    public void testUpdateTask_TaskNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.updateTask(1L, updatedTask, principal);
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    public void testUpdateTask_Exception() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(savedTask));
        doThrow(new RuntimeException("Database error")).when(taskRepository).save(any(Task.class));

        Exception exception = assertThrows(TaskUpdateException.class, () -> {
            taskService.updateTask(1L, updatedTask, principal);
        });

        assertEquals("Error updating task with ID: 1 for user: username", exception.getMessage());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    public void testDeleteTask_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(savedTask));
        taskService.deleteTask(1L, principal);
        verify(taskRepository).delete(any(Task.class));
    }

    @Test
    public void testDeleteTask_TaskNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.deleteTask(1L, principal);
        });

        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    public void testDeleteTask_Exception() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(taskUser));
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(savedTask));
        doThrow(new NullPointerException("Task is null")).when(taskRepository).delete(any(Task.class));

        Exception exception = assertThrows(TaskDeleteException.class, () -> {
            taskService.deleteTask(1L, principal);
        });
        assertEquals("Error deleting task with ID: 1 for user: username", exception.getMessage());
        verify(taskRepository).delete(any(Task.class));
    }

}

