package de.maintenancetool.user;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;

/**
 * Entity für die Benutzerkonten interner Mitarbeiter der Firma im System. Verwaltet
 * sAMAccountName/Mail und die lokalen DB-Rollen.
 */
@Entity
@Table(name = "user_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "mail_adresse", nullable = false, unique = true)
  private String mailAdresse;

  @Column(name = "voller_name", nullable = false)
  private String vollerName;

  @Column(name = "login_name", nullable = false)
  private String loginName;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "user_account_rollen",
      joinColumns = @JoinColumn(name = "user_account_id"))
  @Column(name = "rolle")
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Set<Rolle> rollen = new HashSet<>();

  /**
   * Weist dem Benutzer neue Rollen zu und stellt sicher, dass der letzte Administrator sich nicht
   * selbst entmachten kann.
   *
   * @param neueRollen die neue Menge an Rollen
   * @param activeAdminCount die Gesamtzahl der aktiven Administratoren in der Datenbank
   * @throws de.maintenancetool.error.InvalidOperationException wenn die letzte Admin-Rolle entzogen
   *     werden soll
   */
  public void assignRoles(Set<Rolle> neueRollen, long activeAdminCount) {
    if (!neueRollen.contains(Rolle.ROLE_ADMIN) && this.rollen.contains(Rolle.ROLE_ADMIN)) {
      if (activeAdminCount <= 1) {
        throw new de.maintenancetool.error.InvalidOperationException(
            "Der letzte Administrator kann sich nicht selbst entmachten!");
      }
    }
    this.rollen = new HashSet<>(neueRollen);
  }
}
