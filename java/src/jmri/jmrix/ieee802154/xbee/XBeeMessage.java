// XBeeMessage.java
package jmri.jmrix.ieee802154.xbee;

import com.rapplogic.xbee.api.XBeeRequest;

/**
 * This is a wrapper class for an XBeeAPI XBeeRequest.
 * <P>
 *
 * @author Paul Bender Copyright (C) 2013
 * @version $Revision$
 */
public class XBeeMessage extends jmri.jmrix.ieee802154.IEEE802154Message {

    private XBeeRequest xbm = null;

    /**
     * Suppress the default ctor, as the length must always be specified
     */
    protected XBeeMessage() {
    }

    public XBeeMessage(int l) {
        super(l);
    }

    /**
     * This ctor interprets the String as the exact sequence to send,
     * byte-for-byte.
     *
     * @param m
     */
    public XBeeMessage(String m, int l) {
        super(m, l);
    }

    /**
     * This ctor interprets the byte array as a sequence of characters to send.
     *
     * @param a Array of bytes to send
     */
    public XBeeMessage(byte[] a, int l) {
        super(String.valueOf(a), l);
    }

    /**
     * This ctor interprets the parameter as an XBeeRequest message. This is the
     * message form that will generally be used by the implementation.
     *
     * @param request an XBeeRequest of bytes to send
     */
    public XBeeMessage(XBeeRequest request) {
        _nDataChars = request.getFrameData().length;
        _dataChars = request.getFrameData();
        xbm = request;
    }

    public XBeeRequest getXBeeRequest() {
        return xbm;
    }

    public void setXBeeRequest(XBeeRequest request) {
        xbm = request;
    }

    public String toMonitorString() {
        if (xbm != null) {
            return xbm.toString();
        } else {
            return toString();
        }
    }

    public String toString() {
        String s = "";
        int packet[] = xbm.getFrameData();
        for (int i = 0; i < packet.length; i++) {
            s=jmri.util.StringUtil.appendTwoHexFromInt(packet[i],s);
        }
        return s;
    }

    /**
     * check whether the message has a valid parity
     */
    @Override
    public boolean checkParity() {
        int len = getNumDataElements();
        int chksum = 0x00;  /* the seed */

        int loop;

        for (loop = 0; loop < len - 1; loop++) {  // calculate contents for data part
            chksum ^= getElement(loop);
        }
        return ((chksum & 0xFF) == getElement(len - 1));
    }

    @Override
    public void setParity() {
        int len = getNumDataElements();
        int chksum = 0x00;  /* the seed */

        int loop;

        for (loop = 0; loop < len - 1; loop++) {  // calculate contents for data part
            chksum ^= getElement(loop);
        }
        setElement(len - 1, chksum & 0xFF);
    }

    // a few canned messages
    public static XBeeMessage getHardwareVersionRequest() {
        return new XBeeMessage(new com.rapplogic.xbee.api.AtCommand("HV"));
    }

    public static XBeeMessage getFirmwareVersionRequest() {
        return new XBeeMessage(new com.rapplogic.xbee.api.AtCommand("VR"));
    }

