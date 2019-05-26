package jmri.progdebugger;

import java.util.HashMap;
import jmri.AddressedProgrammer;
import jmri.managers.DefaultProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an implementation of ProgrammerManager for the debug programmer. It
 * will consistently return the same ProgDebugger instance for a given request.
 * <p>
 * It uses the DefaultProgrammerManager to handle the service mode operations.
 *
 * @see jmri.managers.DefaultProgrammerManager
 * @author Bob Jacobsen Copyright (C) 2002
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
    HashMap<Integer, ProgDebugger> opsProgrammers = new HashMap<>();

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        int address = pAddress;
        if (!pLongAddress) {
            address = -address;
        }
        // look for an existing entry by getting something from hash table
        ProgDebugger saw = opsProgrammers.get(address);
        if (saw != null) {
            log.debug("return existing ops-mode programmer {} {}", pAddress, pLongAddress);
            return saw;
        }
        // if not, save a new one & return it
        opsProgrammers.put(address, saw = new ProgDebugger(pLongAddress, pAddress));
        log.debug("return new ops-mode programmer {} {}", pAddress, pLongAddress);
        return saw;
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    /**
     * Debug programmer does provide Ops Mode
     *
     * @return true
     */
    @Override
    public boolean isAddressedModePossible() {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(DebugProgrammerManager.class);
}
