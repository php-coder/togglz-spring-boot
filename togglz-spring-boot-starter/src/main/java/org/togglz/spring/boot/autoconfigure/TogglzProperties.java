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

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Configuration properties for Togglz.
 *
 * @author Marcel Overdijk
 */
@ConfigurationProperties(prefix = "togglz", ignoreUnknownFields = true)
public class TogglzProperties {

    private boolean enabled = true;

    private Console console = new Console();

    private String featureManagerName;

    private String featuresFile;

    private Map<String, String> features;

    public Security security = new Security();

    public boolean isEnabled() {
        return enabled;
    }

    public Console getConsole() {
        return console;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFeatureManagerName() {
        return featureManagerName;
    }

    public void setFeatureManagerName(String featureManagerName) {
        this.featureManagerName = featureManagerName;
    }

    public String getFeaturesFile() {
        return featuresFile;
    }

    public void setFeaturesFile(String featuresFile) {
        this.featuresFile = featuresFile;
    }

    public Map<String, String> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, String> features) {
        this.features = features;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public static class Console {

        private boolean enabled = true;

        private String path = "/togglz";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class Security {

        private String featureAdminAuthority;

        public String getFeatureAdminAuthority() {
            return featureAdminAuthority;
        }

        public void setFeatureAdminAuthority(String featureAdminAuthority) {
            this.featureAdminAuthority = featureAdminAuthority;
        }
    }
}
