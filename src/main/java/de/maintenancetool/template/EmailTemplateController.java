package de.maintenancetool.template;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

  private final EmailTemplateService emailTemplateService;

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public List<EmailTemplateDto> getAllTemplates() {
    return emailTemplateService.getAllTemplates();
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public EmailTemplateDto getTemplateById(@PathVariable UUID id) {
    return emailTemplateService.getTemplateById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
  public EmailTemplateDto createTemplate(@RequestBody EmailTemplateDto dto) {
    return emailTemplateService.createTemplate(dto);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
  public EmailTemplateDto updateTemplate(@PathVariable UUID id, @RequestBody EmailTemplateDto dto) {
    return emailTemplateService.updateTemplate(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
  public void deleteTemplate(@PathVariable UUID id) {
    emailTemplateService.deleteTemplate(id);
  }
}
