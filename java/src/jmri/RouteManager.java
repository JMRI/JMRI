package jmri;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Interface for obtaining Routes
 * <P>
 * This doesn't have a "new" method, since Routes are separately implemented,
 * instead of being system-specific.
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
 * @author Dave Duchamp Copyright (C) 2004
 */
public interface RouteManager extends Manager {

    // to free resources when no longer used
    public void dispose();

    /**
     * Method to create a new Route if the route does not exist Returns null if
     * a Route with the same systemName or userName already exists, or if there
     * is trouble creating a new Route.
     */
    public @Nonnull Route provideRoute(@Nonnull String systemName, String userName);

    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username. Method to create a new Route
     * if the route does not exist Returns null if a Route with the same
     * userName already exists, or if there is trouble creating a new Route.
     */
    public @Nonnull Route newRoute(@Nonnull String userName);

    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    public @CheckForNull Route getRoute(@Nonnull String name);

    public @CheckForNull Route getByUserName(@Nonnull String s);

    public @CheckForNull Route getBySystemName(@Nonnull String s);

    /**
     * Get a list of all Route system names.
     */
    public @Nonnull List<String> getSystemNameList();

    /**
     * Delete Route by removing it from the manager. The Route must first be
     * deactivated so it stops processing.
     */
    void deleteRoute(@Nonnull Route r);
}
