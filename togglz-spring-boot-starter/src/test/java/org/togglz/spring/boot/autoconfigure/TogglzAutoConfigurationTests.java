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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureProvider;
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

    private AnnotationConfigApplicationContext context;

    @Before
    public void setUp() {
        this.context = new AnnotationConfigApplicationContext();
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
        assertThat(this.context.getBeansOfType(ServletRegistrationBean.class).size(), is(equalTo(1)));
        assertThat(this.context.getBean(ServletRegistrationBean.class).getUrlMappings(), hasItems("/togglz/*"));
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
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.enabled:false");
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
    public void customFeatureManagerName() {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.feature-manager-name:My Feature Manager");
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class);
        FeatureManager featureManager = this.context.getBean(FeatureManager.class);
        assertThat(featureManager.getName(), is("My Feature Manager"));
    }

    @Test
    public void consoleDisabled() {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.console.enabled:false");
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class);
        assertThat(this.context.getBeansOfType(ServletRegistrationBean.class).size(), is(0));
    }

    @Test
    public void customConsolePath() {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.console.path:/custom");
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class);
        assertThat(this.context.getBeansOfType(ServletRegistrationBean.class).size(), is(1));
        assertThat(this.context.getBean(ServletRegistrationBean.class).getUrlMappings(), hasItems("/custom/*"));
    }

    @Test
    public void customConsolePathWithTrailingSlash() {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.console.path:/custom/");
        registerAndRefresh(TogglzAutoConfiguration.class, FeatureProviderConfig.class);
        assertThat(this.context.getBeansOfType(ServletRegistrationBean.class).size(), is(1));
        assertThat(this.context.getBean(ServletRegistrationBean.class).getUrlMappings(), hasItems("/custom/*"));
    }

    protected enum MyFeatures implements Feature {

        FEATURE_ONE,
        FEATURE_TWO;

        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }
    }

    @Configuration
    protected static class FeatureProviderConfig {

        @Bean
        public FeatureProvider featureProvider() {
            return new EnumBasedFeatureProvider(MyFeatures.class);
        }
    }
}
