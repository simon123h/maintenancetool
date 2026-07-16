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
@PreAuthorize("isAuthenticated()")
public class EmailTemplateController {

  private final EmailTemplateService emailTemplateService;

  @GetMapping
  public List<EmailTemplateDto> getAllTemplates() {
    return emailTemplateService.getAllTemplates();
  }

  @GetMapping("/{id}")
  public EmailTemplateDto getTemplateById(@PathVariable UUID id) {
    return emailTemplateService.getTemplateById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EmailTemplateDto createTemplate(@RequestBody EmailTemplateDto dto) {
    return emailTemplateService.createTemplate(dto);
  }

  @PutMapping("/{id}")
  public EmailTemplateDto updateTemplate(@PathVariable UUID id, @RequestBody EmailTemplateDto dto) {
    return emailTemplateService.updateTemplate(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTemplate(@PathVariable UUID id) {
    emailTemplateService.deleteTemplate(id);
  }
}
