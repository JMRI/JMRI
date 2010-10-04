package jmri.web.miniserver;

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
 * @version     $Revision: 1.4 $
 * @since       2.9.4
 */

public class MiniServletContext implements javax.servlet.ServletContext {
    public MiniServletContext() {
    }
    
    public Object getAttribute(String name) { 
        return attributes.get(name);
    }
    public void removeAttribute(String name)  {
        attributes.remove(name);
    }
    
    public void setAttribute(String name, Object object) {
        attributes.put(name, object);
    }

    java.util.HashMap<String, Object> attributes = new java.util.HashMap<String, Object>();
    
    // following here are dummy/minimal implementations
    public java.util.Enumeration getAttributeNames() { return null; }
    public ServletContext getContext(String uripath) { return null; }
    public String getInitParameter(String name) { return null; }
    public java.util.Enumeration getInitParameterNames() { return null; }
    public int getMajorVersion() { return -1; }
    public String getMimeType(String file) { return null; }
    public int getMinorVersion() { return -1; }
    public RequestDispatcher getNamedDispatcher(String name) { return null; }
    public String getRealPath(String path) { return null; }
    public RequestDispatcher getRequestDispatcher(String path) { return null; }
    public java.net.URL getResource(String path) { return null; }
    public java.io.InputStream getResourceAsStream(String path) { return null; }
    public java.util.Set getResourcePaths(String path) { return null; }
    public String getServerInfo() { return null; }

    @Deprecated
    public Servlet getServlet(String name) { return null; }

    public String getServletContextName() { return null; }

    @Deprecated
    public java.util.Enumeration getServletNames()  { return null; }

    @Deprecated
    public java.util.Enumeration getServlets() { return null; }

    @Deprecated
    public void log(java.lang.Exception exception, java.lang.String msg)  {}

    public void log(java.lang.String msg)  {}
    public void log(java.lang.String message, java.lang.Throwable throwable)  {}
          
}
