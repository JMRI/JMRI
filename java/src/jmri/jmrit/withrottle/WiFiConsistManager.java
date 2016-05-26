package jmri.jmrit.withrottle;

import java.util.ArrayList;
import jmri.Consist;
import jmri.DccLocoAddress;
import jmri.implementation.AbstractConsistManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brett Hoffman Copyright (C) 2010, 2011
 * @version $Revision: 18416 $
 */
public class WiFiConsistManager extends AbstractConsistManager {

    ArrayList<ControllerInterface> listeners = null;
    boolean isValid = false;

    public WiFiConsistManager() {
        super();
        log.debug("New WiFiConsistManager");
        isValid = true;
    }

    public DccLocoAddress stringToDcc(String s) {
        int num = Integer.parseInt(s.substring(1));
        boolean isLong = (s.charAt(0) == 'L');
        return (new DccLocoAddress(num, isLong));
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
    public Consist addConsist(DccLocoAddress address) {
        WiFiConsist consist;
        consist = new WiFiConsist(address);
        consistTable.put(address, consist);
        return consist;
    }

    @Override
    public boolean isCommandStationConsistPossible() {
        return false;
    }

    @Override
    public boolean csConsistNeedsSeperateAddress() {
        return false;
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

    private final static Logger log = LoggerFactory.getLogger(WiFiConsistManager.class.getName());

}
