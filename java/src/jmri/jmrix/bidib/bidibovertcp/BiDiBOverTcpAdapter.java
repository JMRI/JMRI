package jmri.jmrix.bidib.bidibovertcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

import jmri.jmrix.bidib.BiDiBNetworkPortController;
import jmri.jmrix.bidib.BiDiBTrafficController;
import org.bidib.jbidibc.core.MessageListener;
import org.bidib.jbidibc.core.NodeListener;
import org.bidib.jbidibc.core.node.listener.TransferListener;
import org.bidib.jbidibc.messages.ConnectionListener;
import org.bidib.jbidibc.net.serialovertcp.NetBidib;
import org.bidib.jbidibc.messages.helpers.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements BiDiBPortController for the BiDiBOverTcp system network
 * connection.
 * <p>
 * This connects a DCC++ via a telnet connection. Normally controlled by the
 * DCCppTcpDriverFrame class.
 * 
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003
 * @author Alex Shepherd Copyright (C) 2003, 2006
 * @author Mark Underwood Copyright (C) 2015
 * @author Eckart Meyer Copyright (C) 2023
 *
 * Based on DCCppNetworkDriverAdapter.
 */
public class BiDiBOverTcpAdapter extends BiDiBNetworkPortController {

    public BiDiBOverTcpAdapter() {
        //super(new BiDiBSystemConnectionMemo());
        setManufacturer(jmri.jmrix.bidib.BiDiBConnectionTypeList.BIDIB);
    }
    
    @Override
    public void connect(String host, int port) throws IOException {
        setHostName(host);
        setPort(port);
        connect();
    }

    /**
     * This methods is called from network connection config and creates the BiDiB object from jbidibc and opens it.
     * The connectPort method of the traffic controller is called for generic initialisation.
     * 
     */
    @Override
    public void connect() {// throws IOException {
        log.debug("connect() starts to {}:{}", getHostName(), getPort());
        opened = false;
        Context ctx = getContext();
        log.debug("Context: {}", ctx);
        bidib = NetBidib.createInstance(getContext());
        BiDiBTrafficController tc = new BiDiBTrafficController(bidib);
        context = tc.connnectPort(this); //must be done before configuring managers since they may need features from the device
        log.debug("memo: {}, bidib over TCP: {}", this.getSystemConnectionMemo(), bidib);
        this.getSystemConnectionMemo().setBiDiBTrafficController(tc);
        if (context != null) {
            opened = true;
        }
        else {
            opened = false;
            log.warn("No device found on port {} ({}})",
                    getCurrentPortName(), getCurrentPortName());
        }
    }

    
    @Override
    public void configure() {
        log.debug("configure");
        this.getSystemConnectionMemo().configureManagers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerAllListeners(ConnectionListener connectionListener, Set<NodeListener> nodeListeners,
                Set<MessageListener> messageListeners, Set<TransferListener> transferListeners) {
        
        NetBidib b = (NetBidib)bidib;
        b.setConnectionListener(connectionListener);
        b.registerListeners(nodeListeners, messageListeners, transferListeners);
    }
    
    // base class methods for the BiDiBNetworkPortController interface
    // not used but must be implemented

    @Override
    public DataInputStream getInputStream() {
        return null;
    }

    @Override
    public DataOutputStream getOutputStream() {
        return null;
    }


    private final static Logger log = LoggerFactory.getLogger(BiDiBOverTcpAdapter.class);

    
}
