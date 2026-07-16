package de.maintenancetool;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.ldap.embedded.port=0")
@ActiveProfiles("dev")
class MaintenanceApplicationTests {

  @Test
  void contextLoads() {
    // Überprüft, ob der Spring-ApplicationContext erfolgreich startet
  }
}
