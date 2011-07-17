package jmri.web.miniserver;

import java.net.*;
import java.io.*;

/** 
 * Encapsulate a response to a request.
 * <p>
 * Although not the same as 
 * {@link javax.servlet.ServletResponse},
 * this is meant to be method-signature compatible, so that
 * we can move to servlets later on.
 *
 * @author  Bob Jacobsen Copyright 2008
 * @version     $Revision: 1.1 $
 */

public class MiniServletResponse implements javax.servlet.ServletResponse {
    public MiniServletResponse(Socket server) {
        this.server = server;
    }
    
    Socket server;
    
    public PrintWriter getWriter() { 
        try {
            return SocketUtil.getWriter(server); 
        } catch (IOException e) {
            System.out.println("getWriter error: "+e);
            return null;
        }
    }
    
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

    public void setLocale(java.util.Locale l) {
        reportUnimplemented("setLocale");
    }

    public java.util.Locale getLocale() {
        reportUnimplemented("getLocale");
        return null;
    }

    public void setContentType(String s) {
        reportUnimplemented("setContentType");
    }

    public void setContentLength(int i) {
        reportUnimplemented("setContentLength");
    }

    public void setBufferSize(int i) {
        reportUnimplemented("setBufferSize");
    }

    public int getBufferSize() {
        reportUnimplemented("getBufferSize");
        return -1;
    }

    public String getCharacterEncoding() {
        reportUnimplemented("getCharacterEncoding");
        return null;
    }

    public void flushBuffer() {
        reportUnimplemented("flushBuffer");
    }

    public void reset() {
        reportUnimplemented("reset");
    }

    public boolean isCommitted() {
        reportUnimplemented("isCommitted");
        return false;
    }

    void reportUnimplemented(String where) {
        AbstractMethodError e = new AbstractMethodError(where+" is not implemented");
        e.printStackTrace();
        throw e;
    }

}
