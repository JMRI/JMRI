package jmri.web.miniserver;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;
import javax.servlet.*;

/**
 * Encapsulate a request. <p> Although not the same as
 * javax.servlet.HttpServletRequest, this is meant to be method-signature
 * compatible, so that we can move to servlets later on.
 *
 * @author Bob Jacobsen Copyright 2008
 * @version $Revision$
 * @deprecated
 */
public class MiniServletRequest implements javax.servlet.ServletRequest {

    public MiniServletRequest(BufferedReader in) {
        this.in = in;
    }
    BufferedReader in;

    @Override
    public BufferedReader getReader() {
        return in;
    }

    // non-functional methods required for the interface
    @Override
    public void setAttribute(String s, Object o) {
        reportUnimplemented("setAttribute");
    }

    @Override
    public void removeAttribute(String s) {
        reportUnimplemented("removeAttribute");
    }

    @Override
    public boolean isSecure() {
        reportUnimplemented("isSecure");
        return false;
    }

    @Override
    public int getServerPort() {
        reportUnimplemented("getServerPort");
        return -1;
    }

    @Override
    public ServletInputStream getInputStream() {
        reportUnimplemented("getInputStream");
        return null;
    }

    @Override
    public String getServerName() {
        reportUnimplemented("getServerName");
        return null;
    }

    @Override
    public String getScheme() {
        reportUnimplemented("getScheme");
        return null;
    }

    @Override
    public String getProtocol() {
        reportUnimplemented("getProtocol");
        return null;
    }

    @Override
    public String getContentType() {
        reportUnimplemented("getContentType");
        return null;
    }

    @Override
    public int getContentLength() {
        reportUnimplemented("getContentLength");
        return -1;
    }

    @Override
    public java.util.Enumeration<String> getAttributeNames() {
        reportUnimplemented("getAttributeNames");
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        reportUnimplemented("getAttribute");
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        reportUnimplemented("getCharacterEncoding");
        return null;
    }

    @Override
    public String[] getParameterValues(String s) {
        reportUnimplemented("getParameterValues");
        return null;
    }

    @Override
    public java.util.Enumeration<String> getParameterNames() {
        reportUnimplemented("getParameterNames");
        return null;
    }

    @Override
    public String getParameter(String s) {
        reportUnimplemented("getParameter");
        return null;
    }

    @Override
    public javax.servlet.RequestDispatcher getRequestDispatcher(String s) {
        reportUnimplemented("getRequestDispatcher");
        return null;
    }

    @Override
    public java.util.Enumeration<Locale> getLocales() {
        reportUnimplemented("getLocales");
        return null;
    }

    @Override
    public java.util.Locale getLocale() {
        reportUnimplemented("getLocale");
        return null;
    }

    @Override
    public String getRemoteHost() {
        reportUnimplemented("getRemoteHost");
        return null;
    }

    @Override
    public String getRemoteAddr() {
        reportUnimplemented("getRemoteAddr");
        return null;
    }

    @Deprecated
    @Override
    public String getRealPath(String s) {
        reportUnimplemented("getRealPath");
        return null;
    }

    public void setContentLength(int i) {
        reportUnimplemented("setContentLength");
    }

    void reportUnimplemented(String where) {
        throw new AbstractMethodError(where + " is not implemented");
    }

    @Override
    public void setCharacterEncoding(String string) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRemotePort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLocalName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLocalAddr() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AsyncContext startAsync(ServletRequest sr, ServletResponse sr1) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAsyncStarted() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAsyncSupported() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
