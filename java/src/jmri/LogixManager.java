package jmri;

import java.util.List;

/**
 * Interface for obtaining Logixs
 * <P>
 * This doesn't have a "new" method, since Logixs are separately implemented,
 * instead of being system-specific.
 *
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
 * @author Dave Duchamp Copyright (C) 2007
 */
public interface LogixManager extends Manager {

    // to free resources when no longer used
    public void dispose();

    /**
     * Method to create a new Logix if the Logix does not exist Returns null if
     * a Logix with the same systemName or userName already exists, or if there
     * is trouble creating a new Logix.
     */
    public Logix createNewLogix(String systemName, String userName);

    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username. Returns null if a Logix with
     * the same userName already exists, or if there is trouble creating a new
     * Logix.
     */
    public Logix createNewLogix(String userName);

    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name
     * @return null if no match found
     */
    public Logix getLogix(String name);

    public Logix getByUserName(String s);

    public Logix getBySystemName(String s);

    /**
     * Activate all Logixs that are not currently active This method is called
     * after a configuration file is loaded.
     */
    public void activateAllLogixs();

    /**
     * Get a list of all Logix system names.
     */
    public List<String> getSystemNameList();

    /**
     * Delete Logix by removing it from the manager. The Logix must first be
     * deactivated so it stops processing.
     */
    void deleteLogix(Logix x);

    /**
     * Support for loading Logixs in a disabled state to debug loops
     */
    public void setLoadDisabled(boolean s);

}
