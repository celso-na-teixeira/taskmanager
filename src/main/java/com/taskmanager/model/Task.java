package com.taskmanager.model;

import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

public record Task(@Id
                   Long id,
                   String title,
                   String description,
                   LocalDateTime dueDate,
                   boolean completed,
                   Long userId
) {
}
