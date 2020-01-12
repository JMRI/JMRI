package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.packet.XBeeAPIPacket;
import com.digi.xbee.api.packet.common.ATCommandPacket;
import com.digi.xbee.api.packet.common.RemoteATCommandPacket;
import com.digi.xbee.api.packet.common.TransmitPacket;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This is a wrapper class for a Digi XBeeAPIPacket.
 *
 * @author Paul Bender Copyright (C) 2013
 */
public class XBeeMessage extends jmri.jmrix.ieee802154.IEEE802154Message {

    private XBeeAPIPacket xbm = null;

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
     * @param m message
     * @param l length
     *
     */
    public XBeeMessage(String m, int l) {
        super(m, l);
    }

    /**
     * This ctor interprets the byte array as a sequence of characters to send.
     *
     * @param a Array of bytes to send
     * @param l length
     */
    public XBeeMessage(byte[] a, int l) {
        super(String.valueOf(a), l);
    }

    /**
     * This ctor interprets the parameter as an XBeeAPIPacket message. This is the
     * message form that will generally be used by the implementation.
     *
     * @param request an XBeeAPIPacket of bytes to send
     */
    public XBeeMessage(XBeeAPIPacket request) {
        _nDataChars = request.getPacketData().length;
        byte data[] = request.getPacketData();
        _dataChars = new int[_nDataChars];
        for(int i=0;i<_nDataChars;i++) {
           _dataChars[i] = data[i];
        }
        xbm = request;
    }

    public XBeeAPIPacket getXBeeRequest() {
        return xbm;
    }

    public void setXBeeRequest(XBeeAPIPacket request) {
        xbm = request;
    }

    @Override
    public String toMonitorString() {
        if (xbm != null) {
            return xbm.toPrettyString();
        } else {
            return toString();
        }
    }