    /*
     * Get an XBee Message requesting an digital output pin be turned on or off.
     * @param address XBee Address of the node.  This can be either 
     16 bit or 64 bit.
     * @param pin the DIO Pin on the XBee to use.
     * @param on boolean value stating whether or not the pin should be turned
     *        on (true) or off (false)
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = {"BC_UNCONFIRMED_CAST"}, justification="The passed address must be either a 16 bit address or a 64 bit address, and we check to see if the address is a 16 bit address, so it is redundant to also check for a 64 bit address")
    public static XBeeMessage getRemoteDoutMessage(com.rapplogic.xbee.api.XBeeAddress address, int pin, boolean on) {
        int onValue[] = {0x5};
        int offValue[] = {0x4};
        if (address instanceof com.rapplogic.xbee.api.XBeeAddress16) {
            return new XBeeMessage(new com.rapplogic.xbee.api.RemoteAtRequest((com.rapplogic.xbee.api.XBeeAddress16) address, "D" + pin, on ? onValue : offValue));
        } else {
            return new XBeeMessage(new com.rapplogic.xbee.api.RemoteAtRequest((com.rapplogic.xbee.api.XBeeAddress64) address, "D" + pin, on ? onValue : offValue));
        }
    }

    /*
     * Get an XBee Message requesting the status of a digital IO pin.
     * @param address XBee Address of the node.  This can be either 
     16 bit or 64 bit.
     * @param pin the DIO Pin on the XBee to use.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = {"BC_UNCONFIRMED_CAST"}, justification="The passed address must be either a 16 bit address or a 64 bit address, and we check to see if the address is a 16 bit address, so it is redundant to also check for a 64 bit address")
    public static XBeeMessage getRemoteDoutMessage(com.rapplogic.xbee.api.XBeeAddress address, int pin) {
        if (address instanceof com.rapplogic.xbee.api.XBeeAddress16) {
            return new XBeeMessage(new com.rapplogic.xbee.api.RemoteAtRequest((com.rapplogic.xbee.api.XBeeAddress16) address, "D" + pin));
        } else {
            return new XBeeMessage(new com.rapplogic.xbee.api.RemoteAtRequest((com.rapplogic.xbee.api.XBeeAddress64) address, "D" + pin));
        }
    }

    /*
     * Get an XBee Message requesting an IO sample from the node.
     * @param address XBee Address of the node.  This can be either 
     16 bit or 64 bit.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = {"BC_UNCONFIRMED_CAST"}, justification="The passed address must be either a 16 bit address or a 64 bit address, and we check to see if the address is a 16 bit address, so it is redundant to also check for a 64 bit address")
    public static XBeeMessage getForceSampleMessage(com.rapplogic.xbee.api.XBeeAddress address) {
        if (address instanceof com.rapplogic.xbee.api.XBeeAddress16) {
            return new XBeeMessage(new com.rapplogic.xbee.api.RemoteAtRequest((com.rapplogic.xbee.api.XBeeAddress16) address, "IS"));
        } else {
            return new XBeeMessage(new com.rapplogic.xbee.api.RemoteAtRequest((com.rapplogic.xbee.api.XBeeAddress64) address, "IS"));
        }
    }

    /*
     * Get an XBee Message requesting data be sent to the serial port
     * on a remote node.
     * @param address XBee Address of the node.  This can be either 
     16 bit or 64 bit.
     * @param payload An integer array containing the bytes to be transfered, as the low order word of the integer.
     * @return XBeeMessage with remote transmission request for the provided address containing the provided payload.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = {"BC_UNCONFIRMED_CAST"}, justification="The passed address must be either a 16 bit address or a 64 bit address, and we check to see if the address is a 16 bit address, so it is redundant to also check for a 64 bit address")
    public static XBeeMessage getRemoteTransmissionRequest(com.rapplogic.xbee.api.XBeeAddress address, int[] payload) {
        if (address instanceof com.rapplogic.xbee.api.XBeeAddress16) {
            return getRemoteTransmissionRequest((com.rapplogic.xbee.api.XBeeAddress16) address, payload);
        } else {
            return getRemoteTransmissionRequest((com.rapplogic.xbee.api.XBeeAddress64) address, payload);
        }
    }

    /*
     * Get an XBee Message requesting data be sent to the serial port
     * on a remote node.
     * @param address XBeeAddress16 of the node.
     * @param payload An integer array containing the bytes to be transfered, as the low order word of the integer.
     * @return XBeeMessage with remote transmission request for the provided address containing the provided payload.
     */
    public static XBeeMessage getRemoteTransmissionRequest(com.rapplogic.xbee.api.XBeeAddress16 address, int[] payload) {
        return new XBeeMessage(new com.rapplogic.xbee.api.wpan.TxRequest16(address, payload));
    }
 
    /*
     * Get an XBee Message requesting data be sent to the serial port
     * on a remote node.
     * @param address XBeeAddress64 of the node.
     * @param payload An integer array containing the bytes to be transfered, as the low order word of the integer.
     * @return XBeeMessage with remote transmission request for the provided address containing the provided payload.
     */
    public static XBeeMessage getRemoteTransmissionRequest(com.rapplogic.xbee.api.XBeeAddress64 address, int[] payload) {
        return new XBeeMessage(new com.rapplogic.xbee.api.wpan.TxRequest64(address, payload));
    }

    /*
     * Get an XBee Message requesting data be sent to the serial port
     * on a remote node for a series 2 XBee.
     * @param address XBeeAddress64 of the node.
     * @param payload An integer array containing the bytes to be transfered, as the low order word of the integer.
     * @return XBeeMessage with ZNet remote transmission request for the provided address containing the provided payload.
     */
    public static XBeeMessage getZNetTransmissionRequest(com.rapplogic.xbee.api.XBeeAddress64 address, int[] payload) {
        return new XBeeMessage(new com.rapplogic.xbee.api.zigbee.ZNetTxRequest(address, payload));
    }

}
/* @(#)XBeeMessage.java */
