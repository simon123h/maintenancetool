package de.maintenancetool.applicationuser;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/application-users")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ApplicationUserController {

  private final ApplicationUserService applicationUserService;

  @GetMapping
  public List<ApplicationUserDto> getAllUsers() {
    return applicationUserService.getAllUsers();
  }

  @GetMapping("/{id}")
  public ApplicationUserDto getUserById(@PathVariable UUID id) {
    return applicationUserService.getUserById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApplicationUserDto createUser(@RequestBody ApplicationUserDto dto) {
    return applicationUserService.createUser(dto);
  }

  @PutMapping("/{id}")
  public ApplicationUserDto updateUser(@PathVariable UUID id, @RequestBody ApplicationUserDto dto) {
    return applicationUserService.updateUser(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable UUID id) {
    applicationUserService.deleteUser(id);
  }
}
