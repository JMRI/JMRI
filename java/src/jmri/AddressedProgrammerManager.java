package jmri;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Get access to available {@link Programmer} objects.
 * <p>
 * Programmers come in two types:
 * <ul>
 * <li>Global, previously "Service Mode" or on a programming track. Request
 * these from an instance of {@link GlobalProgrammerManager}.
 * <li>Addressed, previously "Ops Mode" also known as "programming on the main".
 * Request these from an instance of this interface.
 * </ul>
 * You get a {@link Programmer} object from a ProgrammerManager, which in turn
 * can be located from the {@link InstanceManager}.
 * <p>
 * This interface also provides a reserve/release system for tools that want to
 * pretend they have exclusive use of a Programmer. This is a cooperative
 * reservation; both tools (first and second reserver) must be using the
 * reserve/release interface.
 * <p>
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
 * @see jmri.Programmer
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2014
 * @since 3.9.6
 */
public interface AddressedProgrammerManager {

    /**
     * Gain access to a Addressed Mode Programmer without reservation.
     *
     * @param pLongAddress true if this is a long (14 bit) address, else false
     * @param pAddress     specific decoder address to use
     * @return null only if there isn't an Ops Mode Programmer in the system
     */
    @CheckForNull
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress);

    /**
     * Gain access to a Addressed Mode Programmer without reservation.
     *
     * @param address specific decoder address to use
     * @return null only if there isn't an Ops Mode Programmer in the system
     */
    @CheckForNull
    public default AddressedProgrammer getAddressedProgrammer(@Nonnull DccLocoAddress address) {
        return this.getAddressedProgrammer(address.isLongAddress(), address.getNumber());
    }

    /**
     * Gain access to a (the) Addressed Mode Programmer, in the process
     * reserving it for yourself.
     *
     * @param pLongAddress true if this is a long (14 bit) address, else false
     * @param pAddress     Specific decoder address to use
     * @return null if the address is in use by a reserved programmer
     */
    @CheckForNull
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress);

    /**
     * Gain access to a (the) Addressed Mode Programmer, in the process
     * reserving it for yourself.
     *
     * @param address specific decoder address to use
     * @return null if the address is in use by a reserved programmer
     */
    @CheckForNull
    public default AddressedProgrammer reserveAddressedProgrammer(@Nonnull DccLocoAddress address) {
        return this.reserveAddressedProgrammer(address.isLongAddress(), address.getNumber());
    }

    /**
     * Return access to an Addressed Mode Programmer, so that it can be used
     * elsewhere.
     *
     * @param p the programmer to release
     */
    public void releaseAddressedProgrammer(@Nonnull AddressedProgrammer p);

    /**
     * Convenience method to check whether you'll be able to get an Addressed
     * Mode programmer.
     *
     * @return false if there's no chance of getting one
     */
    public boolean isAddressedModePossible();

    /**
     * Convenience method to check whether you'll be able to get an Addressed
     * Mode programmer for a specific address
     *
     * @param address the address to get a programmer for
     * @return false if there's no chance of getting one
     */
    public boolean isAddressedModePossible(@Nonnull LocoAddress address);

    /**
     * Get the list of {@link ProgrammingMode} (generally) supported by
     * Programmers provided by this Manager.
     * <p>
     * Use this to enquire about modes before you're ready to request a specific
     * programmer.
     * <p>
     * If the order is significant, earlier modes are better.
     *
     * @return the programming modes or an empty list
     */
    @Nonnull
    public List<ProgrammingMode> getDefaultModes();

    /**
     * Provides the human-readable representation for including
     * ProgrammerManagers directly in user interface controls, so it should
     * return a user-provided name for this particular one.
     *
     * @return the name for the programmer
     */
    @Nonnull
    public String getUserName();

    /**
     * Provides the human-readable representation for including
     * ProgrammerManagers directly in user interface controls, so it should
     * return a user-provided name for this particular one.
     *
     * @return the name for the programmer
     */
    @Nonnull
    @Override
    public String toString();
}
