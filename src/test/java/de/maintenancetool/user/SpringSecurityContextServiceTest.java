package de.maintenancetool.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class SpringSecurityContextServiceTest {

  private SpringSecurityContextService service;
  private SecurityContext originalContext;

  @BeforeEach
  void setUp() {
    service = new SpringSecurityContextService();
    originalContext = SecurityContextHolder.getContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.setContext(originalContext);
  }

  @Test
  void testGetAuthentication_Empty() {
    SecurityContext mockContext = mock(SecurityContext.class);
    when(mockContext.getAuthentication()).thenReturn(null);
    SecurityContextHolder.setContext(mockContext);

    Optional<Authentication> result = service.getAuthentication();

    assertFalse(result.isPresent());
  }

  @Test
  void testGetAuthentication_Present() {
    Authentication mockAuth = mock(Authentication.class);
    SecurityContext mockContext = mock(SecurityContext.class);
    when(mockContext.getAuthentication()).thenReturn(mockAuth);
    SecurityContextHolder.setContext(mockContext);

    Optional<Authentication> result = service.getAuthentication();

    assertTrue(result.isPresent());
    assertEquals(mockAuth, result.get());
  }
}
