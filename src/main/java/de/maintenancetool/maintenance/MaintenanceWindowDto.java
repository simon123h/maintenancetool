package de.maintenancetool.maintenance;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceWindowDto {
  private UUID id;
  private String title;
  private String description;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private MaintenanceStatus status;
  private UUID applicationId;
  private String applicationName;
  private UUID templateId;
  private String templateName;
  private String overriddenSubject;
  private String overriddenBody;
}
