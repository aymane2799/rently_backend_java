package com.rently.rently.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class PublicSchemaConfig {

    /**
     * Secondary EntityManagerFactory that is NOT wired to the multi-tenancy
     * infrastructure. It always connects to the public schema, making it safe
     * to call from within a tenant-scoped request (e.g., VehicleService
     * checking that a modelId exists in the public catalog).
     */
    @Bean(name = "publicEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean publicEntityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setPackagesToScan("com.rently.rently.catalog", "com.rently.rently.shared");
        factory.setPersistenceUnitName("publicPU");

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.default_schema", "public");
        factory.setJpaPropertyMap(props);

        return factory;
    }
}
