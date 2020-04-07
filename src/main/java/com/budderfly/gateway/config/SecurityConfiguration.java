package com.budderfly.gateway.config;

import com.budderfly.gateway.config.oauth2.OAuth2JwtAccessTokenConverter;
import com.budderfly.gateway.config.oauth2.OAuth2Properties;
import com.budderfly.gateway.security.oauth2.OAuth2SignatureVerifierClient;
import com.budderfly.gateway.security.AuthoritiesConstants;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends ResourceServerConfigurerAdapter {
    private final OAuth2Properties oAuth2Properties;

    private final CorsFilter corsFilter;


    // list of public endpoints for which authentication and CSRF should not be
    // enforced.
    private final String [] publicEndpoints = new String [] {
            "/billingimport/utilityapi_webhook",
            "/inventory/api/**",
            "/auth/login"
    };

    public SecurityConfiguration(OAuth2Properties oAuth2Properties, CorsFilter corsFilter) {
        this.oAuth2Properties = oAuth2Properties;
        this.corsFilter = corsFilter;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .csrf()
            .ignoringAntMatchers("/h2-console/**")
             // disable CSRF for public endpoints
            .ignoringAntMatchers(publicEndpoints)
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .and()
            .addFilterBefore(corsFilter, CsrfFilter.class)
            .headers()
            .frameOptions()
            .disable()
        .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .authorizeRequests()
            .antMatchers("/api/**").authenticated()
            .antMatchers("/inventory/api/**").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.INVENTORY_API)
            .antMatchers(HttpMethod.GET, "/sites/api/sites*").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.PORTAL)
            .antMatchers("/accounting/api/add-autopay-bank-details/").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.PORTAL)
            .antMatchers("/accounting/api/add-all-autopay-bank-details/").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.PORTAL)
            .antMatchers("/invoice/api/budderfly-invoices/by-budderfly-id/by-previous-date-and-balance*").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.PORTAL)
            .antMatchers("/invoice/api/budderfly-invoices/by-status-and-dates/**").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.PORTAL)
            .antMatchers("/sites/api/sites/portal/*").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.PORTAL)
            .antMatchers("/management/health").permitAll()
            .antMatchers("/management/info").permitAll()
            .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/swagger-resources/configuration/ui").permitAll()
            .antMatchers("/sites/api/**").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.INVENTORY_API)
            .antMatchers("/invoice/api/**").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.INVENTORY_API)
            .antMatchers("/accounting/api/**").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.INVENTORY_API)
            .antMatchers("/scheduler/api/**").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.INVENTORY_API)
            .antMatchers("/reports/api/**").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.INVENTORY_API)
            .antMatchers("/injobs/api/**").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.INVENTORY_API)
            .antMatchers("/billing/api/**").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.INVENTORY_API);
        //TODO if we add more services, update this list so that the PORTAL role does not have access to it
    }

    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(OAuth2SignatureVerifierClient signatureVerifierClient) {
        return new OAuth2JwtAccessTokenConverter(oAuth2Properties, signatureVerifierClient);
    }

    @Bean
	@Qualifier("loadBalancedRestTemplate")
    public RestTemplate loadBalancedRestTemplate(RestTemplateCustomizer customizer) {
        RestTemplate restTemplate = new RestTemplate();
        customizer.customize(restTemplate);
        return restTemplate;
    }

    @Bean
    @Qualifier("vanillaRestTemplate")
    public RestTemplate vanillaRestTemplate() {
        return new RestTemplate();
    }
}
