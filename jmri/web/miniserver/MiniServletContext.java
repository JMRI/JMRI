package jmri.web.miniserver;

import java.net.*;
import java.io.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

/** 
 * Minimal function ServletContext
 * <p>
 * Although not the same as 
 * {@link javax.servlet.ServletContext},
 * this is meant to be method-signature compatible, so that
 * we can move to servlets later on.
 *
 * @author  Bob Jacobsen Copyright 2010
 * @version     $Revision: 1.1 $
 * @since       2.9.4
 */

public class MiniServletContext implements javax.servlet.ServletContext {
    public MiniServletContext() {
    }
    

    public Object getAttribute(java.lang.String name) { return null; }
    public java.util.Enumeration getAttributeNames() { return null; }
    public ServletContext getContext(java.lang.String uripath) { return null; }
    public String getInitParameter(java.lang.String name) { return null; }
    public java.util.Enumeration getInitParameterNames() { return null; }
    public int getMajorVersion() { return -1; }
    public String getMimeType(java.lang.String file) { return null; }
    public int getMinorVersion() { return -1; }
    public RequestDispatcher getNamedDispatcher(java.lang.String name) { return null; }
    public String getRealPath(java.lang.String path) { return null; }
    public RequestDispatcher getRequestDispatcher(java.lang.String path) { return null; }
    public java.net.URL getResource(java.lang.String path) { return null; }
    public java.io.InputStream getResourceAsStream(java.lang.String path) { return null; }
    public java.util.Set getResourcePaths(java.lang.String path) { return null; }
    public String getServerInfo() { return null; }

    @Deprecated
    public Servlet getServlet(java.lang.String name) { return null; }

    public String getServletContextName() { return null; }

    @Deprecated
    public java.util.Enumeration getServletNames()  { return null; }

    @Deprecated
    public java.util.Enumeration getServlets() { return null; }

    @Deprecated
    public void log(java.lang.Exception exception, java.lang.String msg)  {}

    public void log(java.lang.String msg)  {}
    public void log(java.lang.String message, java.lang.Throwable throwable)  {}
    public void removeAttribute(java.lang.String name)  {}
    public void setAttribute(java.lang.String name, java.lang.Object object) {}
          
}
