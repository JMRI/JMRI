// XNetPortController.java

package jmri.jmrix.lenz;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import jmri.util.SystemType;

/**
 * Abstract base for classes representing a XNet communications port
 * <p>
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @author			Paul Bender    Copyright (C) 2004
 * @version			$Revision: 2.4 $
 */
public abstract class XNetPortController extends jmri.jmrix.AbstractPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to XNetTrafficController classes, who in turn will deal in messages.
    
    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();
    
    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();
    
    /**
     * Check that this object is ready to operate. This is a question
     * of configuration, not transient hardware status.
     */
    public abstract boolean status();
    
    /**
     * Can the port accept additional characters?  This might
     * go false for short intervals, but it might also stick
     * off if something goes wrong.
     */
    public abstract boolean okToSend();
    
    /**
     * We need a way to say if the output buffer is empty or not
     */
    public abstract void setOutputBufferEmpty(boolean s);
    
    /**
     * Option 2 controls if the buffer status will be checked when 
     * sending data
     */
    public String option2Name() { return "Check Buffer Status when sending? "; }
    public String[] validOption2() { 
        // if first time invoked, set default
        if (mOpt2 == null) {
            switch (SystemType.getType()) {
                case SystemType.LINUX:
                case SystemType.MACOSX:
                    mOpt2="no";
                    break;
                default:
                    mOpt2="yes";
                    break;                
            }
        }
        // return options
        return validOption2;
    }
    // meanings are assigned to these above, so make sure the order is consistent
    protected String [] validOption2 = new String[]{"yes", "no"};
    

}


/* @(#)XNetPortController.java */
