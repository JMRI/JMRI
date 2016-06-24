package jmri;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Interface for obtaining information about signal systems.
 * <p>
 * Each NamedBean here represents a single signal system. The actual objects are
 * SignalAspectTable objects; that's a current anachronism, soon to be fixed.
 * <P>
 * See the common implementation for information on how loaded, etc.
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
public interface SignalGroupManager extends Manager {

    public @CheckForNull SignalGroup getSignalGroup(@Nonnull String name);

    public @CheckForNull SignalGroup getBySystemName(@Nonnull String name);

    public @CheckForNull SignalGroup getByUserName(@Nonnull String name);

    public @Nonnull SignalGroup newSignalGroup(@Nonnull String sys);

    public @Nonnull SignalGroup provideSignalGroup(@Nonnull String systemName, String userName);

    public @Nonnull List<String> getSystemNameList();

    void deleteSignalGroup(@Nonnull SignalGroup s);
}
