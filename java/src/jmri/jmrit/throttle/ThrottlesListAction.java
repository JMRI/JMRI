package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;

import jmri.InstanceManager;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriPanel;

/**
 * Action to open the throttles list window
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

public class ThrottlesListAction extends JmriAbstractAction {

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public ThrottlesListAction(String s) {
        super(s);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
            setEnabled(false);
        }
    }

    public ThrottlesListAction() {
        this("Throttles list");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InstanceManager.getDefault(ThrottleFrameManager.class).showThrottlesList();
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
