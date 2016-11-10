package kz.taimax.tomee.jaas.spi;

import javax.security.auth.login.LoginException;

public interface LoginProvider {

    void login(String username) throws LoginException;
}