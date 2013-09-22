/* Programmer.java */

package jmri;

import jmri.ProgListener;
import java.beans.PropertyChangeListener;

/**
 * Provide access to the hardware DCC decoder programming capability.
 * <P>
 * Programmers come in multiple types:
 * <UL>
 * <LI>Global, previously Service Mode, e.g. on a programming track
 * <LI>Addressed, previously Ops Mode, e.g. "programming on the main"
 * </UL>
 * Different equipment may also require different programmers:
 * <LI>DCC CV programming, on service mode track or on the main
 * <LI>CBUS Node Variable programmers
 * <LI>LocoNet System Variable programmers
 * <LI>LocoNet Op Switch programmers
 * </UL>
 * Depending on which type you have, only certain modes can
 * be set. Valid modes are specified by the class static constants.
 * <P>
 * You get a Programmer object from a {@link ProgrammerManager},
 * which in turn can be located from the {@link InstanceManager}.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @see         jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @version	$Revision$
 */
public interface Programmer  {

    // mode e.g. register, direct, paged

    /**
     * No programming mode available
     */
    public static final int NONE	    =  0;
    /**
     * NMRA "Register" mode
     */
    public static final int REGISTERMODE    = 11;

    /**
     * NMRA "Paged" mode
     */
    public static final int PAGEMODE        = 21;
    
    /**
     * NMRA "Direct" mode, using only the bit-wise operations
     */
    public static final int DIRECTBITMODE   = 31;

    /**
     * NMRA "Direct" mode, using only the byte-wise operations
     */
    public static final int DIRECTBYTEMODE  = 32;
    /**
     * NMRA "Address-only" mode. Often implemented as
     * a proper subset of "Register" mode, as the 
     * underlying operation is the same.
     */
    public static final int ADDRESSMODE     = 41;

    /**
     * NMRA "Operations" or "Programming on the main" mode, using only the byte-wise operations
     */
    public static final int OPSBYTEMODE     = 101;
    /**
     * NMRA "Operations" or "Programming on the main" mode, using only the bit-wise operations
     */
    public static final int OPSBITMODE      = 102;

    /**
     * NMRA "Programming on the main" mode for stationary decoders, 
     * using only the byte-wise operations. Note that this is 
     * defined as using the "normal", not "extended" addressing.
     */
    public static final int OPSACCBYTEMODE  = 111;

    /**
     * NMRA "Programming on the main" mode for stationary decoders, 
     * using only the bit-wise operations. Note that this is 
     * defined as using the "normal", not "extended" addressing.
     */
    public static final int OPSACCBITMODE   = 112;

    /**
     * NMRA "Programming on the main" mode for stationary decoders, 
     * using only the byte-wise operations and "extended" addressing.
     */
    public static final int OPSACCEXTBYTEMODE = 121;

    /**
     * NMRA "Programming on the main" mode for stationary decoders, 
     * using only the bit-wise operations and "extended" addressing.
     */
    public static final int OPSACCEXTBITMODE  = 122;

    /**
     * CBUS mode for programming node variables.
     */
    public static final int CBUSNODEVARMODE  = 140;

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

    /**
     * Set the programmer to a particular mode.  Only certain
     * modes may be available for any particular implementation.
     * If an invalid mode is requested, the active mode is unchanged.
     * @param mode One of the class-constant mode values
     */
    public void setMode(int mode);
    /**
     * Get the current programming mode
     * @return one of the class constants identifying a mode
     */
    public int  getMode();

    /**
     * Check if a given mode is available
     * @param mode Availability of this mode is returned
     * @return True if the mode is available
     */
    public boolean hasMode(int mode);

    public boolean getCanRead();

    /**
     * Provide the highest CV number that can be programmed.
     *<p>
     * Access above this in readCV, writeCV, confirmCV will
     * throw an exception, but this allows you to check without
     * attempting an operation.
     */
    public int getMaxCvAddr();
    
    public void addPropertyChangeListener(PropertyChangeListener p);
    public void removePropertyChangeListener(PropertyChangeListener p);

    // error handling on request is via exceptions
    // results are returned via the ProgListener callback

    public String decodeErrorCode(int i);

}


/* @(#)Programmer.java */
