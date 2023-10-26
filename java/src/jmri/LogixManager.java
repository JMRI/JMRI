package jmri;

/**
 * Interface for obtaining Logixs
 * <p>
 * This doesn't have a "new" method, since Logixs are separately implemented,
 * instead of being system-specific.
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
 * @author Dave Duchamp Copyright (C) 2007
 */
public interface LogixManager extends Manager<Logix> {

    /**
     * Create a new Logix if the Logix does not exist.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return a new Logix or null if unable to create: An error, or the Logix already exists
     */
    Logix createNewLogix(String systemName, String userName);

    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username.
     *
     * @param userName the user name
     * @return a new Logix or null if unable to create
     */
    Logix createNewLogix(String userName);

    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    Logix getLogix(String name);

    @Override
    Logix getByUserName(String s);

    @Override
    Logix getBySystemName(String s);

    /**
     * Activate all Logixs that are not currently active This method is called
     * after a configuration file is loaded.
     */
    void activateAllLogixs();

    /**
     * Delete Logix by removing it from the manager. The Logix must first be
     * deactivated so it stops processing.
     *
     * @param x the Logix to delete
     */
    void deleteLogix(Logix x);

    /**
     * Support for loading Logixs in a disabled state to debug loops
     * 
     * @param s true if Logix should be loadable while disabled
     */
    void setLoadDisabled(boolean s);

}
