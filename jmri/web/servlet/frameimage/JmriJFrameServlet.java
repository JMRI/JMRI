package jmri.web.servlet.frameimage;

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.StringTokenizer;
import jmri.util.JmriJFrame;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

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
 * @version     $Revision: 1.12 $
 */

public class JmriJFrameServlet implements Servlet {

    String clickRetryTime = "0.3";
    String noclickRetryTime = "5.0";
    
    protected int maxRequestLines = 50;
    protected String serverName = "JFrameServer";
    
    static java.util.ResourceBundle rb 
            = java.util.ResourceBundle.getBundle("jmri.web.servlet.frameimage.JmriJFrameServlet");
            
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
        PrintWriter out = res.getWriter();
        
        // parse request
        String frameName = parseRequest(inputLines, i);
        
        // remove any modifiers
        String modifiers = null;
        if (frameName.contains("?")) {
            modifiers = frameName.substring(frameName.indexOf("?"), frameName.length());
            if (modifiers.length()>0) modifiers = modifiers.substring(1, modifiers.length());
            frameName = frameName.substring(0,frameName.indexOf("?"));
        }
        
        // if the click time is being updated, find that and remove from modifiers
        if (modifiers != null && modifiers.startsWith("retry=")) {
            modifiers = modifiers.substring(6);
            int end = modifiers.indexOf("?");
            if (end<=0) end = modifiers.length();
            noclickRetryTime = modifiers.substring(0, end);
            modifiers = modifiers.substring(Math.min(end+1, modifiers.length()));
            if (modifiers.length() == 0) modifiers = null;
        }
        // remove any type suffix
        String suffix = null;
        if (frameName.contains(".")) {
            suffix = frameName.substring(frameName.lastIndexOf("."), frameName.length());
            if (suffix.length()>0) suffix = suffix.substring(1, suffix.length());
            frameName = frameName.substring(0,frameName.indexOf("."));
        }
        log.debug("requested frame +["+frameName+"] suffix ["+suffix+"] modifiers ["+modifiers+"]");
        
        // Find the frame
        JmriJFrame frame = JmriJFrame.getFrame(frameName);
        if (frame == null) {
            handleError("Can't find frame ["+frameName+"]", res);
            return;
        }
        if (!frameName.equals(frame.getTitle())) {
            log.warn("Request for ["+frameName+"] found title ["+frame.getTitle()+"], mismatched");
        }
        
        // If there's a click modifier, parse it and execute
        boolean click = false;
        if ( modifiers!=null && modifiers.contains(",")) try {
                int x = Integer.parseInt(modifiers.substring(0,modifiers.indexOf(",")));
                int y = Integer.parseInt(modifiers.substring(modifiers.indexOf(",")+1,modifiers.length()));
                if (log.isDebugEnabled()) log.debug("Attempt click at "+x+","+y);
                // log.debug("Component is "+frame.getContentPane().findComponentAt(x,y).toString());
                Component c = frame.getContentPane().findComponentAt(x,y);
                // ((javax.swing.JButton) frame.getContentPane().findComponentAt(x,y) ).doClick();
                click = true;
                clickDammit(frameName, c, x, y);
            } catch (Exception ec) {
                log.error("Exception in click code: "+ec);
            }

