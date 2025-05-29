package com.reactivelearning.demo.exception.entities;

public class ExistsException extends RuntimeException {
    public ExistsException(String message) {
        super(message);
    }
}
