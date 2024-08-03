package com.taskmanager.service;

import com.taskmanager.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface TaskService {

    Task getTask(final Long taskId, final Principal principal);

    Page<Task> getTasks(final Pageable pageable, final Principal principal);

    Task createTask(final Task newTask, final Principal principal);

    void updateTask(final Long taskId, final Task taskRequest, final Principal principal);

    void deleteTask(final Long taskId, final Principal principal);

}
