/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.togglz.spring.boot.autoconfigure;

import com.github.heneke.thymeleaf.togglz.TogglzDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.togglz.console.TogglzConsoleServlet;
import org.togglz.core.activation.ActivationStrategyProvider;
import org.togglz.core.activation.DefaultActivationStrategyProvider;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.repository.composite.CompositeStateRepository;
import org.togglz.core.repository.file.FileBasedStateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.repository.property.PropertyBasedStateRepository;
import org.togglz.core.repository.property.PropertySource;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;
import org.togglz.spring.security.SpringSecurityUserProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Togglz.
 *
 * @author Marcel Overdijk
 */
@Configuration
@ConditionalOnProperty(prefix = "togglz", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(TogglzProperties.class)
public class TogglzAutoConfiguration {

    @Bean
    public TogglzApplicationContextBinderApplicationListener togglzApplicationContextBinderApplicationListener() {
        return new TogglzApplicationContextBinderApplicationListener();
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
        private ResourceLoader resourceLoader = new DefaultResourceLoader();

        @Autowired
        private TogglzProperties properties;

        @Bean
        public StateRepository stateRepository() throws IOException {
            StateRepository stateRepository;
            Map<String, String> features = properties.getFeatures();
            String featuresFile = properties.getFeaturesFile();
            if (featuresFile != null) {
                Resource resource = this.resourceLoader.getResource(featuresFile);
                Integer minCheckInterval = properties.getFeaturesFileMinCheckInterval();
                if (minCheckInterval != null) {
                    stateRepository = new FileBasedStateRepository(resource.getFile(), minCheckInterval);
                } else {
                    stateRepository = new FileBasedStateRepository(resource.getFile());
                }
            } else if (features != null && features.size() > 0) {
                Properties props = new Properties();
                props.putAll(features);
                PropertySource propertySource = new PropertiesPropertySource(props);
                stateRepository = new PropertyBasedStateRepository(propertySource);
            } else {
                stateRepository = new InMemoryStateRepository();
            }
            if (properties.getCache().isEnabled()) {
                stateRepository = new CachingStateRepository(stateRepository, properties.getCache().getTimeToLive());
            }
            return stateRepository;
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
            return new NoOpUserProvider();
        }
    }

    @Configuration
    @ConditionalOnClass({EnableWebSecurity.class, AuthenticationEntryPoint.class})
    @ConditionalOnMissingBean(UserProvider.class)
    protected static class SpringSecurityUserProviderConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public UserProvider userProvider() {
            return new SpringSecurityUserProvider(properties.getConsole().getFeatureAdminAuthority());
        }
    }

    @Configuration
    @ConditionalOnWebApplication
    @ConditionalOnClass(TogglzConsoleServlet.class)
    @ConditionalOnProperty(prefix = "togglz.console", name = "enabled", matchIfMissing = true)
    protected static class TogglzConsoleConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Autowired
        private UserProvider userProvider;

        @Bean
        public ServletRegistrationBean togglzConsole() {
            String path = properties.getConsole().getPath();
            String urlMapping = (path.endsWith("/") ? path + "*" : path + "/*");
            TogglzConsoleServlet servlet = new TogglzConsoleServlet();
            servlet.setSecured(properties.getConsole().isSecured());
            return new ServletRegistrationBean(servlet, urlMapping);
        }
    }

    @Configuration
    @ConditionalOnMissingBean(TogglzEndpoint.class)
    @ConditionalOnProperty(prefix = "togglz.endpoint", name = "enabled", matchIfMissing = true)
    protected static class TogglzEndpointConfiguration {

        @Autowired
        private TogglzProperties properties;

        @Bean
        public TogglzEndpoint togglzEndpoint(FeatureManager featureManager) {
            return new TogglzEndpoint(featureManager);
        }
    }

    @Configuration
    @ConditionalOnClass(TogglzDialect.class)
    protected static class ThymeleafTogglzDialectConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public TogglzDialect togglzDialect() {
            return new TogglzDialect();
        }
    }
}
