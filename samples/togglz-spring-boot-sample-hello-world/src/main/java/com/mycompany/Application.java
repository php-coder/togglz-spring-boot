package com.mycompany;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.spi.FeatureProvider;

@SpringBootApplication
public class Application {

    @Bean
    public FeatureProvider featureProvider() {
        return new EnumBasedFeatureProvider(MyFeatures.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
