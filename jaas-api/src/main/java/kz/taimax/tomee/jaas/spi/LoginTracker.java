package kz.taimax.tomee.jaas.spi;

import javax.security.auth.login.LoginException;

public interface LoginTracker {

    void loggedIn(String username);
    void loggedOut(String username);
    void authenticationFailure(String username, String password, AuthenticationProvider.AuthenticationException source);
    void loginFailure(String username, final LoginException source);
}
