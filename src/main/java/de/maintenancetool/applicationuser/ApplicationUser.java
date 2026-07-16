package de.maintenancetool.applicationuser;

import de.maintenancetool.application.Application;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "application_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationUser {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String name;

  @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
  @Builder.Default
  private Set<Application> applications = new HashSet<>();
}
