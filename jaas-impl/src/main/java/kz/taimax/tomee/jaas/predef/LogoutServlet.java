package kz.taimax.tomee.jaas.predef;

import kz.taimax.javaee.generics.rs.RSResponse;
import kz.taimax.tomee.jaas.api.JAASContext;
import kz.taimax.tomee.jaas.spi.LoginTracker;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet {

    public static String PATH = "/logout";

    @Inject @Any
    Instance<LoginTracker> loginTrackers;

    @Inject
    JAASContext jaasContext;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String username = req.getQueryString();
        if (StringUtils.isEmpty(username)) {
            resp.getWriter().write(RSResponse.buildNoData("Username is empty: http://host:port/context/logout?username"));
            return;
        } else if (jaasContext.isLoggedIn(username)) {
            for (final LoginTracker loginTracker : loginTrackers) {
                loginTracker.loggedOut(username);
            }
        }
        resp.getWriter().write(RSResponse.buildNoData("ok"));
    }
}