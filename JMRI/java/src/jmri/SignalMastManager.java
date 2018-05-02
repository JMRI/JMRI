package jmri;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Interface for obtaining signal masts.
 * <P>
 * This doesn't have a "new" method, as SignalMasts are separately implemented,
 * instead of being system-specific.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public interface SignalMastManager extends Manager<SignalMast> {

    // to free resources when no longer used
    @Override
    public void dispose();

    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    @CheckForNull public SignalMast getSignalMast(@Nonnull String name);

    /**
     * Locate via user name, then system name if needed. Create new one from
     * system name if needed
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

    @Nonnull public SignalMast provideSignalMast(@Nonnull String prefix, // nominally IF$shsm
            @Nonnull String signalSystem,
            @Nonnull String mastName,
            @Nonnull String[] heads);

    @CheckForNull public SignalMast getByUserName(@Nonnull String s);

    @CheckForNull public SignalMast getBySystemName(@Nonnull String s);

    /**
     * Get a list of all SignalMast system names.
     */
    @Nonnull@Override
 public List<String> getSystemNameList();

}
