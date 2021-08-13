package io.github.korzepadawid.urlshortener.api.v1.controllers;

import io.github.korzepadawid.urlshortener.api.v1.models.RestException;
import io.github.korzepadawid.urlshortener.exceptions.ResourceNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class RestControllerExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Object> handleResourceNotFound(Exception exception) {
    log.error(exception.getLocalizedMessage());

    HttpStatus httpStatus = HttpStatus.NOT_FOUND;
    RestException restException = new RestException(exception.getLocalizedMessage(),
        httpStatus.value(), null);

    return new ResponseEntity<>(restException, httpStatus);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationErrors(MethodArgumentNotValidException exception) {
    log.error("Validation error.");

    Map<String, String> errors = new HashMap<>();

    exception.getBindingResult()
        .getAllErrors()
        .forEach(error -> errors.put(((FieldError) error).getField(), error.getDefaultMessage()));

    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    RestException restException = new RestException("Validation error.",
        httpStatus.value(), errors);

    return new ResponseEntity<>(restException, HttpStatus.BAD_REQUEST);
  }
}
