package com.taskmanager.service;

import com.taskmanager.config.JwtUtil;
import com.taskmanager.dto.UserDTO;
import com.taskmanager.dto.UserLoginDTO;
import com.taskmanager.exception.UserTaskNotFoundException;
import com.taskmanager.model.User;
import com.taskmanager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;

@Slf4j
@Service
public class TaskUserServiceImpl implements TaskUserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public TaskUserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void registerUser(UserDTO userDTO) {
        User newUser = convertDtoToEntity(userDTO);
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        userRepository.save(newUser);
    }

    @Override
    public String login(UserLoginDTO userLoginDTO) {
        User userFound = getUserByUsername(userLoginDTO.getUsername());
        boolean validUser = new BCryptPasswordEncoder().matches(userLoginDTO.getPassword(), userFound.getPassword());
        if (validUser) {
            return jwtUtil.generateToken(userFound.getUsername());
        } else {
            throw new RuntimeException("Invalid credentials");
        }

    }

    public User getUserByUsername(final String username) {
        log.debug("Fetching user with username: {}", username);
        return userRepository.findByUsername(username).orElseThrow(() -> {
            log.warn("User {} not found", username);
            return new UserTaskNotFoundException("User not found for username: " + username);
        });
    }

    private User convertDtoToEntity(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setRoles(userDTO.getRoles());
        return user;
    }

}
