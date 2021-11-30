package jmri.jmrix.internal;

import javax.annotation.Nonnull;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import jmri.util.PreferNumericComparator;
import jmri.implementation.AbstractTurnout;

/**
 * Implement a turnout manager for "Internal" (virtual) turnouts.
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class InternalTurnoutManager extends AbstractTurnoutManager {

    public InternalTurnoutManager(InternalSystemConnectionMemo memo) {
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

    /**
     * Create and return an internal (no layout connection) Turnout.
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        return new AbstractTurnout(systemName, userName) {

            @Override
            protected void forwardCommandChangeToLayout(int s) {
                // nothing to do
            }

            @Override
            protected void turnoutPushbuttonLockout(boolean b) {
                // nothing to do
            }

            @Override
            public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, NamedBean n) {
                return (new PreferNumericComparator()).compare(suffix1, suffix2);
            }
            
            @Override
            public boolean isCanFollow() {
                return true;
            }
        };
    }

    /**
     * Multiple additions enabled for Internal Turnouts.
     * {@inheritDoc}
     */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    /**
     * No validation for Internal Turnouts.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws jmri.JmriException {
        return prefix + typeLetter() + curAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    /**
     * Turnout operation support. Internal turnouts don't need retries.
     */
    @Override
    @Nonnull
    public String[] getValidOperationTypes() {
        return new String[]{"NoFeedback"};
    }

}
