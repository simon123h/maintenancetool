package de.maintenancetool.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.maintenancetool.user.Rolle;
import de.maintenancetool.user.UserAccount;
import de.maintenancetool.user.UserAccountService;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class DbUserAuthenticationSuccessHandlerTest {

  @Mock private UserAccountService userAccountService;
  @Mock private Authentication authentication;

  private DbUserAuthenticationSuccessHandler successHandler;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    successHandler = new DbUserAuthenticationSuccessHandler(userAccountService);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    SecurityContextHolder.clearContext();
  }

  @Test
  void testOnAuthenticationSuccess() throws IOException, ServletException {
    UserAccount testUser =
        UserAccount.builder()
            .mailAdresse("user@example.com")
            .vollerName("Test User")
            .rollen(new HashSet<>(Set.of(Rolle.ROLE_TEILNEHMER, Rolle.ROLE_MANAGER)))
            .build();

    when(userAccountService.provisionUser(authentication)).thenReturn(testUser);
    when(authentication.getPrincipal()).thenReturn("testuser");
    when(authentication.getCredentials()).thenReturn("credentials");

    successHandler.onAuthenticationSuccess(request, response, authentication);

    Authentication updatedAuth = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(updatedAuth);
    assertEquals("testuser", updatedAuth.getPrincipal());
    assertEquals("credentials", updatedAuth.getCredentials());

    Collection<? extends GrantedAuthority> authorities = updatedAuth.getAuthorities();
    assertEquals(2, authorities.size());
    assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEILNEHMER")));
    assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));

    assertEquals("/", response.getRedirectedUrl());
  }
}
