package jmri;

import java.util.List;

/**
 * Locate a Reporter object representing some specific device on the layout.
 * <P>
 * Reporter objects are obtained from a ReporterManager, which in turn is
 * generally located from the InstanceManager. A typical call sequence might be:
 * <PRE>
 * Reporter device = InstanceManager.reporterManagerInstance().newReporter(null,"23");
 * </PRE>
 * <P>
 * Each Reporter has a two names. The "user" name is entirely free form, and can
 * be used for any purpose. The "system" name is provided by the system-specific
 * implementations, and provides a unique mapping to the layout control system
 * (e.g. LocoNet, NCE, etc) and address within that system.
 * <P>
 * Much of the book-keeping is implemented in the AbstractReporterManager class,
 * which can form the basis for a system-specific implementation.
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
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 * @see jmri.Reporter
 * @see jmri.InstanceManager
 */
public interface ReporterManager extends Manager {

    /**
     * Locate via user name, then system name if needed. If that fails, create a
     * new Reporter. If the name is a valid system name, it will be used for the
     * new Reporter. Otherwise, the makeSystemName method will attempt to turn
     * it into a valid system name.
     *
     * @param name User name, system name, or address which can be promoted to
     *             system name
     * @return Never null
     * @throws IllegalArgumentException if Reporter doesn't already exist and
     *                                  the manager cannot create the Reporter
     *                                  due to e.g. an illegal name or name that
     *                                  can't be parsed.
     */
    public Reporter provideReporter(String name);

    /**
     * Locate via user name, then system name if needed. If that fails, return
     * null
     *
     * @param name
     * @return null if no match found
     */
    public Reporter getReporter(String name);

    /**
     * Locate an instance based on a system name. Returns null if no instance
     * already exists.
     *
     * @return requested Reporter object or null if none exists
     */
    public Reporter getBySystemName(String systemName);

    /**
     * Locate an instance based on a user name. Returns null if no instance
     * already exists.
     *
     * @return requested Reporter object or null if none exists
     */
    public Reporter getByUserName(String userName);

    /**
     * Locate an instance based on a user name, or if that fails, by system
     * name. Returns null if no instance already exists.
     *
     * @return requested Reporter object or null if none exists
     */
    public Reporter getByDisplayName(String userName);

    /**
     * Return an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Reporter object representing a given physical Reporter and
     * therefore only one with a specific system or user name.
     * <P>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     * <UL>
     * <LI>If a null reference is given for user name, no user name will be
     * associated with the Reporter object created; a valid system name must be
     * provided
     * <LI>If both names are provided, the system name defines the hardware
     * access of the desired Reporter, and the user address is associated with
     * it. The system name must be valid.
     * </UL>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * Reporters when you should be looking them up.
     *
     * @return requested Reporter object (never null)
     * @throws IllegalArgumentException if cannot create the Reporter due to
     *                                  e.g. an illegal name or name that can't
     *                                  be parsed.
     */
    public Reporter newReporter(String systemName, String userName);

    /**
     * Get a list of all Reporter's system names.
     */
    public List<String> getSystemNameList();

    /**
     * A method that determines if it is possible to add a range of turnouts in
     * numerical order eg 10 to 30 will return true. where as if the address
     * format is 1b23 this will return false.
     *
     */
    public boolean allowMultipleAdditions(String systemName);

    /**
     * Determine if the address supplied is valid and free, if not then it shall
     * return the next free valid address up to a maximum of 10 address away
     * from the initial address.
     *
     * @param prefix     - The System Prefix used to make up the systemName
     * @param curAddress - The hardware address of the turnout we which to
     *                   check.
     */
    public String getNextValidAddress(String curAddress, String prefix);

}
