package com.mycompany;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum MyFeatures implements Feature {

    @EnabledByDefault
    @Label("Hello World Feature")
    HELLO_WORLD,

    @Label("Hello Tooglz Feature")
    REVERSE_GREETING;
}
