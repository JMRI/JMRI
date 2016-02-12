// z21XNetMessage.java
package jmri.jmrix.roco.z21;

import java.io.Serializable;
import jmri.jmrix.lenz.XNetMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single command or response on the XpressNet.
 * <P>
 * Content is represented with ints to avoid the problems with sign-extension
 * that bytes have, and because a Java char is actually a variable number of
 * bytes in Unicode.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @author	Paul Bender Copyright (C) 2003-2010
 * @version	$Revision: 28013 $
 *
 */
public class z21XNetMessage extends jmri.jmrix.lenz.XNetMessage implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3422831570914017638L;

//    static private int _nRetries = 5;

    // constructors, just pass on to the supperclass.
    public z21XNetMessage(int len) {
        super(len);
    }

    // create messages of a particular form
    public static XNetMessage getReadDirectCVMsg(int cv) {
        XNetMessage m = new XNetMessage(5);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, z21Constants.LAN_X_CV_READ_XHEADER);
        m.setElement(1, z21Constants.LAN_X_CV_READ_DB0);
        m.setElement(2, ((0xff00 & (cv - 1)) >> 8));
        m.setElement(3, (0xff & (cv - 1)));
        m.setParity(); // Set the parity bit
        return m;
    }

    public static XNetMessage getWriteDirectCVMsg(int cv, int val) {
        XNetMessage m = new XNetMessage(6);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, z21Constants.LAN_X_CV_WRITE_XHEADER);
        m.setElement(1, z21Constants.LAN_X_CV_WRITE_DB0);
        m.setElement(2, (0xff00 & (cv - 1)) >> 8);
        m.setElement(3, (0xff & (cv - 1)));
        m.setElement(4, val);
        m.setParity(); // Set the parity bit
        return m;
    }

    // initialize logging    
    private final static Logger log = LoggerFactory.getLogger(z21XNetMessage.class.getName());

}

/* @(#)z21XNetMessage.java */
