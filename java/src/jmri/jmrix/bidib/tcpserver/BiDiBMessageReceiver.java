package jmri.jmrix.bidib.tcpserver;

import java.util.LinkedList;
import java.util.List;
import org.bidib.jbidibc.messages.CRC8;
import org.bidib.jbidibc.messages.base.RawMessageListener;
import org.bidib.jbidibc.messages.exception.ProtocolException;
import org.bidib.jbidibc.messages.message.BidibMessageInterface;
import org.bidib.jbidibc.messages.message.BidibResponseFactory;
import org.bidib.jbidibc.messages.utils.ByteUtils;
import org.bidib.jbidibc.net.serialovertcp.NetBidibPort;
import org.bidib.jbidibc.net.serialovertcp.NetMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is to be registered as a raw listener for all BiDiB messages to and from
 * the BiDiB connection. We are only interested in data received
 * from the connection here. The data is then forwarded to to server message handler, which
 * will send it back to the all connected clients.
 * 
 * @author Eckart Meyer Copyright (C) 2023
 *
 */
public class BiDiBMessageReceiver implements RawMessageListener {
    
    final private NetMessageHandler serverMessageHandler;
    final private NetBidibPort port;
    private final BidibResponseFactory responseFactory = new BidibResponseFactory();


    public BiDiBMessageReceiver(NetMessageHandler netServerMessageHandler, NetBidibPort netPort) {
        serverMessageHandler = netServerMessageHandler;
        port = netPort;
    }
    
    private List<BidibMessageInterface> splitBidibMessages(byte[] data, boolean checkCRC)  throws ProtocolException {
        log.trace("splitMessages: {}", ByteUtils.bytesToHex(data));
        int index = 0;
        List<BidibMessageInterface> result = new LinkedList<>();

        while (index < data.length) {
            int size = ByteUtils.getInt(data[index]) + 1 /* len */;
            log.trace("Current size: {}", size);

            if (size <= 0) {
                throw new ProtocolException("cannot split messages, array size is " + size);
            }

            byte[] message = new byte[size];

            try {
                System.arraycopy(data, index, message, 0, message.length);
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                log
                    .warn("Failed to copy, msg.len: {}, size: {}, output.len: {}, index: {}, output: {}",
                        message.length, size, data.length, index, ByteUtils.bytesToHex(data));
                throw new ProtocolException("Copy message data to buffer failed.");
            }
            result.add(responseFactory.create(message));
            index += size;

            if (checkCRC) {
                // CRC
                if (index == data.length - 1) {
                    int crc = 0;
                    int crcIndex = 0;
                    for (crcIndex = 0; crcIndex < data.length - 1; crcIndex++) {
                        crc = CRC8.getCrcValue((data[crcIndex] ^ crc) & 0xFF);
                    }
                    if (crc != (data[crcIndex] & 0xFF)) {
                        throw new ProtocolException(
                            "CRC failed: should be " + crc + " but was " + (data[crcIndex] & 0xFF));
                    }
                    break;
                }
            }
        }

        return result;

    }
    
/**
 * Process data received from BiDiB connection. Send to network port.
 * Currently we split possible multi-message packets into a sequence of single messages.
 * TODO: forward multi-message packets.
 * 
 * @param data from BiDiB connection
 */
    @Override
    public void notifyReceived(byte[] data) {
        log.debug("BiDiBMessageReceiver received message: {}", ByteUtils.bytesToHex(data));
        try {
            List<BidibMessageInterface> commandMessages = splitBidibMessages(data, true);
            for (BidibMessageInterface message : commandMessages) {
                log.trace("send message {}", message);
                serverMessageHandler.send(port, message.getContent());
            }
            
        }
        catch (ProtocolException e) {
            log.warn("Protocol error while parsing incoming message from BiDiB connection", e);
        }
    }

    @Override
    public void notifySend(byte[] data) {
        // we are not interested in data sent to the connection
    }
    
    private final static Logger log = LoggerFactory.getLogger(BiDiBMessageReceiver.class);
}
