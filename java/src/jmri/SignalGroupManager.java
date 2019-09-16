package jmri;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Interface for obtaining information about signal groups.
 * <p>
 * Each NamedBean here represents a single signal group. The actual objects are
 * SignalGroupTable objects; that's a current anachronism.
 * <p>
 * See the common implementation for information on how loaded, etc.
 * <hr>
 * This file is part of JMRI.
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2018
 */
public interface SignalGroupManager extends Manager<SignalGroup> {

    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User Name or System Name to match
     * @return null if no match found
     */
    @CheckForNull
    public SignalGroup getSignalGroup(@Nonnull String name);

    @CheckForNull public SignalGroup getBySystemName(@Nonnull String name);

    @CheckForNull public SignalGroup getByUserName(@Nonnull String name);

    /**
     * Create a new Signal group if the group does not exist. Intended for use with
     * User GUI, to allow the auto generation of systemNames, where the user can
     * optionally supply a username.
     *
     * @param userName User name for the new group
     * @return null if a Group with the same userName already exists or if there
     *         is trouble creating a new Group
     */
    @Nonnull
    public SignalGroup newSignaGroupWithUserName(@Nonnull String userName);
    
    /**
     * Create a new Signal group if the group does not exist. Intended for use with
     * User GUI, to allow the auto generation of systemNames, where the user can
     * optionally supply a username.
     *
     * @param userName User name for the new group
     * @return null if a Group with the same userName already exists or if there
     *         is trouble creating a new Group
     * @deprecated 4.15.2 use newSignaGroupWithUserName
     */
    @Nonnull
    @Deprecated //  4.15.2 use newSignaGroupWithUserName
    public SignalGroup newSignalGroup(@Nonnull String userName);

    /**
     * Create a new SignalGroup if the group does not exist.
     *
     * @param systemName the system name for the group
     * @param userName   the user name for the group
     * @return null if a Signal Group with the same systemName or userName already
     *         exists or if there is trouble creating a new Group
     */
    @Nonnull
    public SignalGroup provideSignalGroup(@Nonnull String systemName, String userName);

    /**
     * Delete Group by removing it from the manager. The Group must first be
     * deactivated so it stops processing.
     *
     * @param s the group to remove
     */
    void deleteSignalGroup(@Nonnull SignalGroup s);

}
