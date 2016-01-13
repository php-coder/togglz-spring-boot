package com.mycompany;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.console.TogglzConsoleServlet;
import org.togglz.core.Feature;
import org.togglz.core.bootstrap.TogglzBootstrap;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.composite.CompositeStateRepository;
import org.togglz.core.repository.property.PropertyBasedStateRepository;
import org.togglz.core.repository.property.PropertySource;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;
import org.togglz.servlet.TogglzFilter;

import java.util.List;

@Configuration
@ConditionalOnProperty(name = "togglz.enabled")
@EnableConfigurationProperties(TogglzProperties.class)
public class TogglzAutoConfiguration {

    @Configuration
    @ConditionalOnMissingBean({TogglzBootstrap.class, TogglzConfig.class})
    protected static class TogglzBootstrapConfiguration {

        @Autowired
        private FeatureManager featureManager;

        @Bean
        public TogglzBootstrap togglzBootstrap() {
            return new TogglzBootstrap() {
                @Override
                public FeatureManager createFeatureManager() {
                    return featureManager;
                }
            };
        }
    }

    @Configuration
    @ConditionalOnProperty(name = "togglz.console.enabled")
    protected static class TogglzConsoleConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public ServletRegistrationBean togglzConsole() {
            String path = properties.getConsole().getPath();
            String urlMapping = (path.endsWith("/") ? path + "*" : path + "/*");
            return new ServletRegistrationBean(new TogglzConsoleServlet(), urlMapping);
        }
    }

    @Configuration
    protected static class TogglzFilterConfiguration {

        @Bean
        public FilterRegistrationBean togglzFilter() {
            FilterRegistrationBean registration = new FilterRegistrationBean();
            registration.setFilter(new TogglzFilter());
            return registration;
        }
    }

    @Configuration
    @ConditionalOnMissingBean(FeatureManager.class)
    protected static class FeatureManagerConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Autowired
        private FeatureProvider featureProvider;

        @Autowired
        private List<StateRepository> stateRepositories;

        @Autowired
        private UserProvider userProvider;

        @Bean
        public FeatureManager featureManager() {
            StateRepository stateRepository = null;
            if (stateRepositories.size() == 1) {
                stateRepository = stateRepositories.get(0);
            } else if (stateRepositories.size() > 1) {
                StateRepository repository = stateRepositories.get(0);
                StateRepository[] repositories = stateRepositories.subList(1, stateRepositories.size() - 1).toArray(new StateRepository[stateRepositories.size() - 1]);
                stateRepository = new CompositeStateRepository(repository, repositories);
            }
            return new FeatureManagerBuilder()
                    .featureProvider(featureProvider)
                    .stateRepository(stateRepository)
                    .userProvider(userProvider)
                    .build();
        }
    }

    @Configuration
    @ConditionalOnMissingBean(FeatureProvider.class)
    @ConditionalOnProperty(name = "togglz.feature-enums")
    protected static class FeatureProviderConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public FeatureProvider featureProvider() {
            return new EnumBasedFeatureProvider(properties.getFeatureEnums());
        }
    }

    @Configuration
    @ConditionalOnMissingBean(StateRepository.class)
    protected static class StateRepositoryConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public StateRepository stateRepository() {
            PropertySource propertySource = new PropertiesPropertySource(properties.getFeatures());
            return new PropertyBasedStateRepository(propertySource);
        }
    }

    @Configuration
    @ConditionalOnMissingBean(UserProvider.class)
    protected static class UserProviderConfiguration {

        @Bean
        public UserProvider userProvider() {
            // TODO if the application uses spring security we might use a SpringSecurityUserProvider
            // return new NoOpUserProvider();
            return new UserProvider() {
                @Override
                public FeatureUser getCurrentUser() {
                    return new SimpleFeatureUser("admin", true);
                }
            };
        }
    }
}
