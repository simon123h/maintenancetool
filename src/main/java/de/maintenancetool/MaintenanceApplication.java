package de.maintenancetool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Hauptklasse der Maintenance-Tool-Anwendung. Erbt von {@link SpringBootServletInitializer}, um
 * das Packen und Ausführen als traditionelle WAR-Datei zu unterstützen.
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MaintenanceApplication extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(MaintenanceApplication.class, args);
  }
}
