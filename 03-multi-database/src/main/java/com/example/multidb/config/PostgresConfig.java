package com.example.multidb.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

// PostgreSQL datasource — routed through OJP. Marked @Primary so it's the default.
@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.multidb.pg.repository",
        entityManagerFactoryRef = "pgEntityManagerFactory",
        transactionManagerRef = "pgTransactionManager"
)
public class PostgresConfig {

    @Primary
    @Bean
    @ConfigurationProperties("app.datasource.postgres")
    public DataSource pgDataSource() {
        // SimpleDriverDataSource => no local pool; OJP server pools centrally.
        return new SimpleDriverDataSource();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean pgEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("pgDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.multidb.pg.model")
                .persistenceUnit("pg")
                .properties(Map.of(
                        "hibernate.hbm2ddl.auto", "update",
                        "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"))
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager pgTransactionManager(
            @Qualifier("pgEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
