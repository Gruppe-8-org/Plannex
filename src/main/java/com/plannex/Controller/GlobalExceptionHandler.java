package com.plannex.Controller;

import com.plannex.Exception.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;

import javax.naming.OperationNotSupportedException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @GetMapping("/error")
    private String handleBody(Model model, HttpStatus statusCode, String errorString, String errorMessage, HttpServletResponse response) {
        response.setStatus(statusCode.value());
        model.addAttribute("status", statusCode.value());
        model.addAttribute("error", errorString);
        model.addAttribute("message", errorMessage);
        return "error";
    }

    @ExceptionHandler(EntityDoesNotExistException.class)
    public String handleNotFound(EntityDoesNotExistException ex, Model model, HttpServletResponse response) {
        return handleBody(model, HttpStatus.NOT_FOUND, "Not found", ex.getMessage(), response);
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public String handleDuplicates(EntityAlreadyExistsException ex, Model model, HttpServletResponse response) {
        return handleBody(model, HttpStatus.CONFLICT, "Duplicate Entry", ex.getMessage(), response);
    }

    @ExceptionHandler(InvalidValueException.class)
    public String handleInvalidValue(InvalidValueException ex, Model model, HttpServletResponse response) {
        return handleBody(model, HttpStatus.BAD_REQUEST, "Invalid value", ex.getMessage(), response);
    }

    @ExceptionHandler(NotSupportedException.class)
    public String handleOperationNotSupported(OperationNotSupportedException ex, Model model, HttpServletResponse response) {
        return handleBody(model, HttpStatus.METHOD_NOT_ALLOWED, "Method not supported", ex.getMessage(), response);
    }

    @ExceptionHandler(InsufficientPermissionsException.class)
    public String handleInsufficientPermissions(InsufficientPermissionsException ex, Model model, HttpServletResponse response) {
        return handleBody(model, HttpStatus.FORBIDDEN, "Insufficient permissions", ex.getMessage(), response);
    }
}
