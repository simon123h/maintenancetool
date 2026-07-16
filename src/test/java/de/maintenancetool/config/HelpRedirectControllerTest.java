package de.maintenancetool.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HelpRedirectControllerTest {

  @Test
  void testRedirectHelp() {
    HelpRedirectController controller = new HelpRedirectController();
    String result = controller.redirectHelp();
    assertEquals("redirect:/help/", result);
  }

  @Test
  void testForwardHelpIndex() {
    HelpRedirectController controller = new HelpRedirectController();
    String result = controller.forwardHelpIndex();
    assertEquals("forward:/help/index.html", result);
  }

  @Test
  void testForwardHelpPage() {
    HelpRedirectController controller = new HelpRedirectController();

    // Test standard page
    String result = controller.forwardHelpPage("admin");
    assertEquals("forward:/help/admin.html", result);

    // Test null/empty page falls back to index.html
    String resultNull = controller.forwardHelpPage(null);
    assertEquals("forward:/help/index.html", resultNull);

    String resultEmpty = controller.forwardHelpPage(" ");
    assertEquals("forward:/help/index.html", resultEmpty);
  }
}
