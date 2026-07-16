package de.maintenancetool.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller zum Bereitstellen der Branding-Konfiguration für das Frontend. Ermöglicht dynamische
 * Anpassungen von Logo und Primärfarben.
 */
@RestController
@RequestMapping("/api/config")
public class BrandingController {

  private final BrandingProperties brandingProperties;

  public BrandingController(BrandingProperties brandingProperties) {
    this.brandingProperties = brandingProperties;
  }

  @GetMapping("/branding")
  public BrandingProperties getBranding() {
    return brandingProperties;
  }
}
