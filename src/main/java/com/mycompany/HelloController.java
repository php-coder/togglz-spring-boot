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

    private static Log log = LogFactory.getLog(HelloController.class);

    private FeatureManager featureManager;

    @Autowired
    public HelloController(FeatureManager featureManager) {
        this.featureManager = featureManager;
    }

    @RequestMapping("/")
    public ResponseEntity index() {

        log.debug("HELLO WORLD feature active (via enum): " + MyFeatures.HELLO_WORLD.isActive());
        log.debug("HELLO WORLD feature active (via featureManager): " + featureManager.isActive(MyFeatures.HELLO_WORLD));

        if (featureManager.isActive(MyFeatures.HELLO_WORLD)) {
            return ResponseEntity.ok().body("Greetings from Spring Boot!");
        }
        return ResponseEntity.notFound().build();
    }
}
