package org.togglz.spring.boot.autoconfigure;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

public class TogglzApplicationContextBinderApplicationListener implements ApplicationListener {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            ApplicationContext applicationContext = ((ContextRefreshedEvent) event).getApplicationContext();
            ContextClassLoaderApplicationContextHolder.bind(applicationContext);
        } else if (event instanceof ContextClosedEvent) {
            ContextClassLoaderApplicationContextHolder.release();
        }
    }
}