    @Override
    public String toString() {
        String s = "";
	if(xbm != null) {
           byte packet[] = xbm.getPacketData();
           for (int i = 0; i < packet.length; i++) {
               s=jmri.util.StringUtil.appendTwoHexFromInt(packet[i],s);
           }
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
        return new XBeeMessage(new ATCommandPacket(0,"HV",""));
    }

    public static XBeeMessage getFirmwareVersionRequest() {
        return new XBeeMessage(new ATCommandPacket(0,"VR",""));
    }

    /*
     * Get an XBee Message requesting an digital output pin be turned on or off.
     * @param address XBee Address of the node.  This can be either 
     16 bit or 64 bit.
     * @param pin the DIO Pin on the XBee to use.
     * @param on boolean value stating whether or not the pin should be turned
     *        on (true) or off (false)
     */
    @SuppressFBWarnings( value = {"BC_UNCONFIRMED_CAST"}, justification="The passed address must be either a 16 bit address or a 64 bit address, and we check to see if the address is a 16 bit address, so it is redundant to also check for a 64 bit address")
    public static XBeeMessage getRemoteDoutMessage(Object address, int pin, boolean on) {
        byte onValue[] = {0x5};
        byte offValue[] = {0x4};
        if (address instanceof XBee16BitAddress) {
            return new XBeeMessage(new RemoteATCommandPacket(XBeeAPIPacket.NO_FRAME_ID,XBee64BitAddress.COORDINATOR_ADDRESS,(XBee16BitAddress) address,0,"D" + pin, on ? onValue : offValue));
        } else {
            return new XBeeMessage(new RemoteATCommandPacket(XBeeAPIPacket.NO_FRAME_ID,(XBee64BitAddress) address,XBee16BitAddress.UNKNOWN_ADDRESS,0, "D" + pin, on ? onValue : offValue));
        }
    }

    /*
     * Get an XBee Message requesting the status of a digital IO pin.
     * @param address XBee Address of the node.  This can be either 
     16 bit or 64 bit.
     * @param pin the DIO Pin on the XBee to use.
     */
    @SuppressFBWarnings( value = {"BC_UNCONFIRMED_CAST"}, justification="The passed address must be either a 16 bit address or a 64 bit address, and we check to see if the address is a 16 bit address, so it is redundant to also check for a 64 bit address")
    public static XBeeMessage getRemoteDoutMessage(Object address, int pin) {
        if (address instanceof com.digi.xbee.api.models.XBee16BitAddress) {
            return new XBeeMessage(new RemoteATCommandPacket(XBeeAPIPacket.NO_FRAME_ID,XBee64BitAddress.COORDINATOR_ADDRESS,(XBee16BitAddress) address,0,"D" + pin, ""));
        } else {
            return new XBeeMessage(new RemoteATCommandPacket(XBeeAPIPacket.NO_FRAME_ID,(XBee64BitAddress) address,XBee16BitAddress.UNKNOWN_ADDRESS,0,"D" + pin, ""));
        }
    }

    /*
     * Get an XBee Message requesting an IO sample from the node.
     * @param address XBee Address of the node.  This can be either 
     16 bit or 64 bit.
     */
    @SuppressFBWarnings( value = {"BC_UNCONFIRMED_CAST"}, justification="The passed address must be either a 16 bit address or a 64 bit address, and we check to see if the address is a 16 bit address, so it is redundant to also check for a 64 bit address")
    public static XBeeMessage getForceSampleMessage(Object address) {
        if (address instanceof com.digi.xbee.api.models.XBee16BitAddress) {
            return new XBeeMessage(new RemoteATCommandPacket(XBeeAPIPacket.NO_FRAME_ID,XBee64BitAddress.COORDINATOR_ADDRESS,(XBee16BitAddress) address,0,"IS",""));
        } else {
            return new XBeeMessage(new RemoteATCommandPacket(XBeeAPIPacket.NO_FRAME_ID,(XBee64BitAddress) address,XBee16BitAddress.UNKNOWN_ADDRESS,0, "IS",""));
        }
    }

    /*
     * Get an XBee Message requesting data be sent to the serial port
     * on a remote node.
     * @param address XBee Address of the node.  This can be either 
     16 bit or 64 bit.
     * @param payload An byte array containing the bytes to be transfered, as the low order word of the integer.
     * @return XBeeMessage with remote transmission request for the provided address containing the provided payload.
     */
    @SuppressFBWarnings( value = {"BC_UNCONFIRMED_CAST"}, justification="The passed address must be either a 16 bit address or a 64 bit address, and we check to see if the address is a 16 bit address, so it is redundant to also check for a 64 bit address")
    public static XBeeMessage getRemoteTransmissionRequest(Object address, byte[] payload) {
        if (address instanceof com.digi.xbee.api.models.XBee16BitAddress) {
            return getRemoteTransmissionRequest((com.digi.xbee.api.models.XBee16BitAddress) address, payload);
        } else {
            return getRemoteTransmissionRequest((com.digi.xbee.api.models.XBee64BitAddress) address, payload);
        }
    }

    /*
     * Get an XBee Message requesting data be sent to the serial port
     * on a remote node.
     * @param address XBee16BitAddress of the node.
     * @param payload A byte array containing the bytes to be transfered, as the low order word of the integer.
     * @return XBeeMessage with remote transmission request for the provided address containing the provided payload.
     */
    public static XBeeMessage getRemoteTransmissionRequest(XBee16BitAddress address, byte[] payload) {
        return new XBeeMessage(new TransmitPacket(XBeeAPIPacket.NO_FRAME_ID,XBee64BitAddress.COORDINATOR_ADDRESS,address,255,0,payload));
    }
 
    /*
     * Get an XBee Message requesting data be sent to the serial port
     * on a remote node.
     * @param address XBee64BitAddress of the node.
     * @param payload A byte array containing the bytes to be transfered, as the low order word of the integer.
     * @return XBeeMessage with remote transmission request for the provided address containing the provided payload.
     */
    public static XBeeMessage getRemoteTransmissionRequest(XBee64BitAddress address, byte[] payload) {
        return new XBeeMessage(new TransmitPacket(XBeeAPIPacket.NO_FRAME_ID,address,XBee16BitAddress.UNKNOWN_ADDRESS,255,0,payload));
    }

}