        // Send a reply depending on type
        if (suffix == null) 
            imageReply(frameName, out, frame, res);
        else if (suffix.toLowerCase().equals("png"))
            imageReply(frameName, out, frame, res);
        else if (suffix.toLowerCase().equals("html"))
            htmlReply(frameName, out, frame, res, click);
        else
            handleError("Can't handle suffix ["+suffix+"], use .png or .html", res);
    }
    
    void imageReply(String name, PrintWriter out, JmriJFrame frame, ServletResponse res ) 
            throws java.io.IOException {
        printHeader(out);
        putFrameImage(frame, res.getOutputStream());
    }
    
    void htmlReply(String name, PrintWriter out, JmriJFrame frame, ServletResponse res, boolean click ) {
        // 0 is host
        // 1 is name
        // 2 is retry in META tag, click or noclick retry
        // 3 is retry in next URL, future retry
        Object[] args = new String[] {"localhost", name, click?clickRetryTime:noclickRetryTime, noclickRetryTime};
        out.println(java.text.MessageFormat.format(rb.getString("StandardHeader"), args));
        out.println(java.text.MessageFormat.format(rb.getString("StandardDocType"), args));
        out.println(java.text.MessageFormat.format(rb.getString("StandardFront"), args));
        out.println(java.text.MessageFormat.format(rb.getString("StandardBack"), args));
    }
    
    void clickDammit(String name, Component c, int xg, int yg) {  // global positions
        int x = xg-c.getLocation().x;
        int y = yg-c.getLocation().y;
        
        // log.debug("component is "+c);
        if (log.isDebugEnabled()) log.debug("Local click at "+x+","+y);
        
        if (c.getClass().equals(JButton.class)) {
            ((JButton)c).doClick();
            return;
        } else if (c instanceof MouseListener) {
            if (log.isDebugEnabled()) log.debug("Invoke directly on MouseListener");
            sendClick((MouseListener)c, c, x, y);
            return;
        } else if (c instanceof jmri.jmrit.display.Positionable) {
            if (log.isDebugEnabled()) log.debug("Invoke directly on MouseListener");
            MouseEvent e = new MouseEvent(c,
                                          MouseEvent.MOUSE_PRESSED,
                                          0,      // time
                                          0,      // modifiers
                                          x,y,    // x, y not in this component?
                                          1,      // one click
                                          false   // not a popup
                                          );
            ((jmri.jmrit.display.Positionable)c).doMousePressed(e);
            e = new MouseEvent(c,
                                          MouseEvent.MOUSE_RELEASED,
                                          0,      // time
                                          0,      // modifiers
                                          x,y,    // x, y not in this component?
                                          1,      // one click
                                          false   // not a popup
                                          );
            ((jmri.jmrit.display.Positionable)c).doMouseReleased(e);
            return;
        } else {
            MouseListener[] la = c.getMouseListeners();
            if (log.isDebugEnabled()) log.debug("Invoke "+la.length+" contained mouse listeners");
            log.debug("component is "+c);
            for (int i = 0; i<la.length; i++) {
                sendClick(la[i], c, x, y);
            }
            return;
        }
    }
    
    private void sendClick(MouseListener m, Component c, int x, int y) {
        MouseEvent e = new MouseEvent(c,
                                      MouseEvent.MOUSE_PRESSED,
                                      0,      // time
                                      0,      // modifiers
                                      x,y,    // x, y not in this component?
                                      1,      // one click
                                      false   // not a popup
                                      );
        m.mousePressed(e);
        e = new MouseEvent(c,
                                      MouseEvent.MOUSE_RELEASED,
                                      0,      // time
                                      0,      // modifiers
                                      x,y,    // x, y not in this component?
                                      1,      // one click
                                      false   // not a popup
                                      );
        m.mouseReleased(e);
        e = new MouseEvent(c,
                                      MouseEvent.MOUSE_CLICKED,
                                      0,      // time
                                      0,      // modifiers
                                      x,y,    // x, y not in this component?
                                      1,      // one click
                                      false   // not a popup
                                      );
        m.mouseClicked(e);
    
    }
    /**
     * Handle an error by returning an error message
     */
    void handleError(String error, ServletResponse res) 
            throws java.io.IOException {
        PrintWriter out = res.getWriter();

        out.println
            ("HTTP/1.0 200 OK\r\n" +
             "Server: " + serverName + "\r\n" +
             "Content-Type: text/html\r\n" +
             "\r\n" +
             "<!DOCTYPE HTML PUBLIC " +
             "\"-//W3C//DTD HTML 4.0 Transitional//EN\">\n" +
             "<HTML>\n" +
             "<HEAD>\n" +
             "  <TITLE>" + error + "</TITLE>\n" +
             "</HEAD>\n" +
             "\n" +
             "<BODY BGCOLOR=\"#FDF5E6\">\n" +
             "<H1 ALIGN=\"CENTER\">" + error +
             "\n" +
             "</BODY>\n" +
             "</HTML>\n");        
    }
    
    /**
     * Parse input lines to find 
     * frame name.
     */
    String parseRequest(String[] input, int len) {
        // expect "GET /key/Frame HTTP/1.1"
        //
        // remove GET and key from front
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
    
    /** 
     * Get the frame graphics as png
     */
    void putFrameImage(JmriJFrame frame, OutputStream out) {
        try {
            BufferedImage image 
                = new BufferedImage(frame.getContentPane().getWidth(), 
                                    frame.getContentPane().getHeight(), 
                                    BufferedImage.TYPE_INT_RGB);
            frame.getContentPane().paint(image.createGraphics());
            
            // send the image as a png, "jpg" also available
            ImageIO.write(image, "png", out);
            out.flush();
            
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
    
    /**  
     *  Copy an image to the output buffer
     *<P>
     *  Taken from Core Servlets and JavaServer Pages
     *  from Prentice Hall and Sun Microsystems Press,
     *  http://www.coreservlets.com/.
     *  &copy; 2000 Marty Hall; may be freely used or adapted.
     */
	private void copyImage(OutputStream out) throws IOException {
        BufferedInputStream in =
            new BufferedInputStream(new FileInputStream("resources/decoderpro.gif"));
        int imageByte;
        int count = 0;
        while((imageByte = in.read()) != -1) {
            out.write((byte)(imageByte&0xFF));
            count++;
        }
        in.close();
        out.flush();
        if (log.isDebugEnabled()) log.debug("wrote "+count+" bytes");
    }
    
    // Send standard HTTP response for image/gif type
    // Use HTTP 1.0 for compatibility with all clients.
    
    private void printHeader(PrintWriter out) {
        out.print
            ("HTTP/1.0 200 OK\r\n" +
             "Server: " + serverName + "\r\n" +
             "Content-Type: image/png\r\n" +
             "\r\n");
        out.flush();
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
        int length = in.read(postData, 0, contentLength);
        inputs[++i] = new String(postData, 0, length);
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JmriJFrameServlet.class.getName());
}
