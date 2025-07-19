package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBeeTransmitOptions;
import com.digi.xbee.api.packet.XBeeAPIPacket;
import com.digi.xbee.api.packet.raw.TX16Packet;
import jmri.jmrix.BroadcastMessage;

/**
 * This is an extension of the XBeeMessage class for messages
 * that are to be sent as broadcast messages.
 *
 * @author Paul Bender Copyright (C) 2023
 */
public class XBeeBroadcastMessage extends XBeeMessage implements BroadcastMessage {


    private XBeeBroadcastMessage(){}

    private XBeeBroadcastMessage(XBeeAPIPacket xbp){
        super(xbp);
    }

    /**
     * Get an XBee Message for broadcasting a payload to the network.
     *
     * @param payload A byte array containing the bytes to be broadcast as the low order word of the integer.
     * @return XBeeMessage with remote transmission request for the provided address containing the provided payload.
     */
    public static XBeeBroadcastMessage getTX16BroadcastMessage(byte[] payload) {
        return new XBeeBroadcastMessage(
                new TX16Packet(0x00,XBee16BitAddress.BROADCAST_ADDRESS,XBeeTransmitOptions.NONE, payload));

    }
}
