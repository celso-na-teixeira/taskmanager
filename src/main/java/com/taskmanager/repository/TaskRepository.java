package com.taskmanager.repository;

import com.taskmanager.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jdbc.repository.query.Query;
import java.util.Optional;


@Repository
public interface TaskRepository extends CrudRepository <Task, Long>, PagingAndSortingRepository<Task, Long> {

    Page<Task> findByUserId(Long userId, PageRequest pageRequest);

    Optional<Task> findByIdAndUserId(Long id, Long userId);
}
