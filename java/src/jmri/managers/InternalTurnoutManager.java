package jmri.managers;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;

/**
 * Implement a turnout manager for "Internal" (virtual) turnouts.
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 * @deprecated As of 4.3.5, use jmri.jmrix.internal classes
 */
@Deprecated
public class InternalTurnoutManager extends AbstractTurnoutManager {

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

    protected String prefix = "I";

    @Override
    public String getSystemPrefix() {
        return prefix;
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
     * Turnout operation support. Internal turnouts don't need retries.
     */
    @Override
    public String[] getValidOperationTypes() {
        return new String[]{"NoFeedback"};
    }

}
