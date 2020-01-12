package jmri.jmrix.loconet.locormi;

import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.LnTrafficRouter;
import jmri.jmrix.loconet.LocoNetException;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for the RMI LocoNet server.
 * <p>
 * The main() in this class is for test purposes only.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Alex Shepherd Copyright (c) 2002
 * @author Bob Jacobsen
 */
public class LnMessageClient extends LnTrafficRouter {

    String serverName = null;
    int pollTimeout;
    LnMessageServerInterface lnServer = null;
    LnMessageBufferInterface lnMessageBuffer = null;
    LnMessageClientPollThread pollThread = null;

    public LnMessageClient() {
        super(new LocoNetSystemConnectionMemo());
        clientMemo = new LocoNetSystemConnectionMemo();  // client is separate?
    }

    /**
     * Forward messages to the server.
     */
    @Override
    public void sendLocoNetMessage(LocoNetMessage m) {
        // update statistics
        transmittedMsgCount++;

        // attempt to forward message
        try {
            if (lnMessageBuffer != null) {
                lnMessageBuffer.sendLocoNetMessage(m);
            } else {
                log.warn("sendLocoNetMessage: no connection to server");
            }
        } catch (java.rmi.RemoteException ex) {
            log.warn("sendLocoNetMessage: Exception: ", ex);
        }
    }

    // Messages that are received from the server should
    // be passed to this.notify(LocoNetMessage m)
    /**
     * Start the connection to the server. This is invoked once.
     */
    public void configureRemoteConnection(String remoteHostName, int timeoutSec) throws LocoNetException {
        serverName = remoteHostName;
        pollTimeout = timeoutSec * 1000;  // convert to ms

        log.debug("configureRemoteConnection: {} {}", remoteHostName, timeoutSec);

        try {
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            log.debug("security manager set, set interface to //{}//{}", // NOI18N
                    remoteHostName, LnMessageServer.serviceName);
            LnMessageServerInterface lnServer = (LnMessageServerInterface) java.rmi.Naming.lookup(
                    "//" + serverName + "/" + LnMessageServer.serviceName); // NOI18N

            lnMessageBuffer = lnServer.getMessageBuffer(clientMemo.getLnTrafficController());
            lnMessageBuffer.enable(0);
            pollThread = new LnMessageClientPollThread(this);
        } catch (java.rmi.NotBoundException | java.rmi.RemoteException | java.net.MalformedURLException ex) {
            log.error("Exception while trying to connect: ", ex); // NOI18N
            throw new LocoNetException("Failed to Connect to Server: " + serverName); // NOI18N
        }
    }

    /**
     * Set up all of the other objects to operate with a server connected to
     * this application.
     */
    public void configureLocalServices() {
        // This is invoked on the LnMessageClient after it is
        // ready to go, connection running, etc.

        // create SlotManager (includes programmer) and connection memo
        clientMemo.setLnTrafficController(this);
        // do the common manager config
        clientMemo.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100, // for now, assume full capability
                false, false, false);
        clientMemo.configureManagers();

        // the serial connections (LocoBuffer et al) start
        // various threads here.
    }

    LocoNetSystemConnectionMemo clientMemo;

    public SystemConnectionMemo getAdapterMemo() {
        return clientMemo;
    }

    private final static Logger log = LoggerFactory.getLogger(LnMessageClient.class);

}
