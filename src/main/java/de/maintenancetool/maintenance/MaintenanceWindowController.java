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
@PreAuthorize("isAuthenticated()")
public class MaintenanceWindowController {

  private final MaintenanceWindowService maintenanceWindowService;

  @GetMapping
  public List<MaintenanceWindowDto> getAllWindows() {
    return maintenanceWindowService.getAllWindows();
  }

  @GetMapping("/{id}")
  public MaintenanceWindowDto getWindowById(@PathVariable UUID id) {
    return maintenanceWindowService.getWindowById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MaintenanceWindowDto createWindow(@RequestBody MaintenanceWindowDto dto) {
    return maintenanceWindowService.createWindow(dto);
  }

  @PutMapping("/{id}")
  public MaintenanceWindowDto updateWindow(
      @PathVariable UUID id, @RequestBody MaintenanceWindowDto dto) {
    return maintenanceWindowService.updateWindow(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteWindow(@PathVariable UUID id) {
    maintenanceWindowService.deleteWindow(id);
  }

  @PostMapping("/{id}/notify")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void sendNotifications(@PathVariable UUID id) {
    maintenanceWindowService.sendNotifications(id);
  }
}
