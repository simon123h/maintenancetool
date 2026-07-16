package de.maintenancetool.document;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** REST Controller für das Hochladen und Herunterladen von Dateien (Dokumenten, Unterlagen). */
@RestController
@RequestMapping("/api/documents")
public class StoredFileController {

  @Autowired private StoredFileService fileService;

  @PostMapping("/upload")
  @PreAuthorize("hasRole('ADMIN')")
  public StoredFileDto uploadFile(@RequestParam("file") MultipartFile file) {
    return StoredFileDto.fromEntity(fileService.storeFile(file));
  }

  @GetMapping("/download/{id}")
  public ResponseEntity<byte[]> downloadFile(@PathVariable UUID id) {
    StoredFile fileMeta = fileService.getFileMetadata(id);
    byte[] content = fileService.loadFileContent(id);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(fileMeta.getContentType()))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + fileMeta.getOriginalDateiname() + "\"")
        .body(content);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteFile(@PathVariable UUID id) {
    fileService.deleteFile(id);
  }
}
