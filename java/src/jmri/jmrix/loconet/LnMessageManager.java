package jmri.jmrix.loconet;

/**
 * Provide access to throttle-messaging on a LocoNet.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class LnMessageManager implements LocoNetListener {

    public LnMessageManager(LnTrafficController tc) {
        // connect to the TrafficManager
        this.tc = tc;
        this.tc.addLocoNetListener(~0, this);
    }

    public void sendMessage(String text) {
        sendMessage(0, text);
    }

    public void sendMessage(int id, String text) {
        LocoNetMessage l = new LocoNetMessage(16);
        String localText = text + "        "; // ensure at least 8 characters
        l.setOpCode(LnConstants.OPC_PEER_XFER);
        l.setElement(1, 0x10);
        l.setElement(2, 0x7F);   // throttle message
        l.setElement(3, id & 0x7F);
        l.setElement(4, id / 128);
        l.setElement(5, 0);
        l.setElement(6, localText.charAt(0));
        l.setElement(7, localText.charAt(1));
        l.setElement(8, localText.charAt(2));
        l.setElement(9, localText.charAt(3));
        l.setElement(10, 0);
        l.setElement(11, localText.charAt(4));
        l.setElement(12, localText.charAt(5));
        l.setElement(13, localText.charAt(6));
        l.setElement(14, localText.charAt(7));
        tc.sendLocoNetMessage(l);
    }

    /**
     * Free resources when no longer used.
     */
    public void dispose() {
        tc.removeLocoNetListener(~0, this);
        tc = null;
    }

    LnTrafficController tc = null;

    /**
     * Listen for status changes from LocoNet.
     * <p>
     * This doesn't do anything now. Eventually, it will handle the user
     * response.
     */
    @Override
    public void message(LocoNetMessage m) {
    }

}
