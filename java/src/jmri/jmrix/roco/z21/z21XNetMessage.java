// z21XNetMessage.java
package jmri.jmrix.roco.z21;

import java.io.Serializable;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;

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

    /*
     * Given a locomotive address, request its status 
     * @param address is the locomotive address
     */
    public static XNetMessage getLocomotiveInfoRequestMsg(int address) {
        XNetMessage msg = new XNetMessage(5);
        msg.setElement(0, XNetConstants.LOCO_STATUS_REQ);
        msg.setElement(1, z21Constants.LAN_X_LOCO_INFO_REQUEST_Z21);
        msg.setElement(2, jmri.jmrix.lenz.LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3, jmri.jmrix.lenz.LenzCommandStation.getDCCAddressLow(address));
        msg.setParity();
        return (msg);
    }

    /*
     * Given a locomotive address, a function number, and it's value, 
     * generate a message to change the state. 
     * @param address is the locomotive address
     * @param functionno is the function to change
     * @param newstate is boolean representing whether the function is to be on or off.
     */
    public static XNetMessage getLocomotiveFunctionOperationMsg(int address, int functionno, boolean state) {
        XNetMessage msg = new XNetMessage(6);
        int functionbyte = functionno;
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1, z21Constants.LAN_X_SET_LOCO_FUNCTION);
        msg.setElement(2, jmri.jmrix.lenz.LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3, jmri.jmrix.lenz.LenzCommandStation.getDCCAddressLow(address));
        if(state) {
           //This function is on
           functionbyte = functionbyte & 0x3F; // clear the 2 most significant bits.
           functionbyte = functionbyte | 0x40; // set the 2 msb to 01.
        } else {
           //This function is off.
           functionbyte = functionbyte & 0x3F; // clear the 2 most significant bits.
        }
        msg.setElement(4, functionbyte);
        msg.setParity();
        return (msg);
    }

}
/*@(#)z21XNetMessage.java */
