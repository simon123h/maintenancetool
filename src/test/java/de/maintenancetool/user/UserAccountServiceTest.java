package de.maintenancetool.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.maintenancetool.error.InvalidOperationException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

  @Mock private UserAccountRepository userAccountRepository;
  @Mock private SecurityContextService securityContextService;

  @InjectMocks private UserAccountService userAccountService;

  private UserAccount testUser;

  @BeforeEach
  void setUp() {
    testUser =
        UserAccount.builder()
            .id(UUID.randomUUID())
            .mailAdresse("test@example.com")
            .vollerName("Test User")
            .loginName("testuser")
            .rollen(new HashSet<>(Set.of(Rolle.ROLE_TEILNEHMER)))
            .build();
  }

  @Test
  void testGetAllUsers() {
    when(userAccountRepository.findAll()).thenReturn(List.of(testUser));
    List<UserAccount> users = userAccountService.getAllUsers();
    assertEquals(1, users.size());
    assertEquals("test@example.com", users.get(0).getMailAdresse());
  }

  @Test
  void testUpdateUserRoles_Success() {
    UUID userId = testUser.getId();
    when(userAccountRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(userAccountRepository.save(any(UserAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Set<Rolle> newRoles = new HashSet<>(Set.of(Rolle.ROLE_TEILNEHMER, Rolle.ROLE_MANAGER));
    UserAccount updated = userAccountService.updateUserRoles(userId, newRoles);

    assertTrue(updated.getRollen().contains(Rolle.ROLE_MANAGER));
    assertTrue(updated.getRollen().contains(Rolle.ROLE_TEILNEHMER));
  }

  @Test
  void testUpdateUserRoles_PreventLastAdminDemotion() {
    UUID userId = testUser.getId();
    testUser.setRollen(new HashSet<>(Set.of(Rolle.ROLE_TEILNEHMER, Rolle.ROLE_ADMIN)));

    when(userAccountRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(userAccountRepository.findAll()).thenReturn(List.of(testUser));

    Set<Rolle> newRoles = new HashSet<>(Set.of(Rolle.ROLE_TEILNEHMER));

    assertThrows(
        InvalidOperationException.class,
        () -> {
          userAccountService.updateUserRoles(userId, newRoles);
        });
  }

  @Test
  void testProvisionUser_AdminRoles() {
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn("admin");
    when(auth.getPrincipal()).thenReturn("admin");

    when(userAccountRepository.findByMailAdresse("admin@maintenancetool.de"))
        .thenReturn(Optional.empty());
    when(userAccountRepository.count()).thenReturn(1L); // DB is not empty
    when(userAccountRepository.save(any(UserAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserAccount provisioned = userAccountService.provisionUser(auth);

    assertEquals("admin", provisioned.getLoginName());
    assertTrue(provisioned.getRollen().contains(Rolle.ROLE_ADMIN));
    assertTrue(provisioned.getRollen().contains(Rolle.ROLE_MANAGER));
    assertTrue(provisioned.getRollen().contains(Rolle.ROLE_TEILNEHMER));
  }

  @Test
  void testProvisionUser_ManagerRoles() {
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn("manager");
    when(auth.getPrincipal()).thenReturn("manager");

    when(userAccountRepository.findByMailAdresse("manager@maintenancetool.de"))
        .thenReturn(Optional.empty());
    when(userAccountRepository.count()).thenReturn(1L); // DB is not empty
    when(userAccountRepository.save(any(UserAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserAccount provisioned = userAccountService.provisionUser(auth);

    assertEquals("manager", provisioned.getLoginName());
    assertFalse(provisioned.getRollen().contains(Rolle.ROLE_ADMIN));
    assertTrue(provisioned.getRollen().contains(Rolle.ROLE_MANAGER));
    assertTrue(provisioned.getRollen().contains(Rolle.ROLE_TEILNEHMER));
  }

  @Test
  void testProvisionUser_ExistingUserAdminRoles() {
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn("admin");
    when(auth.getPrincipal()).thenReturn("admin");

    UserAccount existingUser =
        UserAccount.builder()
            .id(UUID.randomUUID())
            .mailAdresse("admin@maintenancetool.de")
            .vollerName("Admin User")
            .loginName("admin")
            .rollen(new HashSet<>(Set.of(Rolle.ROLE_TEILNEHMER))) // only has TEILNEHMER initially
            .build();

    when(userAccountRepository.findByMailAdresse("admin@maintenancetool.de"))
        .thenReturn(Optional.of(existingUser));
    when(userAccountRepository.save(any(UserAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserAccount provisioned = userAccountService.provisionUser(auth);

    assertEquals("admin", provisioned.getLoginName());
    assertTrue(provisioned.getRollen().contains(Rolle.ROLE_ADMIN));
    assertTrue(provisioned.getRollen().contains(Rolle.ROLE_MANAGER));
    assertTrue(provisioned.getRollen().contains(Rolle.ROLE_TEILNEHMER));
  }

  @Test
  void testGetCurrentUser_Success() {
    Authentication auth = mock(Authentication.class);
    when(auth.isAuthenticated()).thenReturn(true);
    when(auth.getName()).thenReturn("testuser");
    when(auth.getPrincipal()).thenReturn("testuser");
    when(securityContextService.getAuthentication()).thenReturn(Optional.of(auth));
    when(userAccountRepository.findByLoginName("testuser")).thenReturn(Optional.of(testUser));

    Optional<UserAccount> result = userAccountService.getCurrentUser();

    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getLoginName());
  }

  @Test
  void testGetCurrentUser_Unauthenticated() {
    when(securityContextService.getAuthentication()).thenReturn(Optional.empty());

    Optional<UserAccount> result = userAccountService.getCurrentUser();

    assertFalse(result.isPresent());
  }

  @Test
  void testGetCurrentUser_NotAuthenticatedProperty() {
    Authentication auth = mock(Authentication.class);
    when(auth.isAuthenticated()).thenReturn(false);
    when(securityContextService.getAuthentication()).thenReturn(Optional.of(auth));

    Optional<UserAccount> result = userAccountService.getCurrentUser();
    assertFalse(result.isPresent());
  }

  @Test
  void testGetCurrentUser_Anonymous() {
    Authentication auth = mock(Authentication.class);
    when(auth.isAuthenticated()).thenReturn(true);
    when(auth.getPrincipal()).thenReturn("anonymousUser");
    when(securityContextService.getAuthentication()).thenReturn(Optional.of(auth));

    Optional<UserAccount> result = userAccountService.getCurrentUser();
    assertFalse(result.isPresent());
  }

  @Test
  void testProvisionUser_BootstrappingFirstUser() {
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn("alice");
    when(auth.getPrincipal()).thenReturn("alice");

    when(userAccountRepository.findByMailAdresse("alice@maintenancetool.de"))
        .thenReturn(Optional.empty());
    when(userAccountRepository.count()).thenReturn(0L); // DB is completely empty!
    when(userAccountRepository.save(any(UserAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserAccount provisioned = userAccountService.provisionUser(auth);

    assertEquals("alice", provisioned.getLoginName());
    assertTrue(provisioned.getRollen().contains(Rolle.ROLE_ADMIN));
    assertTrue(provisioned.getRollen().contains(Rolle.ROLE_MANAGER));
    assertTrue(provisioned.getRollen().contains(Rolle.ROLE_TEILNEHMER));
  }

  @Test
  void testProvisionUser_LoginWithEmail() {
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn("bob@company.com");
    when(auth.getPrincipal()).thenReturn("bob@company.com");

    when(userAccountRepository.findByMailAdresse("bob@company.com")).thenReturn(Optional.empty());
    when(userAccountRepository.count()).thenReturn(1L);
    when(userAccountRepository.save(any(UserAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserAccount provisioned = userAccountService.provisionUser(auth);

    assertEquals("bob@company.com", provisioned.getMailAdresse());
    assertEquals("bob@company.com", provisioned.getLoginName());
  }

  @Test
  void testProvisionUser_ExistingManagerRolesUpdated() {
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn("manager");
    when(auth.getPrincipal()).thenReturn("manager");

    UserAccount existingManager =
        UserAccount.builder()
            .id(UUID.randomUUID())
            .mailAdresse("manager@maintenancetool.de")
            .vollerName("Manager User")
            .loginName("manager")
            .rollen(new HashSet<>(Set.of(Rolle.ROLE_TEILNEHMER)))
            .build();

    when(userAccountRepository.findByMailAdresse("manager@maintenancetool.de"))
        .thenReturn(Optional.of(existingManager));
    when(userAccountRepository.save(any(UserAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserAccount provisioned = userAccountService.provisionUser(auth);

    assertEquals("manager", provisioned.getLoginName());
    assertTrue(provisioned.getRollen().contains(Rolle.ROLE_MANAGER));
    assertFalse(provisioned.getRollen().contains(Rolle.ROLE_ADMIN));
  }

  @Test
  void testProvisionUser_Saml2Accessor() {
    Authentication auth = mock(Authentication.class);
    org.springframework.security.saml2.provider.service.authentication
            .Saml2ResponseAssertionAccessor
        samlAccessor =
            mock(
                org.springframework.security.saml2.provider.service.authentication
                    .Saml2ResponseAssertionAccessor.class);

    when(auth.getName()).thenReturn("samlUser");
    when(auth.getPrincipal()).thenReturn(samlAccessor);
    when(samlAccessor.getAttribute("mail")).thenReturn(List.of("saml@test.com"));
    when(samlAccessor.getAttribute("cn")).thenReturn(List.of("SAML User Fullname"));

    when(userAccountRepository.findByMailAdresse("saml@test.com")).thenReturn(Optional.empty());
    when(userAccountRepository.count()).thenReturn(1L);
    when(userAccountRepository.save(any(UserAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserAccount provisioned = userAccountService.provisionUser(auth);

    assertEquals("saml@test.com", provisioned.getMailAdresse());
    assertEquals("SAML User Fullname", provisioned.getVollerName());
    assertEquals("samlUser", provisioned.getLoginName());
  }

  @Test
  void testUpdateUserRoles_UserNotFound() {
    UUID randomId = UUID.randomUUID();
    when(userAccountRepository.findById(randomId)).thenReturn(Optional.empty());

    assertThrows(
        de.maintenancetool.error.ResourceNotFoundException.class,
        () -> userAccountService.updateUserRoles(randomId, Set.of(Rolle.ROLE_TEILNEHMER)));
  }
}
