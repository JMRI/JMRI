/* DefaultProgrammerManager.java */

package jmri;

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


    public Programmer getServiceModeProgrammer() {return mProgrammer;}
    public Programmer getOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    public Programmer reserveServiceModeProgrammer() {
        return mProgrammer;
    }
    public void releaseServiceModeProgrammer(Programmer p) {}

    public Programmer reserveOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    };
    public void releaseOopsModeProgrammer(Programmer p) {}
}


/* @(#)DefaultProgrammerManager.java */
