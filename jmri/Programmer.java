/* Programmer.java */

package jmri;

import jmri.ProgListener;
import java.beans.PropertyChangeListener;

/**
 * "Programmer" is a capability to program decoders.  These come in two types:
 * <UL>
 * <LI>Service Mode, e.g. on a programming track
 * <LI>Ops Mode, e.g. "programming on the main"
 * </UL>
 * depending on which type you have, only certain modes can
 * be set.
 * <P>
 * You get a Programmer object from a ProgrammerManager,
 * which in turn can be located from the InstanceManager.
 * @see         jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 1.14 $
 */
public interface Programmer  {

    // mode e.g. register, direct, paged
    public static final int NONE	    =  0;
    public static final int REGISTERMODE    = 11;
    public static final int PAGEMODE        = 21;
    public static final int DIRECTBITMODE   = 31;
    public static final int DIRECTBYTEMODE  = 32;
    public static final int ADDRESSMODE     = 41;

    public static final int OPSBYTEMODE     = 101;
    public static final int OPSBITMODE      = 102;

    /**
     * Perform a CV write in the system-specific manner,
     * and using the specified programming mode.
     * Note that this returns before the write
     * is complete; you have to provide a ProgListener to hear about
     * completion. The exceptions will only be thrown at the start, not
     * during the actual programming sequence. A typical exception would be
     * due to an invalid mode (though that should be prevented earlier)
     */
    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException;

    /**
     * Perform a CV read in the system-specific manner,
     * and using the specified programming mode.
     * Note that this returns before the read
     * is complete; you have to provide a ProgListener to hear about
     * completion. The exceptions will only be thrown at the start, not
     * during the actual programming sequence. A typical exception would be
     * due to an invalid mode (though that should be prevented earlier)
     */
    public void readCV(int CV, ProgListener p) throws ProgrammerException;

    /**
     * Confirm the value of a CV using the specified programming mode.
     * On some systems, this is faster than a read.
     * Note that this returns before the confirm
     * is complete; you have to provide a ProgListener to hear about
     * completion. The exceptions will only be thrown at the start, not
     * during the actual programming sequence. A typical exception would be
     * due to an invalid mode (though that should be prevented earlier)
     */
    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException;

    /*
     * Mode is a property that can be set and queried for the
     * programmer.  Notification is also possible...
     */
    public void setMode(int mode);
    public int  getMode();

    /**
     * Check if a given mode is available
     * @param mode Availability of this mode is returned
     * @return True if the mode is available
     */
    public boolean hasMode(int mode);

    public boolean getCanRead();

    public void addPropertyChangeListener(PropertyChangeListener p);
    public void removePropertyChangeListener(PropertyChangeListener p);

    // error handling on request is via exceptions
    // results are returned via the ProgListener callback

    public String decodeErrorCode(int i);

}


/* @(#)Programmer.java */
