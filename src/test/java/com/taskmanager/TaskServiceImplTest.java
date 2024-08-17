package com.taskmanager;

import com.taskmanager.exception.*;
import com.taskmanager.model.Role;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.model.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.TaskServiceImpl;
import com.taskmanager.service.TaskUserService;
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

    @Mock
    private TaskUserService userService;

    @InjectMocks
    private TaskServiceImpl taskService;



    private Principal principal;
    private User user;
    private Task task;
    private Task savedTask;
    private Task updatedTask;
    private List<Role> roles;

    @BeforeEach
    public void setUp() {
        roles = Collections.singletonList(new Role(400L, "TASK-OWNER"));
        principal = () -> "username";
        user = new User(200L, "leonardo", "password123", "leonardo@taskmanager.com", roles);
        task = new Task(null, "Wash the car", "Description to wash the car", LocalDateTime.parse("2024-08-16T00:00:00"), TaskStatus.TODO, user.getId());
        savedTask = new Task(103L, "Wash the car", "Description to wash the car", LocalDateTime.parse("2024-08-16T00:00:00"),TaskStatus.TODO, user.getId());
        updatedTask = new Task(103L, "Wash the dishes", "Description to wash the dishes", LocalDateTime.parse("2024-08-17T00:00:00"),TaskStatus.COMPLETED, user.getId());
    }

    @Test
    public void testGetTask_Success() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(task));

        Task result = taskService.getTask(1L, principal);
        assertEquals(task, result);
    }

    @Test
    public void testGetTask_UserNotFound() {
        when(userService.getUserByUsername(anyString())).thenThrow(new UserTaskNotFoundException("User not found for username: " + principal.getName()));

        UserTaskNotFoundException thrownException = assertThrows(UserTaskNotFoundException.class, () -> {
            taskService.getTask(1L, principal);
        });

        assertEquals("User not found for username: username", thrownException.getMessage());

        // Verify interactions
        verify(userService).getUserByUsername(anyString());
        verify(taskRepository, never()).findByIdAndUserId(anyLong(), anyLong());
    }

    @Test
    public void testGetTask_TaskNotFound() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTask(1L, principal);
        });
    }
    
    @Test
    public void testGetTaskByIdAndUser_Success() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(task));

        Task result = taskService.getTaskByIdAndUser(1L, "username");
        assertEquals(task, result);
    }

    @Test
    public void testGetTaskByIdAndUser_UserNotFound() {
        when(userService.getUserByUsername(anyString())).thenThrow(new UserTaskNotFoundException("User not found for username: " + principal.getName()));

        assertThrows(UserTaskNotFoundException.class, () -> {
            taskService.getTaskByIdAndUser(1L, "username");
        });
    }

    @Test
    public void testGetTaskByIdAndUser_TaskNotFound() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
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

        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(taskRepository.findByUserId(anyLong(), any(PageRequest.class))).thenReturn(taskPage);

        Page<Task> result = taskService.getTasks(pageable, principal);
        assertEquals(taskPage, result);
    }

    @Test
    public void testGetTasks_NoTasksFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> emptyTaskPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(taskRepository.findByUserId(anyLong(), any(PageRequest.class))).thenReturn(emptyTaskPage);

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTasks(pageable, principal);
        });
    }

    @Test
    public void testGetTasks_Exception() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(taskRepository.findByUserId(anyLong(), any(PageRequest.class))).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTasks(pageable, principal);
        });

        assertEquals("An error occurred while fetching tasks for user: leonardo", exception.getMessage());
    }

    @Test
    public void testCreateTask_Success() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        Task result = taskService.createTask(task, principal);
        assertEquals(savedTask, result);

        verify(userService).getUserByUsername(principal.getName());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    public void testCreateTask_Exception() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(TaskCreationException.class, () -> {
            taskService.createTask(task, principal);
        });

        assertEquals("Error creating task for user: username", exception.getMessage());

        verify(userService).getUserByUsername(principal.getName());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    public void testUpdateTask_Success() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(savedTask));

        taskService.updateTask(1L, updatedTask, principal);

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    public void testUpdateTask_TaskNotFound() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.updateTask(1L, updatedTask, principal);
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    public void testUpdateTask_Exception() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
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
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(savedTask));
        taskService.deleteTask(1L, principal);
        verify(taskRepository).delete(any(Task.class));
    }

    @Test
    public void testDeleteTask_TaskNotFound() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.deleteTask(1L, principal);
        });

        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    public void testDeleteTask_Exception() {
        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(taskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(savedTask));
        doThrow(new NullPointerException("Task is null")).when(taskRepository).delete(any(Task.class));

        Exception exception = assertThrows(TaskDeleteException.class, () -> {
            taskService.deleteTask(1L, principal);
        });
        assertEquals("Error deleting task with ID: 1 for user: username", exception.getMessage());
        verify(taskRepository).delete(any(Task.class));
    }

}

