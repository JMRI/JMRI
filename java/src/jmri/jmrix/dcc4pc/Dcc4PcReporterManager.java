package jmri.jmrix.dcc4pc;

import jmri.Reporter;

import javax.annotation.Nonnull;

/**
 * Dcc4PcReporterManager implements the ReporterManage for dcc4pc
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class Dcc4PcReporterManager extends jmri.managers.AbstractReporterManager {

    // ctor has to register for LocoNet events
    public Dcc4PcReporterManager(Dcc4PcTrafficController tc, Dcc4PcSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Dcc4PcSystemConnectionMemo getMemo() {
        return (Dcc4PcSystemConnectionMemo) memo;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public Reporter createNewReporter(@Nonnull String systemName, String userName) {
        Reporter r = new Dcc4PcReporter(systemName, userName);
        register(r);
        return r;
    }
}
