package de.maintenancetool.template;

import de.maintenancetool.error.InvalidOperationException;
import de.maintenancetool.error.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

  private final EmailTemplateRepository emailTemplateRepository;

  @Transactional(readOnly = true)
  public List<EmailTemplateDto> getAllTemplates() {
    return emailTemplateRepository.findAll().stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public EmailTemplateDto getTemplateById(UUID id) {
    EmailTemplate temp = emailTemplateRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Email template not found with id: " + id));
    return convertToDto(temp);
  }

  @Transactional
  public EmailTemplateDto createTemplate(EmailTemplateDto dto) {
    if (emailTemplateRepository.findByName(dto.getName()).isPresent()) {
      throw new InvalidOperationException("Email template name already exists: " + dto.getName());
    }
    EmailTemplate temp = EmailTemplate.builder()
        .name(dto.getName())
        .subjectPattern(dto.getSubjectPattern())
        .bodyPattern(dto.getBodyPattern())
        .build();
    EmailTemplate saved = emailTemplateRepository.save(temp);
    return convertToDto(saved);
  }

  @Transactional
  public EmailTemplateDto updateTemplate(UUID id, EmailTemplateDto dto) {
    EmailTemplate temp = emailTemplateRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Email template not found with id: " + id));

    emailTemplateRepository.findByName(dto.getName())
        .ifPresent(existing -> {
          if (!existing.getId().equals(id)) {
            throw new InvalidOperationException("Email template name already exists: " + dto.getName());
          }
        });

    temp.setName(dto.getName());
    temp.setSubjectPattern(dto.getSubjectPattern());
    temp.setBodyPattern(dto.getBodyPattern());
    EmailTemplate saved = emailTemplateRepository.save(temp);
    return convertToDto(saved);
  }

  @Transactional
  public void deleteTemplate(UUID id) {
    if (!emailTemplateRepository.existsById(id)) {
      throw new ResourceNotFoundException("Email template not found with id: " + id);
    }
    emailTemplateRepository.deleteById(id);
  }

  private EmailTemplateDto convertToDto(EmailTemplate temp) {
    return EmailTemplateDto.builder()
        .id(temp.getId())
        .name(temp.getName())
        .subjectPattern(temp.getSubjectPattern())
        .bodyPattern(temp.getBodyPattern())
        .build();
  }
}
