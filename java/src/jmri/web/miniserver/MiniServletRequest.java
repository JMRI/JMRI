package jmri.web.miniserver;

import java.io.BufferedReader;

/** 
 * Encapsulate a request.
 * <p>
 * Although not the same as 
 * javax.servlet.HttpServletRequest,
 * this is meant to be method-signature compatible, so that
 * we can move to servlets later on.
 *
 * @author  Bob Jacobsen Copyright 2008
 * @version     $Revision$
 */

public class MiniServletRequest implements javax.servlet.ServletRequest {
    public MiniServletRequest(BufferedReader in) {
        this.in = in;
    }
    
    BufferedReader in;
    
    public BufferedReader getReader() { return in; }
    
    
    // non-functional methods required for the interface
    
    public void setAttribute(String s, Object o) {
        reportUnimplemented("setAttribute");
    }
    
    public void removeAttribute(String s) {
        reportUnimplemented("removeAttribute");
    }
    
    public boolean isSecure() {
        reportUnimplemented("isSecure");
        return false;
    }
    
    public int getServerPort() {
        reportUnimplemented("getServerPort");
        return -1;
    }
    
    public javax.servlet.ServletInputStream getInputStream() {
        reportUnimplemented("getInputStream");
        return null;
    }
    
    public String getServerName() {
        reportUnimplemented("getServerName");
        return null;
    }
    
    public String getScheme() {
        reportUnimplemented("getScheme");
        return null;
    }
    
    public String getProtocol() {
        reportUnimplemented("getProtocol");
        return null;
    }
    
    public String getContentType() {
        reportUnimplemented("getContentType");
        return null;
    }
    
    public int getContentLength() {
        reportUnimplemented("getContentLength");
        return -1;
    }
    
    public java.util.Enumeration<String> getAttributeNames() {
        reportUnimplemented("getAttributeNames");
        return null;
    }

    public Object getAttribute(String s) {
        reportUnimplemented("getAttribute");
        return null;
    }

    public String getCharacterEncoding() {
        reportUnimplemented("getCharacterEncoding");
        return null;
    }

    public String[] getParameterValues(String s) {
        reportUnimplemented("getParameterValues");
        return null;
    }
    
    public java.util.Enumeration<String> getParameterNames() {
        reportUnimplemented("getParameterNames");
        return null;
    }
    
    public String getParameter(String s) {
        reportUnimplemented("getParameter");
        return null;
    }
    
    public javax.servlet.RequestDispatcher getRequestDispatcher(String s) {
        reportUnimplemented("getRequestDispatcher");
        return null;
    }
    
    public java.util.Enumeration<String> getLocales() {
        reportUnimplemented("getLocales");
        return null;
    }

    public java.util.Locale getLocale() {
        reportUnimplemented("getLocale");
        return null;
    }

    public String getRemoteHost() {
        reportUnimplemented("getRemoteHost");
        return null;
    }
    
    public String getRemoteAddr() {
        reportUnimplemented("getRemoteAddr");
        return null;
    }
    
    @Deprecated
    public String getRealPath(String s) {
        reportUnimplemented("getRealPath");
        return null;
    }
    
    public void setContentLength(int i) {
        reportUnimplemented("setContentLength");
    }

    void reportUnimplemented(String where) {
        AbstractMethodError e = new AbstractMethodError(where+" is not implemented");
        e.printStackTrace();
        throw e;
    }
}
