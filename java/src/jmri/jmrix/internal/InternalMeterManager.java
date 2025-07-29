package jmri.jmrix.internal;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.implementation.DefaultAverageMeter;

/**
 * Implement a MeterManager for "Internal" (virtual) Meters.
 *
 * @author Bob Jacobsen      Copyright (C) 2009
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class InternalMeterManager extends jmri.managers.AbstractMeterManager
        implements HasAverageMeter {

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

    @Override
    public AverageMeter newAverageMeter(String sysName, String userName, Meter m) {
        AverageMeter am = new DefaultAverageMeter(sysName, userName);
        am.setMeter(m);
        return am;
    }

}
