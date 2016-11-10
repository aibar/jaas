package kz.taimax.tomee.jaas.api;

import java.util.List;

public interface JAASContext {

    String lastLoginException(String username);
    int usersCount();

    List<String> getLoggedUsers();

    boolean isLoggedIn(String user);

    boolean isAuthenticationException();
}
