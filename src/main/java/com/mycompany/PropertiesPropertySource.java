package com.mycompany;

import org.togglz.core.repository.property.PropertySource;

import java.util.*;

public class PropertiesPropertySource implements PropertySource {

    private Properties values = new Properties();

    public PropertiesPropertySource(Properties properties) {
        this.values = properties;
    }

    @Override
    public void reloadIfUpdated() {
        // do nothing
    }

    @Override
    public Set<String> getKeysStartingWith(String prefix) {
        Set<String> result = new HashSet<String>();
        Enumeration<?> keys = values.propertyNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (key.startsWith(prefix)) {
                result.add(key);
            }
        }
        return result;
    }

    @Override
    public String getValue(String key, String defaultValue) {
        return values.getProperty(key, defaultValue);
    }

    @Override
    public Editor getEditor() {
        return new PropertiesEditor(values);
    }

    private void setValues(Properties values) {
        this.values = values;
    }

    private class PropertiesEditor implements PropertySource.Editor {

        private Properties newValues;

        private PropertiesEditor(Properties values) {
            newValues = new Properties();
            newValues.putAll(values);
        }

        @Override
        public void setValue(String key, String value) {
            if (value != null) {
                newValues.setProperty(key, value);
            }
            else {
                newValues.remove(key);
            }
        }

        @Override
        public void removeKeysStartingWith(String prefix) {
            Iterator<Map.Entry<Object, Object>> iterator = newValues.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Object, Object> entry = iterator.next();
                if (entry.getKey().toString().startsWith(prefix)) {
                    iterator.remove();
                }
            }
        }

        @Override
        public void commit() {
            setValues(newValues);
        }
    }
}
