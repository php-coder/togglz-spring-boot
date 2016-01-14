package com.mycompany;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.togglz.core.manager.FeatureManager;

@RestController
public class HelloController {

    private static final String GREETING = "Greetings from Spring Boot!";

    private static Log log = LogFactory.getLog(HelloController.class);

    private FeatureManager featureManager;

    @Autowired
    public HelloController(FeatureManager featureManager) {
        this.featureManager = featureManager;
    }

    @RequestMapping("/")
    public ResponseEntity index() {

        boolean active = featureManager.isActive(MyFeatures.HELLO_WORLD);
        boolean reverse = featureManager.isActive(MyFeatures.REVERSE_GREETING);

        log.debug(MyFeatures.HELLO_WORLD + " feature active: " + active);
        log.debug(MyFeatures.REVERSE_GREETING + " feature active: " + reverse);

        if (active) {
            StringBuilder sb = new StringBuilder("Greetings from Spring Boot!");
            if (reverse) {
                sb.reverse();
            }
            return ResponseEntity.ok().body(sb.toString());
        }
        return ResponseEntity.notFound().build();
    }
}
