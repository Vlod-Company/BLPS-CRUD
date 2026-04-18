package ru.gigasigma.blpscrud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

@Configuration
public class TransactionConfig {

    @Bean
    public PlatformTransactionManager transactionManager() {
        JtaTransactionManager jtaTm = new JtaTransactionManager();
        jtaTm.setTransactionManagerName("java:jboss/TransactionManager");
        jtaTm.setUserTransactionName("java:comp/UserTransaction");
        return jtaTm;
    }
}
