package jmri.jmrix.loconet.locormi;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import jmri.jmrix.loconet.LnTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alex Shepherd Copyright (c) 2002
 */
public class LnMessageServer extends UnicastRemoteObject implements LnMessageServerInterface {

    // This is required for RMI usage, do not remove
    static final long serialVersionUID = 8934498417916438203L;

    private static LnMessageServer self = null;
    static final String serviceName = "LocoNetServer"; // NOI18N

    private LnMessageServer() throws RemoteException {
        super();
    }

    @Override
    public LnMessageBufferInterface getMessageBuffer(LnTrafficController tc) throws RemoteException {
        return new LnMessageBuffer(tc);
    }

    public static synchronized LnMessageServer getInstance() throws RemoteException {
        if (self == null) {
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            self = new LnMessageServer();
        }
        return self;
    }

    public synchronized void enable() {
        Registry localRegistry = null;
        try {
            log.debug("Create RMI Registry for: {}", serviceName); // NOI18N
            localRegistry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        } catch (java.rmi.RemoteException ex) {
        }
        try {
            if (localRegistry == null) {
                log.warn("Could not Create RMI Registry, Attempting to Locate existing Registry for: {}", serviceName); // NOI18N
                localRegistry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
            }

            log.debug("Register LocoNet Server: {} with RMI Registry", serviceName); // NOI18N
            localRegistry.rebind(serviceName, self);

            log.debug("Register LocoNet Server Complete"); // NOI18N
        } catch (Exception ex) {
            log.warn("LnMessageServer: " + ex); // NOI18N
        }
    }

    public synchronized void disable() {
        try {
            Naming.unbind(serviceName);
        } catch (Exception ex) {
            log.error("Exception during disable: " + ex); // NOI18N
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LnMessageServer.class);

}
