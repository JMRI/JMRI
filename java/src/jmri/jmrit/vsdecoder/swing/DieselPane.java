package jmri.jmrit.vsdecoder.swing;

/** class DieselPane
 *
 * Diesel sound control buttons for the new GUI
 */

/*
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
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision: 18410 $
 */

import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JToggleButton;
import javax.swing.BorderFactory;
import java.awt.GridLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import jmri.jmrit.vsdecoder.EnginePane;
import jmri.jmrit.vsdecoder.EngineSoundEvent;

@SuppressWarnings("serial")
public class DieselPane extends EnginePane {
    
    static final int THROTTLE_MIN = 1;
    static final int THROTTLE_MAX = 8;
    static final int THROTTLE_INIT = 1;

    JSpinner throttle_spinner;
    JSlider throttle_slider;
    JToggleButton start_button;

    EngineSoundEvent engine;

    Integer throttle_setting;
    Boolean engine_started;

    /** Constructor
     *
     * @param String n : pane title
     */
    public DieselPane(String n) {
	super(n);
	initComponents();
	throttle_setting = throttle_slider.getValue();
	engine_started = start_button.isSelected();
    }

    /** Null constructor */
    public DieselPane() {
	this(null);
    }

    /** Init Context. */
    public void initContext(Object context) {
	initComponents();
    }

    /** Build teh GUI components */
    public void initComponents() {
        listenerList = new javax.swing.event.EventListenerList();
        
	this.setLayout(new GridLayout(0, 2));
	
	//Set up the throttle spinner
	throttle_spinner = new JSpinner(new SpinnerNumberModel(THROTTLE_INIT, THROTTLE_MIN, THROTTLE_MAX, 1));
	throttle_spinner.setEnabled(false);

	//Setup the throttle slider.
	throttle_slider = new JSlider(JSlider.HORIZONTAL, THROTTLE_MIN,
				      THROTTLE_MAX, THROTTLE_INIT);
	throttle_slider.setMajorTickSpacing(1);
	throttle_slider.setPaintTicks(true);
	throttle_slider.setMinimumSize(new Dimension(400, 0));
	throttle_slider.setSnapToTicks(true);
	throttle_slider.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    throttleChange(e);
		}
	    });
	//this.add(throttle_slider);
	this.add(throttle_spinner);

	// Setup the start button
	start_button = new JToggleButton();
	start_button.setText("Engine Start");
	start_button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    startButtonChange(e);
		}
	    });
	this.add(start_button);
	//throttle_slider.setVisible(true);
	//start_button.setVisible(true);
	this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	this.setVisible(true);
    }

    /** Respond to a throttle change.  Basically, doesn't do anything */
    public void throttleChange(ChangeEvent e) {
	firePropertyChangeEvent(new PropertyChangeEvent(this, "throttle",
						        throttle_setting,
							throttle_slider.getValue()));
	throttle_setting = throttle_slider.getValue();
    }

    /** Respond to a start button press */
    public void startButtonChange(ActionEvent e) {
	firePropertyChangeEvent(new PropertyChangeEvent(this, "start",
							engine_started, 
							start_button.isSelected()));
	engine_started = start_button.isSelected();
    }

    /** Return true if the start button is "on" */
    public boolean engineIsOn() {
	return(start_button.isSelected());
    }

    /** Return current notch setting of the throttle slider */
    public int throttleNotch() {
	return(throttle_slider.getValue());
    }

    /** set the throttle spinner value */
    public void setThrottle(int t) {
	//throttle_slider.setValue(t);
	throttle_spinner.setValue(t);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DieselPane.class.getName());

}