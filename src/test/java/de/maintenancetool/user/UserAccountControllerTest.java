package de.maintenancetool.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAccountControllerTest {

  @Mock private UserAccountService userAccountService;

  @InjectMocks private UserAccountController controller;

  private UUID testUserId;
  private UserAccount testUser;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testUser =
        UserAccount.builder()
            .id(testUserId)
            .mailAdresse("user@example.com")
            .vollerName("User Account")
            .loginName("useraccount")
            .rollen(new HashSet<>(Set.of(Rolle.ROLE_TEILNEHMER)))
            .build();
  }

  @Test
  void testGetMe_Success() {
    when(userAccountService.getCurrentUser()).thenReturn(Optional.of(testUser));

    UserAccountDto result = controller.getMe();

    assertNotNull(result);
    assertEquals(testUserId, result.getId());
    assertEquals("user@example.com", result.getMailAdresse());
    assertEquals("User Account", result.getVollerName());
    assertEquals("useraccount", result.getLoginName());
    assertTrue(result.getRollen().contains(Rolle.ROLE_TEILNEHMER));
  }

  @Test
  void testGetMe_NotFound() {
    when(userAccountService.getCurrentUser()).thenReturn(Optional.empty());

    assertThrows(
        de.maintenancetool.error.ResourceNotFoundException.class, () -> controller.getMe());
  }

  @Test
  void testGetAllUsers() {
    when(userAccountService.getAllUsers()).thenReturn(List.of(testUser));

    List<UserAccountDto> result = controller.getAllUsers();

    assertEquals(1, result.size());
    assertEquals(testUserId, result.get(0).getId());
  }

  @Test
  void testUpdateUserRoles() {
    Set<Rolle> roles = new HashSet<>(Set.of(Rolle.ROLE_ADMIN));
    testUser.setRollen(roles);
    when(userAccountService.updateUserRoles(testUserId, roles)).thenReturn(testUser);

    UserAccountDto result = controller.updateUserRoles(testUserId, roles);

    assertNotNull(result);
    assertTrue(result.getRollen().contains(Rolle.ROLE_ADMIN));
  }

  @Test
  void testDtoNullHandling() {
    assertNull(UserAccountDto.fromEntity(null));
  }
}
