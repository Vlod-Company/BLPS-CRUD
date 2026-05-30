package ru.gigasigma.blpscrud.config;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import one.laxo.crm.api.LaxoCrmConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "crm.laxo.enabled", havingValue = "true")
public class LaxoCrmJcaConfig {

    @Bean
    public LaxoCrmConnectionFactory laxoCrmConnectionFactory() throws NamingException {
        InitialContext context = new InitialContext();
        return (LaxoCrmConnectionFactory) context.lookup("java:/eis/LaxoCrmConnectionFactory");
    }
}
