package kz.taimax.tomee.jaas.module;

import kz.taimax.tomee.jaas.api.JAASContext;
import kz.taimax.tomee.jaas.spi.AuthenticationProvider;
import kz.taimax.tomee.jaas.spi.LoginTracker;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
class LoginModuleState implements JAASContext, LoginTracker {

    @Inject
    Logger logger;

    private final Map<String, String> lastFails = new HashMap<>();
    private final Set<String> state = new LinkedHashSet<>();
    private boolean authenticationException = false;

    @Override
    public String lastLoginException(String username) {
        return lastFails.get(username);
    }

    @Override
    public void loggedIn(final String username) {
        state.add(username);
        logger.log(Level.FINE, "Logged IN: {0}, count: {2}, users: [{1}]", new Object[]{username, StringUtils.join(state, ","), usersCount()});
    }

    @Override
    public void loggedOut(final String username) {
        state.remove(username);
        logger.log(Level.FINE, "Logged OUT: {0}, count: {2}, users: [{1}]", new Object[]{username, StringUtils.join(state, ","), usersCount()});
    }

    @Override
    public void authenticationFailure(final String username, final String password, final AuthenticationProvider.AuthenticationException source) {
        authenticationException = true;
        logger.log(Level.FINE, "Authentication exception: {0}", new Object[]{username, password});
    }

    @Override
    public void loginFailure(final String username, final LoginException source) {
        lastFails.put(username, source.getLocalizedMessage());
        authenticationException = false;
        logger.log(Level.FINE, "Login exception: {0}", username);
    }

    @Override
    public int usersCount() {
        return state.size();
    }

    @Override
    public List<String> getLoggedUsers() {
        return state.stream().collect(Collectors.toList());
    }

    @Override
    public boolean isLoggedIn(String user) {
        return state.contains(user);
    }

    @Override
    public boolean isAuthenticationException() {
        return authenticationException;
    }
}
