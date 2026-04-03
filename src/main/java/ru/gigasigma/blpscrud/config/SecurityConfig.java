package ru.gigasigma.blpscrud.config;

import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import ru.gigasigma.blpscrud.security.RoleAuthorityGranter;
import ru.gigasigma.blpscrud.security.XmlUserLoginModule;
import ru.gigasigma.blpscrud.security.XmlUserStore;

@Configuration
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/auth/register",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/api/payments/**", "/api/booking/external/callback").hasRole("ADMIN")
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}