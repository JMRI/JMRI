package jmri.jmrix.internal;

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
    public InternalSystemConnectionMemo getMemo() {
        return (InternalSystemConnectionMemo) memo;
    }

    /**
     * Create and return an internal (no layout connection) turnout
     */
    @Override
    protected Turnout createNewTurnout(String systemName, String userName) {
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
            public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n) {
                return (new PreferNumericComparator()).compare(suffix1, suffix2);
            }
        };
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws jmri.JmriException {
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
    public String[] getValidOperationTypes() {
        return new String[]{"NoFeedback"};
    }

}
