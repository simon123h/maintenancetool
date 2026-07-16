package de.maintenancetool.application;

import de.maintenancetool.applicationuser.ApplicationUserDto;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

  private final ApplicationService applicationService;

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public List<ApplicationDto> getAllApplications() {
    return applicationService.getAllApplications();
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ApplicationDto getApplicationById(@PathVariable UUID id) {
    return applicationService.getApplicationById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
  public ApplicationDto createApplication(@RequestBody ApplicationDto dto) {
    return applicationService.createApplication(dto);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
  public ApplicationDto updateApplication(@PathVariable UUID id, @RequestBody ApplicationDto dto) {
    return applicationService.updateApplication(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
  public void deleteApplication(@PathVariable UUID id) {
    applicationService.deleteApplication(id);
  }

  @GetMapping("/{id}/users")
  @PreAuthorize("isAuthenticated()")
  public Set<ApplicationUserDto> getApplicationUsers(@PathVariable UUID id) {
    return applicationService.getApplicationUsers(id);
  }

  @PostMapping("/{id}/users/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
  public void addUserToApplication(@PathVariable UUID id, @PathVariable UUID userId) {
    applicationService.addUserToApplication(id, userId);
  }

  @DeleteMapping("/{id}/users/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
  public void removeUserFromApplication(@PathVariable UUID id, @PathVariable UUID userId) {
    applicationService.removeUserFromApplication(id, userId);
  }
}
