package jmri.jmrix.bidib.tcpserver;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.jmrix.bidib.BiDiBTrafficController;

import org.apache.commons.collections4.CollectionUtils;

import org.bidib.jbidibc.core.*;
import org.bidib.jbidibc.messages.message.*;
import org.bidib.jbidibc.messages.MessageReceiver;
import org.bidib.jbidibc.messages.exception.ProtocolException;
import org.bidib.jbidibc.messages.utils.ByteUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the server message receiver. Message from the network client will
 * be received and then forwarded to the JMRI BiDiB connection via the
 * traffic controler.
 * 
 * @author Eckart Meyer Copyright (C) 2023
 *
 */
public abstract class ServerMessageReceiver implements ServerBidibMessageProcessor, MessageReceiver {

    //private final BiDiBSystemConnectionMemo memo;
    private final BiDiBTrafficController tc;
    private BidibRequestFactory requestFactory;
    protected AtomicBoolean running = new AtomicBoolean();
    boolean escapeHot = false;
    private final Object accessLock = new Object();

    public ServerMessageReceiver(BiDiBTrafficController tc) {

        //memo = InstanceManager.getDefault(BiDiBSystemConnectionMemo.class);
        //log.info("Server connected to {}", memo.getUserName());
        //tc = memo.getBiDiBTrafficController();
        this.tc = tc;
        log.info("Server connected to {}", tc.getUserName());
        // enable the running flag
        running.set(true);
    }

    private synchronized BidibRequestFactory getRequestFactory() {
        if (requestFactory == null) {
            requestFactory = new BidibRequestFactory();
        }

        return requestFactory;
    }

    @Override
    public void enable() {
        log.info("enable is called.");
        synchronized (accessLock) {
            escapeHot = false;
        }

        running.set(true);
    }

    @Override
    public void disable() {
        log.info("Disable is called.");

        running.set(false);

        synchronized (accessLock) {
            escapeHot = false;
        }
    }
    
//    @Override
//    public void purgeReceivedDataInBuffer() {
//
//    }

/**
 * Process data received from network. Forward to BiDiB connection
 * Currently we split possible multi-message packets into a sequence of single messages.
 * TODO: forward multi-message packets, this would require that the BiDiB traffic controller supports this
 * 
 * @param output data received
 */
    @Override
    public void receive(final ByteArrayOutputStream output) {

        if (!running.get()) {
            log.warn("Receiver is not running. Discard messages.");

            return;
        }

        log.info("Receive data: {}", ByteUtils.bytesToHex(output));

        // if a CRC error is detected in splitMessages the reading loop will terminate ...
        try {
            List<BidibMessageInterface> commands = new ArrayList<>();
            getRequestFactory().createFromRaw(output.toByteArray(), commands);

            if (CollectionUtils.isNotEmpty(commands)) {

                for (BidibMessageInterface bidibCommand : commands) {
                    log.info("Process the current bidibCommand: {}", bidibCommand);

                    //String nodeAddress = NodeUtils.formatAddress(bidibCommand.getAddr());
                    //log.debug("node address: {}", tc.getNodeByAddr(bidibCommand.getAddr()));
                    
                    tc.sendBiDiBMessage((BidibCommandMessage)bidibCommand, tc.getNodeByAddr(bidibCommand.getAddr()));

//                    SimulatorNode simulatorNode = simulatorRegistry.getSimulator(nodeAddress);
//                    if (simulatorNode != null) {
//                        simulatorNode.processRequest(bidibCommand);
//                    }
//                    else {
//                        log.error("No simulator available for address: {}", nodeAddress);
//                    }
                }
            }
            else {
                log.warn("No commands in packet received.");
            }
        }
        catch (ProtocolException ex) {
            log.warn("Create BiDiB message failed.", ex);
        }

    }

    @Override
    public abstract void publishResponse(ByteArrayOutputStream output) throws ProtocolException;

    @Override
    public void processMessages(ByteArrayOutputStream output) throws ProtocolException {
        log.warn("processMessages() is not implemented in SimulationMessageReceiver.");
    }

    @Override
    public String getErrorInformation() {
        return null;
    }

    @Override
    public void addMessageListener(MessageListener messageListener) {
    }

    @Override
    public void removeMessageListener(MessageListener messageListener) {
    }

    @Override
    public void addNodeListener(NodeListener nodeListener) {
    }

    @Override
    public void cleanup() {
    }

    private final static Logger log = LoggerFactory.getLogger(ServerMessageReceiver.class);

}
