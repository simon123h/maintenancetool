package de.maintenancetool.maintenance;

import de.maintenancetool.application.Application;
import de.maintenancetool.application.ApplicationRepository;
import de.maintenancetool.applicationuser.ApplicationUser;
import de.maintenancetool.communication.EmailService;
import de.maintenancetool.error.InvalidOperationException;
import de.maintenancetool.error.ResourceNotFoundException;
import de.maintenancetool.template.EmailTemplate;
import de.maintenancetool.template.EmailTemplateRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceWindowService {

  private final MaintenanceWindowRepository maintenanceWindowRepository;
  private final ApplicationRepository applicationRepository;
  private final EmailTemplateRepository emailTemplateRepository;
  private final EmailService emailService;

  @Transactional(readOnly = true)
  public List<MaintenanceWindowDto> getAllWindows() {
    return maintenanceWindowRepository.findAll().stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public MaintenanceWindowDto getWindowById(UUID id) {
    MaintenanceWindow window =
        maintenanceWindowRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Maintenance window not found with id: " + id));
    return convertToDto(window);
  }

  @Transactional
  public MaintenanceWindowDto createWindow(MaintenanceWindowDto dto) {
    if (dto.getStartTime().isAfter(dto.getEndTime())) {
      throw new InvalidOperationException("Start time must be before end time");
    }

    Application app =
        applicationRepository
            .findById(dto.getApplicationId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Application not found with id: " + dto.getApplicationId()));

    EmailTemplate template = null;
    if (dto.getTemplateId() != null) {
      template =
          emailTemplateRepository
              .findById(dto.getTemplateId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Email template not found with id: " + dto.getTemplateId()));
    }

    MaintenanceWindow window =
        MaintenanceWindow.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .startTime(dto.getStartTime())
            .endTime(dto.getEndTime())
            .status(dto.getStatus() != null ? dto.getStatus() : MaintenanceStatus.PLANNED)
            .application(app)
            .template(template)
            .overriddenSubject(dto.getOverriddenSubject())
            .overriddenBody(dto.getOverriddenBody())
            .notificationLeadTimeDays(
                dto.getNotificationLeadTimeDays() != null ? dto.getNotificationLeadTimeDays() : 7)
            .emailsSent(false)
            .build();

    MaintenanceWindow saved = maintenanceWindowRepository.save(window);
    saved = checkAndSendImmediateNotification(saved);
    return convertToDto(saved);
  }

  @Transactional
  public MaintenanceWindowDto updateWindow(UUID id, MaintenanceWindowDto dto) {
    if (dto.getStartTime().isAfter(dto.getEndTime())) {
      throw new InvalidOperationException("Start time must be before end time");
    }

    MaintenanceWindow window =
        maintenanceWindowRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Maintenance window not found with id: " + id));

    Application app =
        applicationRepository
            .findById(dto.getApplicationId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Application not found with id: " + dto.getApplicationId()));

    EmailTemplate template = null;
    if (dto.getTemplateId() != null) {
      template =
          emailTemplateRepository
              .findById(dto.getTemplateId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Email template not found with id: " + dto.getTemplateId()));
    }

    int oldLeadTime =
        window.getNotificationLeadTimeDays() != null ? window.getNotificationLeadTimeDays() : 7;
    int newLeadTime =
        dto.getNotificationLeadTimeDays() != null ? dto.getNotificationLeadTimeDays() : 7;

    if (!window.getStartTime().equals(dto.getStartTime()) || oldLeadTime != newLeadTime) {
      window.setEmailsSent(false);
    }

    window.setTitle(dto.getTitle());
    window.setDescription(dto.getDescription());
    window.setStartTime(dto.getStartTime());
    window.setEndTime(dto.getEndTime());
    window.setStatus(dto.getStatus());
    window.setApplication(app);
    window.setTemplate(template);
    window.setOverriddenSubject(dto.getOverriddenSubject());
    window.setOverriddenBody(dto.getOverriddenBody());
    window.setNotificationLeadTimeDays(newLeadTime);

    MaintenanceWindow saved = maintenanceWindowRepository.save(window);
    saved = checkAndSendImmediateNotification(saved);
    return convertToDto(saved);
  }

  @Transactional
  public void deleteWindow(UUID id) {
    if (!maintenanceWindowRepository.existsById(id)) {
      throw new ResourceNotFoundException("Maintenance window not found with id: " + id);
    }
    maintenanceWindowRepository.deleteById(id);
  }

  @Transactional
  public void sendNotifications(UUID id) {
    MaintenanceWindow window =
        maintenanceWindowRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Maintenance window not found with id: " + id));

    sendNotificationsInternal(window);
  }

  private void sendNotificationsInternal(MaintenanceWindow window) {
    if (Boolean.TRUE.equals(window.getEmailsSent())) {
      log.info("Notifications already sent for window: {}, skipping.", window.getTitle());
      return;
    }

    Application app = window.getApplication();
    if (app.getUsers().isEmpty()) {
      log.info("No users mapped to application {}, marking notifications as sent.", app.getName());
      window.setEmailsSent(true);
      maintenanceWindowRepository.save(window);
      return;
    }

    // Determine subject and body to use
    String subjectPattern = "";
    String bodyPattern = "";

    if (window.getOverriddenSubject() != null && !window.getOverriddenSubject().trim().isEmpty()) {
      subjectPattern = window.getOverriddenSubject();
    } else if (window.getTemplate() != null) {
      subjectPattern = window.getTemplate().getSubjectPattern();
    } else {
      subjectPattern = "Wartungsankündigung für {appName}";
    }

    if (window.getOverriddenBody() != null && !window.getOverriddenBody().trim().isEmpty()) {
      bodyPattern = window.getOverriddenBody();
    } else if (window.getTemplate() != null) {
      bodyPattern = window.getTemplate().getBodyPattern();
    } else {
      bodyPattern =
          "Hallo {userName},\n\nfür die Anwendung {appName} ist ein Wartungsfenster geplant:\n"
              + "Start: {startTime}\n"
              + "Ende: {endTime}\n\n"
              + "Details: {description}";
    }

    for (ApplicationUser user : app.getUsers()) {
      String subject = substituteVariables(subjectPattern, user, window);
      String body = substituteVariables(bodyPattern, user, window);
      emailService.sendMail(user.getEmail(), subject, body);
    }

    window.setEmailsSent(true);
    maintenanceWindowRepository.save(window);
    log.info(
        "Triggered {} notification emails for maintenance window {}",
        app.getUsers().size(),
        window.getTitle());
  }

  private MaintenanceWindow checkAndSendImmediateNotification(MaintenanceWindow window) {
    if (Boolean.TRUE.equals(window.getEmailsSent())) {
      return window;
    }
    LocalDateTime threshold =
        LocalDateTime.now()
            .plusDays(
                window.getNotificationLeadTimeDays() != null
                    ? window.getNotificationLeadTimeDays()
                    : 7);
    if (window.getStartTime().isBefore(threshold) || window.getStartTime().isEqual(threshold)) {
      log.info(
          "Immediate email dispatch triggered for window '{}' due to proximity to start time.",
          window.getTitle());
      sendNotificationsInternal(window);
    }
    return window;
  }

  @org.springframework.scheduling.annotation.Scheduled(
      cron = "0 */15 * * * *") // Runs every 15 minutes
  @Transactional
  public void checkAndSendScheduledNotifications() {
    log.debug("Checking for scheduled maintenance window notifications...");
    List<MaintenanceWindow> unsentWindows = maintenanceWindowRepository.findByEmailsSentFalse();
    LocalDateTime now = LocalDateTime.now();
    for (MaintenanceWindow window : unsentWindows) {
      LocalDateTime threshold =
          now.plusDays(
              window.getNotificationLeadTimeDays() != null
                  ? window.getNotificationLeadTimeDays()
                  : 7);
      if (window.getStartTime().isBefore(threshold) || window.getStartTime().isEqual(threshold)) {
        log.info("Auto-sending scheduled notifications for window: {}", window.getTitle());
        sendNotificationsInternal(window);
      }
    }
  }

  private String substituteVariables(String text, ApplicationUser user, MaintenanceWindow window) {
    if (text == null) return "";
    return text.replace("{userName}", user.getName())
        .replace("{userEmail}", user.getEmail())
        .replace("{appName}", window.getApplication().getName())
        .replace("{appUrl}", window.getApplication().getUrl())
        .replace("{title}", window.getTitle())
        .replace("{description}", window.getDescription() != null ? window.getDescription() : "")
        .replace("{startTime}", window.getStartTime().toString())
        .replace("{endTime}", window.getEndTime().toString())
        .replace("{status}", window.getStatus().name());
  }

  private MaintenanceWindowDto convertToDto(MaintenanceWindow window) {
    return MaintenanceWindowDto.builder()
        .id(window.getId())
        .title(window.getTitle())
        .description(window.getDescription())
        .startTime(window.getStartTime())
        .endTime(window.getEndTime())
        .status(window.getStatus())
        .applicationId(window.getApplication().getId())
        .applicationName(window.getApplication().getName())
        .templateId(window.getTemplate() != null ? window.getTemplate().getId() : null)
        .templateName(window.getTemplate() != null ? window.getTemplate().getName() : null)
        .overriddenSubject(window.getOverriddenSubject())
        .overriddenBody(window.getOverriddenBody())
        .notificationLeadTimeDays(
            window.getNotificationLeadTimeDays() != null ? window.getNotificationLeadTimeDays() : 7)
        .emailsSent(window.getEmailsSent() != null ? window.getEmailsSent() : false)
        .build();
  }
}
