package ru.gigasigma.blpscrud.config;

import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.jaas.AuthorityGranter;
import org.springframework.security.authentication.jaas.DefaultJaasAuthenticationProvider;
import org.springframework.security.authentication.jaas.memory.InMemoryConfiguration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import ru.gigasigma.blpscrud.filter.IpWhiteListFilter;
import ru.gigasigma.blpscrud.filter.JwtAuthenticationFilter;
import ru.gigasigma.blpscrud.security.JwtAuthenticationProvider;
import ru.gigasigma.blpscrud.security.RoleAuthorityGranter;
import ru.gigasigma.blpscrud.security.XmlUserLoginModule;
import ru.gigasigma.blpscrud.security.XmlUserStore;
import ru.gigasigma.blpscrud.service.CIDRService;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider jaasAuthenticationProvider(
            XmlUserStore xmlUserStore,
            PasswordEncoder passwordEncoder
    ) throws Exception {
        DefaultJaasAuthenticationProvider provider = new DefaultJaasAuthenticationProvider();
        provider.setConfiguration(new InMemoryConfiguration(Map.of(
                "SPRINGSECURITY",
                new AppConfigurationEntry[]{
                        new AppConfigurationEntry(
                                XmlUserLoginModule.class.getName(),
                                LoginModuleControlFlag.REQUIRED,
                                Map.of(
                                        XmlUserStore.class.getName(), xmlUserStore,
                                        PasswordEncoder.class.getName(), passwordEncoder
                                )
                        )
                }
        )));
        provider.setAuthorityGranters(new AuthorityGranter[]{new RoleAuthorityGranter()});
        provider.afterPropertiesSet();
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider jaasAuthenticationProvider, JwtAuthenticationProvider jwtAuthenticationProvider, CIDRService cidrService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(jaasAuthenticationProvider)
                .addFilterBefore(new IpWhiteListFilter(cidrService), AuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtAuthenticationProvider), IpWhiteListFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/flights", "/api/flights/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/internal-purchases/callback",
                                "/api/external-purchases/callback"
                        ).hasRole("ADMIN")
                        .anyRequest().hasAnyRole("ADMIN", "USER")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"" + accessDeniedException.getMessage() + "\"}");
                        })
                );
        return http.build();
    }
}
