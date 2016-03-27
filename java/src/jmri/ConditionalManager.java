package jmri;

import java.util.List;

/**
 * Interface for obtaining Conditionals
 * <P>
 * This doesn't have a "new" method, since Conditionals are separately
 * implemented, instead of being system-specific.
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
 * @version	$Revision$
 */
public interface ConditionalManager extends Manager {

    // to free resources when no longer used
    public void dispose();

    /**
     * Method to create a new Conditional if the Conditional does not exist
     * Returns null if a Conditional with the same systemName or userName
     * already exists, or if there is trouble creating a new Conditional If the
     * parent Logix cannot be found, the userName cannot be checked, but the
     * Conditional is still created. The scenario can happen when a Logix is
     * loaded from a file after its Conditionals.
     */
    public Conditional createNewConditional(String systemName, String userName);

    /**
     * Parses the Conditional system name to get the parent Logix system name,
     * then gets the parent Logix, and returns it.
     *
     * @param name - system name of Conditional (must be trimmed and upper case)
     */
    public Logix getParentLogix(String name);

    /**
     * Method to get an existing Conditional. First looks up assuming that name
     * is a User Name. Note: the parent Logix must be passed in x for user name
     * lookup. If this fails, or if x == null, looks up assuming that name is a
     * System Name. If both fail, returns null.
     *
     * @param x    - parent Logix (may be null)
     * @param name - name to look up
     * @return null if no match found
     */
    public Conditional getConditional(Logix x, String name);

    public Conditional getConditional(String name);

    public Conditional getByUserName(String s);

    public Conditional getByUserName(Logix x, String s);

    public Conditional getBySystemName(String s);

    /**
     * Get a list of all Conditional system names with the specified Logix
     * parent
     */
    public List<String> getSystemNameListForLogix(Logix x);

    /**
     * Delete Conditional by removing it from the manager. The parent Logix must
     * first be deactivated so it stops processing.
     */
    void deleteConditional(Conditional c);
}
