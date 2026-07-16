package de.maintenancetool.user;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Standard-Implementierung von {@link SecurityContextService}, die auf den Thread-lokalen {@link
 * SecurityContextHolder} von Spring Security zugreift.
 */
@Component
public class SpringSecurityContextService implements SecurityContextService {

  @Override
  public Optional<Authentication> getAuthentication() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
  }
}
