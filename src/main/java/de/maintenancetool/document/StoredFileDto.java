package de.maintenancetool.document;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

/**
 * Data Transfer Object (DTO) für Dateimetadaten. Verhindert das Offenlegen interner Server-Pfade an
 * Clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoredFileDto {
  private UUID id;
  private String originalDateiname;
  private String contentType;
  private Long dateigroesse;
  private LocalDateTime uploadDatum;

  public static StoredFileDto fromEntity(StoredFile file) {
    if (file == null) return null;
    return StoredFileDto.builder()
        .id(file.getId())
        .originalDateiname(file.getOriginalDateiname())
        .contentType(file.getContentType())
        .dateigroesse(file.getDateigroesse())
        .uploadDatum(file.getUploadDatum())
        .build();
  }
}
