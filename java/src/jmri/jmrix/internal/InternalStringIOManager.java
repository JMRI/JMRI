package jmri.jmrix.internal;

import javax.annotation.Nonnull;

/**
 * Implement a StringIOManager for "Internal" (virtual) StringIOs.
 *
 * @author Bob Jacobsen      Copyright (C) 2009
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class InternalStringIOManager extends jmri.managers.AbstractStringIOManager {

    public InternalStringIOManager(InternalSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public InternalSystemConnectionMemo getMemo() {
        return (InternalSystemConnectionMemo) memo;
    }

}
