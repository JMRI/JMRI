package jmri.jmrix.internal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Implementation of the InternalSensorManager interface.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2006
  */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "name assigned historically")
public class InternalSensorManager extends jmri.managers.InternalSensorManager {

    public InternalSensorManager(String prefix) {
        super();
        this.prefix = prefix;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * Provide a manager-specific tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddInputEntryToolTip");
        return entryToolTip;
    }

}


