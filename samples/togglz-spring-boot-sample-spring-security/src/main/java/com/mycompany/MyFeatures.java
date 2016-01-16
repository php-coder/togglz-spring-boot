package com.mycompany;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum MyFeatures implements Feature {

    @EnabledByDefault
    @Label("Hello World Feature")
    HELLO_WORLD,

    @Label("Reverse Greeting Feature")
    REVERSE_GREETING;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
