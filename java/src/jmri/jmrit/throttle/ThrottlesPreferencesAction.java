package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.InstanceManager;

/**
 * Action to open the JMRI throttles UI preferences window
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Lionel Jeanson
 * 
 */

public class ThrottlesPreferencesAction extends AbstractAction {

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public ThrottlesPreferencesAction(String s) {
        super(s);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
            setEnabled(false);
        }
    }

    public ThrottlesPreferencesAction() {
        this("Throttles preferences");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InstanceManager.getDefault(ThrottleFrameManager.class).showThrottlesPreferences();
    }
}
