package jmri;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Interface for obtaining information about signal groups.
 * <p>
 * Each NamedBean here represents a single signal group. The actual objects are
 * SignalGroupTable objects; that's a current anachronism.
 * <P>
 * See the common implementation for information on how loaded, etc.
 *
 * <hr>
 * This file is part of JMRI.
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
