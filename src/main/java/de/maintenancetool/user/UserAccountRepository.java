package de.maintenancetool.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA Repository für {@link UserAccount}. */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

  /**
   * Findet einen Benutzer anhand seiner E-Mail-Adresse.
   *
   * @param mailAdresse E-Mail-Adresse des Benutzers
   * @return Optional des gefundenen Benutzers
   */
  Optional<UserAccount> findByMailAdresse(String mailAdresse);

  /**
   * Findet einen Benutzer anhand seines Login-Namens.
   *
   * @param loginName Login-Name des Benutzers (sAMAccountName/E-Mail)
   * @return Optional des gefundenen Benutzers
   */
  Optional<UserAccount> findByLoginName(String loginName);
}
