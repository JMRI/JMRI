package jmri.jmrit.throttle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.ThrottleManager;
import jmri.jmrit.throttle.actions.ThrottleWindowActionsFactory;
import jmri.jmrit.throttle.actions.ThrottleWindowInputsListener;
import jmri.jmrit.throttle.implementation.SimpleThrottlePanel;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;
import jmri.jmrit.throttle.interfaces.ThrottleControllersUIContainer;
import jmri.jmrit.throttle.preferences.ThrottlesPreferences;
import jmri.util.JmriJFrame;

/**
 * A very simple throttle window.
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
 * @author Lionel Jeanson 2007-2026
 * 
 */

public class SimpleThrottleWindow extends JmriJFrame implements ThrottleControllersUIContainer, PropertyChangeListener {

    private final ThrottleFrameManager throttleFrameManager = InstanceManager.getDefault(ThrottleFrameManager.class);
    private final ThrottleWindowActionsFactory myActionFactory;
    private SimpleThrottlePanel throttleControllerUI;

    /**
     * Default constructor
     * 
     * @param la the loco address to set in the throttle
     */
    public SimpleThrottleWindow(DccLocoAddress la) {
        this((jmri.jmrix.ConnectionConfig) null, la);
    }

    public SimpleThrottleWindow(jmri.jmrix.ConnectionConfig connectionConfig, DccLocoAddress la) {
        super(Bundle.getMessage("ThrottleTitle"));

        ThrottleManager throttleManager;
        if (connectionConfig != null) {
            throttleManager = connectionConfig.getAdapter().getSystemConnectionMemo().get(jmri.ThrottleManager.class);
        } else {
            throttleManager = InstanceManager.getDefault(jmri.ThrottleManager.class);
        }
        myActionFactory = new ThrottleWindowActionsFactory(this);
        throttleControllerUI = new SimpleThrottlePanel(this, throttleManager, true, true, true);
        initGUI();
        InstanceManager.getDefault(ThrottlesPreferences.class).addPropertyChangeListener(this);
        if (la != null) {
            throttleControllerUI.setAddress(la);
        }        
    }

    private void initGUI() {
        setTitle(Bundle.getMessage("ThrottleTitle") );        
        add(throttleControllerUI);
        pack();

        // add keyboard controls
        ActionMap am = myActionFactory.buildActionMap();
        for (Object k : am.allKeys()) {
            getRootPane().getActionMap().put(k, am.get(k));
        }        
        ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);
        ComponentInputMap im = new ComponentInputMap(getRootPane());
        for (Object k : this.getRootPane().getActionMap().allKeys()) {
            KeyStroke[] kss = preferences.getThrottlesKeyboardControls().getKeyStrokes((String)k);
            if (kss !=null) {
                for (KeyStroke keystroke : kss) {
                    if (keystroke != null) {
                        im.put(keystroke, k);
                    }
                }
            }
        }
        getRootPane().setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW,im);
        getRootPane().setFocusable(true);
        getRootPane().requestFocusInWindow();
        // mouse wheel listener
        addMouseWheelListener( new ThrottleWindowInputsListener(this) );
    }

    @Override
    public int getNbThrottlesControllers() {
        return 1;
    }

    @Override
    public ThrottleControllerUI newThrottleController() {
        return InstanceManager.getDefault(ThrottleFrameManager.class).createSimpleThrottleFrame(null, null);
    }

    @Override
    public void addThrottleControllerAt(ThrottleControllerUI tf, int n) {
        // create a new window with the same address as the one provided in parameter
        InstanceManager.getDefault(ThrottleFrameManager.class).createSimpleThrottleFrame(tf.getAddress());
    }

    @Override
    public void removeThrottleController(ThrottleControllerUI tf) {
        // if we remove our one and only controler, we dispose ourselves
        if (tf == throttleControllerUI) {
            dispose();
        }        
    }

    @Override
    public ThrottleControllerUI getThrottleControllerAt(int n) {
        if (n == 0) {
            return throttleControllerUI;
        }
        return null;
    }

    @Override
    public ThrottleControllerUI getCurentThrottleController() {
        return throttleControllerUI;
    }

    @Override
    public void emergencyStopAll() {
        throttleControllerUI.eStop();
    }

    @Override
    public int getNumberOfEntriesFor(DccLocoAddress la) {
        if (throttleControllerUI.getAddress() != null && throttleControllerUI.getAddress().equals(la)) {
            return 1;
        }
        return 0;
    }

    @Override
    public void dispose() {        
        InstanceManager.getDefault(ThrottlesPreferences.class).removePropertyChangeListener(this);
        throttleControllerUI.dispose(); // will release the throttle
        throttleFrameManager.requestThrottleWindowDestruction(this);
        super.dispose();        
    }

    private void applyPreferences() {
        ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);

        ComponentInputMap im = new ComponentInputMap(getRootPane());
        for (Object k : this.getRootPane().getActionMap().allKeys()) {
            KeyStroke[] kss = preferences.getThrottlesKeyboardControls().getKeyStrokes((String)k);
            if (kss !=null) {
                for (KeyStroke keystroke : kss) {
                    if (keystroke != null) {
                        im.put(keystroke, k);
                    }
                }
            }
        }
        getRootPane().setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW,im);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ThrottlesPreferences.prefPopertyName.compareTo(evt.getPropertyName()) == 0) {
            applyPreferences();
        }        
    }
}
