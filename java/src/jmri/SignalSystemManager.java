package jmri;

import javax.annotation.CheckForNull;

/**
 * Interface for obtaining information about signal systems.
 * <p>
 * Each NamedBean here represents a single signal system. The actual objects are
 * SignalAspectTable objects; that's a current anachronism, soon to be fixed.
 * <p>
 * See the common implementation for information on how loaded, etc.
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
 * @author Bob Jacobsen Copyright (C) 2009
 */
public interface SignalSystemManager extends Manager<SignalSystem> {

    /**
     * Get SignalSystem by Name.
     * @param name to search for.
     * @return SignalSystem or null if no system found.
     */
    @CheckForNull
    public SignalSystem getSystem(String name);

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public SignalSystem getBySystemName(String name);

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public SignalSystem getByUserName(String name);
}
