/* DebugProgrammerManager.java */

package jmri.progdebugger;

import jmri.*;
import java.util.Hashtable;

/**
 * Provides an implementation of ProgrammerManager for the
 * debug programmer. It will consistently return the same
 * ProgDebugger instance for a given request.
 * <P>
 * It uses the DefaultProgrammerManager to handle the service
 * mode operations.
 *
 * @see             jmri.ProgrammerManager
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision: 1.3 $
 */
public class DebugProgrammerManager extends DefaultProgrammerManager {


    public DebugProgrammerManager() {
        super(new ProgDebugger());
    }

    /**
     * Save the mapping from addresses to Programmer objects.
     * Short addresses are saved as negative numbers.
     */
    Hashtable opsProgrammers = new Hashtable();


    public Programmer getOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        int address = pAddress;
        if (!pLongAddress) address = -address;
        // look for an existing entry by getting something from hash table
        ProgDebugger saw = ((ProgDebugger)opsProgrammers.get(new Integer(address)));
        if (saw!=null) {
            if (log.isDebugEnabled()) log.debug("return existing ops-mode programmer "
                                                +pAddress+" "+pLongAddress);
            return saw;
        }
        // if not, save a new one & return it
        opsProgrammers.put(new Integer(address), saw = new ProgDebugger());
        if (log.isDebugEnabled()) log.debug("return new ops-mode programmer "
                                                +pAddress+" "+pLongAddress);
        return saw;
    }

    public Programmer reserveOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    };
    public void releaseOopsModeProgrammer(Programmer p) {}

    /**
     * Debug programmer does provide Ops Mode
     * @returns true
     */
    public boolean isOpsModePossible() {return true;}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DebugProgrammerManager.class.getName());
}


/* @(#)DefaultProgrammerManager.java */
