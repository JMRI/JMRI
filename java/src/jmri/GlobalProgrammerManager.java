package jmri;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Get access to available {@link Programmer} objects.
 * <p>
 * Programmers come in two types:
 * <ul>
 * <li>Global, previously "Service Mode" or on a programming track. Request
 * these from an instance of this interface.
 * <li>Addressed, previously "Ops Mode" also known as "programming on the main". Request
 * these from an instance of {@link AddressedProgrammerManager}.
 * </ul>
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
public interface GlobalProgrammerManager {

    /**
     * Gain access to the Global Mode Programmer without reservation.
     *
     * @return null only if there isn't a Global Mode Programmer available via
     *         this Manager.
     */
    @CheckForNull
    public Programmer getGlobalProgrammer();

    /**
     * Gain access to the Global Mode Programmer, in the process reserving it
     * for yourself.
     *
     * @return null if the existing Global Mode programmer is in use
     */
    @CheckForNull
    public Programmer reserveGlobalProgrammer();

    /**
     * Return access to the Global Mode Programmer, so that it can be used
     * elsewhere.
     *
     * @param p the Programmer to release
     */
    public void releaseGlobalProgrammer(@Nonnull Programmer p);

    /**
     * Convenience method to check whether you'll be able to get a Global Mode
     * programmer.
     *
     * @return false if there's no chance of getting one
     */
    public boolean isGlobalProgrammerAvailable();

    /**
     * Provides the human-readable representation for including
     * ProgrammerManagers directly in user interface components, so it should return a
     * user-provided name for this particular one.
     *
     * @return user name of the GlobalProgrammerManager
     */
    @Nonnull
    public String getUserName();

    /**
     * toString() provides the human-readable representation for including
     * ProgrammerManagers directly in user interface components, so it should return a
     * user-provided name for this particular one.
     *
     * @return String representation of the GlobalProgrammerManager
     */
    @Nonnull
    @Override
    public String toString();
}
