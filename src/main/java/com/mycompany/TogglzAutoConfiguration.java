package com.mycompany;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.console.TogglzConsoleServlet;
import org.togglz.core.activation.ActivationStrategyProvider;
import org.togglz.core.activation.DefaultActivationStrategyProvider;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.composite.CompositeStateRepository;
import org.togglz.core.repository.property.PropertyBasedStateRepository;
import org.togglz.core.repository.property.PropertySource;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.List;

@Configuration
@ConditionalOnProperty(name = "togglz.enabled")
@EnableConfigurationProperties(TogglzProperties.class)
public class TogglzAutoConfiguration {

    @Configuration
    protected static class TogglzContextParamConfiguration {

        @Bean
        public ServletContextInitializer  togglzServletContextInitializer() {
            return new ServletContextInitializer() {
                @Override
                public void onStartup(ServletContext servletContext) throws ServletException {
                    servletContext.setInitParameter("org.togglz.FEATURE_MANAGER_PROVIDED", "true");
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
    @ConditionalOnMissingBean(FeatureManager.class)
    protected static class FeatureManagerConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Autowired
        private FeatureProvider featureProvider;

        @Autowired
        private ActivationStrategyProvider activationStrategyProvider;

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
            FeatureManagerBuilder featureManagerBuilder = new FeatureManagerBuilder();
            String name = properties.getFeatureManagerName();
            if (name != null && name.length() > 0) {
                featureManagerBuilder.name(name);
            }
            featureManagerBuilder
                    .featureProvider(featureProvider)
                    .activationStrategyProvider(activationStrategyProvider)
                    .stateRepository(stateRepository)
                    .userProvider(userProvider)
                    .build();
            return featureManagerBuilder.build();
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
    @ConditionalOnMissingBean(ActivationStrategyProvider.class)
    protected static class ActivationStrategyProviderConfiguration {

        @Autowired(required = false)
        private List<ActivationStrategy> activationStrategies;

        @Bean
        public ActivationStrategyProvider activationStrategyProvider() {
            DefaultActivationStrategyProvider provider = new DefaultActivationStrategyProvider();
            if (activationStrategies != null && activationStrategies.size() > 0) {
                provider.addActivationStrategies(activationStrategies);
            }
            return provider;
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

        @Autowired
        private TogglzProperties properties;

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
