package de.maintenancetool.applicationuser;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationUserDto {
  private UUID id;
  private String email;
  private String name;
}
