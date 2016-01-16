package org.togglz.spring.boot.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.togglz.console.TogglzConsoleServlet;
import org.togglz.core.activation.ActivationStrategyProvider;
import org.togglz.core.activation.DefaultActivationStrategyProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.composite.CompositeStateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.repository.property.PropertyBasedStateRepository;
import org.togglz.core.repository.property.PropertySource;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;
import org.togglz.spring.security.SpringSecurityUserProvider;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
@ConditionalOnProperty(name = "togglz.enabled")
@EnableConfigurationProperties(TogglzProperties.class)
public class TogglzAutoConfiguration {

    @Bean
    public TogglzApplicationContextBinderApplicationListener togglzApplicationContextBinderApplicationListener() {
        return new TogglzApplicationContextBinderApplicationListener();
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
                stateRepository = new CompositeStateRepository(stateRepositories.toArray(new StateRepository[stateRepositories.size()]));
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
            Map<String, String> features = properties.getFeatures();
            if (features != null && features.size() > 0) {
                Properties props = new Properties();
                props.putAll(features);
                PropertySource propertySource = new PropertiesPropertySource(props);
                return new PropertyBasedStateRepository(propertySource);
            }
            return new InMemoryStateRepository();
        }
    }

    @Configuration
    @ConditionalOnMissingClass("org.springframework.security.config.annotation.web.configuration.EnableWebSecurity")
    @ConditionalOnMissingBean(UserProvider.class)
    protected static class UserProviderConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public UserProvider userProvider() {
            // TODO when to return NoOpUserProvider (in case console not used?)
            // return new NoOpUserProvider();
            return new UserProvider() {
                @Override
                public FeatureUser getCurrentUser() {
                    return new SimpleFeatureUser("admin", true);
                }
            };
        }
    }

    @Configuration
    @ConditionalOnClass({ EnableWebSecurity.class, AuthenticationEntryPoint.class })
    @ConditionalOnMissingBean(UserProvider.class)
    protected static class SpringSecurityUserProviderConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public UserProvider userProvider() {
            return new SpringSecurityUserProvider(properties.getSecurity().getFeatureAdminAuthority());
        }
    }
}
