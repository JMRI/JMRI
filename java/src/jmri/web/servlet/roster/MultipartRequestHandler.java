package jmri.web.servlet.roster;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import org.eclipse.jetty.server.Request;

/**
 * helper class for getting uploaded files which are returned as a list object
 * based on examples at http://hmkcode.com/java-servlet-jquery-file-upload/ 
 *
 */
public class MultipartRequestHandler {

    private static final MultipartConfigElement MULTI_PART_CONFIG = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));

    public static List<FileMeta> uploadByJavaServletAPI(HttpServletRequest request) throws IOException, ServletException {

        List<FileMeta> files = new LinkedList<FileMeta>();

        if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
            request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, MULTI_PART_CONFIG);
        }

        // 1. Get all parts and the flag
        Collection<Part> parts = request.getParts();
        //set replace flag from parameter
        boolean fileReplace = request.getParameter("fileReplace").equals("true");
        //set replace flag from parameter
        String rosterGroup = request.getParameter("rosterGroup");

        // deal with each each part
        FileMeta temp = null;
        for (Part part : parts) {

            // if part is multiparts "file"
            if (part.getContentType() != null) {
                
                // populate a new FileMeta object
                temp = new FileMeta();
                temp.setFileName(getFilename(part));
                temp.setFileSize(part.getSize() / 1024 + " Kb");
                temp.setFileType(part.getContentType());
                temp.setContent(part.getInputStream());
                temp.setFileReplace(fileReplace);
                temp.setRosterGroup(rosterGroup);

                // 3.3 Add created FileMeta object to List<FileMeta> files
                files.add(temp);

            }
        }
        return files;
    }

    // this method is used to get file name out of request headers
    // 
    private static String getFilename(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
            }
        }
        return null;
    }
}
