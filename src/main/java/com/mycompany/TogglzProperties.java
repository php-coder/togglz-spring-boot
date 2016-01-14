package com.mycompany;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.togglz.core.Feature;

import java.util.Map;
import java.util.Properties;

@ConfigurationProperties("togglz")
public class TogglzProperties {

    private boolean enabled = true;

    private Console console = new Console();

    private Class<? extends Feature>[] featureEnums;

    private String featureManagerName;

    private Map<String,String> features;

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

    public Class<? extends Feature>[] getFeatureEnums() {
        return featureEnums;
    }

    public void setFeatureEnums(Class<? extends Feature>[] featureEnums) {
        this.featureEnums = featureEnums;
    }

    public String getFeatureManagerName() {
        return featureManagerName;
    }

    public void setFeatureManagerName(String featureManagerName) {
        this.featureManagerName = featureManagerName;
    }

    public Map<String, String> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, String> features) {
        this.features = features;
    }

    public static class Console {

        private boolean enabled = true;

        private String path;

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
}
