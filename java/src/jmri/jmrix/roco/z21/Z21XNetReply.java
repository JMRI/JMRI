package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.XNetReply;

/**
 * Represents a single response from the XpressNet, with extensions
 * from Roco for the Z21.
 *
 * @author Paul Bender Copyright (C) 2018
 *
 */
public class Z21XNetReply extends XNetReply {

    // Create a new reply.
    public Z21XNetReply() {
        super();
    }

    // Create a new reply from an existing reply
    public Z21XNetReply(Z21XNetReply reply) {
        super(reply);
    }

    /**
     * Create a reply from an XNetMessage.
     */
    public Z21XNetReply(Z21XNetMessage message) {
        super(message);
    }

    /**
     * Create a reply from a string of hex characters.
     */
    public Z21XNetReply(String message) {
        super(message);
    }

    /**
     * Is this message a service mode response?
     */
    @Override
    public boolean isServiceModeResponse() {
        return ((getElement(0) == Z21Constants.LAN_X_CV_RESULT_XHEADER && 
                (getElement(1) == Z21Constants.LAN_X_CV_RESULT_DB0)) ||
                super.isServiceModeResponse());
    }

    /**
     * @return a string representation of the reply suitable for display in the
     * XpressNet monitor.
     */
    @Override
    public String toMonitorString(){
        String text;
        
        if (getElement(0) == Z21Constants.LAN_X_CV_RESULT_XHEADER ) {
            if (getElement(1) == Z21Constants.LAN_X_CV_RESULT_DB0 ) {
                int value = getElement(4) & 0xFF;
                int cv = ( (getElement(2)&0xFF) << 8) + 
                          ( getElement(3)& 0xFF ) + 1;
                text = Bundle.getMessage("Z21LAN_X_CV_RESULT",cv,value);
            } else {
                text = super.toMonitorString();
            }
        } else if((getElement(0)&0xE0)==0xE0 && ((getElement(0)&0x0f) >= 7 && (getElement(0)&0x0f) <=15 )){
            //This is a Roco specific throttle information message.
            //Data Byte 0 and 1 contain the locomotive address
            int messageaddress=((getElement(1)&0x3F) << 8)+(getElement(2)&0xff);
               text = "Z21 Mobile decoder info reply for address " + 
                      messageaddress + ":";
               //The message is for this throttle.
               int b2= getElement(3)&0xff;
               int b3= getElement(4)&0xff;
               int b4= getElement(5)&0xff;
               int b5= getElement(6)&0xff;
               int b6= getElement(7)&0xff;
               int b7= getElement(8)&0xff;
               // byte 2 contains the speed step mode and availability
               // information.
               // byte 3 contains the direction and the speed information
               text += " " + parseSpeedAndDirection(b2,b3);
               // byte 4 contains flags for whether or not the locomotive
               // is in a double header and for smart search.  These aren't used
               // here.

               // byte 4 and 5 contain function information for F0-F12
               text += " " + parseFunctionStatus(b4,b5);
               // byte 6 and 7 contain function information for F13-F28
               text += " " + parseFunctionHighStatus(b6,b7);
        } else {
            text = super.toMonitorString();
        }
        return text;
    }

}
