package jmri;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Interface for obtaining VariableLights.
 * <p>
 * This doesn't have a "new" method, as all VariableLights is also located in
 * LightManager. Use LightManager to add or create new VariableLights.
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
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public interface VariableLightManager extends Manager<VariableLight> {

    /** {@inheritDoc} */
    @Override
    public void dispose();

    /**
     * Locate a VariableLight by its user name.
     *
     * @param s the user name
     * @return the light or null if not found
     */
    @CheckReturnValue
    @CheckForNull
    @Override
    public VariableLight getByUserName(@Nonnull String s);

    /**
     * Locate a VariableLight by its system name.
     *
     * @param s the system name
     * @return the light or null if not found
     */
    @CheckReturnValue
    @CheckForNull
    @Override
    public VariableLight getBySystemName(@Nonnull String s);

}
