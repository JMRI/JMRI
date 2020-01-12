package jmri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Interface for obtaining Conditionals
 * <p>
 * This doesn't have a "new" method, since Conditionals are separately
 * implemented, instead of being system-specific.
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
 * @author Dave Duchamp Copyright (C) 2007
 */
public interface ConditionalManager extends Manager<Conditional> {

    // to free resources when no longer used
    @Override
    public void dispose();

    /**
     * Method to create a new Conditional if the Conditional does not exist
     * Returns null if a Conditional with the same systemName or userName
     * already exists, or if there is trouble creating a new Conditional If the
     * parent Logix cannot be found, the userName cannot be checked, but the
     * Conditional is still created. The scenario can happen when a Logix is
     * loaded from a file after its Conditionals.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return the new conditional or null if a Conditional already exists with
     *         either name
     */
    public Conditional createNewConditional(String systemName, String userName);

    /**
     * Parses the Conditional system name to get the parent Logix system name,
     * then gets the parent Logix, and returns it.
     *
     * @param name system name of Conditional
     * @return the logix for the conditional
     */
    public Logix getParentLogix(String name);

    /**
     * Method to get an existing Conditional. First looks up assuming that name
     * is a User Name. Note: the parent Logix must be passed in x for user name
     * lookup. If this fails, or if x == null, looks up assuming that name is a
     * System Name. If both fail, returns null.
     *
     * @param x   parent Logix (may be null)
     * @param name name to look up
     * @return null if no match found
     */
    public Conditional getConditional(Logix x, String name);

    public Conditional getConditional(String name);

    public Conditional getByUserName(String s);

    public Conditional getByUserName(Logix x, String s);

    public Conditional getBySystemName(String s);

    /**
     * Get a list of all Conditional system names with the specified Logix
     * parent.
     *
     * @param x the logix
     * @return a list of Conditional system names
     */
    public List<String> getSystemNameListForLogix(Logix x);

    /**
     * Delete Conditional by removing it from the manager. The parent Logix must
     * first be deactivated so it stops processing.
     *
     * @param c the conditional to remove
     */
    void deleteConditional(Conditional c);

    /**
     * Return a copy of the entire map.  Used by
     * {@link jmri.jmrit.beantable.LogixTableAction#buildWhereUsedListing}
     * @return a copy of the map
     * @since 4.7.4
     */
    public HashMap<String, ArrayList<String>> getWhereUsedMap();

    /**
     * Add a conditional reference to the array indicated by the target system name.
     * @since 4.7.4
     * @param target The system name for the target conditional
     * @param reference The system name of the conditional that contains the conditional reference
     */
    public void addWhereUsed(String target, String reference);

    /**
     * Get a list of conditional references for the indicated conditional
     * @since 4.7.4
     * @param target The target conditional for a conditional reference
     * @return an ArrayList or null if none
     */
    public ArrayList<String> getWhereUsed(String target);

    /**
     * Remove a conditional reference from the array indicated by the target system name.
     * @since 4.7.4
     * @param target The system name for the target conditional
     * @param reference The system name of the conditional that contains the conditional reference
     */
    public void removeWhereUsed(String target, String reference);

    /**
     * Display the complete structure, used for debugging purposes.
     * @since 4.7.4
     */
    public void displayWhereUsed();

    /**
     * Get the target system names used by this conditional
     * @since 4.7.4
     * @param reference The system name of the conditional the refers to other conditionals.
     * @return a list of the target conditionals
     */
    public ArrayList<String> getTargetList(String reference);

}
