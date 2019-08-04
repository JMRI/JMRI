package jmri.jmrit.vsdecoder.swing;

import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import java.beans.PropertyChangeEvent;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.Timer;
import jmri.jmrit.vsdecoder.EnginePane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class DieselPane
 *
 * Diesel sound control buttons for the new GUI
 */

/**
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author Mark Underwood Copyright (C) 2011
 * @author Klaus Killinger Copyright (C) 2018
 */
public class DieselPane extends EnginePane {

    static final int THROTTLE_MIN = 1;
    static final int THROTTLE_MAX = 8;
    static final int THROTTLE_INIT = 1;

    JSpinner throttle_spinner;
    JToggleButton start_button;

    Integer throttle_setting;
    Boolean engine_started;

    private Timer timer;
    int dtime = 1;
    float lastSpeed = 0.0f;

    /**
     * Constructor
     *
     * @param n pane title
     */
    public DieselPane(String n) {
        super(n);
        initComponents();
        throttle_setting = THROTTLE_INIT;
        engine_started = start_button.isSelected();
    }

    /**
     * Null constructor
     */
    public DieselPane() {
        this(null);
    }

    /**
     * Init Context.
     */
    @Override
    public void initContext(Object context) {
        initComponents();
    }

    protected Timer newTimer(int time, boolean repeat, ActionListener al) {
        timer = new Timer(time, al);
        timer.setRepeats(repeat);
        return timer;
    }

    // Lock the start/stop-button until the start/stop-sound has finished
    void startDelayTimer() {
        if (dtime > 1) {
            start_button.setEnabled(false);
            timer = newTimer(dtime, false, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    start_button.setEnabled(true);
                }
            });
            timer.start();
        }
    }

    @Override
    public void setButtonDelay(long t) {
        // Timer only takes an int ... cap the length at MAXINT
        // Note: this only works for positive lengths ...
        if (t > Integer.MAX_VALUE) {
            t = Integer.MAX_VALUE; // small enough to safely cast
        }
        dtime = (int) t; // time in ms
    }

    /**
     * Build the GUI components
     */
    @Override
    public void initComponents() {
        listenerList = new javax.swing.event.EventListenerList();

        this.setLayout(new GridLayout(0, 2));

        //Set up the throttle spinner
        throttle_spinner = new JSpinner(new SpinnerNumberModel(THROTTLE_INIT, THROTTLE_MIN, THROTTLE_MAX, 1));
        throttle_spinner.setToolTipText(Bundle.getMessage("ToolTipDP_ThrottleSpinner"));
        throttle_spinner.setEnabled(false);

        this.add(throttle_spinner);

        // Setup the start button
        start_button = new JToggleButton();
        start_button.setText(Bundle.getMessage("ButtonEngineStart"));
        start_button.setToolTipText(Bundle.getMessage("ToolTipDP_StartButton"));
        start_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startButtonChange(e);
            }
        });

        start_button.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ev) {
                startButtonStateChange(ev);
            }
        });

        this.add(start_button);
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.setVisible(true);
    }

    // Respond to a start button press with stateChanged
    public void startButtonStateChange(ChangeEvent ev) {
        AbstractButton abstractButton = (AbstractButton) ev.getSource();
        ButtonModel buttonModel = abstractButton.getModel();
        boolean armed = buttonModel.isArmed();
        boolean pressed = buttonModel.isPressed();
        boolean selected = buttonModel.isSelected();
        if (armed && pressed && selected && (lastSpeed > 0.0f) && (!engine_started)) {
            buttonModel.setArmed(false);
            buttonModel.setPressed(false);
            buttonModel.setSelected(false);
            if (GraphicsEnvironment.isHeadless()) {
                log.info(Bundle.getMessage("EngineStartSpeedMessage"));
            } else {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("EngineStartSpeedMessage"));
            }
        }
    }

    /**
     * Respond to a throttle change. Basically, doesn't do anything
     */
    public void throttleChange(ChangeEvent e) {
        firePropertyChangeEvent(new PropertyChangeEvent(this, "throttle", throttle_setting, // NOI18N
                throttle_spinner.getModel().getValue()));
        throttle_setting = (Integer) throttle_spinner.getModel().getValue();
    }

    /**
     * Respond to a start button press
     */
    public void startButtonChange(ActionEvent e) {
        firePropertyChangeEvent(new PropertyChangeEvent(this, "start", // NOI18N
                engine_started,
                start_button.isSelected()));
        engine_started = start_button.isSelected();
        if (engine_started) { // switch button name to make the panel more responsive
            start_button.setText(Bundle.getMessage("ButtonEngineStop"));
        } else {
            start_button.setText(Bundle.getMessage("ButtonEngineStart"));
        }
        startDelayTimer();
    }

    @Override
    public void startButtonClick() {
        start_button.doClick(); // Animate button and process ChangeEvent
    }

    /**
     * Return true if the start button is "on"
     */
    public boolean engineIsOn() {
        return start_button.isSelected();
    }

    /**
     * Return current notch setting of the throttle slider
     */
    public int throttleNotch() {
        return (Integer) throttle_spinner.getModel().getValue();
    }

    /**
     * set the throttle spinner value
     */
    @Override
    public void setThrottle(int t) {
        throttle_spinner.setValue(t);
    }

    @Override
    public void setSpeed(float s) {
        lastSpeed = s;
    }

    private static final Logger log = LoggerFactory.getLogger(DieselPane.class);

}
