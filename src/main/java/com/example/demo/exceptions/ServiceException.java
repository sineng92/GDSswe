package com.example.demo.exceptions;

public class ServiceException extends RuntimeException {
    public ServiceException(String result) {
        super(result);
    }
}

