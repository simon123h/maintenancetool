package de.maintenancetool.user;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service zur Verwaltung und Provisionierung von {@link UserAccount}s. Erstellt Accounts beim
 * ersten Login und teilt dem ersten Nutzer automatisch ROLE_ADMIN zu.
 */
@Service
public class UserAccountService {

  private final UserAccountRepository userAccountRepository;
  private final LdapTemplate ldapTemplate;
  private final SecurityContextService securityContextService;

  public UserAccountService(
      UserAccountRepository userAccountRepository,
      @Autowired(required = false) LdapTemplate ldapTemplate,
      SecurityContextService securityContextService) {
    this.userAccountRepository = userAccountRepository;
    this.ldapTemplate = ldapTemplate;
    this.securityContextService = securityContextService;
  }

  /**
   * Provisioniert oder aktualisiert einen Benutzeraccount basierend auf dem erfolgreichen Login.
   *
   * @param authentication das erfolgreiche Security-Authentication-Objekt
   * @return der geladene oder erstellte UserAccount
   */
  @Transactional
  public UserAccount provisionUser(Authentication authentication) {
    String loginName = authentication.getName();
    Object principal = authentication.getPrincipal();

    String mail = null;
    String name = null;

    // 1. Attribute aus SAML2 extrahieren
    if (principal
        instanceof
        org.springframework.security.saml2.provider.service.authentication
                .Saml2ResponseAssertionAccessor
            saml2Accessor) {
      mail = getFirstSamlAttribute(saml2Accessor, "mail", "email");
      name = getFirstSamlAttribute(saml2Accessor, "cn", "displayName", "name");
    }
    // 2. Attribute aus LDAP extrahieren (über LdapTemplate-Suche)
    else if (ldapTemplate != null) {
      try {
        List<LdapUserDto> ldapUsers =
            ldapTemplate.search(
                "",
                "(uid=" + loginName + ")",
                (AttributesMapper<LdapUserDto>)
                    attrs ->
                        LdapUserDto.builder()
                            .mail(
                                attrs.get("mail") != null
                                    ? attrs.get("mail").get().toString()
                                    : null)
                            .cn(attrs.get("cn") != null ? attrs.get("cn").get().toString() : null)
                            .build());
        if (!ldapUsers.isEmpty()) {
          mail = ldapUsers.get(0).getMail();
          name = ldapUsers.get(0).getCn();
        }
      } catch (Exception e) {
        // Fallback bei Verbindungsfehlen zum embedded LDAP
      }
    }

    // 3. Fallbacks für fehlende Attribute
    if (mail == null) {
      mail = loginName.contains("@") ? loginName : loginName + "@maintenancetool.de";
    }
    if (name == null) {
      name = loginName;
    }

    // 4. Prüfen ob Benutzer bereits existiert
    Optional<UserAccount> existing = userAccountRepository.findByMailAdresse(mail);
    if (existing.isPresent()) {
      UserAccount user = existing.get();
      user.setVollerName(name);
      user.setLoginName(loginName);

      // Garantieren, dass "admin" und "manager" in der Dev-Umgebung immer ihre Admin/Manager-Rollen
      // haben
      Set<Rolle> rollen = new HashSet<>(user.getRollen());
      boolean rolesChanged = false;
      if ("admin".equals(loginName)) {
        if (!rollen.contains(Rolle.ROLE_ADMIN)) {
          rollen.add(Rolle.ROLE_ADMIN);
          rolesChanged = true;
        }
        if (!rollen.contains(Rolle.ROLE_MANAGER)) {
          rollen.add(Rolle.ROLE_MANAGER);
          rolesChanged = true;
        }
      } else if ("manager".equals(loginName)) {
        if (!rollen.contains(Rolle.ROLE_MANAGER)) {
          rollen.add(Rolle.ROLE_MANAGER);
          rolesChanged = true;
        }
      }
      if (rolesChanged) {
        user.setRollen(rollen);
      }
      return userAccountRepository.save(user);
    }

    // 5. Bootstrapping: Wenn die DB leer ist, erhält der erste User Admin-Rechte
    boolean isFirstUser = userAccountRepository.count() == 0;
    Set<Rolle> rollen = new HashSet<>();
    rollen.add(Rolle.ROLE_TEILNEHMER); // Jeder hat standardmäßig ROLE_TEILNEHMER

    if (isFirstUser || "admin".equals(loginName)) {
      rollen.add(Rolle.ROLE_ADMIN);
      rollen.add(Rolle.ROLE_MANAGER);
    } else if ("manager".equals(loginName)) {
      rollen.add(Rolle.ROLE_MANAGER);
    }

    UserAccount newUser =
        UserAccount.builder()
            .mailAdresse(mail)
            .vollerName(name)
            .loginName(loginName)
            .rollen(rollen)
            .build();

    return userAccountRepository.save(newUser);
  }

  private String getFirstSamlAttribute(
      org.springframework.security.saml2.provider.service.authentication
              .Saml2ResponseAssertionAccessor
          accessor,
      String... attributeNames) {
    for (String name : attributeNames) {
      List<Object> values = accessor.getAttribute(name);
      if (values != null && !values.isEmpty()) {
        return values.get(0).toString();
      }
    }
    return null;
  }

  @Transactional(readOnly = true)
  public Optional<UserAccount> getCurrentUser() {
    Optional<Authentication> authOpt = securityContextService.getAuthentication();
    if (authOpt.isEmpty()) {
      return Optional.empty();
    }
    Authentication auth = authOpt.get();
    if (!auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
      return Optional.empty();
    }
    return userAccountRepository.findByLoginName(auth.getName());
  }

  @Transactional(readOnly = true)
  public List<UserAccount> getAllUsers() {
    return userAccountRepository.findAll();
  }

  @Transactional
  public UserAccount updateUserRoles(UUID userId, Set<Rolle> rollen) {
    UserAccount user =
        userAccountRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new de.maintenancetool.error.ResourceNotFoundException(
                        "Benutzer mit ID " + userId + " nicht gefunden."));

    long adminCount =
        userAccountRepository.findAll().stream()
            .filter(u -> u.getRollen().contains(Rolle.ROLE_ADMIN))
            .count();

    user.assignRoles(rollen, adminCount);
    return userAccountRepository.save(user);
  }

  @lombok.Value
  @lombok.Builder
  private static class LdapUserDto {
    String mail;
    String cn;
  }
}
