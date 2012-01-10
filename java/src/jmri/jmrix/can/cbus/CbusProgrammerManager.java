/* CbusProgrammerManager.java */

package jmri.jmrix.can.cbus;

import jmri.managers.DefaultProgrammerManager;
import jmri.Programmer;
import jmri.jmrix.can.TrafficController;

/**
 * Extend DefaultProgrammerManager to provide node variable programmer for CBUS
 *
 * @see     jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version	$Revision$
 */
public class CbusProgrammerManager  extends DefaultProgrammerManager {
    
    public CbusProgrammerManager(TrafficController tc) {
        super(null);  // no service mode programmer available
        this.tc =tc;
    }
    
    TrafficController tc;
    
    public String getUserName() { return "MERG"; }

    /**
     * Global not relevant for CBUS
     * @return false
     */
    public boolean isGlobalModePossible() {return false;}

    public boolean isAddressedModePossible() {return true;}

    public Programmer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new CbusProgrammer(pAddress, tc);
    }

    public Programmer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}


/* @(#)CbusProgrammerManager.java */
