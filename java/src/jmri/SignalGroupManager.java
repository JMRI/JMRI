package jmri;

import java.util.List;
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
public interface SignalGroupManager extends Manager<SignalGroup> {

    @CheckForNull public SignalGroup getSignalGroup(@Nonnull String name);

    @CheckForNull public SignalGroup getBySystemName(@Nonnull String name);

    @CheckForNull public SignalGroup getByUserName(@Nonnull String name);

    @Nonnull public SignalGroup newSignalGroup(@Nonnull String sys);

    @Nonnull public SignalGroup provideSignalGroup(@Nonnull String systemName, String userName);

    @Nonnull@Override public List<String> getSystemNameList();

    void deleteSignalGroup(@Nonnull SignalGroup s);
}
