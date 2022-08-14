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
 * <p>
 * Added possibility to supply CV value hint to the system
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
 * @author Andrew Crosland (C) 2021
 */
public interface Programmer extends jmri.Disposable {

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
    void writeCV(String CV, int val, ProgListener p) throws ProgrammerException;

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
    void readCV(String CV, ProgListener p) throws ProgrammerException;

    /**
     * Perform a CV read in the system-specific manner, and using the specified
     * programming mode, possibly using a hint of the current value to speed up
     * programming.
     * <p>
     * Handles a general address space through a String address. Each programmer
     * defines the acceptable formats.
     * <p>
     * On systems that support it, the startVal is a hint as to what the current
     * value of the CV might be (e.g. the value from the roster). This could be
     * verified immediately in direct byte mode to speed up the read process.
     * <p>
     * Note that this returns before the write is complete; you have to provide
     * a ProgListener to hear about completion. For simplicity, expect the return to be on the 
     * <a href="http://jmri.org/help/en/html/doc/Technical/Threads.shtml">GUI thread</a>.
     * <p>
     * Defaults to the normal read method if not overridden in a specific implementation.
     * <p>
     * Exceptions will only be
     * thrown at the start, not during the actual programming sequence. A
     * typical exception would be due to an invalid mode (though that should be
     * prevented earlier)
     *
     * @param CV the CV to read
     * @param p  the listener that will be notified of the read
     * @param startVal  a hint of what the current value might be, or 0
     * @throws jmri.ProgrammerException if unable to communicate
     */
    public default void readCV(String CV, ProgListener p, int startVal) throws ProgrammerException {
        readCV(CV, p);
    }

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
    void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException;

    /**
     * Get the list of {@link ProgrammingMode} supported by this Programmer. If
     * the order is significant, earlier modes are better.
     *
     * @return the list of supported modes or an empty list
     */
    @Nonnull
    List<ProgrammingMode> getSupportedModes();

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
    void setMode(ProgrammingMode p);

    /**
     * Get the current programming mode
     *
     * @return the current mode or null if none is defined and no default mode
     *         is defined
     */
    ProgrammingMode getMode();

    /**
     * Checks the general read capability, regardless of mode
     *
     * @return true if the programmer is capable of reading; false otherwise
     */
    boolean getCanRead();

    /**
     * Checks the general read capability, regardless of mode, for a specific
     * address
     *
     * @param addr the address to read
     * @return true if the address can be read; false otherwise
     */
    boolean getCanRead(String addr);

    /**
     * Checks the general write capability, regardless of mode
     *
     * @return true if the programmer is capable of writing; false otherwise
     */
    boolean getCanWrite();

    /**
     * Checks the general write capability, regardless of mode, for a specific
     * address
     *
     * @param addr the address to write to
     * @return true if the address can be written to; false otherwise
     */
    boolean getCanWrite(String addr);

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
    WriteConfirmMode getWriteConfirmMode(String addr);

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
    default void notifyProgListenerEnd(ProgListener p, int value, int status) {
        if ( p != null ) {
           p.programmingOpReply(value, status);
        }
    }

    void addPropertyChangeListener(PropertyChangeListener p);

    void removePropertyChangeListener(PropertyChangeListener p);

    // error handling on request is via exceptions
    // results are returned via the ProgListener callback
    @Nonnull
    String decodeErrorCode(int i);

    /**
     * Free up system resources.
     * Overriding classes should be capable of this being called
     * multiple times as per the {@link jmri.Disposable} interface.
     * {@inheritDoc}
     */
    @Override
    default void dispose() {}

}
