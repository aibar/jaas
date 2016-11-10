package kz.taimax.tomee.jaas.spi;

import java.util.List;

public interface AuthorityProvider {

    List<String> get(String username);
}