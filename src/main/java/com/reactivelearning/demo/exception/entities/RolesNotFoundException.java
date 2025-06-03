package com.reactivelearning.demo.exception.entities;

public class RolesNotFoundException extends RuntimeException {
    public RolesNotFoundException(String message) {
        super(message);
    }
}
