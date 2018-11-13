package jmri;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Interface for obtaining signal heads.
 * <P>
 * This doesn't have a "new" method, as SignalHeads are separately implemented,
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
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface SignalHeadManager extends Manager<SignalHead> {

    /** {@inheritDoc} */
    @Override
    public void dispose();

    /** {@inheritDoc} */
    @CheckReturnValue
    @CheckForNull public SignalHead getSignalHead(@Nonnull String name);

    /** {@inheritDoc} */
    @CheckReturnValue
    @CheckForNull public SignalHead getByUserName(@Nonnull String s);

    /** {@inheritDoc} */
    @CheckReturnValue
    @CheckForNull public SignalHead getBySystemName(@Nonnull String s);

}
