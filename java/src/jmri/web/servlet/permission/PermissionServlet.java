package jmri.web.servlet.permission;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import jmri.*;
import jmri.util.FileUtil;
import jmri.web.servlet.ServletUtil;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood (C) 2014, 2016
 * @author mstevetodd (C) 2017
 * @author Daniel Bergqvist (C) 2024
 */
@WebServlet(name = "PermissionServlet",
        urlPatterns = {"/permission"})
@ServiceProvider(service = HttpServlet.class)
public class PermissionServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String theRequest = request.getRequestURI().substring("/permission/".length());
        // log.error("Request: {}", theRequest);

        switch (theRequest) {
            case "login":
                login(request, response);
                break;
            case "logout":
                logout(request, response);
                break;
//            case "status":
//                status(request, response);
//                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static void sendPage(String page, String message,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        //print the html, using the replacement values listed to fill in the calculated stuff
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setContentType(UTF8_TEXT_HTML);
        response.getWriter().print(String.format(request.getLocale(),
                FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), page))),
                Bundle.getMessage(request.getLocale(), "PermissionTitle"),                              // page title is parm 1
                InstanceManager.getDefault(ServletUtil.class).getNavBar(request.getLocale(), "/permission"), // navbar is parm 2
                InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),                   // railroad name is parm 3
                InstanceManager.getDefault(ServletUtil.class).getFooter(request.getLocale(), "/permission"), // footer is parm 4
                message                                                                                 // Response message
        ));
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getContentLength() > 0) {
            StringBuilder textBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    request.getInputStream(), StandardCharsets.UTF_8))) {
                int c;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }

            PermissionManager mngr = InstanceManager.getDefault(PermissionManager.class);

            String username = null;
            String password = null;

            for (String param : textBuilder.toString().split("&")) {
                String[] parts = param.split("=");
                switch (parts[0]) {
                    case "username":
                        username = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                        break;
                    case "password":
                        if (parts.length > 1)
                            password = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                        else
                            password = "";
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown parameter: \""+parts[0]+"\" with value \""+parts[1]+"\"");
                }
            }

            if (username != null && password != null) {
                // log.warn("Login with {} and {}", username, password);

                if (mngr.isAGuestUser(username)) {
                    String message = Bundle.getMessage("GuestMessage");
                    sendPage("Response.html", message, request, response);
                    return;
                }

                String sessId = PermissionServlet.getSessionId(request);
                if (sessId == null) sessId = "";

                StringBuilder sessionId = new StringBuilder(sessId);

                boolean result = mngr.remoteLogin(sessionId, request.getLocale(), username, password);

                if (result) {
                    setSessionId(sessionId.toString(), response);
                }
                String message = result ? Bundle.getMessage("LoginSuccessful") : Bundle.getMessage("BadUsernameOrPassword");
                sendPage("Response.html", message, request, response);

                return;
            }
        }

        //print the html, using the replacement values listed to fill in the calculated stuff
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setContentType(UTF8_TEXT_HTML);
        response.getWriter().print(String.format(request.getLocale(),
                FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Login.html"))),
                Bundle.getMessage(request.getLocale(), "PermissionTitle"),                              // page title is parm 1
                InstanceManager.getDefault(ServletUtil.class).getNavBar(request.getLocale(), "/permission"), // navbar is parm 2
                InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),                   // railroad name is parm 3
                InstanceManager.getDefault(ServletUtil.class).getFooter(request.getLocale(), "/permission") // footer is parm 4
        ));
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sessionId = PermissionServlet.getSessionId(request);
        InstanceManager.getDefault(PermissionManager.class).remoteLogout(sessionId);
        sendPage("Response.html", Bundle.getMessage("LogoutSuccessful"), request, response);
    }
/*
    private void status(HttpServletRequest request, HttpServletResponse response) throws IOException {

        log.error("Context path: {}", request.getContextPath());
        log.error("Servlet path: {}", request.getServletPath());
        log.error("Query string: {}", request.getQueryString());
        log.error("Request URI: {}", request.getRequestURI());
        log.error("Request URL: {}", request.getRequestURL().toString());

//        request.getContentLength();
//        request.getInputStream();
//        request.getMethod();
    }
*/
    public static void permissionDenied(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String session = "";
        InstanceManager.getDefault(PermissionManager.class).remoteLogout(session);
        sendPage("Response.html", Bundle.getMessage("PermissionDenied"), request, response);
    }

    public static String getSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if ("sessionId".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    public static void setSessionId(String sessionId, HttpServletResponse response) {
        Cookie cookie = new Cookie("sessionId", sessionId);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "About Servlet";
    }// </editor-fold>

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PermissionServlet.class);

}
