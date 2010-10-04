package jmri.web.miniserver;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import java.util.StringTokenizer;

import java.io.IOException;
import java.io.BufferedReader;

/** 
 * Implement some useful tools for a Servlet.
 *
 * @author  Bob Jacobsen Copyright 2008
 * @version     $Revision: 1.6 $
 */

public abstract class AbstractServlet implements Servlet, ServletConfig {

    abstract public void service(ServletRequest req, ServletResponse res)
        throws java.io.IOException;

    protected int maxRequestLines = 50;
    String[] inputLines = null;
        
    /**
     * As a service, get the requested URL.
     * <p>
     * Requires getInputLines has been called.
     */
    protected String getRequest() {
        for (int i=0; i<maxRequestLines; i++) {
            if (inputLines[i]!=null && inputLines[i].startsWith("GET ")) {
                int first = inputLines[i].indexOf(' ');
                int last = inputLines[i].indexOf(' ', first+1);
                return inputLines[i].substring(first+1, last);
            }
        }
        return null;
        
    }
    
    /**
     * As a service, parse the entire request into individual lines.
     */
    protected String[] getInputLines(BufferedReader in) 
            throws IOException {
        if (inputLines != null) return inputLines;
        
        inputLines = new String[maxRequestLines];
        int i;
        for (i=0; i<maxRequestLines; i++) {
            inputLines[i] = in.readLine();
            if(log.isDebugEnabled()) log.debug("line: "+inputLines[i]);
            if (inputLines[i] == null) // Client closed connection.
                break;
            if (inputLines[i].length() == 0) { // Blank line.
                if (usingPost(inputLines)) {
                    readPostData(inputLines, i, in);
                }
                break;
            }
        }
        
        return inputLines;
    }


    // dummy methods to avoid overloading issues
    
    public void destroy() {}
    
    public void init(javax.servlet.ServletConfig config) {}
    
    public String getServletInfo() { return ""; }
    
    public javax.servlet.ServletConfig getServletConfig() { return null; }

    // *********************************
    // Start dummy ServletConfig methods
    
    public String getInitParameter(String name) { return null; }
    
    public java.util.Enumeration<String> getInitParameterNames() { return null; }
    
    public ServletContext getServletContext() {
        if (context == null) context = new MiniServletContext();
        return context; 
    }
  
    public String getServletName() { return null; }
    
    // End dummy ServletConfig methods
    // *********************************
    
    // there is only one of these because the MiniServer only
    // implements one application context.
    static ServletContext context = null;
    
    // Normal Web page requests use GET, so this can simply
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
        int seenLength = in.read(postData, 0, contentLength);
        if (seenLength != contentLength) 
            log.warn("requested "+contentLength+" but saw "+seenLength+" bytes");
        inputs[++i] = new String(postData, 0, seenLength);
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractServlet.class.getName()); 
}
