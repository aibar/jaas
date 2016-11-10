package kz.taimax.tomee.jaas.module;

import java.util.HashSet;
import java.util.Set;

final class UserData {

    public final String user;
    public final String pass;
    public final Set<String> groups = new HashSet<>();

    UserData(final String user, final String pass) {
        this.user = user;
        this.pass = pass;
    }
}
