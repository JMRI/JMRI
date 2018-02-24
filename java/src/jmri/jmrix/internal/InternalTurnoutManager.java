package jmri.jmrix.internal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Implement a turnout manager for "Internal" (virtual) turnouts.
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "name assigned historically")
public class InternalTurnoutManager extends jmri.managers.InternalTurnoutManager {

    public InternalTurnoutManager(String prefix) {
        super();
        this.prefix = prefix;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
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

}
