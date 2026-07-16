package de.maintenancetool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Konfigurationsklasse für das Branding/Corporate Identity der Anwendung. Ermöglicht es, das Logo
 * und die Theme-Farben über die application.properties anzupassen.
 */
@Component
@ConfigurationProperties(prefix = "app.branding")
public class BrandingProperties {
  private String companyName = "Schulungsplanung";
  private String logoPath = "";
  private String themeColor = "#3b82f6";
  private String themeColorHover = "#2563eb";

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getLogoPath() {
    return logoPath;
  }

  public void setLogoPath(String logoPath) {
    this.logoPath = logoPath;
  }

  public String getThemeColor() {
    return themeColor;
  }

  public void setThemeColor(String themeColor) {
    this.themeColor = themeColor;
  }

  public String getThemeColorHover() {
    return themeColorHover;
  }

  public void setThemeColorHover(String themeColorHover) {
    this.themeColorHover = themeColorHover;
  }
}
