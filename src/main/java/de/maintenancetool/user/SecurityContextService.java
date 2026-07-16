package de.maintenancetool.user;

import java.util.Optional;
import org.springframework.security.core.Authentication;

/**
 * Interface zur Abstraktion von Spring Securitys statischem SecurityContextHolder. Dies ermöglicht
 * das saubere Unit-Testing von Services, die den aktuell angemeldeten Benutzer ermitteln müssen.
 */
public interface SecurityContextService {
  Optional<Authentication> getAuthentication();
}
