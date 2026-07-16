package de.maintenancetool.user;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserAccountController {

  private final UserAccountService userAccountService;

  public UserAccountController(UserAccountService userAccountService) {
    this.userAccountService = userAccountService;
  }

  @GetMapping("/me")
  public UserAccountDto getMe() {
    return UserAccountDto.fromEntity(
        userAccountService
            .getCurrentUser()
            .orElseThrow(
                () ->
                    new de.maintenancetool.error.ResourceNotFoundException(
                        "Aktueller Benutzer nicht in der Datenbank gefunden.")));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<UserAccountDto> getAllUsers() {
    return userAccountService.getAllUsers().stream()
        .map(UserAccountDto::fromEntity)
        .collect(Collectors.toList());
  }

  @PutMapping("/{id}/roles")
  @PreAuthorize("hasRole('ADMIN')")
  public UserAccountDto updateUserRoles(@PathVariable UUID id, @RequestBody Set<Rolle> roles) {
    return UserAccountDto.fromEntity(userAccountService.updateUserRoles(id, roles));
  }
}
