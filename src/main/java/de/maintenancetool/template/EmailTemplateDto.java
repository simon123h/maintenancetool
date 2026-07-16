package de.maintenancetool.template;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplateDto {
  private UUID id;
  private String name;
  private String subjectPattern;
  private String bodyPattern;
}
