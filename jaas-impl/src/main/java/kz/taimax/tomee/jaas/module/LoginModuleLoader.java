package kz.taimax.tomee.jaas.module;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Logger;

@WebListener
public class LoginModuleLoader implements ServletContextListener {

    @Inject
    Logger log;

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        log.info("Init JAAS LoginModule");

        if (System.getProperty("java.security.auth.login.config") == null) {
            System.setProperty("java.security.auth.login.config", getClass().getResource("/login.config").getPath());
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
    }
}
