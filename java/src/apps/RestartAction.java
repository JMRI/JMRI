package apps;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple AbstractAction class that can be invoked to restart JMRI
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
 * @author Matthew Harris Copyright (C) 2011
 */
public class RestartAction extends JmriAbstractAction {

    public RestartAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public RestartAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public RestartAction() {
        super(Bundle.getMessage("RestartAction")); // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        log.debug("Source: {}; class: {}", e.getSource().toString(), e.getSource().getClass().getName());

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

    private static final Logger log = LoggerFactory.getLogger(RestartAction.class);

}
