package com.mycompany;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private static Log log = LogFactory.getLog(HelloController.class);

    @RequestMapping("/")
    public ResponseEntity index() {

        log.debug("HELLO WORLD feature active: " + MyFeatures.HELLO_WORLD.isActive());

        if (MyFeatures.HELLO_WORLD.isActive()) {
            return ResponseEntity.ok().body("Greetings from Spring Boot!");
        }
        return ResponseEntity.notFound().build();
    }
}
