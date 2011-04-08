package jmri.web.miniserver;

import java.net.*;
import java.io.*;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import jmri.web.miniserver.servlet.echoservlet.EchoServlet;

/** A simple HTTP server that generates a Web page showing all
 *  of the data that it received from the Web client (usually
 *  a browser). To use this server, start it on the system of
 *  your choice, supplying a port number if you want something
 *  other than port 12080. Call this system server.com. Next,
 *  start a Web browser on the same or a different system, and
 *  connect to http://server.com:12080/whatever. The resultant
 *  Web page will show the data that your browser sent. For 
 *  debugging in servlet or CGI programming, specify 
 *  http://server.com:12080/whatever as the ACTION of your HTML
 *  form. You can send GET or POST data; either way, the
 *  resultant page will show what your browser sent.
 *<P>
 *  Adapted with permission from Core Web Programming from 
 *  Prentice Hall and Sun Microsystems Press,
 *  http://www.corewebprogramming.com/.
 *  &copy; 2001 Marty Hall and Larry Brown;
 *  may be freely used or adapted. 
 *
 * @author  Modifications by Bob Jacobsen  Copyright 2005, 2006
 * @version     $Revision: 1.13 $
 */

public class MiniServer extends NetworkServer {
    
    static java.util.ResourceBundle services = java.util.ResourceBundle.getBundle("jmri.web.miniserver.Services");

    protected int maxRequestLines = 50;
    protected String serverName = "MiniServer";
    
    /** 
     * Run standalone.
     * <p>
     * Supply a port number as a command-line
     *  argument. Otherwise, use port 12080.
     */
    
    public static void main(String[] args) {
        int port = Integer.parseInt(MiniServerManager.miniServerPreferencesInstance().getPort());

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch(NumberFormatException nfe) {}
        }
        new MiniServer(port, 0);
    }
    
    
    public MiniServer(int port, int maxConnections) {
        super(port, maxConnections);
        listen();
    }
    

    static String lastConnectionAddr = "";
    
    /** Overrides the NetworkServer handleConnection method to 
     *  invoke a particular servlet.
     */
    
    public void handleConnection(Socket server)
        throws IOException {

    	//log IP only for speed
        String connectionAddr = server.getInetAddress().toString();
        if (connectionAddr.equals(lastConnectionAddr)) {
        	if (log.isDebugEnabled()) log.debug(serverName + ": got connection from " +connectionAddr);  
        } else {
        	log.info(serverName + ": got connection from " +connectionAddr+" (only reporting changes)");
        }
        lastConnectionAddr = connectionAddr; 
        
        BufferedReader in = SocketUtil.getReader(server);
        
        ServletRequest req = new MiniServletRequest(in);
        ServletResponse res = new MiniServletResponse(server);
        
        // get the request string, being sure to be able to put Reader back
        in.mark(2000);   // If this is exceeded, probably should be using POST
        String line = in.readLine();

        try {
			in.reset();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
		}

        
        if (line != null) {
            try {
                // extract the path from the request, and use it to select a servlet
            	//    drop the GET or POST and the HTTP/1.1 parts
                String request = line.substring(Math.max(0, line.indexOf(" ")+1), line.lastIndexOf(" ")>0 ? line.lastIndexOf(" ") : line.length());
                //    then drop anything past the ?
                request = request.substring(0, request.lastIndexOf("?")>0 ? request.lastIndexOf("?") : request.length());
                if (log.isDebugEnabled()) log.debug("Request ["+request+"]");
            
                Servlet s = pickServlet(request);
            
                // invoke        
                s.service(req, res);
            } catch (Exception e) {
                log.error("exception handling request: "+e);
                e.printStackTrace();
            }
        } else {
            log.error("found no info in request");
        }
        
        server.close();
        
    }

    /**
     * Scan URL, trying to make the longest match against
     * the servlet properties.  
     * @return new Servlet object matching this request
     */
    public Servlet pickServlet(String name) {
        // Search for longest match
        String serviceClass;
        while (name.length()>0) {
            if (log.isDebugEnabled()) log.debug("Check ["+name+"]");
            try {
                serviceClass = services.getString(name);
                // found it!
                if (log.isDebugEnabled()) log.debug("Will invoke "+serviceClass);
                try {
                    return (Servlet)Class.forName(serviceClass).newInstance();
                } catch (ClassNotFoundException e1) {
                    log.error("Can't find class "+serviceClass+" "+e1);
                    return new EchoServlet();
                } catch (InstantiationException e2) {
                    log.error("Can't instantiate "+serviceClass+" "+e2);
                    return new EchoServlet();
                } catch (IllegalAccessException e2) {
                    log.error("Illegal access to "+serviceClass+" "+e2);
                    return new EchoServlet();
                }
            } catch (java.util.MissingResourceException e3) {
                // normal, not a problem
            } 
            // No match, drop last part of path and try again
            name = name.substring(0, Math.max(0,name.lastIndexOf("/")));  
        }
        
        // failed, return EchoServlet
        log.debug("class loading failed, returning EchoServlet by default");
        return new EchoServlet();
    }
    
    public String getLocalAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress().toString();
        } catch (java.net.UnknownHostException e) {
            return "(unknown host)";
        }
    }
    public int getPort() {
        return port;
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MiniServer.class.getName());    
}
