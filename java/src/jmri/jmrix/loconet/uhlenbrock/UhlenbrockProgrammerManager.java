/* UhlenbrockProgrammerManager.java */
package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.loconet.LnProgrammerManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.SlotManager;

/**
 * Extend LnProgrammerManager to disable on-the-track programming, which is not
 * supported by IB-COM or Intellibox II
 *
 * @see jmri.ProgrammerManager
 * @author	Lisby Copyright (C) 2014
 * @version	$Revision: 27668 $
 */
public class UhlenbrockProgrammerManager extends LnProgrammerManager {

    public UhlenbrockProgrammerManager(SlotManager pSlotManager, LocoNetSystemConnectionMemo memo) {
        super(pSlotManager, memo);
    }

    public boolean isAddressedModePossible() {
        return true;
    }
}
