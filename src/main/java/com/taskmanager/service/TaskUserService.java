package com.taskmanager.service;

import com.taskmanager.dto.UserDTO;
import com.taskmanager.dto.UserLoginDTO;
import com.taskmanager.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface TaskUserService  {

    User getUserByUsername(final String username);

    void registerUser(final UserDTO user);

    String login(final UserLoginDTO userLoginDTO);
}
