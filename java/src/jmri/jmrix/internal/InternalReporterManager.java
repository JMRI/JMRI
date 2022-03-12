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
    @Nonnull
    @Override
    protected Reporter createNewReporter(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        return new TrackReporter(systemName, userName);
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    /**
     * No validation for Internal Reporters.
     * {@inheritDoc}
     */
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws jmri.JmriException {
        return prefix + typeLetter() + curAddress;
    }

}
