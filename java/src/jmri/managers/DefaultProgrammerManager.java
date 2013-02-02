/* DefaultProgrammerManager.java */

package jmri.managers;

import org.apache.log4j.Logger;
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
    
    String userName = "Internal";

    public String getUserName() { return userName; }
    
    public Programmer getGlobalProgrammer() {
        if (log.isDebugEnabled()) log.debug("return default service-mode programmer");
        return mProgrammer;
    }
    public Programmer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    public Programmer reserveGlobalProgrammer() {
        return mProgrammer;
    }
    public void releaseGlobalProgrammer(Programmer p) {}

    public Programmer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
    public void releaseAddressedProgrammer(Programmer p) {}

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

    static Logger log = Logger.getLogger(DefaultProgrammerManager.class.getName());
}
/* @(#)DefaultProgrammerManager.java */
