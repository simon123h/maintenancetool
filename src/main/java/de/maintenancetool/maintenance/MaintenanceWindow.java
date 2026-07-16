package de.maintenancetool.maintenance;

import de.maintenancetool.application.Application;
import de.maintenancetool.template.EmailTemplate;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "maintenance_window")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceWindow {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String title;

  private String description;

  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalDateTime endTime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MaintenanceStatus status;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "application_id", nullable = false)
  private Application application;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_id")
  private EmailTemplate template;

  @Column(name = "overridden_subject")
  private String overriddenSubject;

  @Lob
  @Column(name = "overridden_body")
  private String overriddenBody;
}
