package jmri.web.servlet.xmlio;

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import java.util.StringTokenizer;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import javax.imageio.*;

import javax.swing.*;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import jmri.util.JmriJFrame;
import jmri.web.xmlio.*;

/** 
 * A simple servlet that returns a JMRI window as
 * a PNG image or enclosing HTML file.
 * <p>
 * The suffix of the request determines which.
 * <dl>
 * <dt>.html<dd>Returns a HTML file that displays the panel enabled for 
 *      clicking via server side image map; see the .properties file for the content
 * <dt>.png<dd>Just return the image
 * </dl>
 *<P>
 * The associated .properties file contains the HTML fragments used to 
 * form replies.
 *<P>
 *  Parts taken from Core Web Programming from 
 *  Prentice Hall and Sun Microsystems Press,
 *  http://www.corewebprogramming.com/.
 *  &copy; 2001 Marty Hall and Larry Brown;
 *  may be freely used or adapted. 
 *
 * @author  Modifications by Bob Jacobsen  Copyright 2005, 2006, 2008
 * @version     $Revision: 1.2 $
 */

public class XMLIOServlet implements Servlet, XmlIORequestor {

    String clickRetryTime = "1.0";
    String noclickRetryTime = "5.0";
    
    protected int maxRequestLines = 50;
    protected String serverName = "JFrameServer";
    
    public void destroy() {}
    
    public void init(javax.servlet.ServletConfig config) {}
    
    public String getServletInfo() { return ""; }
    
    public javax.servlet.ServletConfig getServletConfig() { return null; }

    public void service(ServletRequest req, ServletResponse res) throws java.io.IOException {
        
        // get the reader from the request
        BufferedReader in = req.getReader();
        
        // read in the info
        String[] inputLines = new String[maxRequestLines];

        int i=0;
        try {
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
        } catch (IOException e) {
            log.error("IO Exception reading request: "+e);
        }

        // get the writer from the response
        out = res.getWriter();
        
        // parse request
        String request = parseRequest(inputLines, i);

        if (builder == null) builder = jmri.jmrit.XmlFile.getBuilder(false);

        Reader reader = new StringReader(request);
        try {
            doc = builder.build(reader);
        } catch (JDOMException e1) {
            log.error("JDOMException on input: "+e1, e1);
        }
        Element root = doc.getRootElement();
        
        // start processing reply
        if (factory == null) factory = new XmlIOFactory();
        XmlIOServer srv = factory.getServer();

        // if list elements present, do immediate operation
        if (root.getChild("list") != null) {
            log.debug("immediate reply");
            Element result = null;
            try {
                result = srv.immediateRequest(root);
            } catch (jmri.JmriException e1) {
                log.error("JmriException while creating reply: "+e1, e1);
            }
            sendReply(doc);
            return;
        }        
        
        // do monitoring operation
        try {
            // start processing the request
            thread = Thread.currentThread();
            srv.monitorRequest(root, this);
            log.debug("stalling thread, waiting for reply");
            
            try {
                Thread.sleep(10000000000000L);  // really long
            } catch (InterruptedException e) {
                log.debug("Interrupted");
            }
            log.debug("thread resumes after reply");
            sendReply(doc);
        } catch (jmri.JmriException e1) {
            log.error("JmriException while creating reply: "+e1, e1);
        }
    }
    
    Document doc = null;
    PrintWriter out;
    Thread thread;
    
    public void monitorReply(Element e) {
        thread.interrupt();
    }
    
    protected void sendReply(Document doc) {
        // send reply header
        out.print("HTTP/1.0 200 OK\r\n");
        out.print("Server: JMRI\r\n");
        out.print("Content-Type: text/xml\r\n");
        out.print("Cache-Control: no-cache\r\n");
        out.println();
        
        // format and send reply
        if (fmt == null) {
            fmt = new XMLOutputter();
            fmt.setFormat(org.jdom.output.Format.getPrettyFormat());
        }
        
        try {
            fmt.output(doc, out);  // new element is within existing document
        } catch (IOException e) {
            log.error("IOException while in fmt.output: "+e,e);
        }
        
        // send dummy reply
        //out.println("<xmlio><item><type>sensor</type><name>IS1</name><value>3</value></item></xmlio>");
    }
    
    SAXBuilder builder = null;
    XMLOutputter fmt = null;
    XmlIOFactory factory = null;
    
    /**
     * Parse input lines to extract request
     */
    String parseRequest(String[] input, int len) {
        // expect "GET /key/Frame HTTP/1.1"
        //
        // remove GET and key from front
        log.debug("get request of "+len+" lines: "+input[0]);
        String part = input[0].substring(5, input[0].length());
        part = part.substring(part.indexOf('/'), part.length());
        
        // remove HTTP from back
        String rawRequest = part.substring(0, part.lastIndexOf(" HTTP"));
        
        // decode
        String request = "<error>";
        try {
            URI u = new URI(rawRequest);
            if (log.isDebugEnabled()) log.debug("URI ["+u+"]");
            request = u.getSchemeSpecificPart();
            // drop leading "/"
            request = request.substring(1, request.length());
        } catch (java.net.URISyntaxException e4) {
            log.error("error in URI: "+e4);
        }
        if (log.isDebugEnabled()) log.debug("request is ["+request+"]");
        
        return request;
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XMLIOServlet.class.getName());
}
