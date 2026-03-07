package com.github.nesterukia.mymarket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
public class OAuth2LoginConfig {

    @Value("${KC_INNER_REALM_URI}")
    private String kcInnerRealmUri;

    @Value("${KC_OUTER_REALM_URI}")
    private String kcOuterRealmUri;

    @Bean
    public ReactiveClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryReactiveClientRegistrationRepository(
                this.keycloakServiceRegistration(),
                this.keycloakLoginRegistration()
        );
    }

    private ClientRegistration keycloakServiceRegistration() {
        return ClientRegistration.withRegistrationId("keycloak-service")
                .clientId("market-service")
                .clientSecret("DYhdsEM7GitzbCwpb9gpYiSoPufddOym")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("openid")
                .tokenUri(kcInnerRealmUri + "/protocol/openid-connect/token")
                .clientName("Keycloak Service")
                .build();
    }

    private ClientRegistration keycloakLoginRegistration() {
        return ClientRegistration.withRegistrationId("keycloak-login")
                .clientId("market-ui")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri(kcOuterRealmUri + "/protocol/openid-connect/auth")
                .tokenUri(kcInnerRealmUri + "/protocol/openid-connect/token")
                .userInfoUri(kcInnerRealmUri + "/protocol/openid-connect/userinfo")
                .jwkSetUri(kcInnerRealmUri + "/protocol/openid-connect/certs")
                .userNameAttributeName("preferred_username")
                .clientName("Keycloak Login")
                .build();
    }
}

