package jmri.jmrit.vsdecoder.swing;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import java.beans.PropertyChangeEvent;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.JSlider;
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
 * Sound control buttons for the new GUI.
 *
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
 * @author Klaus Killinger Copyright (C) 2018-2021
 */
public class DieselPane extends EnginePane {

    static final int THROTTLE_MIN = 1;
    static final int THROTTLE_MAX = 18;
    static final int THROTTLE_INIT = 1;

    public static final String THROTTLE = "VSDDP:Throttle"; // NOI18N
    public static final String START = "VSDDP:Start"; // NOI18N
    public static final String VOLUME = "VSDDP:Volume"; // NOI18N

    JSpinner throttle_spinner;
    public JSlider volume_slider;
    JToggleButton start_button;

    int throttle_setting;
    private boolean engine_is_started;

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
        engine_is_started = start_button.isSelected();
    }

    /**
     * Null constructor
     */
    public DieselPane() {
        this(null);
    }

    /**
     * Init Context.
     * @param context unused.
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
    // Skip the timer if there is no start/stop-sound
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
        //Set up the throttle spinner
        throttle_spinner = new JSpinner(new SpinnerNumberModel(THROTTLE_INIT, THROTTLE_MIN, THROTTLE_MAX, 1));
        throttle_spinner.setPreferredSize(new Dimension(40, 30));
        throttle_spinner.setToolTipText(Bundle.getMessage("ToolTipDP_ThrottleSpinner"));
        throttle_spinner.setEnabled(false);

        this.add(throttle_spinner);

        // Setup the start button
        start_button = new JToggleButton();
        start_button.setText(Bundle.getMessage("ButtonEngineStart"));
        start_button.setPreferredSize(new Dimension(150, 30));
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

        //Set up the volume slider
        volume_slider = new JSlider(0, 100);
        volume_slider.setMinorTickSpacing(10);
        volume_slider.setPaintTicks(true);
        volume_slider.setPreferredSize(new Dimension(160, 30));
        volume_slider.setToolTipText(Bundle.getMessage("DecoderVolumeToolTip"));
        volume_slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                volumeChange(e); // slider in real time
            }
        });
        this.add(volume_slider);

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
        if (armed && pressed && selected && (lastSpeed > 0.0f) && (!engine_is_started)) {
            buttonModel.setArmed(false);
            buttonModel.setPressed(false);
            buttonModel.setSelected(false);
            if (GraphicsEnvironment.isHeadless()) {
                log.info(Bundle.getMessage("EngineStartSpeedMessage"));
            } else {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("EngineStartSpeedMessage"));
            }
        }

        if (armed && pressed && selected && (lastSpeed > 0.0f) && (engine_is_started) && (getStopOption())) {
            buttonModel.setArmed(false);
            buttonModel.setPressed(false);
            if (GraphicsEnvironment.isHeadless()) {
                log.info(Bundle.getMessage("EngineStopSpeedMessage"));
            } else {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("EngineStopSpeedMessage"));
            }
        }
    }

    /**
     * Respond to a throttle change.
     * Basically, doesn't do anything.
     * @param e unused.
     */
    public void throttleChange(ChangeEvent e) {
        firePropertyChangeEvent(new PropertyChangeEvent(this, THROTTLE, throttle_setting, throttle_spinner.getModel().getValue()));
        throttle_setting = throttleNotch();
    }

    /**
     * Respond to a start button press.
     * @param e unused.
     */
    public void startButtonChange(ActionEvent e) {
        firePropertyChangeEvent(new PropertyChangeEvent(this, START, engine_is_started, start_button.isSelected()));
        engine_is_started = start_button.isSelected();
        if (engine_is_started) { // switch button name to make the panel more responsive
            start_button.setText(Bundle.getMessage("ButtonEngineStop"));
        } else {
            start_button.setText(Bundle.getMessage("ButtonEngineStart"));
        }
        startDelayTimer();
    }

    protected void volumeChange(ChangeEvent e) {
        JSlider v = (JSlider) e.getSource();
        log.debug("Decoder Volume slider set to value: {}", v.getValue());
        firePropertyChangeEvent(new PropertyChangeEvent(this, VOLUME, v.getValue(), null));
    }

    @Override
    public void startButtonClick() {
        start_button.doClick(); // Animate button and process ChangeEvent
    }

    /**
     * Get if Engine is On.
     * @return true if the start button is "on".
     */
    public boolean engineIsOn() {
        return start_button.isSelected();
    }

    /**
     * Get Throttle notch.
     * @return current notch setting of the throttle slider.
     */
    public int throttleNotch() {
        return (Integer) throttle_spinner.getModel().getValue();
    }

    /**
     * Set the Throttle spinner value.
     * @param t new value.
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
