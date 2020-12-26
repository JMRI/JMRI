package jmri.jmrix.internal;

import javax.annotation.Nonnull;

/**
 * Implement a AnalogIOManager for "Internal" (virtual) AnalogIOs.
 *
 * @author Bob Jacobsen      Copyright (C) 2009
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class InternalAnalogIOManager extends jmri.managers.AbstractAnalogIOManager {

    public InternalAnalogIOManager(InternalSystemConnectionMemo memo) {
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
