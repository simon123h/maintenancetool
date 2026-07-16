package de.maintenancetool.document;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class StoredFileControllerTest {

  @Mock private StoredFileService fileService;

  @InjectMocks private StoredFileController controller;

  private UUID testFileId;
  private StoredFile testFile;

  @BeforeEach
  void setUp() {
    testFileId = UUID.randomUUID();
    testFile =
        StoredFile.builder()
            .id(testFileId)
            .originalDateiname("document.pdf")
            .dateipfad("/tmp/document.pdf")
            .contentType("application/pdf")
            .dateigroesse(100L)
            .uploadDatum(LocalDateTime.now())
            .build();
  }

  @Test
  void testUploadFile() {
    MultipartFile mockFile = mock(MultipartFile.class);
    when(fileService.storeFile(mockFile)).thenReturn(testFile);

    StoredFileDto result = controller.uploadFile(mockFile);

    assertNotNull(result);
    assertEquals(testFileId, result.getId());
    assertEquals("document.pdf", result.getOriginalDateiname());
    assertEquals("application/pdf", result.getContentType());
    assertEquals(100L, result.getDateigroesse());
  }

  @Test
  void testDownloadFile() {
    byte[] content = "file-content-bytes".getBytes();
    when(fileService.getFileMetadata(testFileId)).thenReturn(testFile);
    when(fileService.loadFileContent(testFileId)).thenReturn(content);

    ResponseEntity<byte[]> response = controller.downloadFile(testFileId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("application/pdf", response.getHeaders().getContentType().toString());
    assertEquals(
        "attachment; filename=\"document.pdf\"",
        response.getHeaders().getFirst("Content-Disposition"));
    assertArrayEquals(content, response.getBody());
  }

  @Test
  void testDeleteFile() {
    doNothing().when(fileService).deleteFile(testFileId);

    controller.deleteFile(testFileId);

    verify(fileService, times(1)).deleteFile(testFileId);
  }

  @Test
  void testDtoNullHandling() {
    assertNull(StoredFileDto.fromEntity(null));
  }
}
