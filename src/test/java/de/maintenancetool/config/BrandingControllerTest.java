package de.maintenancetool.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BrandingControllerTest {

  @Mock private BrandingProperties brandingProperties;

  @InjectMocks private BrandingController brandingController;

  @Test
  void testGetBranding() {
    when(brandingProperties.getCompanyName()).thenReturn("Test Company");
    when(brandingProperties.getLogoPath()).thenReturn("/assets/test-logo.svg");
    when(brandingProperties.getThemeColor()).thenReturn("#112233");
    when(brandingProperties.getThemeColorHover()).thenReturn("#445566");

    BrandingProperties result = brandingController.getBranding();

    assertEquals("Test Company", result.getCompanyName());
    assertEquals("/assets/test-logo.svg", result.getLogoPath());
    assertEquals("#112233", result.getThemeColor());
    assertEquals("#445566", result.getThemeColorHover());
  }
}
