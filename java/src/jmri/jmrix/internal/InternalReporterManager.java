package jmri.jmrix.internal;

import jmri.Reporter;

import javax.annotation.Nonnull;

/**
 * Implementation of the InternalReporterManager interface.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @since 2.9.4
 */
public class InternalReporterManager extends jmri.managers.AbstractReporterManager {

    public InternalReporterManager(InternalSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * Create an internal TrackReporter object
     *
     * @return new null
     */
    @Override
    protected Reporter createNewReporter(@Nonnull String systemName, String userName) {
        return new TrackReporter(systemName, userName);
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
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
