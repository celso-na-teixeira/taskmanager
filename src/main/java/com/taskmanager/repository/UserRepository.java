package com.taskmanager.repository;

import com.taskmanager.model.TaskUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<TaskUser, Long> {

    Optional<TaskUser> findByUsername(String username);
}
