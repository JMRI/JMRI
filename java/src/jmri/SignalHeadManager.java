package jmri;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Interface for obtaining signal heads.
 * <p>
 * This doesn't have a "new" method, as SignalHeads are separately implemented,
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
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface SignalHeadManager extends Manager<SignalHead> {

    /** {@inheritDoc} */
    @Override
    public void dispose();

    /**
     * Get an existing SignalHead or return null if it doesn't exist. 
     * 
     * Locates via user name, then system name if needed.
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    @CheckReturnValue
    @CheckForNull public SignalHead getSignalHead(@Nonnull String name);

    /**
     * Get an existing SignalHead or return null if it doesn't exist. 
     * 
     * Locates via user name.
     *
     * @param name User name o to match
     * @return null if no match found
     */
    @CheckReturnValue
    @CheckForNull public SignalHead getByUserName(@Nonnull String name);

    /**
     * Get an existing SignalHead or return null if it doesn't exist. 
     * 
     * Locates via system name.
     *
     * @param name System name to match
     * @return null if no match found
     */
    @CheckReturnValue
    @CheckForNull public SignalHead getBySystemName(@Nonnull String name);

}
