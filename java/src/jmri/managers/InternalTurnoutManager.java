package jmri.managers;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;

/**
 * Implement a turnout manager for "Internal" (virtual) turnouts.
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public class InternalTurnoutManager extends AbstractTurnoutManager {

    /**
     * Create and return an internal (no layout connection) turnout
     */
    protected Turnout createNewTurnout(String systemName, String userName) {
        return new AbstractTurnout(systemName, userName) {

            protected void forwardCommandChangeToLayout(int s) {
            }

            protected void turnoutPushbuttonLockout(boolean b) {
            }
        };
    }

    protected String prefix = "I";

    public String getSystemPrefix() {
        return prefix;
    }

    public String createSystemName(String curAddress, String prefix) throws jmri.JmriException {
        return prefix + typeLetter() + curAddress;
    }
    /*
     * Turnout operation support. Internal turnouts don't need retries.
     */

    public String[] getValidOperationTypes() {
        return new String[]{"NoFeedback"};
    }
}
