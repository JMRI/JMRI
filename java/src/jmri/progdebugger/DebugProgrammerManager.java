/* DebugProgrammerManager.java */
package jmri.progdebugger;

import java.util.Hashtable;
import jmri.AddressedProgrammer;
import jmri.managers.DefaultProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an implementation of ProgrammerManager for the debug programmer. It
 * will consistently return the same ProgDebugger instance for a given request.
 * <P>
 * It uses the DefaultProgrammerManager to handle the service mode operations.
 *
 * @see jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Revision$
 */
public class DebugProgrammerManager extends DefaultProgrammerManager {

    public DebugProgrammerManager() {
        super(new ProgDebugger());
    }

    public DebugProgrammerManager(jmri.jmrix.SystemConnectionMemo memo) {
        super(new ProgDebugger(), memo);
    }

    /**
     * Save the mapping from addresses to Programmer objects. Short addresses
     * are saved as negative numbers.
     */
    Hashtable<Integer, ProgDebugger> opsProgrammers = new Hashtable<Integer, ProgDebugger>();

    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        int address = pAddress;
        if (!pLongAddress) {
            address = -address;
        }
        // look for an existing entry by getting something from hash table
        ProgDebugger saw = opsProgrammers.get(Integer.valueOf(address));
        if (saw != null) {
            if (log.isDebugEnabled()) {
                log.debug("return existing ops-mode programmer "
                        + pAddress + " " + pLongAddress);
            }
            return saw;
        }
        // if not, save a new one & return it
        opsProgrammers.put(Integer.valueOf(address), saw = new ProgDebugger(pLongAddress, pAddress));
        if (log.isDebugEnabled()) {
            log.debug("return new ops-mode programmer "
                    + pAddress + " " + pLongAddress);
        }
        return saw;
    }

    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    /**
     * Debug programmer does provide Ops Mode
     *
     * @return true
     */
    public boolean isAddressedModePossible() {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(DebugProgrammerManager.class.getName());
}


/* @(#)DefaultProgrammerManager.java */
