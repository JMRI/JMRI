package jmri;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Locate an IdTag object representing a specific IdTag.
 * <p>
 * IdTag objects are obtained from an IdTagManager, which in turn is generally
 * located from the InstanceManager. A typical call sequence might be:
 * <pre>
 * IdTag tag = InstanceManager.idTagManagerInstance().newIdTag(null,"23");
 * </pre>
 * <p>
 * Each IdTag has a two names. The "user" name is entirely free form, and can be
 * used for any purpose. The "system" name is provided by the system-specific
 * implementations, and provides a unique mapping to the layout control system
 * (for example LocoNet or NCE) and address within that system.
 * <p>
 * Much of the book-keeping is implemented in the AbstractIdTagManager class,
 * which can form the basis for a system-specific implementation.
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
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Matthew Harris Copyright (C) 2011
 * @see jmri.IdTag
 * @see jmri.InstanceManager
 * @since 2.11.4
 */
public interface IdTagManager extends ProvidingManager<IdTag> {

    /**
     * Locate via tag ID, then user name, and finally system name if needed. If
     * that fails, create a new IdTag. If the name is a valid system name, it
     * will be used for the new IdTag. Otherwise, the makeSystemName method will
     * attempt to turn it into a valid system name.
     *
     * @param name Tag ID, user name, system name, or address which can be
     *             promoted to system name
     * @return A tag ID
     * @throws IllegalArgumentException if IdTag doesn't already exist and the
     *                                  manager cannot create the IdTag due to
     *                                  an illegal name or name that can't
     *                                  be parsed.
     */
    @Nonnull
    public IdTag provideIdTag(@Nonnull String name);

    /**
     * Locate via tag ID, then by user name, and finally system name if needed.
     * If that fails, return null
     *
     * @param name tag name being requested
     * @return null if no match found
     */
    @CheckReturnValue
    @CheckForNull
    public IdTag getIdTag(@Nonnull String name);

    /**
     * Locate an instance based on a system name. Returns null if no instance
     * already exists.
     *
     * @param systemName system name being requested
     * @return requested IdTag object or null if none exists
     */
    @CheckReturnValue
    @CheckForNull
    public IdTag getBySystemName(@Nonnull String systemName);

    /**
     * Locate an instance based on a user name. Returns null if no instance
     * already exists.
     *
     * @param userName user name being requested
     * @return requested IdTag object or null if none exists
     */
    @CheckReturnValue
    @CheckForNull
    public IdTag getByUserName(@Nonnull String userName);

    /**
     * Locate an instance based on a tag ID. Returns null if no instance already
     * exists.
     *
     * @param tagID tag ID being requested
     * @return requested IdTag object or null if none exists
     */
    @CheckReturnValue
    @CheckForNull
    public IdTag getByTagID(@Nonnull String tagID);

    /**
     * Return an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one IdTag object representing a given physical IdTag and therefore
     * only one with a specific system or user name.
     * <p>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     * <ul>
     * <li>If a null reference is given for user name, no user name will be
     * associated with the IdTag object created; a valid system name must be
     * provided
     * <li>If both are provided, the system name defines the hardware access of
     * the desired IdTag, and the user address is associated with it. The system
     * name must be valid.
     * </ul>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * RfidTags when you should be looking them up.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return requested IdTag object (never null)
     * @throws IllegalArgumentException if cannot create the IdTag due to e.g.
     *                                  an illegal name or name that can't be
     *                                  parsed.
     */
    @Nonnull
    public IdTag newIdTag(@Nonnull String systemName, @CheckForNull String userName);

    /**
     * Get a list of all IdTags seen by a specified Reporter within a specific
     * time threshold from the most recently seen.
     *
     * @param reporter  Reporter to return list for (can be null)
     * @param threshold Time threshold (in ms)
     * @return List of matching IdTags
     */
    @CheckReturnValue
    @Nonnull
    public List<IdTag> getTagsForReporter(@Nonnull Reporter reporter, long threshold);

    /**
     * Define if the manager should persist details of when and where all known
     * IdTags were seen.
     *
     * @param state True to store; False to omit
     */
    public void setStateStored(boolean state);

    /**
     * Determines if the state of known IdTags should be stored.
     *
     * @return True to store state; False to discard state
     */
    @CheckReturnValue
    public boolean isStateStored();

    /**
     * Define if the manager should use the fast clock when setting the times
     * when a given IdTag was last seen.
     *
     * @param fastClock True to use the fast clock; False to use the system
     *                  clock
     */
    public void setFastClockUsed(boolean fastClock);

    /**
     * Determines if fast clock times should be recorded for when a given IdTag
     * was last seen.
     *
     * @return True to use the fast clock; False to use the system clock
     */
    @CheckReturnValue
    public boolean isFastClockUsed();

    /**
     * Perform initialization.
     */
    public void init();

    /**
     * Determines if the manager has been initialized.
     *
     * @return state of initialization
     */
    @CheckReturnValue
    public boolean isInitialised();

}
