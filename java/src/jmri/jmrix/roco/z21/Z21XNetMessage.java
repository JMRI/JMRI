package jmri.jmrix.roco.z21;

import jmri.SpeedStepMode;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;

/**
 * Represents a single command or response on the XpressNet.
 * <p>
 * Content is represented with ints to avoid the problems with sign-extension
 * that bytes have, and because a Java char is actually a variable number of
 * bytes in Unicode.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @author	Paul Bender Copyright (C) 2003-2010
 */
public class Z21XNetMessage extends jmri.jmrix.lenz.XNetMessage {

    /**
     * Constructor, just pass on to the supperclass.
     */
    public Z21XNetMessage(int len) {
        super(len);
    }

    /**
     * Constructor from a Z21Message
     */
    public Z21XNetMessage(Z21Message m) {
        super(m.getLength()-4);
        for(int i = 4; i< m.getLength() ; i++ ){
           this.setElement(i-4,m.getElement(i));
        }
    }

    /**
     * Create a new object, that is a copy of an existing message.
     *
     * @param message an existing Z21XpressNet message
     */
    public Z21XNetMessage(Z21XNetMessage message) {
        super(message);
    }

    /**
     * Create an Z21XNetMessage from an Z21XNetReply.
     */
    public Z21XNetMessage(Z21XNetReply message) {
        super(message);
    }

    /**
     * Create an XNetMessage from a String containing bytes.
     */
    public Z21XNetMessage(String s) {
        super(s);
    }

    /**
     * Create messages of a particular form
     */
    public static XNetMessage getZ21ReadDirectCVMsg(int cv) {
        XNetMessage m = new XNetMessage(5);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, Z21Constants.LAN_X_CV_READ_XHEADER);
        m.setElement(1, Z21Constants.LAN_X_CV_READ_DB0);
        m.setElement(2, ((0xff00 & (cv - 1)) >> 8));
        m.setElement(3, (0xff & (cv - 1)));
        m.setParity(); // Set the parity bit
        return m;
    }

    public static XNetMessage getZ21WriteDirectCVMsg(int cv, int val) {
        XNetMessage m = new XNetMessage(6);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, Z21Constants.LAN_X_CV_WRITE_XHEADER);
        m.setElement(1, Z21Constants.LAN_X_CV_WRITE_DB0);
        m.setElement(2, (0xff00 & (cv - 1)) >> 8);
        m.setElement(3, (0xff & (cv - 1)));
        m.setElement(4, val);
        m.setParity(); // Set the parity bit
        return m;
    }

    /**
     * Given a locomotive address, request its status.
     *
     * @param address is the locomotive address
     */
    public static XNetMessage getZ21LocomotiveInfoRequestMsg(int address) {
        XNetMessage msg = new XNetMessage(5);
        msg.setElement(0, XNetConstants.LOCO_STATUS_REQ);
        msg.setElement(1, Z21Constants.LAN_X_LOCO_INFO_REQUEST_Z21);
        msg.setElement(2, jmri.jmrix.lenz.LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3, jmri.jmrix.lenz.LenzCommandStation.getDCCAddressLow(address));
        msg.setParity();
        return (msg);
    }

    /**
     * Given a locomotive address, a function number, and its value,
     * generate a message to change the state.
     *
     * @param address is the locomotive address
     * @param functionno is the function to change
     * @param state is boolean representing whether the function is to be on or off
     */
    public static XNetMessage getZ21LocomotiveFunctionOperationMsg(int address, int functionno, boolean state) {
        XNetMessage msg = new XNetMessage(6);
        int functionbyte = functionno;
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1, Z21Constants.LAN_X_SET_LOCO_FUNCTION);
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
        msg.setBroadcastReply();
        return (msg);
    }

    /**
     * Generate a Z21 message to change the speed/direction of a locomotive.
     *
     * @param address the locomotive address
     * @param speedMode the speedstep mode see @jmri.DccThrottle
     *                       for possible values.
     * @param speed a normalized speed value (a floating point number between 0
     *              and 1).  A negative value indicates emergency stop.
     * @param isForward true for forward, false for reverse.
     */
    public static XNetMessage getZ21LanXSetLocoDriveMsg(int address, SpeedStepMode speedMode, float speed, boolean isForward) {
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(address,
                        speedMode,speed,isForward);
        msg.setBroadcastReply();
        return (msg);
    }


    /**
     * Given a turnout address, generate a message to request the state.
     *
     * @param address the turnout address
     */
    public static XNetMessage getZ21TurnoutInfoRequestMessage(int address ) {
        // refer to section 5.1 of the z21 lan protocol manual.
        XNetMessage msg = new XNetMessage(4);
        msg.setElement(0,Z21Constants.LAN_X_GET_TURNOUT_INFO);
        // compared to Lenz devices, the addresses on the Z21 is one below 
        // the numerical value.  We will correct it here so higher level 
        // code doesn't see the difference.
        msg.setElement(1,((address-1) &0xff00)>>8);
        msg.setElement(2,((address-1) & 0x00ff));
        msg.setParity();
        return(msg);
    }

    /**
     * Given a turnout address and whether or not it is thrown, generate 
     * a message to operate the turnout.
     *
     * @param address is the turnout address
     * @param thrown boolean value representing whether the turnout is thrown.
     * @param active boolean value representing whether the output is being set
     * to active.
     * @param queue boolean value representing whehter or not the message is 
     * added to the queue.
     */
    public static XNetMessage getZ21SetTurnoutRequestMessage(int address, boolean thrown, boolean active, boolean queue) {
        // refer to section 5.2 of the z21 lan protocol manual.
        XNetMessage msg = new XNetMessage(5);
        msg.setElement(0,Z21Constants.LAN_X_SET_TURNOUT);
        // compared to Lenz devices, the addresses on the Z21 is one below 
        // the numerical value.  We will correct it here so higher level 
        // code doesn't see the difference.
        msg.setElement(1,((address-1) &0xff00)>>8);
        msg.setElement(2,((address-1) & 0x00ff));
        int element3=0x80;
        if(active) {
           element3 |=  0x08;
        } 
        if(thrown) {
           element3 |=  0x01;
        } 
        if(queue) {
           element3 |=  0x20;
        } 

        msg.setElement(3,element3);
        msg.setParity();
        return(msg);
    }

}
