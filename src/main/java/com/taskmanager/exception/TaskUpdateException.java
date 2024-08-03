package com.taskmanager.exception;

public class TaskUpdateException extends RuntimeException {
    public TaskUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
