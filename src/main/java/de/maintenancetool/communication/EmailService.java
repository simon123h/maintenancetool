package de.maintenancetool.communication;

import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Async
public class EmailService {

  private final JavaMailSender mailSender;

  public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendMail(String to, String subject, String body) {
    log.info("Sende E-Mail an {} | Betreff: {}", to, subject);
    if (mailSender == null) {
      log.info("Mail-Sender ist nicht konfiguriert (Mock-Modus). Nachricht:\n{}", body);
      return;
    }
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(to);
      message.setSubject(subject);
      message.setText(body);
      message.setFrom("maintenance@meinefirma.de");
      mailSender.send(message);
      log.info("E-Mail erfolgreich versendet an {}", to);
    } catch (Exception e) {
      log.error("Fehler beim E-Mail-Versand an {}: {}", to, e.getMessage());
    }
  }

  public void sendMailWithCalendar(
      String to, String subject, String body, String icsContent, String method) {
    log.info("Sende E-Mail mit Kalendereinladung ({}) an {} | Betreff: {}", method, to, subject);
    if (mailSender == null) {
      log.info(
          "Mail-Sender ist nicht konfiguriert (Mock-Modus). Kalendereinladung:\n{}\nNachricht:\n{}",
          icsContent,
          body);
      return;
    }
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();

      // Set the Content-Class header for Outlook compatibility
      mimeMessage.addHeader("Content-Class", "urn:content-classes:calendarmessage");

      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom("maintenance@meinefirma.de");

      // Create alternative parts for standard text and text/calendar content
      MimeMultipart multipart = new MimeMultipart("alternative");

      // Text part
      MimeBodyPart textPart = new MimeBodyPart();
      textPart.setText(body, "UTF-8");
      multipart.addBodyPart(textPart);

      // Calendar part
      MimeBodyPart calendarPart = new MimeBodyPart();
      calendarPart.setContent(icsContent, "text/plain; charset=UTF-8");
      calendarPart.setHeader("Content-Type", "text/calendar; charset=UTF-8; method=" + method);
      multipart.addBodyPart(calendarPart);

      mimeMessage.setContent(multipart);

      mailSender.send(mimeMessage);
      log.info("E-Mail mit Kalendereinladung erfolgreich versendet an {}", to);
    } catch (Exception e) {
      log.error("Fehler beim E-Mail-Versand mit Kalendereinladung an {}: {}", to, e.getMessage());
    }
  }
}

