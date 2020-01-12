package jmri.jmrit.withrottle;

import java.util.ArrayList;
import jmri.Consist;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.implementation.NmraConsistManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brett Hoffman Copyright (C) 2010, 2011
 * 
 */
public class WiFiConsistManager extends NmraConsistManager {

    ArrayList<ControllerInterface> listeners = null;
    boolean isValid = false;

    public WiFiConsistManager() {
        super(jmri.InstanceManager.getDefault(jmri.CommandStation.class));
        log.debug("New WiFiConsistManager");
        isValid = true;
    }

    /**
     * Check to see if an address will try to broadcast (0) a programming
     * message.
     *
     * @param addr The address to check
     * @return true if address is no good, otherwise false
     */
    public boolean checkForBroadcastAddress(DccLocoAddress addr) {
        if (addr.getNumber() < 1) {
            log.warn("Trying to use broadcast address!");
            return true;
        }
        return false;
    }

    @Override
    public Consist addConsist(LocoAddress address) {
        if (! (address instanceof DccLocoAddress)) { 
            throw new IllegalArgumentException("address is not a DccLocoAddress object");
        }
        WiFiConsist consist;
        consist = new WiFiConsist((DccLocoAddress) address);
        consistTable.put(address, consist);
        return consist;
    }

    /**
     * Add a listener to handle: listener.sendPacketToDevice(message);
     *
     */
    public void addControllerListener(ControllerInterface listener) {
        if (listeners == null) {
            listeners = new ArrayList<ControllerInterface>(1);
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeControllerListener(ControllerInterface listener) {
        if (listeners == null) {
            return;
        }
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(WiFiConsistManager.class);

}
