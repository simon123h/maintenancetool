package de.maintenancetool.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Controller zum Umleiten von /help auf /help/ (mit abschließendem Schrägstrich). Dadurch kann
 * Spring Boot statische Index-Ressourcen des Vitepress-Projekts korrekt auflösen.
 */
@Controller
public class HelpRedirectController {

  @GetMapping("/help")
  public String redirectHelp() {
    return "redirect:/help/";
  }

  @GetMapping("/help/")
  public String forwardHelpIndex() {
    return "forward:/help/index.html";
  }

  @GetMapping("/help/{page:[^\\.]*}")
  public String forwardHelpPage(@PathVariable String page) {
    if (page == null || page.trim().isEmpty()) {
      return "forward:/help/index.html";
    }
    return "forward:/help/" + page + ".html";
  }
}
