package de.maintenancetool.document;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.maintenancetool.error.InvalidOperationException;
import de.maintenancetool.error.ResourceNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class StoredFileServiceTest {

  @Mock private StoredFileRepository repository;

  private StoredFileService fileService;

  @TempDir Path tempDir;

  private StoredFile testFile;
  private UUID testFileId;

  @BeforeEach
  void setUp() {
    fileService = new StoredFileService(repository, tempDir.toString());
    testFileId = UUID.randomUUID();
    testFile =
        StoredFile.builder()
            .id(testFileId)
            .originalDateiname("test.txt")
            .dateipfad(tempDir.resolve("test.txt").toString())
            .contentType("text/plain")
            .dateigroesse(12L)
            .build();
  }

  @Test
  void testGetFileMetadata_Success() {
    when(repository.findById(testFileId)).thenReturn(Optional.of(testFile));

    StoredFile result = fileService.getFileMetadata(testFileId);

    assertNotNull(result);
    assertEquals("test.txt", result.getOriginalDateiname());
  }

  @Test
  void testGetFileMetadata_NotFound() {
    when(repository.findById(testFileId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> fileService.getFileMetadata(testFileId));
  }

  @Test
  void testStoreFile_Success() throws IOException {
    MultipartFile mockMultipartFile = mock(MultipartFile.class);
    when(mockMultipartFile.isEmpty()).thenReturn(false);
    when(mockMultipartFile.getOriginalFilename()).thenReturn("hello.txt");
    when(mockMultipartFile.getInputStream())
        .thenReturn(new ByteArrayInputStream("hello world".getBytes()));
    when(mockMultipartFile.getSize()).thenReturn(11L);
    when(mockMultipartFile.getContentType()).thenReturn("text/plain");

    when(repository.save(any(StoredFile.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    StoredFile result = fileService.storeFile(mockMultipartFile);

    assertNotNull(result);
    assertEquals("hello.txt", result.getOriginalDateiname());
    assertEquals(11L, result.getDateigroesse());
    assertTrue(Files.exists(Path.of(result.getDateipfad())));
    assertEquals("hello world", Files.readString(Path.of(result.getDateipfad())));
  }

  @Test
  void testStoreFile_Empty() {
    MultipartFile mockMultipartFile = mock(MultipartFile.class);
    when(mockMultipartFile.isEmpty()).thenReturn(true);

    assertThrows(InvalidOperationException.class, () -> fileService.storeFile(mockMultipartFile));
  }

  @Test
  void testLoadFileContent_Success() throws IOException {
    Path filePath = tempDir.resolve("test-load.txt");
    Files.writeString(filePath, "hello load");
    testFile.setDateipfad(filePath.toString());

    when(repository.findById(testFileId)).thenReturn(Optional.of(testFile));

    byte[] content = fileService.loadFileContent(testFileId);

    assertEquals("hello load", new String(content));
  }

  @Test
  void testLoadFileContent_FileNotFound() {
    testFile.setDateipfad(tempDir.resolve("nonexistent.txt").toString());

    when(repository.findById(testFileId)).thenReturn(Optional.of(testFile));

    assertThrows(ResourceNotFoundException.class, () -> fileService.loadFileContent(testFileId));
  }

  @Test
  void testDeleteFile_Success() throws IOException {
    Path filePath = tempDir.resolve("test-delete.txt");
    Files.writeString(filePath, "delete me");
    testFile.setDateipfad(filePath.toString());

    when(repository.findById(testFileId)).thenReturn(Optional.of(testFile));
    doNothing().when(repository).delete(testFile);

    fileService.deleteFile(testFileId);

    assertFalse(Files.exists(filePath));
    verify(repository, times(1)).delete(testFile);
  }

  @Test
  void testStoreFile_ByteArray_Success() throws IOException {
    byte[] content = "byte content".getBytes();
    when(repository.save(any(StoredFile.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    StoredFile result = fileService.storeFile(content, "bytes.bin", "application/octet-stream");

    assertNotNull(result);
    assertEquals("bytes.bin", result.getOriginalDateiname());
    assertEquals((long) content.length, result.getDateigroesse());
    assertTrue(Files.exists(Path.of(result.getDateipfad())));
    assertEquals("byte content", Files.readString(Path.of(result.getDateipfad())));
  }

  @Test
  void testStoreFile_ByteArray_NullOrEmpty() {
    assertThrows(
        InvalidOperationException.class,
        () -> fileService.storeFile(null, "bytes.bin", "application/octet-stream"));
    assertThrows(
        InvalidOperationException.class,
        () -> fileService.storeFile(new byte[0], "bytes.bin", "application/octet-stream"));
  }

  @Test
  void testStoreFile_MultipartFile_FilenameNull() throws IOException {
    MultipartFile mockMultipartFile = mock(MultipartFile.class);
    when(mockMultipartFile.isEmpty()).thenReturn(false);
    when(mockMultipartFile.getOriginalFilename()).thenReturn(null);
    when(mockMultipartFile.getInputStream())
        .thenReturn(new ByteArrayInputStream("no name".getBytes()));
    when(mockMultipartFile.getSize()).thenReturn(7L);
    when(mockMultipartFile.getContentType()).thenReturn(null);

    when(repository.save(any(StoredFile.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    StoredFile result = fileService.storeFile(mockMultipartFile);

    assertNotNull(result);
    assertEquals("unbekannt", result.getOriginalDateiname());
    assertEquals("application/octet-stream", result.getContentType());
  }

  @Test
  void testStoreFile_MultipartFile_IoException() throws IOException {
    MultipartFile mockMultipartFile = mock(MultipartFile.class);
    when(mockMultipartFile.isEmpty()).thenReturn(false);
    when(mockMultipartFile.getOriginalFilename()).thenReturn("error.txt");
    when(mockMultipartFile.getInputStream()).thenThrow(new IOException("Simulated disk error"));

    assertThrows(InvalidOperationException.class, () -> fileService.storeFile(mockMultipartFile));
  }

  @Test
  void testStoreFile_ByteArray_IoException() throws IOException {
    Path writeProtectedDir = tempDir.resolve("readonly_dir");
    Files.createDirectories(writeProtectedDir);

    StoredFileService badService = new StoredFileService(repository, writeProtectedDir.toString());

    writeProtectedDir.toFile().setWritable(false);
    try {
      assertThrows(
          InvalidOperationException.class,
          () -> badService.storeFile("content".getBytes(), "test.txt", "text/plain"));
    } finally {
      writeProtectedDir.toFile().setWritable(true);
    }
  }

  @Test
  void testConstructor_DirectoryCreationFailure() {
    Path existingFile = tempDir.resolve("already_exists.txt");
    try {
      Files.writeString(existingFile, "content");
    } catch (IOException ignored) {
    }

    assertThrows(
        InvalidOperationException.class,
        () -> new StoredFileService(repository, existingFile.resolve("child").toString()));
  }

  @Test
  void testDeleteFile_IOExceptionIgnored() throws IOException {
    Path dirPath = tempDir.resolve("dir_to_delete");
    Files.createDirectories(dirPath);
    Files.writeString(dirPath.resolve("file.txt"), "content");

    testFile.setDateipfad(dirPath.toString());
    when(repository.findById(testFileId)).thenReturn(Optional.of(testFile));
    doNothing().when(repository).delete(testFile);

    assertDoesNotThrow(() -> fileService.deleteFile(testFileId));
    verify(repository, times(1)).delete(testFile);
  }
}
