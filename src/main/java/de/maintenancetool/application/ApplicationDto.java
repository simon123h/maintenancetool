package de.maintenancetool.application;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDto {
  private UUID id;
  private String name;
  private String url;
  private String description;
}
