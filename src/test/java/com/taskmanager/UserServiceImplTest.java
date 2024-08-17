package com.taskmanager;

import com.taskmanager.exception.UserTaskNotFoundException;
import com.taskmanager.model.Role;
import com.taskmanager.model.User;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.TaskUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private TaskUserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private User user;
    private List<Role> roles;

    @BeforeEach
    public void setUp() {
        roles = Collections.singletonList(new Role(400L, "TASK-OWNER"));
        user = new User(200L, "leonardo", "password123", "leonardo@taskmanager.com", roles);
    }

    @Test
    public void testGetUserByUsername_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        User result = userService.getUserByUsername("username");
        assertEquals(user, result);
    }

    @Test
    public void testGetUserByUsername_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UserTaskNotFoundException.class, () -> {
            userService.getUserByUsername("username");
        });
    }

}
