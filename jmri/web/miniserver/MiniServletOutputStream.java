package jmri.web.miniserver;

import java.io.OutputStream;

/** 
 * Although not the same as 
 * {@link javax.servlet.ServletOutputStream},
 * this is meant to be method-signature compatible, so that
 * we can move to servlets later on.
 *
 * @author  Bob Jacobsen Copyright 2008
 * @version     $Revision: 1.1 $
 */

public class MiniServletOutputStream extends javax.servlet.ServletOutputStream {
    public MiniServletOutputStream(OutputStream out) {
        this.out = out;
    }
    
    OutputStream out;
    
    public void write(int val) 
        throws java.io.IOException { 
        out.write(val);
    }
    
}
