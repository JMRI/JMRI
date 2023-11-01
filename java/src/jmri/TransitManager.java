package jmri;

import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Implementation of a Transit Manager
 * <p>
 * This doesn't need an interface, since Transits are globaly implemented,
 * instead of being system-specific.
 * <p>
 * Note that Transit system names must begin with system prefix and type character,
 * usually IZ, and be followed by a string, usually, but not always, a number. This
 * is enforced when a Transit is created.
 * <br>
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
 * @author Dave Duchamp Copyright (C) 2008, 2011
 */
public interface TransitManager extends Manager<Transit> {

    /**
     * Create a new Transit if the Transit does not exist.
     * This is NOT a provide method.
     *
     * @param systemName the desired system name
     * @param userName   the desired user name
     * @return a new Transit
     * @throws NamedBean.BadNameException if a Transit with the same systemName or
     *         userName already exists, or if there is trouble creating a new
     *         Transit.
     */
    @Nonnull
    Transit createNewTransit(@CheckForNull String systemName, String userName) throws NamedBean.BadNameException;

    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username.
     * <p>
     * Note: Since system names should be kept short for use in Dispatcher,
     * automatically generated system names are in the form {@code IZnn}, where
     * {@code nn} is the first available number.
     *
     * @param userName the desired user name
     * @return a new Transit
     * @throws NamedBean.BadNameException if userName is already associated with
     *         another Transit
     */
    @Nonnull
    Transit createNewTransit(String userName) throws NamedBean.BadNameException;

    /**
     * Get an existing Transit.
     * First looks up assuming that name is a User
     * Name. If this fails looks up assuming that name is a System Name.
     * If both fail, returns null.
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    @CheckForNull
    Transit getTransit(String name);

    /**
     * Remove an existing Transit.
     *
     * @param z the transit to remove
     */
    void deleteTransit(Transit z);

    /**
     * Get a list of Transits which use a specified Section.
     *
     * @param s the section to check Transits against
     * @return a list, possibly empty, of Transits using section s.
     */
    @Nonnull
    ArrayList<Transit> getListUsingSection(Section s);

    @Nonnull
    ArrayList<Transit> getListUsingBlock(Block b);

    @Nonnull
    ArrayList<Transit> getListEntryBlock(Block b);

}
