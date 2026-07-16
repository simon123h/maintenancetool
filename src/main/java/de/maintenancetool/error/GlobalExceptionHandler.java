package de.maintenancetool.error;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(
      NoResourceFoundException ex, HttpServletRequest request) {
    log.warn("Static resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
    return createResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(
      ResourceNotFoundException ex, HttpServletRequest request) {
    log.warn("Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
    return createResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
  }

  @ExceptionHandler(InvalidOperationException.class)
  public ResponseEntity<ErrorResponse> handleInvalidOperation(
      InvalidOperationException ex, HttpServletRequest request) {
    log.warn("Invalid operation: {} - Path: {}", ex.getMessage(), request.getRequestURI());
    return createResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
  }

  @ExceptionHandler(MaintenanceException.class)
  public ResponseEntity<ErrorResponse> handleMaintenanceException(
      MaintenanceException ex, HttpServletRequest request) {
    log.warn(
        "Business exception occurred: {} - Path: {}", ex.getMessage(), request.getRequestURI());
    return createResponse(HttpStatus.BAD_REQUEST, "Business Error", ex.getMessage(), request);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(
      AccessDeniedException ex, HttpServletRequest request) {
    log.warn("Access denied: {} - Path: {}", ex.getMessage(), request.getRequestURI());
    return createResponse(
        HttpStatus.FORBIDDEN,
        "Forbidden",
        "Sie haben keine Berechtigung für diese Aktion.",
        request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(
      Exception ex, HttpServletRequest request) {
    log.error("Unhandled exception occurred on path: {}", request.getRequestURI(), ex);
    return createResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        "Ein unerwarteter Fehler ist aufgetreten: " + ex.getMessage(),
        request);
  }

  private ResponseEntity<ErrorResponse> createResponse(
      HttpStatus status, String error, String message, HttpServletRequest request) {
    ErrorResponse response =
        ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(error)
            .message(message)
            .path(request.getRequestURI())
            .build();
    return new ResponseEntity<>(response, status);
  }
}
