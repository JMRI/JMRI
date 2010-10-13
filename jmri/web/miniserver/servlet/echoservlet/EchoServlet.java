package jmri.web.miniserver.servlet.echoservlet;

import java.io.*;

import java.util.StringTokenizer;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import jmri.web.miniserver.AbstractServlet;

/** A simple HTTP service that generates a Web page showing all
 *  of the data that it received from the Web client (usually
 *  a browser). 
 *<P>
 *  Taken in part from Core Web Programming from 
 *  Prentice Hall and Sun Microsystems Press,
 *  http://www.corewebprogramming.com/.
 *  &copy; 2001 Marty Hall and Larry Brown;
 *  may be freely used or adapted. 
 *
 * @author  Bob Jacobsen  Copyright 2005, 2006
 * @version     $Revision: 1.4 $
 */

public class EchoServlet extends AbstractServlet {

    public void service(ServletRequest req, ServletResponse res) 
            throws java.io.IOException {
        
        // get the reader from the request
        BufferedReader in = req.getReader();
        
        // read in the info
        String[] inputLines = new String[maxRequestLines];
        int i=0;

        for (i=0; i<maxRequestLines; i++) {
            inputLines[i] = in.readLine();
            if (inputLines[i] == null) // Client closed connection.
                break;
            if (inputLines[i].length() == 0) { // Blank line.
                if (usingPost(inputLines)) {
                    readPostData(inputLines, i, in);
                    i = i + 2;
                }
                break;
            }
        }
        
        // get the writer from the response
        PrintWriter out = res.getWriter();
        
        // start output with the header
        printHeader(out);
        for (int j=0; j<i; j++) {
            out.println(inputLines[j]);
        }
        printTrailer(out);
    }
    
    public void destroy() {}
    
    public void init(javax.servlet.ServletConfig config) {}
    
    public String getServletInfo() { return ""; }
    
    public javax.servlet.ServletConfig getServletConfig() { return null; }

    // Send standard HTTP response and top of a standard Web page.
    // Use HTTP 1.0 for compatibility with all clients.
    
    private void printHeader(PrintWriter out) {
        out.println(htmlStrings.getString("StandardHeader"));
        out.println(htmlStrings.getString("StandardDocType"));
        out.println(htmlStrings.getString("EchoFront"));
    }
    
    // Print bottom of a standard Web page.
    
    static java.util.ResourceBundle htmlStrings = java.util.ResourceBundle.getBundle("jmri.web.miniserver.Html");

    private void printTrailer(PrintWriter out) {
        out.println(htmlStrings.getString("EchoBack"));
    }
    
    // Normal Web page requests use GET, so this server can simply
    // read a line at a time. However, HTML forms can also use 
    // POST, in which case we have to determine the number of POST
    // bytes that are sent so we know how much extra data to read
    // after the standard HTTP headers.
    
    private boolean usingPost(String[] inputs) {
        return(inputs[0].toUpperCase().startsWith("POST"));
    }
    
    private void readPostData(String[] inputs, int i,
                              BufferedReader in)
        throws IOException {
        int contentLength = contentLength(inputs);
        char[] postData = new char[contentLength];
        in.read(postData, 0, contentLength);
        inputs[++i] = new String(postData, 0, contentLength);
    }
    
    // Given a line that starts with Content-Length,
    // this returns the integer value specified.
    
    private int contentLength(String[] inputs) {
        String input;
        for (int i=0; i<inputs.length; i++) {
            if (inputs[i].length() == 0)
                break;
            input = inputs[i].toUpperCase();
            if (input.startsWith("CONTENT-LENGTH"))
                return(getLength(input));
        }
        return(0);
    }
    
    private int getLength(String length) {
        StringTokenizer tok = new StringTokenizer(length);
        tok.nextToken();
        return(Integer.parseInt(tok.nextToken()));
    }
}
