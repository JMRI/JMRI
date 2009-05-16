/* DefaultProgrammerManager.java */

package jmri.managers;

import jmri.*;

/**
 * Provides a very-basic implementation of ProgrammerManager.  You give it
 * a service-mode Programmer at construction time; Ops Mode requests
 * get a null in response.
 *
 * @see             jmri.ProgrammerManager
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */
public class DefaultProgrammerManager implements ProgrammerManager {

    private Programmer mProgrammer;

    public DefaultProgrammerManager(Programmer pProgrammer) {
        mProgrammer = pProgrammer;
    }


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
    };
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

    /** 
     * Deprecated Since 2.5.1
     */
    public Programmer getServiceModeProgrammer() {
        return getGlobalProgrammer();
    }
    /** 
     * Deprecated Since 2.5.1
     */
    public Programmer getOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return getAddressedProgrammer(pLongAddress, pAddress);
    }
    /** 
     * Deprecated Since 2.5.1
     */
    public Programmer reserveServiceModeProgrammer() {
        return reserveGlobalProgrammer();
    }
    /** 
     * Deprecated Since 2.5.1
     */
    public void releaseServiceModeProgrammer(Programmer p) {
        releaseGlobalProgrammer(p);
    }
    /** 
     * Deprecated Since 2.5.1
     */
    public Programmer reserveOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return reserveAddressedProgrammer(pLongAddress, pAddress);
    }
    /** 
     * Deprecated Since 2.5.1
     */
    public void releaseOpsModeProgrammer(Programmer p) {
        releaseAddressedProgrammer(p);
    }
    /** 
     * Deprecated Since 2.5.1
     */
    public boolean isServiceModePossible() {
        return isGlobalProgrammerAvailable();
    }
    /** 
     * Deprecated Since 2.5.1
     */
    public boolean isOpsModePossible() {
        return isAddressedModePossible();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultProgrammerManager.class.getName());
}
/* @(#)DefaultProgrammerManager.java */
