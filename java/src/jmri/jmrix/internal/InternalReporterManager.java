package jmri.jmrix.internal;

import javax.annotation.Nonnull;
import jmri.Reporter;

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
     * {@inheritDoc}
     *
     * @return an internal Reporter of class TrackReporter object with the given name
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
