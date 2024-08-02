package com.taskmanager.service;

import com.taskmanager.model.Task;

import java.security.Principal;

public interface TaskService {

    Task getTask(Long taskId, Principal principal);
}
