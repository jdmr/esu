/*
 * The MIT License
 *
 * Copyright 2016 Universidad de Montemorelos.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.davidmendoza.esu.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.Filter;
import org.davidmendoza.esu.dao.RolRepository;
import org.davidmendoza.esu.dao.UsuarioRepository;
import org.davidmendoza.esu.shared.Rol;
import org.davidmendoza.esu.shared.Usuario;
import org.davidmendoza.esu.utils.LoginHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.CompositeFilter;

/**
 *
 * @author J. David Mendoza <jdmendoza@swau.edu>
 */
@Configuration
@EnableOAuth2Client
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private OAuth2ClientContext oauth2ClientContext;

    @Autowired
    private LoginHandler loginHandler;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/admin/search/**").hasRole("ADMIN")
                .antMatchers("/admin/**").authenticated()
                .antMatchers("/**").permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .and()
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
    }

    private Filter ssoFilter() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();

        OAuth2ClientAuthenticationProcessingFilter facebookFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/facebook");
        OAuth2RestTemplate facebookTemplate = new OAuth2RestTemplate(facebook(), oauth2ClientContext);
        facebookFilter.setRestTemplate(facebookTemplate);
        facebookFilter.setTokenServices(new UserInfoTokenServices(facebookResource().getUserInfoUri(), facebook().getClientId()));
        facebookFilter.setAuthenticationSuccessHandler(loginHandler);
        filters.add(facebookFilter);

        OAuth2ClientAuthenticationProcessingFilter googleFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/google");
        OAuth2RestTemplate googleTemplate = new OAuth2RestTemplate(google(), oauth2ClientContext);
        googleFilter.setRestTemplate(googleTemplate);
        UserInfoTokenServices uits = new UserInfoTokenServices(googleResource().getUserInfoUri(), google().getClientId());
        uits.setPrincipalExtractor(googlePrincipalExtractor(usuarioRepository, rolRepository));
        uits.setAuthoritiesExtractor(googleAuthoritiesExtractor(usuarioRepository));
        googleFilter.setTokenServices(uits);
        googleFilter.setAuthenticationSuccessHandler(loginHandler);
        filters.add(googleFilter);

        filter.setFilters(filters);
        return filter;
    }

    @Bean
    @ConfigurationProperties("facebook.client")
    public AuthorizationCodeResourceDetails facebook() {
        return new AuthorizationCodeResourceDetails();
    }

    @Bean
    @ConfigurationProperties("facebook.resource")
    public ResourceServerProperties facebookResource() {
        return new ResourceServerProperties();
    }

    @Bean
    @ConfigurationProperties("google.client")
    public AuthorizationCodeResourceDetails google() {
        return new AuthorizationCodeResourceDetails();
    }

    @Bean
    @ConfigurationProperties("google.resource")
    public ResourceServerProperties googleResource() {
        return new ResourceServerProperties();
    }

    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(
            OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    public PrincipalExtractor googlePrincipalExtractor(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        return map -> {
            log.debug("Google extractor");
            for (String key : map.keySet()) {
                log.debug("{} : {}", key, map.get(key));
            }

            List<Map<String, String>> emails = (List<Map<String, String>>) map.get("emails");
            Map<String, String> email = emails.get(0);
            String username = email.get("value");
            Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username);
            if (usuario == null) {
                log.info("Deberia crear usuario {}", username);
                Date date = new Date();
                Map<String, String> name = (Map<String, String>) map.get("name");
                usuario = new Usuario();
                usuario.setUsername(username);
                usuario.setNombre(name.get("givenName"));
                usuario.setApellido(name.get("familyName"));
                Rol rol = rolRepository.findByAuthority("ROLE_USER");
                usuario.getRoles().add(rol);
                usuario.setDateCreated(date);
                usuario.setLastUpdated(date);
                usuario.setPassword(UUID.randomUUID().toString());
                usuario = usuarioRepository.save(usuario);
            }
            return usuario;
        };
    }

    public AuthoritiesExtractor googleAuthoritiesExtractor(UsuarioRepository usuarioRepository) {
        return map -> {
            log.debug("Google Authorities Extractor");
            List<Map<String, String>> emails = (List<Map<String, String>>) map.get("emails");
            Map<String, String> email = emails.get(0);
            String username = email.get("value");
            log.debug("Getting {} authorities", username);
            Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username);
            if (usuario != null) {
                String roles = "";
                for (Rol rol : usuario.getRoles()) {
                    if (roles.length() > 0) {
                        roles += ",";
                    }
                    roles += rol.getAuthority();
                }
                switch (roles) {
                    case "ROLE_ADMIN":
                        roles += ",ROLES_EDITOR";
                    case "ROLE_EDITOR":
                        roles += ",ROL_AUTOR";
                    case "ROLE_AUTOR":
                        roles += ",ROLE_USER";
                }
                return AuthorityUtils.commaSeparatedStringToAuthorityList(roles);
            }
            throw new BadCredentialsException("El usuario no existe");
        };
    }

    public PrincipalExtractor facebookPrincipalExtractor(UsuarioRepository usuarioRepository) {
        return map -> {
            log.debug("Facebook extractor");
            for (String key : map.keySet()) {
                log.debug("{} : {}", key, map.get(key));
            }

            String username = (String) map.get("login");
            Usuario speaker = usuarioRepository.findByUsernameIgnoreCase(username);
            if (speaker == null) {
                log.info("Deberia crear usuario {}", username);
            }
            return speaker;
        };
    }

}
