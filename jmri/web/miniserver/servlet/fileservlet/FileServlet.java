package jmri.web.miniserver.servlet.fileservlet;

import java.io.*;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import jmri.web.miniserver.AbstractServlet;

/** 
 * A simple HTTP servlet that returns the contents of a file.
 * The MIME type is taken from a property file, as is
 * the path map.
 *<P>
 *  Parts taken from Core Web Programming from 
 *  Prentice Hall and Sun Microsystems Press,
 *  http://www.corewebprogramming.com/.
 *  &copy; 2001 Marty Hall and Larry Brown;
 *  may be freely used or adapted. 
 *
 * @author  Modifications by Bob Jacobsen  Copyright 2008
 * @version $Revision: 1.9 $
 */

public class FileServlet extends AbstractServlet {

    static java.util.ResourceBundle types = java.util.ResourceBundle.getBundle("jmri.web.miniserver.servlet.fileservlet.FileServletTypes");
    static java.util.ResourceBundle paths = java.util.ResourceBundle.getBundle("jmri.web.miniserver.servlet.fileservlet.FileServletPaths");

    public void service(ServletRequest req, ServletResponse res) 
        throws java.io.IOException {
        
        // get the reader from the request
        BufferedReader in = req.getReader();
        
        // get the writer from the response
        PrintWriter out = res.getWriter();
        
        // get input        
        getInputLines(in);
        String filename = getFilename(getRequest().substring(1)); // drop leading /
        if (log.isDebugEnabled()) log.debug("resolve to filename: "+filename);
        
        // silently drop requests that don't satisfy security check
        if (!isSecurityLimitOK(filename)) {
            log.info("Dropping unauthorized request for \""+filename+"\"");
            return;
        }
        // now reply
        if (! isDirectory(filename) ) {
            printHeader(out, getMimeType(filename));
            copyFileContent(filename, res.getOutputStream());
        } else {
            if (filename.endsWith("/")) {
                // show content
                printHeader(out, "text/html");
                createDirectoryContent(filename, res.getOutputStream());
            } else {
                // send redirect
                printHeader(out, "text/html");
                OutputStreamWriter writer = new OutputStreamWriter(res.getOutputStream());
                try {
                    writer.write("<head>\n");
                    writer.write("<META HTTP-EQUIV=\"PRAGMA\" CONTENT=\"NO-CACHE\">");
                    writer.write("<META HTTP-EQUIV=\"Refresh\" CONTENT=\"0; URL="+filename+"/ \">");
                    writer.write("</html>");
                    writer.flush();
                } finally {
                    writer.close();
                }
                out.flush();
            }
        }        
    }
    
    public void destroy() {}
    
    public void init(javax.servlet.ServletConfig config) {}
    
    public String getServletInfo() { return ""; }
    
    public javax.servlet.ServletConfig getServletConfig() { return null; }


    /**
     * Items to be substituted into FileServletPaths
     */
    Object[] args;


    /** 
     * Check if the file is a directory
     */
    boolean isDirectory(String filename) {
        return new File(filename).isDirectory();
    }
    
    /**
     * Convert the request to a 
     * filename using the FileServletPaths
     * property list.  
     *<p>
     * This takes pieces off the right side of the 
     * request until it matches an entry.
     */
    String getFilename(String name){
        // initialize substitutions
        if (args == null)
            args = new String[] {jmri.jmrit.XmlFile.prefsDir()};

        // strip any trailing ? info
        int qm = name.indexOf('?');
        if (qm != -1) name = name.substring(0, qm);

        String initialName = name;
        // Seach for longest match
        while (name.length()>0) {
            // name is the piece we're going to replace
            if (log.isDebugEnabled()) log.debug("Check ["+name+"]");
            try {
                String prefix = paths.getString(name);
                // found it!
                if (log.isDebugEnabled()) log.debug("Found prefix "+prefix);

                // do substitution
                prefix = java.text.MessageFormat.format(prefix, args);
                
                // check for special case of complete substitution
                if (name.length() == initialName.length()) {
                    return prefix;
                }

                // now combine
                int offset = name.length()+1;
                if (prefix.length()>0 && !prefix.endsWith("/")) prefix = prefix+"/";
                String result = prefix+initialName.substring(Math.min(offset,initialName.length()), initialName.length());
                log.debug("getFilename result: "+result);
                return result;
            } catch (java.util.MissingResourceException e3) {
                // normal, not a problem
                // just proceed
            } 
            // No luck, remove last token
            int last = name.lastIndexOf("/");
            if (last <0) // nothing to remove
                break;
            name = name.substring(0, last);  
        }
        
        log.debug("No match, return original filename: "+initialName);
        return initialName;
    }
    
