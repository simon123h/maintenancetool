package de.maintenancetool.template;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "email_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(name = "subject_pattern", nullable = false)
  private String subjectPattern;

  @Lob
  @Column(name = "body_pattern", nullable = false)
  private String bodyPattern;
}
