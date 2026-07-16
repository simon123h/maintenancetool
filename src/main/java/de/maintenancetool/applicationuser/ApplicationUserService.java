package de.maintenancetool.applicationuser;

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
public class ApplicationUserService {

  private final ApplicationUserRepository applicationUserRepository;

  @Transactional(readOnly = true)
  public List<ApplicationUserDto> getAllUsers() {
    return applicationUserRepository.findAll().stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public ApplicationUserDto getUserById(UUID id) {
    ApplicationUser user =
        applicationUserRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Application user not found with id: " + id));
    return convertToDto(user);
  }

  @Transactional
  public ApplicationUserDto createUser(ApplicationUserDto dto) {
    if (applicationUserRepository.findByEmail(dto.getEmail()).isPresent()) {
      throw new InvalidOperationException(
          "Application user with email already exists: " + dto.getEmail());
    }
    ApplicationUser user =
        ApplicationUser.builder().email(dto.getEmail()).name(dto.getName()).build();
    ApplicationUser saved = applicationUserRepository.save(user);
    return convertToDto(saved);
  }

  @Transactional
  public ApplicationUserDto updateUser(UUID id, ApplicationUserDto dto) {
    ApplicationUser user =
        applicationUserRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Application user not found with id: " + id));

    applicationUserRepository
        .findByEmail(dto.getEmail())
        .ifPresent(
            existing -> {
              if (!existing.getId().equals(id)) {
                throw new InvalidOperationException(
                    "Application user with email already exists: " + dto.getEmail());
              }
            });

    user.setEmail(dto.getEmail());
    user.setName(dto.getName());
    ApplicationUser saved = applicationUserRepository.save(user);
    return convertToDto(saved);
  }

  @Transactional
  public void deleteUser(UUID id) {
    if (!applicationUserRepository.existsById(id)) {
      throw new ResourceNotFoundException("Application user not found with id: " + id);
    }
    applicationUserRepository.deleteById(id);
  }

  private ApplicationUserDto convertToDto(ApplicationUser user) {
    return ApplicationUserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .build();
  }
}
