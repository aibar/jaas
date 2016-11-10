package kz.taimax.tomee.jaas.predef;

import kz.taimax.javaee.generics.rs.RSResponse;
import kz.taimax.tomee.jaas.api.JAASContext;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/401", "/403"})
public class LoginExceptionServlet extends HttpServlet {

    @Inject
    JAASContext jaasContext;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (jaasContext.isAuthenticationException()) {
            resp.setStatus(401);
            resp.setHeader("WWW-Authenticate", "Basic realm=\"Authentication required\"");
            resp.getWriter().write(RSResponse.buildNoData("401 Unauthorized"));
        } else {
            resp.setStatus(403);
            resp.getWriter().write(RSResponse.buildNoData("403 Access denied"));
        }
    }
}
