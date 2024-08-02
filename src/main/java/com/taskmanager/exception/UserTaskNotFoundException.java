package com.taskmanager.exception;

public class UserTaskNotFoundException extends RuntimeException {
        public UserTaskNotFoundException(String message) {
        super(message);
    }
}
