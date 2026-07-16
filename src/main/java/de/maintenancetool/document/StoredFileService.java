package de.maintenancetool.document;

import de.maintenancetool.error.InvalidOperationException;
import de.maintenancetool.error.ResourceNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Service für die Dateiverwaltung (Upload, Download und Löschen von Schulungsunterlagen). */
@Service
public class StoredFileService {

  private final StoredFileRepository repository;
  private final Path storageLocation;

  public StoredFileService(
      StoredFileRepository repository, @Value("${app.file-storage.path}") String storagePath) {
    this.repository = repository;
    this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.storageLocation);
    } catch (IOException e) {
      throw new InvalidOperationException(
          "Konnte das Upload-Verzeichnis nicht erstellen: " + storagePath, e);
    }
  }

  @Transactional
  public StoredFile storeFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new InvalidOperationException("Datei ist leer und kann nicht hochgeladen werden.");
    }

    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null) {
      originalFilename = "unbekannt";
    }

    // UUID-basierter sicherer Speichername, um Namenskollisionen und Path-Traversal zu verhindern
    String storedFilename =
        UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
    Path targetPath = this.storageLocation.resolve(storedFilename);

    try {
      Files.copy(file.getInputStream(), targetPath);
    } catch (IOException e) {
      throw new InvalidOperationException(
          "Fehler beim Speichern der Datei: " + originalFilename, e);
    }

    StoredFile storedFile =
        StoredFile.builder()
            .originalDateiname(originalFilename)
            .dateipfad(targetPath.toString())
            .contentType(
                file.getContentType() != null ? file.getContentType() : "application/octet-stream")
            .dateigroesse(file.getSize())
            .uploadDatum(LocalDateTime.now())
            .build();

    return repository.save(storedFile);
  }

  @Transactional
  public StoredFile storeFile(byte[] content, String originalFilename, String contentType) {
    if (content == null || content.length == 0) {
      throw new InvalidOperationException(
          "Datei-Inhalt ist leer und kann nicht gespeichert werden.");
    }

    // UUID-basierter sicherer Speichername, um Namenskollisionen und Path-Traversal zu verhindern
    String storedFilename =
        UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
    Path targetPath = this.storageLocation.resolve(storedFilename);

    try {
      Files.write(targetPath, content);
    } catch (IOException e) {
      throw new InvalidOperationException(
          "Fehler beim Speichern der Datei: " + originalFilename, e);
    }

    StoredFile storedFile =
        StoredFile.builder()
            .originalDateiname(originalFilename)
            .dateipfad(targetPath.toString())
            .contentType(contentType != null ? contentType : "application/octet-stream")
            .dateigroesse((long) content.length)
            .uploadDatum(LocalDateTime.now())
            .build();

    return repository.save(storedFile);
  }

  @Transactional(readOnly = true)
  public StoredFile getFileMetadata(UUID id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Datei mit ID " + id + " nicht gefunden."));
  }

  @Transactional(readOnly = true)
  public byte[] loadFileContent(UUID id) {
    StoredFile storedFile = getFileMetadata(id);
    Path filePath = Paths.get(storedFile.getDateipfad());
    try {
      return Files.readAllBytes(filePath);
    } catch (IOException e) {
      throw new ResourceNotFoundException("Datei-Inhalt konnte nicht geladen werden für ID " + id);
    }
  }

  @Transactional
  public void deleteFile(UUID id) {
    StoredFile storedFile = getFileMetadata(id);
    Path filePath = Paths.get(storedFile.getDateipfad());
    try {
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      // Ignorieren und im Log vermerken
    }
    repository.delete(storedFile);
  }
}
