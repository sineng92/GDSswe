package com.example.demo.exceptions;

import com.example.demo.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@ControllerAdvice
public class CustomizedExceptionHandling {
    Logger logger = LoggerFactory.getLogger(UserInfoService.class);

    @ExceptionHandler(value = ServiceException.class)
    public ResponseEntity<Object> runtimeException(ServiceException obj) {
        ExceptionResponse response = new ExceptionResponse();
        response.setError(obj.getMessage());
        response.setDateTime(LocalDateTime.now());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler({MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestPartException.class, Exception.class})
    public ResponseEntity<Object> handleException(Exception e, HttpServletRequest request) {
        ExceptionResponse response = new ExceptionResponse();
        response.setError(e.getMessage());
        response.setDateTime(LocalDateTime.now());
        logger.error(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

}
