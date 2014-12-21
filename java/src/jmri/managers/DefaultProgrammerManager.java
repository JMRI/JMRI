/* DefaultProgrammerManager.java */

package jmri.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;

/**
 * Provides a very-basic implementation of ProgrammerManager.  You give it
 * a service-mode Programmer at construction time; Ops Mode requests
 * get a null in response.
 *
 * @see             jmri.ProgrammerManager
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision$
 */
public class DefaultProgrammerManager implements ProgrammerManager {

    private Programmer mProgrammer;
    public DefaultProgrammerManager(Programmer pProgrammer) {
        mProgrammer = pProgrammer;
    }

    public DefaultProgrammerManager(Programmer pProgrammer, jmri.jmrix.SystemConnectionMemo memo) {
        this(pProgrammer);
		this.userName = memo.getUserName();
	}
    
    String userName = "<Default>";

    /**
     * Provides the human-readable 
     * representation for including ProgrammerManagers
     * directly in e.g. JComboBoxes, so it should return a
     * user-provided name for this particular one.
     */
    public String getUserName() { return userName; }
    
    /**
     * Provides the human-readable 
     * representation for including ProgrammerManagers
     * directly in e.g. JComboBoxes, so it should return a
     * user-provided name for this particular one.
     */
    public String toString() { return getUserName(); }
    
    public Programmer getGlobalProgrammer() {
        if (log.isDebugEnabled()) log.debug("return default service-mode programmer");
        return mProgrammer;
    }
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    public Programmer reserveGlobalProgrammer() {
        return mProgrammer;
    }
    public void releaseGlobalProgrammer(Programmer p) {}

    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
    public void releaseAddressedProgrammer(AddressedProgrammer p) {}

    /**
     * Default programmer does not provide Ops Mode
     * @return false if there's no chance of getting one
     */
    public boolean isAddressedModePossible() {return false;}

    /**
     * Allow for implementations that do not support Service mode programming
     * @return false if there's no chance of getting one
     */
    public boolean isGlobalProgrammerAvailable() {return true;}

    static Logger log = LoggerFactory.getLogger(DefaultProgrammerManager.class.getName());
}
/* @(#)DefaultProgrammerManager.java */
