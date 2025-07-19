package jmri.web.servlet.help;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static jmri.web.servlet.ServletUtil.APPLICATION_JAVASCRIPT;
import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;
import jmri.util.FileUtil;

import org.eclipse.jetty.server.Request;
import org.openide.util.lookup.ServiceProvider;

/**
 * Parse server side include tags on web pages
 * @author Randall Wood     (C) 2014, 2016
 * @author Daniel Bergqvist (C) 2021
 * @author mstevetodd (C) 2023
 */
@WebServlet(name = "HelpSSIServlet",
        urlPatterns = {
            "/help",
            "/plugin"
        })
@ServiceProvider(service = HttpServlet.class)
public class HelpSSIServlet extends HttpServlet {

    private void handleRegularFile(String fileName, HttpServletResponse response) throws IOException {
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        String ext = fileName.substring(fileName.lastIndexOf('.')+1).toLowerCase();
        switch (ext) {
            case "svg":
                response.setContentType("image/svg");
                break;
            case "png":
                response.setContentType("image/png");
                break;
            case "gif":
                response.setContentType("image/gif");
                break;
            case "jpg":
            case "jpeg":
                response.setContentType("image/jpeg");
                break;
            case "js":
                response.setContentType(APPLICATION_JAVASCRIPT);
                break;
            default:
                response.setContentType("application/octet-stream");
        }
        byte[] b = new byte[1024];
        if (fileName.startsWith("/plugin/")) {
            String resourceName = fileName.substring("/plugin/".length());
            try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resourceName)) {
                if (inputStream != null) {
                    int byteRead;
                    while ((byteRead = inputStream.read(b)) != -1) {
                        response.getOutputStream().write(b, 0, byteRead);
                    }
                } else {
                    String error = String.format("--- ERROR: Plugin resource \"%s\" couldn't be found", resourceName);
                    response.getOutputStream().write(error.getBytes());
                    log.warn(error);
                }
            }
        } else {
            try (InputStream inputStream = new FileInputStream(fileName);) {
                int byteRead;
                while ((byteRead = inputStream.read(b)) != -1) {
                    response.getOutputStream().write(b, 0, byteRead);
                }
            }
        }
        response.getOutputStream().flush();
    }

    private String convertDotDotFolders(String theFileName, String path) {
        if (theFileName.startsWith("../")) {
            String[] paths = path.split("/");
            int numDotDots = 0;
            while (theFileName.startsWith("../")) {
                theFileName = theFileName.substring(3);
                numDotDots++;
            }
            if (numDotDots < paths.length) {
                StringBuilder sb = new StringBuilder();
                for (int i=0; i < (paths.length - numDotDots); i++) {
                    sb.append(paths[i]).append('/');
                }
                theFileName = sb.toString() + theFileName;
            } else {
                // We have more ../ than subfolders in path
                theFileName = '/' + theFileName;
            }
        }
        return theFileName;
    }

    private String quoteBackslash(String content) {
        // A single backslash needs to be replaced by a double backslash
        return content.replaceAll("\\\\", "\\\\\\\\");
    }

    private String readAndParseFile(String fileName) throws IOException {
        log.debug("readAndParseFile('{}')", fileName);

        int lastSlash = fileName.lastIndexOf('/');
        String path = lastSlash != -1 ? fileName.substring(0, lastSlash+1) : "";

        if (!fileName.startsWith("/plugin/")) {
            fileName = FileUtil.getProgramPath() + fileName;
        }

        String content;
        try {
            if (fileName.startsWith("/plugin/")) {
                String resourceName = fileName.substring("/plugin/".length());
                try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourceName)) {
                    if (is != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                            content = sb.toString();
                        }
                    } else {
                        content = String.format("%n<br>%nERROR: Plugin resource \"%s\" couldn't be found%n<br>%n", resourceName);
                        log.warn("Plugin resource \"{}\" couldn't be found", resourceName);
                    }
                }
            } else {
                content = new String(Files.readAllBytes(Paths.get(fileName)));
            }
        } catch (IOException ex) {
            content = "Exception thrown: " + ex.getMessage();
            log.warn("Cannot read file: {}", fileName, ex);
        }

        String serverSideIncludePattern = "<!--#include\\s*virtual=\"(.+?)\"\\s*-->";

        Pattern pattern = Pattern.compile(serverSideIncludePattern);
        Matcher matcher = pattern.matcher(content);

        content = matcher.replaceAll((MatchResult t) -> {
            String theFileName = t.group(1);
            try {
                theFileName = convertDotDotFolders(theFileName, path);

                if (path.startsWith("/")) {
                    if (theFileName.startsWith("/")) {
                        return quoteBackslash(readAndParseFile(theFileName));
                    } else {
                        return quoteBackslash(readAndParseFile(path + theFileName));
                    }
                } else {
                    return quoteBackslash(readAndParseFile("web/" + path + theFileName));
                }
            } catch (IOException ex) {
                log.warn("Cannot include SSI: {}", theFileName, ex);
                return "";
            }
        });
        return content;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (!uri.endsWith(".shtml")) {
            if (!(request instanceof Request)) throw new IllegalArgumentException("request is not a Request");
            log.debug("Handling regular file: '{}'", uri);
            String fileName = uri;
            if (!fileName.startsWith("/plugin/")) {
                fileName = FileUtil.getProgramPath() + uri;
            }
            handleRegularFile(fileName, response);
            return;
        }

        log.debug("Handling .shtml  file: '{}'", uri);
        String content = readAndParseFile(uri);

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
        return "Help SSI Servlet";
    }// </editor-fold>


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HelpSSIServlet.class);
}
