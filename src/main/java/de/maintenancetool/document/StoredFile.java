package de.maintenancetool.document;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

/**
 * Entity für hochgeladene Dateien (Schulungsunterlagen, Dokumente). Speichert Metadaten in der DB
 * und verweist auf die Datei auf der Festplatte des Servers.
 */
@Entity
@Table(name = "stored_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoredFile {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "original_dateiname", nullable = false)
  private String originalDateiname;

  @Column(name = "dateipfad", nullable = false, length = 512)
  private String dateipfad;

  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  @Column(name = "dateigroesse", nullable = false)
  private Long dateigroesse;

  @Column(name = "upload_datum", nullable = false)
  private LocalDateTime uploadDatum;
}
