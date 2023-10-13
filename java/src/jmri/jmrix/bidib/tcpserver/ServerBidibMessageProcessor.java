package jmri.jmrix.bidib.tcpserver;

import java.io.ByteArrayOutputStream;

import org.bidib.jbidibc.core.BidibMessageProcessor;
import org.bidib.jbidibc.messages.exception.ProtocolException;

/**
 * 
 * @author Eckart Meyer Copyright (C) 2023
 *
 */
public interface ServerBidibMessageProcessor extends BidibMessageProcessor {

    /**
     * Publish the response in the provided byte array output stream.
     * 
     * @param output
     *            the output stream that contains the responses
     * @throws ProtocolException when can't provide sequence
     */
    void publishResponse(final ByteArrayOutputStream output) throws ProtocolException;

    void enable();

    void disable();

    //void purgeReceivedDataInBuffer();
}
