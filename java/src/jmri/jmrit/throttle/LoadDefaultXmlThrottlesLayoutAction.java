package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.Icon;
import jmri.InstanceManager;
import jmri.jmrit.throttle.implementation.ThrottleUICore;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load Default Throttles Layout Action
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
 * @author Lionel Jeanson Copyright 2009
 */
public class LoadDefaultXmlThrottlesLayoutAction extends JmriAbstractAction {

    public LoadDefaultXmlThrottlesLayoutAction(String s, WindowInterface wi) {
        super(s, wi);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
            setEnabled(false);
        }
    }

    public LoadDefaultXmlThrottlesLayoutAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
            setEnabled(false);
        }
    }

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public LoadDefaultXmlThrottlesLayoutAction(String s) {
        super(s);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
            setEnabled(false);
        }
    }

    public LoadDefaultXmlThrottlesLayoutAction() {
        this("Load default throttle layout...");
    }

    /**
     * The action is performed. Create a new ThrottleFrame.
     *
     * @param e The event causing the action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // load throttle preference
        LoadXmlThrottlesLayoutAction lxta = new LoadXmlThrottlesLayoutAction();
        try {
            if (lxta.loadThrottlesLayout(new File(ThrottleUICore.getDefaultThrottleFilename()))) {
                return;
            }
        } catch (java.io.IOException ex) {
            log.error("No default throttle layout, creating an empty throttle window");
        }
        // need to create a new one
        ThrottleControllerUI tf = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame();
        tf.toFront();
    }

    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(LoadDefaultXmlThrottlesLayoutAction.class);

    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

}
