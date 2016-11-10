package kz.taimax.tomee.jaas.module;

import kz.taimax.javaee.utils.Beans;
import kz.taimax.tomee.jaas.spi.*;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.core.security.jaas.GroupPrincipal;
import org.apache.openejb.core.security.jaas.UserPrincipal;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LoginModuleImpl implements LoginModule {

    private static final Logger log = Logger.getLogger(LoginModuleImpl.class.getName());

    private Subject subject;
    private CallbackHandler callbackHandler;

    private Set<Principal> principals = new LinkedHashSet<>();

    private UserData userData;

    private static final AuthenticationProvider authenticationProvider;
    private static final AuthorityProvider authorityProvider;
    private static final Set<LoginProvider> loginProviders;
    private static final Set<LoginTracker> loginTrackers;
    private static final BeanManagerImpl beanManager;

    static {
        beanManager = WebBeansContext.currentInstance().getBeanManagerImpl();
        if (!beanManager.isInUse()) {
            throw new OpenEJBRuntimeException("CDI not activated");
        }

        authenticationProvider = Beans.getBean(beanManager, AuthenticationProvider.class, () -> AppAuthenticationProvider.class);
        if (authenticationProvider == null) {
            log.fine("AuthenticationProvider not implemented");
        }

        authorityProvider = Beans.getBean(beanManager, AuthorityProvider.class, () -> AppAuthorityProvider.class);
        if (authenticationProvider == null) {
            log.fine("AuthorityProvider not implemented");
        }

        loginProviders = Beans.getBeans(beanManager, LoginProvider.class);
        loginTrackers = Beans.getBeans(beanManager, LoginTracker.class);
    }

    public LoginModuleImpl() {
    }

    @Override
    public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        this.userData = getUserData();

        try {
            authenticationProvider.authenticate(userData.user, userData.pass);
        } catch (AuthenticationProvider.AuthenticationException e) {
            for (final LoginTracker loginTracker : loginTrackers) {
                loginTracker.authenticationFailure(userData.user, userData.pass, e);
            }
            return false;
        }

        for (final LoginProvider loginProvider : loginProviders) {
            try {
                loginProvider.login(userData.user);
            } catch (LoginException e) {
                for (final LoginTracker loginTracker : loginTrackers) {
                    loginTracker.loginFailure(userData.user, e);
                }
                return false;
            }
        }

        final List<String> myGroups = authorityProvider.get(userData.user);
        if (myGroups != null) {
            this.userData.groups.addAll(myGroups);
        }

        for (final LoginTracker loginTracker : loginTrackers) {
            loginTracker.loggedIn(userData.user);
        }

        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        principals.add(new UserPrincipal(userData.user));
        principals.addAll(userData.groups.stream().map(GroupPrincipal::new).collect(Collectors.toList()));
        subject.getPrincipals().addAll(principals);

        log.fine("commit");
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        log.fine("abort");
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        for (final LoginTracker loginTracker : loginTrackers) {
            loginTracker.loggedOut(userData.user);
        }

        this.subject.getPrincipals().removeAll(this.principals);
        this.principals.clear();

        log.fine("logout");
        return true;
    }

    private UserData getUserData() throws LoginException {
        final Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);
        try {
            this.callbackHandler.handle(callbacks);
        } catch (final IOException ioe) {
            throw new LoginException(ioe.getMessage());
        } catch (final UnsupportedCallbackException uce) {
            throw new LoginException(uce.getMessage() + " not available to obtain information from user");
        }

        final String user = ((NameCallback) callbacks[0]).getName();

        char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
        if (tmpPassword == null) {
            tmpPassword = new char[0];
        }

        final String password = new String(tmpPassword);

        return new UserData(user, password);
    }
}
