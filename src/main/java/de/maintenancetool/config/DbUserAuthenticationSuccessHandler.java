package de.maintenancetool.config;

import de.maintenancetool.user.UserAccount;
import de.maintenancetool.user.UserAccountService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * Erfolgreicher Login-Handler. Provisioniert den Benutzer in der DB und überschreibt die Spring
 * Security-Rollen mit den lokal in der Datenbank gepflegten Rechten (ROLE_ADMIN, ROLE_MANAGER
 * etc.).
 */
public class DbUserAuthenticationSuccessHandler
    extends SavedRequestAwareAuthenticationSuccessHandler {

  private final UserAccountService userAccountService;

  public DbUserAuthenticationSuccessHandler(UserAccountService userAccountService) {
    this.userAccountService = userAccountService;
    // Standardmäßig leiten wir nach erfolgreichem Login auf die Hauptseite / um
    setDefaultTargetUrl("/");
    setAlwaysUseDefaultTargetUrl(true);
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    // 1. Benutzer provisionieren (anlegen falls neu, aktualisieren falls vorhanden)
    UserAccount user = userAccountService.provisionUser(authentication);

    // 2. Lokale DB-Rollen in Spring Security Authorities konvertieren
    List<SimpleGrantedAuthority> authorities =
        user.getRollen().stream()
            .map(rolle -> new SimpleGrantedAuthority(rolle.name()))
            .collect(Collectors.toList());

    // 3. SecurityContext aktualisieren mit den neuen lokalen Rechten
    UsernamePasswordAuthenticationToken updatedAuth =
        new UsernamePasswordAuthenticationToken(
            authentication.getPrincipal(), authentication.getCredentials(), authorities);
    SecurityContextHolder.getContext().setAuthentication(updatedAuth);

    // 4. Standard-Weiterleitung fortführen
    super.onAuthenticationSuccess(request, response, updatedAuth);
  }
}
