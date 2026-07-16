package de.maintenancetool.maintenance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.maintenancetool.application.Application;
import de.maintenancetool.application.ApplicationRepository;
import de.maintenancetool.applicationuser.ApplicationUser;
import de.maintenancetool.communication.EmailService;
import de.maintenancetool.error.InvalidOperationException;
import de.maintenancetool.template.EmailTemplate;
import de.maintenancetool.template.EmailTemplateRepository;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaintenanceWindowServiceTest {

  @Mock private MaintenanceWindowRepository maintenanceWindowRepository;

  @Mock private ApplicationRepository applicationRepository;

  @Mock private EmailTemplateRepository emailTemplateRepository;

  @Mock private EmailService emailService;

  @InjectMocks private MaintenanceWindowService maintenanceWindowService;

  private Application app;
  private EmailTemplate template;
  private MaintenanceWindow window;
  private MaintenanceWindowDto windowDto;
  private ApplicationUser user;

  @BeforeEach
  void setUp() {
    app =
        Application.builder()
            .id(UUID.randomUUID())
            .name("AppOne")
            .url("http://appone.com")
            .users(new HashSet<>())
            .build();

    user =
        ApplicationUser.builder()
            .id(UUID.randomUUID())
            .email("recipient@company.com")
            .name("Bob User")
            .build();

    template =
        EmailTemplate.builder()
            .id(UUID.randomUUID())
            .name("DefaultTemp")
            .subjectPattern("Maintenance for {appName}")
            .bodyPattern("Hi {userName}, {appName} will be down at {startTime} until {endTime}.")
            .build();

    window =
        MaintenanceWindow.builder()
            .id(UUID.randomUUID())
            .title("Database Upgrade")
            .description("Upgrading SQL Server")
            .startTime(LocalDateTime.of(2026, 7, 20, 10, 0))
            .endTime(LocalDateTime.of(2026, 7, 20, 12, 0))
            .status(MaintenanceStatus.PLANNED)
            .application(app)
            .template(template)
            .build();

    windowDto =
        MaintenanceWindowDto.builder()
            .title("Database Upgrade")
            .description("Upgrading SQL Server")
            .startTime(LocalDateTime.of(2026, 7, 20, 10, 0))
            .endTime(LocalDateTime.of(2026, 7, 20, 12, 0))
            .status(MaintenanceStatus.PLANNED)
            .applicationId(app.getId())
            .templateId(template.getId())
            .build();
  }

  @Test
  void testCreateWindow_Success() {
    when(applicationRepository.findById(app.getId())).thenReturn(Optional.of(app));
    when(emailTemplateRepository.findById(template.getId())).thenReturn(Optional.of(template));
    when(maintenanceWindowRepository.save(any(MaintenanceWindow.class))).thenReturn(window);

    MaintenanceWindowDto result = maintenanceWindowService.createWindow(windowDto);

    assertNotNull(result);
    assertEquals("Database Upgrade", result.getTitle());
    assertEquals("AppOne", result.getApplicationName());
  }

  @Test
  void testCreateWindow_InvalidDates() {
    windowDto.setStartTime(LocalDateTime.now().plusDays(2));
    windowDto.setEndTime(LocalDateTime.now().plusDays(1)); // end before start

    assertThrows(
        InvalidOperationException.class, () -> maintenanceWindowService.createWindow(windowDto));
  }

  @Test
  void testSendNotifications_Success() {
    app.getUsers().add(user);

    when(maintenanceWindowRepository.findById(window.getId())).thenReturn(Optional.of(window));

    maintenanceWindowService.sendNotifications(window.getId());

    // Verify template rendering and mail delivery
    String expectedSubject = "Maintenance for AppOne";
    String expectedBody =
        "Hi Bob User, AppOne will be down at 2026-07-20T10:00 until 2026-07-20T12:00.";
    verify(emailService, times(1)).sendMail(user.getEmail(), expectedSubject, expectedBody);
  }

  @Test
  void testSendNotifications_WithOverrides() {
    app.getUsers().add(user);
    window.setOverriddenSubject("Override Subject {title}");
    window.setOverriddenBody("Override Body for {userName}");

    when(maintenanceWindowRepository.findById(window.getId())).thenReturn(Optional.of(window));

    maintenanceWindowService.sendNotifications(window.getId());

    String expectedSubject = "Override Subject Database Upgrade";
    String expectedBody = "Override Body for Bob User";
    verify(emailService, times(1)).sendMail(user.getEmail(), expectedSubject, expectedBody);
  }
}
