### JAAS CDI LoginModule для TomEE 1.7.2 (JavaEE 6).

Установка.

    <dependency>
        <groupId>kz.taimax.tomee</groupId>
        <artifactId>tomee-jaas-api</artifactId>
        <version>2.0-SNAPSHOT</version>
    </dependency>
    
    <dependency>
        <groupId>kz.taimax.tomee</groupId>
        <artifactId>tomee-jaas-impl</artifactId>
        <version>2.0-SNAPSHOT</version>
    </dependency>

Создать файл webapp/META-INF/context.xml

    <?xml version="1.0" encoding="UTF-8"?>
    <Context antiJARLocking="true">
        <Realm className="org.apache.catalina.realm.JAASRealm" appName="LoginModule"
               userClassNames="org.apache.openejb.core.security.jaas.UserPrincipal"
               roleClassNames="org.apache.openejb.core.security.jaas.GroupPrincipal">
        </Realm>
    </Context>
    
В web.xml добавить (NONE -> CONFIDENTIAL для HTTPS):
    
    <security-constraint>
        <web-resource-collection>
            <web-resource-name>All</web-resource-name>
            <url-pattern>/*</url-pattern>
        </web-resource-collection>

        <auth-constraint>
            <role-name>*</role-name>
        </auth-constraint>

        <user-data-constraint>
            <transport-guarantee>NONE</transport-guarantee>
        </user-data-constraint>
    </security-constraint>

    <login-config>
        <auth-method>BASIC</auth-method>
    </login-config>

    <!-- Список всех ролей -->
    <security-role>
        <role-name>admin</role-name>
    </security-role>

    <security-role>
        <role-name>user</role-name>
    </security-role>
    
Имплементировать интерфейсы:
    
    kz.taimax.tomee.jaas.impl.AuthenticationProvider
    kz.taimax.tomee.jaas.impl.AuthorityProvider
    kz.taimax.tomee.jaas.impl.LoginProvider
    
## Аутентификация. Имплементировать один раз

    // Если username, password не проходит аутентификацию, выкинуть LoginException
    @AppAuthenticationProvider
    public class AuthenticationProviderImpl implements AuthenticationProvider {
        @Override
        public void authenticate(String username, String password) throws LoginException {
            if (!username.equals("admin"))
                throw new LoginException("You are not Admin");
        }
    }

## Авторизация. Имплементировать один раз

    // Возвращаем список ролей username
    // Если нет ролей, возвращаем пустой список либо null
    @AppAuthorityProvider
    public class AuthorityProviderImpl implements AuthorityProvider {
        @Override
        public List<String> get(String username) {
            if (username.equals("admin")) {
                return Arrays.asList("admin", "superman", "chuck_norris", "batman", "tor");
            }
            return Arrays.asList("looser");
        }
    }

## Проверка возможности входа в систему. Можно имплементировать сколько угодно раз

    public class HackerLoginProvider implements LoginProvider {    
        @Override
        public void login(String username) throws LoginException {
            if (username.equals("hacker")) throw new LoginException("Fuck you");
        }
    }
    
    @Inject
    JAASContext jaasContext;
    
    public class LicenseLoginProvider implements LoginProvider {
        @Override
        public void login(String username) throws LoginException {
            if (jaasContext.usersCount() == 2) throw new LoginException("Buy license");
        }
    }

## Текущий пользователь:
    
    @Resource
    SessionContext context;
    
    ...
    String username = context.getCallerPrincipal().getName();
    ...

## Трекинг. Можно имплементировать сколько угодно раз

    @ApplicationScoped
    class LoginModuleState implements LoginTracker {

        @Override
        public void loggedIn(final String username) {
            System.out.println("Пользователь " + username " вошел в систему");
        }

        @Override
        public void loggedOut(final String username) {
            System.out.println("Пользователь " + username " вышел из системы");
        }

        @Override
        public void authenticationFailure(final String username, final String password, final LoginException source) {
            System.out.println("Неверное сочетание username/password: " + username "/" + password);
        }

        @Override
        public void loginFailure(final String username, final LoginException source) {
            System.out.println("Пользователю " + username " не разрешили войти в систему");
        }
    }
