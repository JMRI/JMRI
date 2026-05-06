package jmri.jmrit.throttle.implementation;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * A small class extending JInternalFrame. 
 * Used to host the various throttle panels in a throttle frame
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
 * @author Lionel Jeanson Copyright 2026
 *
 */

public class ThrottleJInternalFrameSubControl extends JInternalFrame  {

    public ThrottleJInternalFrameSubControl(String title, JPanel content, boolean visible) {
        super(title, true, true, true, true);           
        setContentPane(content);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }
    
}
