package com.taskmanager.controller;

import com.taskmanager.config.JwtUtil;
import com.taskmanager.dto.UserDTO;
import com.taskmanager.dto.UserLoginDTO;
import com.taskmanager.model.User;
import com.taskmanager.service.TaskUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/taskmanager/auth")
public class AuthController {

    @Autowired
    private TaskUserService userService;



    @PostMapping("/register")
    public void registerUser(@RequestBody UserDTO user) {
        userService.registerUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginDTO user) {
        String token = userService.login(user);
        return ResponseEntity.ok(token);
    }
}
