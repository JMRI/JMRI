package jmri.jmrix.internal;

import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import jmri.implementation.AbstractTurnout;

import javax.annotation.Nonnull;

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
    @Nonnull
    @Override
    public InternalSystemConnectionMemo getMemo() {
        return (InternalSystemConnectionMemo) memo;
    }

    /**
     * Create and return an internal (no layout connection) turnout
     */
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) {
        return new AbstractTurnout(systemName, userName) {

            @Override
            protected void forwardCommandChangeToLayout(int s) {
            }

            @Override
            protected void turnoutPushbuttonLockout(boolean b) {
            }
        };
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    @Override
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
    @Nonnull
    @Override
    public String[] getValidOperationTypes() {
        return new String[]{"NoFeedback"};
    }

}
