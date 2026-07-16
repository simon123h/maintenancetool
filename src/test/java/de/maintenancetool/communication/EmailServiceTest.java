package de.maintenancetool.communication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock private JavaMailSender mailSender;

  private EmailService emailService;

  @BeforeEach
  void setUp() {
    emailService = new EmailService(mailSender);
  }

  @Test
  void testSendMail_Success() {
    emailService.sendMail("test@example.com", "Test Subject", "Test Body");

    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender, times(1)).send(messageCaptor.capture());

    SimpleMailMessage sentMessage = messageCaptor.getValue();
    assertEquals("test@example.com", sentMessage.getTo()[0]);
    assertEquals("Test Subject", sentMessage.getSubject());
    assertEquals("Test Body", sentMessage.getText());
    assertEquals("maintenance@meinefirma.de", sentMessage.getFrom());
  }

  @Test
  void testSendMail_NoMailSender() {
    EmailService serviceWithNoSender = new EmailService(null);
    assertDoesNotThrow(() -> serviceWithNoSender.sendMail("test@example.com", "Subject", "Body"));
  }

  @Test
  void testSendMail_ExceptionHandling() {
    doThrow(new RuntimeException("SMTP Server offline"))
        .when(mailSender)
        .send(any(SimpleMailMessage.class));

    assertDoesNotThrow(() -> emailService.sendMail("test@example.com", "Subject", "Body"));
  }
}
