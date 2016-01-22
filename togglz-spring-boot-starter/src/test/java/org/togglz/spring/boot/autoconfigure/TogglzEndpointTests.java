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

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.togglz.core.Feature;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureProvider;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link TogglzEndpoint}.
 *
 * @author Marcel Overdijk
 */
public class TogglzEndpointTests {

    private AnnotationConfigApplicationContext context;

    @Before
    public void setup() {
        this.context = new AnnotationConfigApplicationContext();
    }

    private void registerAndRefresh(Class<?>... annotatedClasses) {
        this.context.register(annotatedClasses);
        this.context.refresh();
    }

    @Test
    public void invoke() throws Exception {
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.features.FEATURE_ONE: false");
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.features.FEATURE_TWO: true");
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.features.FEATURE_TWO.strategy: test");
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.features.FEATURE_TWO.param.p1: foo");
        EnvironmentTestUtils.addEnvironment(this.context, "togglz.features.FEATURE_TWO.param.p2: bar");
        registerAndRefresh(JacksonAutoConfiguration.class, Config.class);
        assertThat(getEndpointBean().invoke().size(), is(2));
        assertThat(getEndpointBean().invoke().get(0).getName(), is("FEATURE_ONE"));
        assertThat(getEndpointBean().invoke().get(0).isEnabled(), is(false));
        assertThat(getEndpointBean().invoke().get(0).getStrategy(), is(nullValue()));
        assertThat(getEndpointBean().invoke().get(0).getParams().size(), is(0));
        assertThat(getEndpointBean().invoke().get(1).getName(), is("FEATURE_TWO"));
        assertThat(getEndpointBean().invoke().get(1).isEnabled(), is(true));
        assertThat(getEndpointBean().invoke().get(1).getStrategy(), is("test"));
        assertThat(getEndpointBean().invoke().get(1).getParams().size(), is(2));
        assertThat(getEndpointBean().invoke().get(1).getParams().get("p1"), is("foo"));
        assertThat(getEndpointBean().invoke().get(1).getParams().get("p2"), is("bar"));
    }

    private TogglzEndpoint getEndpointBean() {
        return this.context.getBean(TogglzEndpoint.class);
    }

    public enum MyFeatures implements Feature {

        FEATURE_ONE,
        FEATURE_TWO;
    }

    @Configuration
    @Import(TogglzAutoConfiguration.class)
    public static class Config {

        @Autowired
        private FeatureManager featureManager;

        @Bean
        public FeatureProvider featureProvider() {
            return new EnumBasedFeatureProvider(MyFeatures.class);
        }

        @Bean
        public TogglzEndpoint endpoint() {
            return new TogglzEndpoint(this.featureManager);
        }
    }
}
