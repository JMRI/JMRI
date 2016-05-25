package jmri.jmrix.loconet.locormi;

/**
 * Title: Description: Copyright: Copyright (c) 2002 Company:
 *
 * @author Alex Shepherd
 * @version $Revision$
 */
 // -Djava.security.policy=lib/security.policy
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LnMessageServer extends UnicastRemoteObject implements LnMessageServerInterface {

    // versioned Jul 17, 2003 - was CVS revision 1.5
    static final long serialVersionUID = 8934498417916438203L;

    private static LnMessageServer self = null;
    static final String serviceName = "LocoNetServer";
    private final static Logger log = LoggerFactory.getLogger(LnMessageServer.class.getName());

    private LnMessageServer() throws RemoteException {
        super();
    }

    public LnMessageBufferInterface getMessageBuffer() throws RemoteException {
        return new LnMessageBuffer();
    }

    public static synchronized LnMessageServer getInstance() throws RemoteException {
        if (self == null) {
            System.setSecurityManager(new java.rmi.RMISecurityManager());

            self = new LnMessageServer();
        }

        return self;
    }

    public synchronized void enable() {
        Registry localRegistry = null;
        try {
            log.debug("Create RMI Registry for: " + serviceName);
            localRegistry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        } catch (java.rmi.RemoteException ex) {
        }
        try {
            if (localRegistry == null) {
                log.warn("Could not Create RMI Registry, Attempting to Locate existing Registry for: " + serviceName);
                localRegistry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
            }

            log.debug("Register LocoNet Server: " + serviceName + " with RMI Registry");
            localRegistry.rebind(serviceName, self);

            log.debug("Register LocoNet Server Complete");
        } catch (Exception ex) {
            log.warn("LnMessageServer: " + ex);
        }
    }

    public synchronized void disable() {
        try {
            Naming.unbind(serviceName);
        } catch (Exception ex) {
            log.error("Exception during disable: " + ex);
        }
    }
}
