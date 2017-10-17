package jmri.managers;

import jmri.Reporter;
import jmri.implementation.AbstractReporter;

/**
 * Implementation of the InternalReporterManager interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @since 2.9.4
 * @deprecated As of 4.3.5, use jmri.jmrix.internal classes
 */
@Deprecated
public class InternalReporterManager extends AbstractReporterManager {

    /**
     * Create an internal (dummy) reporter object
     *
     * @return new null
     */
    @Override
    protected Reporter createNewReporter(String systemName, String userName) {
        return new AbstractReporter(systemName, userName) {

            @Override
            public int getState() {
                return state;
            }

            @Override
            public void setState(int s) {
                state = s;
            }
            int state = 0;
        };
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return NameValidity.VALID;
    }

    @Override
    public String getSystemPrefix() {
        return "I";
    }

}
