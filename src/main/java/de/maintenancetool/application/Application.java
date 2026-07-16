package de.maintenancetool.application;

import de.maintenancetool.applicationuser.ApplicationUser;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private String url;

  private String description;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "application_user_mapping",
      joinColumns = @JoinColumn(name = "application_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  @Builder.Default
  private Set<ApplicationUser> users = new HashSet<>();
}
