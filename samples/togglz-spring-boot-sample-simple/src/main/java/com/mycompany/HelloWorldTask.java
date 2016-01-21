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

package com.mycompany;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HelloWorldTask {

    private static final String GREETING = "Greetings from Spring Boot!";

    @Scheduled(fixedRate = 5000)
    public void greet() {
        StringBuilder sb = new StringBuilder();
        if (MyFeatures.HELLO_WORLD.isActive()) {
            sb.append(GREETING);
            if (MyFeatures.REVERSE_GREETING.isActive()) {
                sb.reverse();
            }
        } else {
            sb.append("/dev/null");
        }
        System.out.println(sb.toString());
    }
}
