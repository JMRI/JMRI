package jmri.jmrix.internal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import jmri.implementation.AbstractTurnout;

/**
 * Implement a turnout manager for "Internal" (virtual) turnouts.
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class InternalTurnoutManager extends AbstractTurnoutManager {

    public InternalTurnoutManager(String prefix) {
        super();
        this.prefix = prefix;
    }

    protected String prefix = "I";

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    /**
     * Create and return an internal (no layout connection) turnout
     */
    @Override
    protected Turnout createNewTurnout(String systemName, String userName) {
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
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws jmri.JmriException {
        return prefix + typeLetter() + curAddress;
    }

    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return NameValidity.VALID;
    }

    /**
     * Provide a manager-specific tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddOutputEntryToolTip");
        return entryToolTip;
    }

    /**
     * Turnout operation support. Internal turnouts don't need retries.
     */
    @Override
    public String[] getValidOperationTypes() {
        return new String[]{"NoFeedback"};
    }
}
