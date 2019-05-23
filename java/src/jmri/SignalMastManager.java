package jmri;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Interface for obtaining signal masts.
 * <p>
 * This doesn't have a "new" method, as SignalMasts are separately implemented,
 * instead of being system-specific.
 *
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
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public interface SignalMastManager extends ProvidingManager<SignalMast> {

    // to free resources when no longer used
    @Override
    public void dispose();

    /**
     * Get an existing SignalMast or return null if it doesn't exist. 
     * 
     * Locates via user name, then system name if needed.
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    @CheckForNull public SignalMast getSignalMast(@Nonnull String name);

    /**
     * Get the SignalMast with the user name, then system name if needed; if that fails, create a
     * new SignalMast. 
     * If the name is a valid system name, it will be used for the
     * new SignalMast.
     *
     * @param name User name, system name, or address which can be promoted to
     *             system name
     * @return Never null
     * @throws IllegalArgumentException if SignalMast doesn't already exist and
     *                                  the manager cannot create the SignalMast
     *                                  due to an illegal name or name that
     *                                  can't be parsed.
     */
    @Nonnull public SignalMast provideSignalMast(@Nonnull String name);

    /**
     * Retrieve or create a new signal mast with a given system name. If a new object is created,
     * it is also registered in this manager.
     * @param systemName the system name by which to look up the mast, or to create anew.
     * @param mastClass specific signal mast class. Must have a single-argument string
     *                  constructor to crete it by system name.
     * @return a registered signal mast (might be newly created),
     * @throws JmriException if a signal mast with the given system name is already registered
     * but it is not of the correct class, or an internal error happens during construction.
     */
    @Nonnull public SignalMast provideCustomSignalMast(@Nonnull String systemName,
                                                       Class<? extends SignalMast> mastClass)
            throws JmriException;

    @Nonnull public SignalMast provideSignalMast(@Nonnull String prefix, // nominally IF$shsm
            @Nonnull String signalSystem,
            @Nonnull String mastName,
            @Nonnull String[] heads);

    @CheckForNull public SignalMast getByUserName(@Nonnull String s);

    @CheckForNull public SignalMast getBySystemName(@Nonnull String s);

}
