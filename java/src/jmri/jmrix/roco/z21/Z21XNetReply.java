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
        } else {
            text = super.toMonitorString();
        }
        return text;
    }

}
