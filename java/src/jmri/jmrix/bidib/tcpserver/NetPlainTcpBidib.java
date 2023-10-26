package jmri.jmrix.bidib.tcpserver;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.jmrix.bidib.BiDiBTrafficController;

import org.bidib.jbidibc.core.NodeListener;
import org.bidib.jbidibc.messages.exception.ProtocolException;
import org.bidib.jbidibc.net.serialovertcp.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the BiDiB controller of the BiDiB over TCP server.
 * The class starts and stops the receiver on both sides,
 * the incoming TCP server and the JMRI BiDiB connection.
 * 
 * @author Eckart Meyer Copyright (C) 2023
 *
 */
public class NetPlainTcpBidib {

    private final BiDiBTrafficController tc;
    private Thread portWorker;
    private NetMessageHandler netServerMessageHandler;
    private NetBidibPort netBidibTcpPort;
    private BiDiBMessageReceiver bidibMessageReveiver;
    private final AtomicBoolean isStarted = new AtomicBoolean();
    private final Object stopSync = new Object();
    
    public NetPlainTcpBidib(BiDiBTrafficController tc) {
        this.tc = tc;
    }
    
    public boolean isStarted() {
        return isStarted.get();
    }

    //@Override
    public void start(int portNumber) {
        log.info("Start the TCP server.");

        // create the server components
        try {

            // create the message receiver that handles incoming commands from the host and forward the commands to the
            // simulators
            netServerMessageHandler = createServerMessageHandler();

            //LOGGER.info("Create simulator for protocol: {}", protocol);

            // open the port that simulates the interface device
            log.info("Create a NetBidibTcpPort with the portnumber: {}", portNumber);
            netBidibTcpPort =
                new NetBidibServerPlainTcpPort(portNumber, null, netServerMessageHandler);

            log.info("Prepare and start the port worker for netBidibPortSimulator: {}", netBidibTcpPort);

            portWorker = new Thread(netBidibTcpPort);
            portWorker.start();

            // create the BiDiB connection receiver
            //BiDiBTrafficController tc = InstanceManager.getDefault(BiDiBSystemConnectionMemo.class).getBiDiBTrafficController();
            bidibMessageReveiver = new BiDiBMessageReceiver(netServerMessageHandler, netBidibTcpPort);
            tc.addRawMessageListener(bidibMessageReveiver);
            
            isStarted.set(true);
        }
        catch (Exception ex) {
            log.warn("Start the TCP server failed.", ex);
            isStarted.set(false);
        }
        
    }
        
    protected NetMessageHandler createServerMessageHandler() {
        // create the message handler that delegates the incoming messages to the message receiver that has a
        // simulator node configured
        final ServerMessageReceiver serverMessageReceiver = new ServerMessageReceiver(tc) {
            @Override
            public void publishResponse(ByteArrayOutputStream output) throws ProtocolException {

                // Publish the responses to the host
                log.info("Publish the response. Prepare message to send to host using netSimulationMessageHandler: {}",
                        netServerMessageHandler);
                try {
                    // send to handler
                    ///// netServerMessageHandler.send(netBidibTcpPort, output.toByteArray());
                }
                catch (Exception ex) {
                    log.warn("Process messages failed.", ex);
                }
            }

            @Override
            public void removeNodeListener(NodeListener nodeListener) {

            }

//            @Override
//            public void setIgnoreWrongMessageNumber(boolean ignoreWrongMessageNumber) {
//
//            }
        };
        
        TcpServerNetMessageHandler netMessageHandler = new TcpServerNetMessageHandler(serverMessageReceiver);
        log.info("Created the server netMessageHandler: {}", netMessageHandler);
        return netMessageHandler;
        
    }

    //@Override
    public void stop() {
        log.info("Stop the TCP server.");

        if (netBidibTcpPort != null) {
            log.info("Stop the port.");
            netBidibTcpPort.stop();

            if (portWorker != null) {
                synchronized (stopSync) {
                    try {
                        portWorker.join(5000L);
                    }
                    catch (InterruptedException ex) {
                        log.warn("Wait for termination of port worker failed.", ex);
                    }
                    portWorker = null;
                }
            }

            isStarted.set(false);
            netBidibTcpPort = null;
        }
        
        if (bidibMessageReveiver != null) {
            //BiDiBTrafficController tc = InstanceManager.getDefault(BiDiBSystemConnectionMemo.class).getBiDiBTrafficController();
            tc.removeRawMessageListener(bidibMessageReveiver);
            bidibMessageReveiver = null;
        }

        log.info("Stop the TCP server finished.");
    }

    private final static Logger log = LoggerFactory.getLogger(NetPlainTcpBidib.class);
    
}
