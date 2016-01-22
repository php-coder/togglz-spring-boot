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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.togglz.core.Feature;
import org.togglz.core.activation.Parameter;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.repository.file.FileBasedStateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.repository.property.PropertyBasedStateRepository;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.FeatureUser;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for {@link TogglzAutoConfiguration}.
 *
 * @author Marcel Overdijk
 */
public class TogglzAutoConfigurationTests {

    private AnnotationConfigWebApplicationContext context;

    @Before
    public void setUp() {
        this.context = new AnnotationConfigWebApplicationContext();
    }

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    private void registerAndRefresh(Class<?>... annotatedClasses) {
        this.context.register(annotatedClasses);
        this.context.refresh();
    }

    @Test
    public void defaultTogglz() {
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class);
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        assertThat(featureManager, is(notNullValue()));
        assertThat(featureManager.getFeatures(), hasSize(2));
        assertThat(featureManager.getFeatures(), hasItem(MyFeatures.FEATURE_ONE));
        assertThat(featureManager.getFeatures(), hasItem(MyFeatures.FEATURE_TWO));
        assertThat(this.context.getBean(StateRepository.class), is(instanceOf(InMemoryStateRepository.class)));
        assertThat(this.context.getBeansOfType(ServletRegistrationBean.class).size(), is(equalTo(1)));
        assertThat(this.context.getBean(ServletRegistrationBean.class).getUrlMappings(), hasItems("/togglz-console/*"));
        assertThat(ContextClassLoaderApplicationContextHolder.get(), is((ApplicationContext) this.context));
        assertThat(FeatureContext.getFeatureManager(), is(sameInstance(featureManager)));
    }

    @Test
    public void applicationContextBinder() {
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class);
        assertThat(ContextClassLoaderApplicationContextHolder.get(), is((ApplicationContext) this.context));
    }

    @Test
    public void disabled() {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.enabled: false");
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class);
        try {
            this.context.getBean(FeatureManager.class);
            fail();
        } catch (NoSuchBeanDefinitionException e) {
            // expected
        }
        assertThat(this.context.getBeansOfType(ServletRegistrationBean.class).size(), is(0));
        assertThat(ContextClassLoaderApplicationContextHolder.get(), is(nullValue()));
        try {
            assertThat(FeatureContext.getFeatureManager(), is(nullValue()));
            fail();
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void featureEnums() {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.feature-enums: org.togglz.spring.boot.autoconfigure.TogglzAutoConfigurationTests.MyFeatures");
        registerAndRefresh(TogglzAutoConfiguration.class);
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        assertThat(featureManager, is(notNullValue()));
        assertThat(featureManager.getFeatures(), hasSize(2));
        assertThat(featureManager.getFeatures(), hasItem(MyFeatures.FEATURE_ONE));
        assertThat(featureManager.getFeatures(), hasItem(MyFeatures.FEATURE_TWO));
    }

    @Test(expected = BeanCreationException.class)
    public void featureEnumsClassNotFound() {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.feature-enums: i.dont.exist.MyFeatures");
        registerAndRefresh(TogglzAutoConfiguration.class);
    }

    @Test
    public void customFeatureManagerName() {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.feature-manager-name: My Feature Manager");
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class);
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        assertThat(featureManager.getName(), is("My Feature Manager"));
    }

    @Test
    public void features() {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.features.FEATURE_ONE: false");
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.features.FEATURE_TWO: true");
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class);
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        assertThat(featureManager.isActive(MyFeatures.FEATURE_ONE), is(false));
        assertThat(featureManager.isActive(MyFeatures.FEATURE_TWO), is(true));
        assertThat(this.context.getBean(StateRepository.class), is(instanceOf(PropertyBasedStateRepository.class)));
    }

    @Test
    public void featuresFile() {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.features-file: classpath:/features-file/features.properties");
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class);
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        assertThat(featureManager.isActive(MyFeatures.FEATURE_ONE), is(true));
        assertThat(featureManager.isActive(MyFeatures.FEATURE_TWO), is(false));
        assertThat(this.context.getBean(StateRepository.class), is(instanceOf(FileBasedStateRepository.class)));
    }

    @Test
    public void consoleDisabled() {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.console.enabled: false");
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class);
        assertThat(this.context.getBeansOfType(ServletRegistrationBean.class).size(), is(0));
    }

    @Test
    public void customConsolePath() {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.console.path: /custom");
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class);
        assertThat(this.context.getBeansOfType(ServletRegistrationBean.class).size(), is(1));
        assertThat(this.context.getBean(ServletRegistrationBean.class).getUrlMappings(), hasItems("/custom/*"));
    }

    @Test
    public void customConsolePathWithTrailingSlash() {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.console.path: /custom/");
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class);
        assertThat(this.context.getBeansOfType(ServletRegistrationBean.class).size(), is(1));
        assertThat(this.context.getBean(ServletRegistrationBean.class).getUrlMappings(), hasItems("/custom/*"));
    }

    @Test
    public void cacheEnabled() {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.cache.enabled: true");
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class);
        assertThat(this.context.getBean(StateRepository.class), is(instanceOf(CachingStateRepository.class)));
    }

    @Test
    public void customActivationStrategy() {
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class, ActivationStrategyConfig.class);
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        CustomActivationStrategy customActivationStrategy = this.context.getBean(CustomActivationStrategy.class);
        assertThat(featureManager.getActivationStrategies().contains(customActivationStrategy), is(true));
    }

    public enum MyFeatures implements Feature {

        FEATURE_ONE,
        FEATURE_TWO;

        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }
    }

    public static class CustomActivationStrategy implements ActivationStrategy {

        @Override
        public String getId() {
            return "custom";
        }

        @Override
        public String getName() {
            return "Custom";
        }

        @Override
        public boolean isActive(FeatureState featureState, FeatureUser user) {
            return true;
        }

        @Override
        public Parameter[] getParameters() {
            return new Parameter[0];
        }
    }

    @Configuration
    public static class FeatureProviderConfig {

        @Bean
        public FeatureProvider featureProvider() {
            return new EnumBasedFeatureProvider(MyFeatures.class);
        }
    }

    @Configuration
    public static class ActivationStrategyConfig {

        @Bean
        public CustomActivationStrategy customActivationStrategy() {
            return new CustomActivationStrategy();
        }
    }
}
