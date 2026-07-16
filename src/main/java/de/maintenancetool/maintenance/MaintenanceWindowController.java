package de.maintenancetool.maintenance;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenance-windows")
@RequiredArgsConstructor
public class MaintenanceWindowController {

  private final MaintenanceWindowService maintenanceWindowService;

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public List<MaintenanceWindowDto> getAllWindows() {
    return maintenanceWindowService.getAllWindows();
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public MaintenanceWindowDto getWindowById(@PathVariable UUID id) {
    return maintenanceWindowService.getWindowById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
  public MaintenanceWindowDto createWindow(@RequestBody MaintenanceWindowDto dto) {
    return maintenanceWindowService.createWindow(dto);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
  public MaintenanceWindowDto updateWindow(
      @PathVariable UUID id, @RequestBody MaintenanceWindowDto dto) {
    return maintenanceWindowService.updateWindow(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
  public void deleteWindow(@PathVariable UUID id) {
    maintenanceWindowService.deleteWindow(id);
  }

  @PostMapping("/{id}/notify")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
  public void sendNotifications(@PathVariable UUID id) {
    maintenanceWindowService.sendNotifications(id);
  }
}
