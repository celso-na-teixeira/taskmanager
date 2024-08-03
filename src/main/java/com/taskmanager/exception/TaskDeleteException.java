package com.taskmanager.exception;

public class TaskDeleteException extends RuntimeException {
    public TaskDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}
