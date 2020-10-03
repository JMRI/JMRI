package jmri.jmrix.internal;

import javax.annotation.Nonnull;

/**
 * Implement a MeterManager for "Internal" (virtual) Meters.
 *
 * @author Bob Jacobsen      Copyright (C) 2009
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class InternalMeterManager extends jmri.managers.AbstractMeterManager {

    public InternalMeterManager(InternalSystemConnectionMemo memo) {
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
