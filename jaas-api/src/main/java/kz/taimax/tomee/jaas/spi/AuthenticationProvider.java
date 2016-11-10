package kz.taimax.tomee.jaas.spi;

public interface AuthenticationProvider {

    void authenticate(String username, String password) throws AuthenticationException;

    class AuthenticationException extends Throwable {

        public AuthenticationException(final String msg) {
            super(msg);
        }
    }
}
