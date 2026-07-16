package de.maintenancetool.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.maintenancetool.applicationuser.ApplicationUser;
import de.maintenancetool.applicationuser.ApplicationUserRepository;
import de.maintenancetool.error.InvalidOperationException;
import de.maintenancetool.error.ResourceNotFoundException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

  @Mock private ApplicationRepository applicationRepository;

  @Mock private ApplicationUserRepository applicationUserRepository;

  @InjectMocks private ApplicationService applicationService;

  private Application app;
  private ApplicationDto appDto;
  private ApplicationUser user;

  @BeforeEach
  void setUp() {
    app =
        Application.builder()
            .id(UUID.randomUUID())
            .name("TestApp")
            .url("http://test.com")
            .description("Test Description")
            .users(new HashSet<>())
            .build();

    appDto =
        ApplicationDto.builder()
            .name("TestApp")
            .url("http://test.com")
            .description("Test Description")
            .build();

    user =
        ApplicationUser.builder()
            .id(UUID.randomUUID())
            .email("user@test.com")
            .name("Test User")
            .build();
  }

  @Test
  void testGetAllApplications() {
    when(applicationRepository.findAll()).thenReturn(List.of(app));
    List<ApplicationDto> result = applicationService.getAllApplications();
    assertEquals(1, result.size());
    assertEquals("TestApp", result.getFirst().getName());
  }

  @Test
  void testGetApplicationById_Success() {
    when(applicationRepository.findById(app.getId())).thenReturn(Optional.of(app));
    ApplicationDto result = applicationService.getApplicationById(app.getId());
    assertNotNull(result);
    assertEquals("TestApp", result.getName());
  }

  @Test
  void testGetApplicationById_NotFound() {
    UUID randomId = UUID.randomUUID();
    when(applicationRepository.findById(randomId)).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> applicationService.getApplicationById(randomId));
  }

  @Test
  void testCreateApplication_Success() {
    when(applicationRepository.findByName("TestApp")).thenReturn(Optional.empty());
    when(applicationRepository.save(any(Application.class))).thenReturn(app);

    ApplicationDto result = applicationService.createApplication(appDto);
    assertNotNull(result);
    assertEquals("TestApp", result.getName());
  }

  @Test
  void testCreateApplication_AlreadyExists() {
    when(applicationRepository.findByName("TestApp")).thenReturn(Optional.of(app));
    assertThrows(
        InvalidOperationException.class, () -> applicationService.createApplication(appDto));
  }

  @Test
  void testAddUserToApplication() {
    when(applicationRepository.findById(app.getId())).thenReturn(Optional.of(app));
    when(applicationUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(applicationRepository.save(app)).thenReturn(app);

    applicationService.addUserToApplication(app.getId(), user.getId());

    assertTrue(app.getUsers().contains(user));
    verify(applicationRepository, times(1)).save(app);
  }

  @Test
  void testRemoveUserFromApplication() {
    app.getUsers().add(user);
    when(applicationRepository.findById(app.getId())).thenReturn(Optional.of(app));
    when(applicationUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(applicationRepository.save(app)).thenReturn(app);

    applicationService.removeUserFromApplication(app.getId(), user.getId());

    assertFalse(app.getUsers().contains(user));
    verify(applicationRepository, times(1)).save(app);
  }
}
