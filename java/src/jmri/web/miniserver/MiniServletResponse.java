package jmri.web.miniserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/** 
 * Encapsulate a response to a request.
 * <p>
 * Although not the same as 
 * {@link javax.servlet.ServletResponse},
 * this is meant to be method-signature compatible, so that
 * we can move to servlets later on.
 *
 * @author  Bob Jacobsen Copyright 2008
 * @version     $Revision$
 * @deprecated
 */

public class MiniServletResponse implements javax.servlet.ServletResponse {
    public MiniServletResponse(Socket server) {
        this.server = server;
    }
    
    Socket server;
    
    @Override
    public PrintWriter getWriter() { 
        try {
            return SocketUtil.getWriter(server); 
        } catch (IOException e) {
            System.out.println("getWriter error: "+e);
            return null;
        }
    }
    
    @Override
    public javax.servlet.ServletOutputStream getOutputStream() { 
        try {
            MiniServletOutputStream str = new MiniServletOutputStream(server.getOutputStream());
            return str;
        } catch (IOException e) {
            System.out.println("getOutputStream error: "+e);
            return null;
        }
    }

    // non-functional methods required for the interface

    @Override
    public void setLocale(java.util.Locale l) {
        reportUnimplemented("setLocale");
    }

    @Override
    public java.util.Locale getLocale() {
        reportUnimplemented("getLocale");
        return null;
    }

    @Override
    public void setContentType(String s) {
        reportUnimplemented("setContentType");
    }

    @Override
    public void setContentLength(int i) {
        reportUnimplemented("setContentLength");
    }

    @Override
    public void setBufferSize(int i) {
        reportUnimplemented("setBufferSize");
    }

    @Override
    public int getBufferSize() {
        reportUnimplemented("getBufferSize");
        return -1;
    }

    @Override
    public String getCharacterEncoding() {
        reportUnimplemented("getCharacterEncoding");
        return null;
    }

    @Override
    public void flushBuffer() {
        reportUnimplemented("flushBuffer");
    }

    @Override
    public void reset() {
        reportUnimplemented("reset");
    }

    @Override
    public boolean isCommitted() {
        reportUnimplemented("isCommitted");
        return false;
    }

    void reportUnimplemented(String where) {
        throw new AbstractMethodError(where+" is not implemented");
    }

    @Override
    public String getContentType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setCharacterEncoding(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resetBuffer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
