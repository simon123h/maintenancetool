package de.maintenancetool.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguration zur Bereitstellung eines {@link Clock}-Beans für die Applikation. Ermöglicht
 * deterministische Unit-Tests für datums- und zeitabhängige Logik.
 */
@Configuration
public class DateTimeConfig {

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}
