package de.maintenancetool.config;

import de.maintenancetool.user.UserAccountService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Zentrale Sicherheitskonfiguration. Lädt bedingt entweder die LDAP- oder SAML2-Konfiguration
 * basierend auf der Eigenschaft 'app.security.auth-method' in den application.properties.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  /** LDAP Sicherheits-Konfiguration. Aktiviert, wenn 'app.security.auth-method=LDAP'. */
  @Configuration
  @ConditionalOnProperty(name = "app.security.auth-method", havingValue = "LDAP")
  public static class LdapSecurityConfig {

    private final UserAccountService userAccountService;

    public LdapSecurityConfig(UserAccountService userAccountService) {
      this.userAccountService = userAccountService;
    }

    @Bean
    public SecurityFilterChain ldapSecurityFilterChain(HttpSecurity http) throws Exception {
      http
          // Autorisierungsregeln
          .authorizeHttpRequests(
              auth ->
                  auth
                      // H2-Konsole freigeben für die Entwicklung
                      .requestMatchers("/h2-console/**")
                      .permitAll()
                      // Statische Assets & integrierte Vitepress-Hilfeseite freigeben
                      .requestMatchers(
                          "/help", "/help/**", "/assets/**", "/favicon.ico", "/api/config/branding")
                      .permitAll()
                      // Alles andere erfordert eine Anmeldung
                      .anyRequest()
                      .authenticated())
          // Bei unauthentifizierten API-Anfragen 401 Unauthorized zurückgeben statt Umleitung auf
          // /login
          .exceptionHandling(
              exceptions ->
                  exceptions.defaultAuthenticationEntryPointFor(
                      new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                      PathPatternRequestMatcher.withDefaults().matcher("/api/**")))
          // Formular-basierter LDAP Login mit unserem DB-User-Erfolgshandler
          .formLogin(
              form ->
                  form.successHandler(new DbUserAuthenticationSuccessHandler(userAccountService))
                      .permitAll())
          // Logout-Konfiguration
          .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
          // Spezielle Header für die H2-Konsole (X-Frame-Options deaktivieren)
          .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
          // CSRF aktivieren (Cookie-basiert für Single Page Applications)
          .csrf(
              csrf ->
                  csrf.ignoringRequestMatchers("/h2-console/**")
                      .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                      .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
          .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);

      return http.build();
    }

    @Bean
    public AuthenticationManager ldapAuthenticationManager(
        BaseLdapPathContextSource contextSource) {
      LdapBindAuthenticationManagerFactory factory =
          new LdapBindAuthenticationManagerFactory(contextSource);
      // Suchen nach Benutzern in der ou=users
      factory.setUserSearchBase("ou=users");
      factory.setUserSearchFilter("(uid={0})");
      return factory.createAuthenticationManager();
    }
  }

  /** SAML2 Sicherheits-Konfiguration. Aktiviert, wenn 'app.security.auth-method=SAML2'. */
  @Configuration
  @ConditionalOnProperty(name = "app.security.auth-method", havingValue = "SAML2")
  public static class Saml2SecurityConfig {

    private final UserAccountService userAccountService;

    public Saml2SecurityConfig(UserAccountService userAccountService) {
      this.userAccountService = userAccountService;
    }

    @Bean
    public SecurityFilterChain saml2SecurityFilterChain(HttpSecurity http) throws Exception {
      http.authorizeHttpRequests(
              auth ->
                  auth
                      // Statische Assets & integrierte Hilfeseite freigeben
                      .requestMatchers(
                          "/help", "/help/**", "/assets/**", "/favicon.ico", "/api/config/branding")
                      .permitAll()
                      .anyRequest()
                      .authenticated())
          // Bei unauthentifizierten API-Anfragen 401 Unauthorized zurückgeben statt Umleitung auf
          // /login
          .exceptionHandling(
              exceptions ->
                  exceptions.defaultAuthenticationEntryPointFor(
                      new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                      PathPatternRequestMatcher.withDefaults().matcher("/api/**")))
          // SAML2 Relying Party Login mit DB-User-Erfolgshandler
          .saml2Login(
              saml ->
                  saml.successHandler(new DbUserAuthenticationSuccessHandler(userAccountService)))
          // CSRF aktivieren (Cookie-basiert für Single Page Applications)
          .csrf(
              csrf ->
                  csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                      .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
          .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
          .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

      return http.build();
    }
  }

  /**
   * Filter, der das verzögerte (deferred) CSRF-Token auflöst, damit es als Cookie (XSRF-TOKEN) an
   * den Client gesendet wird.
   */
  private static final class CsrfCookieFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
      CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
      if (csrfToken != null) {
        csrfToken.getToken(); // Erzwingt die Generierung/Auflösung des Tokens
      }
      filterChain.doFilter(request, response);
    }
  }
}
