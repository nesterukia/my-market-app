package com.github.nesterukia.mymarket.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.*;
import org.springframework.security.web.server.header.ClearSiteDataServerHttpHeadersWriter;

import java.net.URI;
import java.util.Map;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {

    @Value("${KC_INNER_REALM_URI}")
    private String innerRealmUri;

    @Value("${KC_OUTER_REALM_URI}")
    private String outerRealmUri;

    @Value("${server.port}")
    private Long serverPort;

    private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    public SecurityConfig(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/login",
                                "/logout",
                                "/",
                                "/items",
                                "/items/**",
                                "/static/**",
                                "/images/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oAuth2LoginSpec -> oAuth2LoginSpec
                        .authorizationRequestResolver(keycloakPublicResolver())
                        .authenticationSuccessHandler(successHandler())
                )
                .oauth2Client(withDefaults())
                .logout((logout) -> logout
                        .logoutHandler(logoutHandler())
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                );

        return http.build();
    }

    private ServerOAuth2AuthorizationRequestResolver keycloakPublicResolver() {
        DefaultServerOAuth2AuthorizationRequestResolver resolver = new DefaultServerOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository
        );

        resolver.setAuthorizationRequestCustomizer(request -> request
                .additionalParameters(
                        Map.of(
                        "redirect_uri",
                        "http://localhost:%d/login/oauth2/code/keycloak-login".formatted(serverPort)
                        )
                )
        );

        return resolver;
    }

    private ServerAuthenticationSuccessHandler successHandler() {
        RedirectServerAuthenticationSuccessHandler handler = new RedirectServerAuthenticationSuccessHandler();
        handler.setLocation(URI.create("/items"));
        return handler;
    }

    @Bean
    public ServerLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedServerLogoutSuccessHandler(this.clientRegistrationRepository);

        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");

        return oidcLogoutSuccessHandler;
    }

    @Bean
    public ServerLogoutHandler logoutHandler() {
        return new DelegatingServerLogoutHandler(
                new SecurityContextServerLogoutHandler(),
                new WebSessionServerLogoutHandler(),
                new HeaderWriterServerLogoutHandler(
                        new ClearSiteDataServerHttpHeadersWriter(ClearSiteDataServerHttpHeadersWriter.Directive.COOKIES)
                )
        );
    }
}