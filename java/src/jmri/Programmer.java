package jmri;

import java.beans.PropertyChangeListener;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Provide access to the hardware DCC decoder programming capability.
 * <p>
 * Programmers come in multiple types:
 * <ul>
 * <li>Global, previously "Service Mode" or on a programming track
 * <li>Addressed, previously "Ops Mode" also known as "programming on the main"
 * </ul>
 * Different equipment may also require different programmers:
 * <ul>
 * <li>DCC CV programming, on service mode track or on the main
 * <li>CBUS Node Variable programmers
 * <li>LocoNet System Variable programmers
 * <li>LocoNet Op Switch programmers
 * <li>etc
 * </ul>
 * Depending on which type you have, only certain modes can be set. Valid modes
 * are specified by the class static constants.
 * <p>
 * You get a Programmer object from a {@link GlobalProgrammerManager} or an
 * {@link AddressedProgrammerManager}, which in turn can be located from the
 * {@link InstanceManager}.
 * <p>
 * Starting in JMRI 3.5.5, the CV addresses are Strings for generality. The
 * methods that use ints for CV addresses will later be deprecated.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @see jmri.GlobalProgrammerManager
 * @see jmri.AddressedProgrammerManager
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2013
 */
public interface Programmer {

    /**
     * Perform a CV write in the system-specific manner, and using the specified
     * programming mode.
     * <p>
     * Handles a general address space through a String address. Each programmer
     * defines the acceptable formats.
     * <p>
     * Note that this returns before the write is complete; you have to provide
     * a ProgListener to hear about completion. For simplicity, expect the return to be on the 
     * <a href="http://jmri.org/help/en/html/doc/Technical/Threads.shtml">GUI thread</a>.
     * <p>
     * Exceptions will only be
     * thrown at the start, not during the actual programming sequence. A
     * typical exception would be due to an invalid mode (though that should be
     * prevented earlier)
     *
     * @param CV  the CV to write
     * @param val the value to write
     * @param p   the listener that will be notified of the write
     * @throws jmri.ProgrammerException if unable to communicate
     */
    public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException;

    /**
     * Perform a CV read in the system-specific manner, and using the specified
     * programming mode.
     * <p>
     * Handles a general address space through a String address. Each programmer
     * defines the acceptable formats.
     * <p>
     * Note that this returns before the write is complete; you have to provide
     * a ProgListener to hear about completion. For simplicity, expect the return to be on the 
     * <a href="http://jmri.org/help/en/html/doc/Technical/Threads.shtml">GUI thread</a>.
     * <p>
     * Exceptions will only be
     * thrown at the start, not during the actual programming sequence. A
     * typical exception would be due to an invalid mode (though that should be
     * prevented earlier)
     *
     * @param CV the CV to read
     * @param p  the listener that will be notified of the read
     * @throws jmri.ProgrammerException if unable to communicate
     */
    public void readCV(String CV, ProgListener p) throws ProgrammerException;

    /**
     * Confirm the value of a CV using the specified programming mode. On some
     * systems, this is faster than a read.
     * <p>
     * Handles a general address space through a String address. Each programmer
     * defines the acceptable formats.
     * <p>
     * Note that this returns before the write is complete; you have to provide
     * a ProgListener to hear about completion. For simplicity, expect the return to be on the 
     * <a href="http://jmri.org/help/en/html/doc/Technical/Threads.shtml">GUI thread</a>.
     * <p>
     * Exceptions will only be
     * thrown at the start, not during the actual programming sequence. A
     * typical exception would be due to an invalid mode (though that should be
     * prevented earlier)
     *
     * @param CV  the CV to confirm
     * @param val the value to confirm
     * @param p   the listener that will be notified of the confirmation
     * @throws jmri.ProgrammerException if unable to communicate
     */
    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException;

    /**
     * Get the list of {@link ProgrammingMode} supported by this Programmer. If
     * the order is significant, earlier modes are better.
     *
     * @return the list of supported modes or an empty list
     */
    @Nonnull
    public List<ProgrammingMode> getSupportedModes();

    /**
     * Set the programmer to a particular mode.
     * <p>
     * Mode is a bound parameter; mode changes fire listeners.
     * <p>
     * Only modes returned by {@link #getSupportedModes} are supported. If an
     * invalid mode is requested, the active mode is unchanged.
     *
     * @param p a valid node returned by {@link #getSupportedModes()} or null;
     *          null is ignored if {@link #getSupportedModes()} is not empty
     */
    public void setMode(ProgrammingMode p);

    /**
     * Get the current programming mode
     *
     * @return the current mode or null if none is defined and no default mode
     *         is defined
     */
    public ProgrammingMode getMode();

    /**
     * Checks the general read capability, regardless of mode
     *
     * @return true if the programmer is capable of reading; false otherwise
     */
    public boolean getCanRead();

    /**
     * Checks the general read capability, regardless of mode, for a specific
     * address
     *
     * @param addr the address to read
     * @return true if the address can be read; false otherwise
     */
    public boolean getCanRead(String addr);

    /**
     * Checks the general write capability, regardless of mode
     *
     * @return true if the programmer is capable of writing; false otherwise
     */
    public boolean getCanWrite();

    /**
     * Checks the general write capability, regardless of mode, for a specific
     * address
     *
     * @param addr the address to write to
     * @return true if the address can be written to; false otherwise
     */
    public boolean getCanWrite(String addr);

    /**
     * Learn about whether the programmer does any kind of verification of write
     * operations
     *
     * @param addr A CV address to check (in case this varies with CV range) or
     *             null for any
     * @return The confirmation behavior that can be counted on (might be better
     *         in some cases)
     */
    @Nonnull
    public WriteConfirmMode getWriteConfirmMode(String addr);

    enum WriteConfirmMode {
        /**
         * No verification available, writes are blind
         */
        NotVerified,
        /**
         * Programmer signals error if there's no reply from the device
         */
        DecoderReply,
        /**
         * Programmer does a read after write to verify
         */
        ReadAfterWrite
    }

    /**
     * wrapper to call {@link jmri.ProgListener#programmingOpReply} that verifies
     * the specified progListener is not null.
     *
     * @param p listener to notify
     * @param value result value
     * @param status code from jmri.ProgListener 
     */
    default public void notifyProgListenerEnd(ProgListener p, int value, int status) {
        if ( p != null ) {
           p.programmingOpReply(value, status);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener p);

    public void removePropertyChangeListener(PropertyChangeListener p);

    // error handling on request is via exceptions
    // results are returned via the ProgListener callback
    @Nonnull
    public String decodeErrorCode(int i);

}
