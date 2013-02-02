// RestartAction.java
package apps;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;

/**
 * Simple AbstractAction class that can be invoked to restart JMRI
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <P>
 *
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 */
public class RestartAction extends JmriAbstractAction {
    
    public RestartAction(String s, WindowInterface wi) {
        super(s, wi);
    }
    
    public RestartAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }
    
    public RestartAction() {
        super(ResourceBundle.getBundle("apps.ActionListBundle").getString("apps.RestartAction"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (log.isDebugEnabled()) log.debug("Source: " + e.getSource().toString() + "; class: " + e.getSource().getClass().getName());
        
        // Don't actually do this if launched as a start-up action
        // as we'll be in an endless loop
        if (!e.getSource().toString().equals("prefs")) {
            Apps.handleRestart();
        } else {
            log.warn("RestartAction called in error - this should not be done...");
        }
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    
    private static final Logger log = Logger.getLogger(RestartAction.class.getName());
    
}
