package jmri.jmrix.bidib.tcpserver;

import java.io.ByteArrayOutputStream;

import org.bidib.jbidibc.messages.MessageReceiver;
import org.bidib.jbidibc.messages.utils.ByteUtils;
import org.bidib.jbidibc.net.serialovertcp.*;
import org.bidib.jbidibc.serial.SerialMessageEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the network message handler for both directions.
 * It receives the raw data from the TCP socket and forwards them
 * to the message receiver.
 * 
 * The send() message is called from the BiDiBMessageReceiver to send
 * data from tge BiDIB connection back to the TCP client. Data is encoded
 * with the serial encoder (i.e. add the Magic bytes etc.) and then sent
 * to the NetBidibPort, which then finally send the messages to all
 * connected clients.
 * 
 * @author Eckart Meyer Copyright (C) 2023
 *
 */
public class TcpServerNetMessageHandler  implements NetMessageHandler {
    
    private final MessageReceiver messageReceiverDelegate;
    
    public TcpServerNetMessageHandler(MessageReceiver messageReceiverDelegate) {
        this.messageReceiverDelegate = messageReceiverDelegate;
    }

    @Override
    public void receive(final DataPacket packet) {
        // a data packet was received ... process the envelope and extract the message (or even messages?)
        log.debug("Received a packet from address: {}, port: {}, data: {}", packet.getAddress(), packet.getPort(),
                ByteUtils.bytesToHex(packet.getData()));

//        BidibNetAddress current = new BidibNetAddress(packet.getAddress(), packet.getPort());
//        if (!knownBidibHosts.contains(current)) {
//
//            LOGGER.info("Adding new known Bidib host: {}", current);
//            knownBidibHosts.add(current);
//        }

        // TODO for the first magic response we need special processing because we need to keep the session key

        // remove the  paket wrapper data and forward to the MessageReceiver
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(packet.getData(), 0, packet.getData().length);

        log.info("Forward received message to messageReceiverDelegate: {}, output: {}", messageReceiverDelegate,
                ByteUtils.bytesToHex(output));
        try {
            messageReceiverDelegate.receive(output);
        }
        catch (Exception ex) {
            log.warn("Process messages failed.", ex);
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Send serial encoded data (one or more messages) to the to the TCP port
     * 
     * @param port
     *            the port
     * @param data
     *            the data
     */
    @Override
    public void send(NetBidibPort port, byte[] data) {
        log.trace("Send message to port: {}, message: {}", port, ByteUtils.bytesToHex(data));
        ByteArrayOutputStream output = new ByteArrayOutputStream(100);
        ByteArrayOutputStream input = new ByteArrayOutputStream(100);
        //input.write(data, 0, data.length);
        try {
            input.write(data);
            SerialMessageEncoder.encodeMessage(input, output);
            log.trace("Send, after encoding: {}", ByteUtils.bytesToHex(output));
            port.send(output.toByteArray(), null, 0); //InetAdress and port are not used in send, so no need to provide them
        }
        catch (Exception e) {
            log.warn("no data sent", e);
        }
    }
    
    @Override
    public void acceptClient(String remoteHost) {
        log.info("Accept client with address: {}", remoteHost);
    }

    @Override
    public void cleanup(String remoteHost) {
        log.info("Cleanup client with address: {}", remoteHost);
    }

    private final static Logger log = LoggerFactory.getLogger(TcpServerNetMessageHandler.class);
}
