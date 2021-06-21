package jmri.web.servlet.help;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;
import jmri.util.FileUtil;
import jmri.web.server.*;
import jmri.web.servlet.home.*;

import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood (C) 2014, 2016
 */
@WebServlet(name = "HomeServlet",
        urlPatterns = {
            "/help2"
        })
@ServiceProvider(service = HttpServlet.class)
public class HelplServlet extends HttpServlet {

    private String readAndParseFile(String fileName) throws IOException {
        
        fileName = FileUtil.getProgramPath() + fileName;
        
        String content = new String(Files.readAllBytes(Paths.get(fileName)));
        
        String serverSideIncludePattern = "<!--#include\\s*virtual=\"(.+?)\"\\s*-->";
        
        Pattern pattern = Pattern.compile(serverSideIncludePattern);
        Matcher matcher = pattern.matcher(content);
        matcher.replaceAll(new java.util.function.Function<java.util.regex.MatchResult,String>() {
            @Override
            public String apply(MatchResult t) {
                System.out.format("Group: %s%n", t.group(1));
                try {
                    return readAndParseFile("website" + t.group(1));
                } catch (IOException ex) {
                    log.warn("Cannot include SSI: %s", t.group(1), ex);
                    return "";
                }
            }
        });
        return content;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.format("HelpServlet: %s%n", request.getRequestURI());
        
        String fileName =
                FileUtil.getProgramPath()
                + request.getRequestURI().replaceFirst("/help2/", "help/");
        System.out.format("HelpServlet: %s%n", fileName);
        
        String content = readAndParseFile(fileName);
        
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setContentType(UTF8_TEXT_HTML);
        response.getWriter().write(content);
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
        return "Daniel Servlet";
    }// </editor-fold>


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HelplServlet.class);
}
