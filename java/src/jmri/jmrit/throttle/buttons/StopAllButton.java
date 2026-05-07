package jmri.jmrit.throttle.buttons;

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.throttle.ThrottleFrameManager;

/**
 * 
 * A button to send an estop to all JMRI UI managed throttles
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
 */

public class StopAllButton extends JButton {

    public StopAllButton() {
        super();
        initGUI();
    }
        
    private void initGUI() {    
        //    stop.setText(Bundle.getMessage("ThrottleToolBarStopAll"));
        setIcon(new NamedIcon("resources/icons/throttles/estop.png", "resources/icons/throttles/estop.png"));
        setToolTipText(Bundle.getMessage("ThrottleToolBarStopAllToolTip"));
        setVerticalTextPosition(JButton.BOTTOM);
        setHorizontalTextPosition(JButton.CENTER);
        addActionListener((ActionEvent e) -> {
            InstanceManager.getDefault(ThrottleFrameManager.class).emergencyStopAll();
        });
    }
}
