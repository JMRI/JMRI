package jmri.web.servlet.help;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
// import jmri.web.servlet.directory.DirectoryHandler;

import org.eclipse.jetty.server.Request;
// import org.eclipse.jetty.server.handler.ResourceHandler;
import org.openide.util.lookup.ServiceProvider;

/**
 * Parse server side include tags on web pages
 * @author Randall Wood     (C) 2014, 2016
 * @author Daniel Bergqvist (C) 2021
 */
@WebServlet(name = "HomeServlet",
        urlPatterns = {
            "/help"
//            "/help_ssi"
        })
@ServiceProvider(service = HttpServlet.class)
public class HelpSSIServlet extends HttpServlet {

//    private final ResourceHandler programHandler;

//    public HelpSSIServlet() {
//        programHandler = new DirectoryHandler(FileUtil.getAbsoluteFilename("F:\\Projekt\\Java\\GitHub\\JMRI\\"));
//        programHandler = new DirectoryHandler(FileUtil.getAbsoluteFilename("program:help"));
//    }

    private void readFile(String fileName, HttpServletResponse response) throws IOException {
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        String ext = fileName.substring(fileName.lastIndexOf('.')+1).toLowerCase();
        switch (ext) {
            case "svg":
                log.error("Image svg: {}", fileName);
                response.setContentType("image/svg");
                break;
            case "png":
                log.error("Image png: {}", fileName);
                response.setContentType("image/png");
                break;
            case "gif":
                log.error("Image gif: {}", fileName);
                response.setContentType("image/gif");
                break;
            case "jpg":
                log.error("Image jpeg: {}", fileName);
                response.setContentType("image/jpeg");
                break;
            case "jpeg":
                log.error("Image jpeg: {}", fileName);
                response.setContentType("image/jpeg");
                break;
            case "js":
                log.error("Javascript: {}", fileName);
                response.setContentType(APPLICATION_JAVASCRIPT);
                break;
            default:
                log.error("Image binary: {}", fileName);
                response.setContentType("application/octet-stream");
        }
        byte[] b = new byte[1024];
        try (InputStream inputStream = new FileInputStream(fileName);) {
            int byteRead;
            while ((byteRead = inputStream.read(b)) != -1) {
//                log.error("Read {} bytes from {}", byteRead, fileName);
                response.getOutputStream().write(b, 0, byteRead);
            }
        }
        response.getOutputStream().flush();
    }

    private String convertDotDotFolders(String theFileName, String path) {
        if (theFileName.startsWith("../")) {
            log.error("convertDotDotFolders: {}, {}", theFileName, path);
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
        log.error("convertDotDotFolders: result: {}", theFileName);
        return theFileName;
    }

    private String readAndParseFile(String fileName, String origPath, boolean helpFolder) throws IOException {
        log.error("File: {}, {}", fileName, FileUtil.getProgramPath() + fileName);

        int lastSlash = fileName.lastIndexOf('/');
        String path = lastSlash != -1 ? fileName.substring(0, lastSlash+1) : "";
        log.error("Path: {}", path);

        if (origPath == null) {
            origPath = path;
        }

//        if ("web/../../../../Header.shtml".equals(fileName)) {
//            fileName = "Header.shtml";
//        }

        fileName = FileUtil.getProgramPath() + fileName;

        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException ex) {
            content = "Exception thrown: " + ex.getMessage();
            log.warn("Cannot read file: {}", fileName, ex);
        }

        String serverSideIncludePattern = "<!--#include\\s*virtual=\"(.+?)\"\\s*-->";

        Pattern pattern = Pattern.compile(serverSideIncludePattern);
        Matcher matcher = pattern.matcher(content);
        String fn = fileName;
        String opath = origPath;

        content = matcher.replaceAll((MatchResult t) -> {
            String theFileName = t.group(1);
            log.error("Group: {}", theFileName);
            try {
                theFileName = convertDotDotFolders(theFileName, opath);

                if (theFileName.startsWith("/") && theFileName.lastIndexOf('/') == 0) {
                    // theFileName contains one and only one slash
                    log.error("Filename starts with /");
                    return readAndParseFile("web/website" + theFileName, opath, false);
                } else if (theFileName.startsWith("./help/") || helpFolder) {
                    log.error("Filename starts with ./help/ or is helpFolder");
                    return readAndParseFile(theFileName, opath, true);
//                    return readAndParseFile("./" + theFileName);
                } else {
                    log.error("Filename starts with other: fileName: {}, path: {}, theFileName: {}", fn, path, theFileName);
                    if (opath.startsWith("/")) {
                        log.error("readAndParseFile AAA: file: {}, opath: {}", theFileName, opath);
                        if (theFileName.startsWith("/")) {
                            return readAndParseFile(theFileName, opath, false);
                        } else {
                            return readAndParseFile(opath + theFileName, opath, false);
                        }
                    } else {
                        log.error("readAndParseFile BBB: file: {}, opath: {}", "web/" + path + theFileName, opath);
                        return readAndParseFile("web/" + path + theFileName, opath, false);
                    }
                }
            } catch (IOException ex) {
                log.warn("Cannot include SSI: {}", theFileName, ex);
                return "";
            }
        });
        return content;
    }

    private String convertJavascriptLinks(String content) {
        String serverSideIncludePattern = "<script src=\"(/js/.+?)\"></script>";

        Pattern pattern = Pattern.compile(serverSideIncludePattern);
        Matcher matcher = pattern.matcher(content);
        content = matcher.replaceAll((MatchResult t) -> {
            return "DANIEL_website" + t.group(1);
/*
            String theFileName = t.group(1);
//            log.error("Group: {}", theFileName);
            try {
                if (theFileName.startsWith("/") || helpFolder) {
                    log.error("Filename starts with /");
                    return readAndParseFile("web/website/" + theFileName, false);
                } else if (theFileName.startsWith("./help/") || helpFolder) {
                    log.error("Filename starts with ./help/ or is helpFolder");
                    return readAndParseFile(theFileName, true);
//                    return readAndParseFile("./" + theFileName);
                } else {
                    log.error("Filename starts with other");
                    return readAndParseFile("web/" + theFileName, false);
//                    return readAndParseFile(theFileName, false);
                }
            } catch (IOException ex) {
                log.warn("Cannot include SSI: {}", theFileName, ex);
                return "";
            }
*/
        });
        return content;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        log.error("HelpServlet: {}", request.getRequestURI());

        if (!request.getRequestURI().endsWith(".shtml")) {
            if (!(request instanceof Request)) throw new IllegalArgumentException("request is not a Request");
//            Request r = (Request) request;
            log.error("target: {}", request.getRequestURI());
            try {
//                programHandler.handle(request.getRequestURI(), r, request, response);
                String fileName = FileUtil.getProgramPath() + request.getRequestURI();
                readFile(fileName, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
//            String newUrl = request.getRequestURI().replaceFirst("/help2/", "/help/");
//            log.error("newUrl: {}", newUrl);
//            response.sendRedirect(newUrl);
        }

//        log.error("HelpServlet: {}", request.getRequestURI());

        String content = readAndParseFile(request.getRequestURI(), null, false);

        content = convertJavascriptLinks(content);

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


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HelpSSIServlet.class);
}
