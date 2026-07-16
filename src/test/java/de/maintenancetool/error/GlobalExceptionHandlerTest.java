package de.maintenancetool.error;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  @Mock private HttpServletRequest request;

  private GlobalExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    exceptionHandler = new GlobalExceptionHandler();
    when(request.getRequestURI()).thenReturn("/api/test-endpoint");
  }

  @Test
  void testHandleNoResourceFound() {
    NoResourceFoundException ex = mock(NoResourceFoundException.class);
    when(ex.getMessage()).thenReturn("Resource not found message");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleNoResourceFound(ex, request);

    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals(404, body.getStatus());
    assertEquals("Not Found", body.getError());
    assertEquals("Resource not found message", body.getMessage());
    assertEquals("/api/test-endpoint", body.getPath());
  }

  @Test
  void testHandleResourceNotFound() {
    ResourceNotFoundException ex = new ResourceNotFoundException("User not found");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex, request);

    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals(404, body.getStatus());
    assertEquals("Not Found", body.getError());
    assertEquals("User not found", body.getMessage());
    assertEquals("/api/test-endpoint", body.getPath());
  }

  @Test
  void testHandleInvalidOperation() {
    InvalidOperationException ex = new InvalidOperationException("Invalid action");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidOperation(ex, request);

    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals(400, body.getStatus());
    assertEquals("Bad Request", body.getError());
    assertEquals("Invalid action", body.getMessage());
  }

  @Test
  void testHandleMaintenanceException() {
    MaintenanceException ex = new MaintenanceException("Business error message") {};

    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleMaintenanceException(ex, request);

    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals(400, body.getStatus());
    assertEquals("Business Error", body.getError());
    assertEquals("Business error message", body.getMessage());
  }

  @Test
  void testHandleAccessDenied() {
    AccessDeniedException ex = new AccessDeniedException("Access Denied");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(ex, request);

    assertNotNull(response);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals(403, body.getStatus());
    assertEquals("Forbidden", body.getError());
    assertEquals("Sie haben keine Berechtigung für diese Aktion.", body.getMessage());
  }

  @Test
  void testHandleGeneralException() {
    Exception ex = new RuntimeException("Null pointer occurred");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneralException(ex, request);

    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals(500, body.getStatus());
    assertEquals("Internal Server Error", body.getError());
    assertTrue(
        body.getMessage()
            .contains("Ein unerwarteter Fehler ist aufgetreten: Null pointer occurred"));
  }
}
