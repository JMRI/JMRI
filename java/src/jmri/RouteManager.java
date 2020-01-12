package jmri;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Interface for obtaining Routes.
 * <p>
 * This doesn't have a "new" method, since Routes are separately implemented,
 * instead of being system-specific.
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
 * @author Dave Duchamp Copyright (C) 2004
 */
public interface RouteManager extends ProvidingManager<Route> {

    // to free resources when no longer used
    @Override
    public void dispose();

    /**
     * Create a new Route if the route does not exist.
     *
     * @param systemName the system name for the route
     * @param userName   the user name for the route
     * @return null if a Route with the same systemName or userName already
     *         exists or if there is trouble creating a new Route
     */
    @Nonnull
    public Route provideRoute(@Nonnull String systemName, String userName);

    /**
     * Create a new Route if the route does not exist. Intended for use with
     * User GUI, to allow the auto generation of systemNames, where the user can
     * optionally supply a username.
     *
     * @param userName user name for the new route
     * @return null if a Route with the same userName already exists or if there
     *         is trouble creating a new Route
     */
    @Nonnull
    public Route newRoute(@Nonnull String userName);

    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    @CheckForNull
    public Route getRoute(@Nonnull String name);

    @CheckForNull
    public Route getByUserName(@Nonnull String s);

    @CheckForNull
    public Route getBySystemName(@Nonnull String s);

    /**
     * Delete Route by removing it from the manager. The Route must first be
     * deactivated so it stops processing.
     *
     * @param r the route to remove
     */
    void deleteRoute(@Nonnull Route r);

}
