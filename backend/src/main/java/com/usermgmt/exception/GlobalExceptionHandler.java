package com.usermgmt.exception;

import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    record Err(String message, int status, LocalDateTime timestamp, List<String> errors){}

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Err> notFound(ResourceNotFoundException e){ return build(e.getMessage(),HttpStatus.NOT_FOUND,null); }
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Err> dup(DuplicateResourceException e){ return build(e.getMessage(),HttpStatus.CONFLICT,null); }
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Err> val(ValidationException e){ return build(e.getMessage(),HttpStatus.BAD_REQUEST,null); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Err> bind(MethodArgumentNotValidException e){
        var errs=e.getBindingResult().getFieldErrors().stream()
            .map(f->f.getField()+": "+f.getDefaultMessage()).collect(Collectors.toList());
        return build("Validation failed",HttpStatus.BAD_REQUEST,errs);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Err> creds(BadCredentialsException e){ return build("Invalid credentials",HttpStatus.UNAUTHORIZED,null); }
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Err> locked(LockedException e){ return build(e.getMessage(),HttpStatus.LOCKED,null); }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Err> denied(AccessDeniedException e){ return build("Access denied",HttpStatus.FORBIDDEN,null); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Err> general(Exception e){ return build("Internal error",HttpStatus.INTERNAL_SERVER_ERROR,null); }

    private ResponseEntity<Err> build(String msg, HttpStatus s, List<String> errs){
        return ResponseEntity.status(s).body(new Err(msg,s.value(),LocalDateTime.now(),errs));
    }
}