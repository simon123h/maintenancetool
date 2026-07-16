package de.maintenancetool.user;

import java.util.Set;
import java.util.UUID;
import lombok.*;

/** Data Transfer Object (DTO) für Benutzerkonten-Daten. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountDto {
  private UUID id;
  private String mailAdresse;
  private String vollerName;
  private String loginName;
  private Set<Rolle> rollen;

  public static UserAccountDto fromEntity(UserAccount user) {
    if (user == null) return null;
    return UserAccountDto.builder()
        .id(user.getId())
        .mailAdresse(user.getMailAdresse())
        .vollerName(user.getVollerName())
        .loginName(user.getLoginName())
        .rollen(user.getRollen())
        .build();
  }
}
