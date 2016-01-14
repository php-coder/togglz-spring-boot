package org.togglz.spring.boot.autoconfigure;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerProvider;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

public class SpringBootFeatureManagerProvider implements FeatureManagerProvider {

    @Override
    public FeatureManager getFeatureManager() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            ServletContext servletContext = httpServletRequest.getServletContext();
            ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            if (applicationContext != null) {
                Set<FeatureManager> managers = new HashSet<FeatureManager>();
                managers.addAll(applicationContext.getBeansOfType(FeatureManager.class).values());
                if (!managers.isEmpty()) {
                    return managers.iterator().next();
                }
            }
        }
        return null;
    }

    @Override
    public int priority() {
        return 0;
    }
}
