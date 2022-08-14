package jmri;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Locate a Reporter object representing some specific device on the layout.
 * <p>
 * Reporter objects are obtained from a ReporterManager, which in turn is
 * generally located from the InstanceManager. A typical call sequence might be:
 * <pre>
 * Reporter device = InstanceManager.getDefault(jmri.ReporterManager.class).newReporter("23",null);
 * </pre>
 * <p>
 * Each Reporter has a two names. The "user" name is entirely free form, and can
 * be used for any purpose. The "system" name is provided by the system-specific
 * implementations, and provides a unique mapping to the layout control system
 * (for example LocoNet or NCE) and address within that system.
 * <p>
 * Much of the book-keeping is implemented in the AbstractReporterManager class,
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
 * @see jmri.Reporter
 * @see jmri.InstanceManager
 */
public interface ReporterManager extends ProvidingManager<Reporter>, NameIncrementingManager {

    /**
     * Locate via user name, then system name if needed. If that fails, create a
     * new Reporter. If the name is a valid system name, it will be used for the
     * new Reporter. Otherwise, the makeSystemName method will attempt to turn
     * it into a valid system name.
     * <p>This provides the same function as {@link ProvidingManager#provide}
     * which has a more generic form.
     *
     * @param name User name, system name, or address which can be promoted to
     *             system name
     * @return Never null
     * @throws IllegalArgumentException if Reporter doesn't already exist and
     *                                  the manager cannot create the Reporter
     *                                  due to an illegal name or name that
     *                                  can't be parsed.
     */
    @Nonnull
    public Reporter provideReporter(@Nonnull String name) throws IllegalArgumentException;

    /** {@inheritDoc} */
    @Override
    @Nonnull
    default public Reporter provide(@Nonnull String name) throws IllegalArgumentException { return provideReporter(name); }

    /**
     * Locate via user name, then system name if needed. If that fails, return
     * null
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    @CheckForNull
    public Reporter getReporter(@Nonnull String name);

    /**
     * Locate an instance based on a system name. Returns null if no instance
     * already exists.
     *
     * @param systemName the system name to locate
     * @return requested Reporter object or null if none exists
     */
    @CheckForNull
    @Override
    public Reporter getBySystemName(@Nonnull String systemName);

    /**
     * Locate an instance based on a user name. Returns null if no instance
     * already exists.
     *
     * @param userName the user name to locate
     * @return requested Reporter object or null if none exists
     */
    @CheckForNull
    @Override
    public Reporter getByUserName(@Nonnull String userName);

    /**
     * Locate an instance based on a user name, or if that fails, by system
     * name. Returns null if no instance already exists.
     *
     * @param userName the name to locate
     * @return requested Reporter object or null if none exists
     */
    @CheckForNull
    public Reporter getByDisplayName(@Nonnull String userName);

    /**
     * Return an instance with the specified system and user names.
     * <p>
     * Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Reporter object representing a given physical Reporter and
     * therefore only one with a specific system or user name.
     * <p>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     * <ul>
     * <li>If a null reference is given for user name, no user name will be
     * associated with the Reporter object created; a valid system name must be
     * provided
     * <li>If both names are provided, the system name defines the hardware
     * access of the desired Reporter, and the user address is associated with
     * it. The system name must be valid.
     * </ul>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * Reporters when you should be looking them up.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return requested Reporter object (never null)
     * @throws IllegalArgumentException if cannot create the Reporter due to
     *                                  an illegal name or name that can't
     *                                  be parsed.
     */
    @Nonnull
    public Reporter newReporter(@Nonnull String systemName, String userName) throws IllegalArgumentException;

    /**
     * Get a system name for a given hardware address and system prefix.
     *
     * @param curAddress desired hardware address
     * @param prefix     system prefix used in system name, excluding Bean type-letter..
     * @return the complete Reporter system name for the prefix and current
     *         address
     * @throws jmri.JmriException if unable to create a system name for the
     *                            given address, possibly due to invalid address
     *                            format
     */
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip();

}