    /**
     * From a path name ("foo/file.bif"), 
     * determine the mime type.
     *<p>
     * Works with the FileServlet property file
     */
    String getMimeType(String name) {
        // strip any trailing ? info
        int qm = name.indexOf('?');
        if (qm != -1) name = name.substring(0, qm);

        // Seach for longest match
        while (name.length()>0) {
            if (log.isDebugEnabled()) log.debug("Check ["+name+"]");
            try {
                String type = types.getString(name);
                // found it!
                if (log.isDebugEnabled()) log.debug("Found type "+type);
                return type;
            } catch (java.util.MissingResourceException e3) {
                // normal, not a problem
                // just proceed
            } 
            // No luck, try next
            int next = name.indexOf(".",1);
            if (next < 0) break;  // no more dots
            name = name.substring(next, name.length());  
        }
        
        log.debug("No type matches "+name+", return default text type");
        return "text";
    }
    
    static String canonicalWD;
    static String canonicalPrefs;
    
    /**
     * Do security check as to whether file should be served
     *<p>
     * @return true if OK to present file
     */
    boolean isSecurityLimitOK(String filename) 
                throws java.io.IOException {
        if (canonicalWD == null)
            canonicalWD = new File("").getCanonicalPath();
        if (canonicalPrefs == null)
            canonicalPrefs = new File(jmri.jmrit.XmlFile.prefsDir()).getCanonicalPath();

        String thisPath = new File(filename).getCanonicalPath();
        if (thisPath.startsWith(canonicalWD)) return true;
        if (thisPath.startsWith(canonicalPrefs)) return true;
        return false;
    }
    
    /**  
     *  Taken in part from Core Servlets and JavaServer Pages
     *  from Prentice Hall and Sun Microsystems Press,
     *  http://www.coreservlets.com/.
     *  &copy; 2000 Marty Hall; may be freely used or adapted.
     */
    protected void copyFileContent(String filename, OutputStream out) throws IOException {
        InputStream in = null;
        try {
            // get file contents
            in = new BufferedInputStream(new FileInputStream(filename));
                
            // write out
            int imageByte;
            int count = 0;
            while((imageByte = in.read()) != -1) {
                out.write((byte)(imageByte&0xFF));
                count++;
            }
            if (log.isDebugEnabled()) log.debug("wrote "+count+" bytes");
        } catch (java.io.FileNotFoundException efnf) {
            log. warn("file not found: "+filename);
        } finally {
            if (in != null)
                in.close();
            out.flush();
        }
    }
    
    protected void createDirectoryContent(String filename, OutputStream out) throws IOException {
        log.debug("return directory listing");
        java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance();
        File dir = new File(filename);
        OutputStreamWriter writer = new OutputStreamWriter(out);
        writer.write("<body>\n");
        
        writer.write("<table><tr><th>Name</th><th>Last modified</th><th>Size</th></tr><tr><th colspan=\"5\"><hr></th></tr>");

        try {
            // write out directory
            for ( File f : dir.listFiles()) {
                // writer.write("<tr><td><a href=\""+f.getName()+"\">"+f.getName()+"</a></td></tr>\n");

                writer.write("<tr><td><a href=\""
                    +f.getName()+(f.isDirectory() ? "/" : "")+"\">"
                    +f.getName()+(f.isDirectory() ? "/" : "")+"</a></td><td align=\"right\">"
                    +df.format(new java.util.Date(f.lastModified()))
                    +"</td><td align=\"right\">"+f.length()+"</td></tr>");

            }
            writer.write("<tr><th colspan=\"5\"><hr></th></tr></table>");
            writer.write("<address>JMRI "+jmri.Version.name()+" Mini Server</address>" );
            writer.write("</body></html>");

        } finally {
            writer.flush();
            out.flush();
        }
    }
    
    // Send standard HTTP response for image/gif type
    // Use HTTP 1.0 for compatibility with all clients.
    
    protected void printHeader(PrintWriter out, String mime) {
        out.print
            ("HTTP/1.0 200 OK\r\n" +
             "Server: FileServlet\r\n" +
             "Content-Type: "+mime+"\r\n" +
             "\r\n");
        out.flush();
    }
    
    // initialize logging
    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileServlet.class.getName());
}
