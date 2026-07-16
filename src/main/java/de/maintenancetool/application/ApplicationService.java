package de.maintenancetool.application;

import de.maintenancetool.applicationuser.ApplicationUser;
import de.maintenancetool.applicationuser.ApplicationUserDto;
import de.maintenancetool.applicationuser.ApplicationUserRepository;
import de.maintenancetool.error.InvalidOperationException;
import de.maintenancetool.error.ResourceNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationService {

  private final ApplicationRepository applicationRepository;
  private final ApplicationUserRepository applicationUserRepository;

  @Transactional(readOnly = true)
  public List<ApplicationDto> getAllApplications() {
    return applicationRepository.findAll().stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public ApplicationDto getApplicationById(UUID id) {
    Application app = applicationRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    return convertToDto(app);
  }

  @Transactional
  public ApplicationDto createApplication(ApplicationDto dto) {
    if (applicationRepository.findByName(dto.getName()).isPresent()) {
      throw new InvalidOperationException("Application name already exists: " + dto.getName());
    }
    Application app = Application.builder()
        .name(dto.getName())
        .url(dto.getUrl())
        .description(dto.getDescription())
        .build();
    Application saved = applicationRepository.save(app);
    return convertToDto(saved);
  }

  @Transactional
  public ApplicationDto updateApplication(UUID id, ApplicationDto dto) {
    Application app = applicationRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    
    applicationRepository.findByName(dto.getName())
        .ifPresent(existing -> {
          if (!existing.getId().equals(id)) {
            throw new InvalidOperationException("Application name already exists: " + dto.getName());
          }
        });

    app.setName(dto.getName());
    app.setUrl(dto.getUrl());
    app.setDescription(dto.getDescription());
    Application saved = applicationRepository.save(app);
    return convertToDto(saved);
  }

  @Transactional
  public void deleteApplication(UUID id) {
    if (!applicationRepository.existsById(id)) {
      throw new ResourceNotFoundException("Application not found with id: " + id);
    }
    applicationRepository.deleteById(id);
  }

  @Transactional
  public void addUserToApplication(UUID appId, UUID userId) {
    Application app = applicationRepository.findById(appId)
        .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + appId));
    ApplicationUser user = applicationUserRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Application user not found with id: " + userId));
    app.getUsers().add(user);
    applicationRepository.save(app);
  }

  @Transactional
  public void removeUserFromApplication(UUID appId, UUID userId) {
    Application app = applicationRepository.findById(appId)
        .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + appId));
    ApplicationUser user = applicationUserRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Application user not found with id: " + userId));
    app.getUsers().remove(user);
    applicationRepository.save(app);
  }

  @Transactional(readOnly = true)
  public Set<ApplicationUserDto> getApplicationUsers(UUID appId) {
    Application app = applicationRepository.findById(appId)
        .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + appId));
    return app.getUsers().stream()
        .map(user -> ApplicationUserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .build())
        .collect(Collectors.toSet());
  }

  private ApplicationDto convertToDto(Application app) {
    return ApplicationDto.builder()
        .id(app.getId())
        .name(app.getName())
        .url(app.getUrl())
        .description(app.getDescription())
        .build();
  }
}
