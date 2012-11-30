package jmri.jmrit.vsdecoder.swing;

/**
 * class VSDControl
 *
 * New GUI pane for a Virtual Sound Decoder (VSDecoder).
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
 * @version			$Revision: 21510 $
 */

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.util.swing.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.Border;
import jmri.jmrit.vsdecoder.VSDConfig;
import java.util.ResourceBundle;
import jmri.jmrit.vsdecoder.SoundEvent;
import jmri.jmrit.vsdecoder.EngineSoundEvent;

/**
 * Virtual Sound Decoder for playing sounds off of LocoNet messages.
 * Based on the LocoMon tool by Bob Jacobsen
 * @author	   Mark Underwood   Copyright (C) 2011
 * @version   $Revision: 21510 $
 */
@SuppressWarnings("serial")
public class VSDControl extends JPanel {

    private static final ResourceBundle rb = VSDSwingBundle.bundle();


    public static enum PropertyChangeID { ADDRESS_CHANGE, CONFIG_CHANGE, PROFILE_SELECT, HORN, BELL, NOTCH, COUPLER, BRAKE, ESTART, DELETE }

    public static final Map<PropertyChangeID, String> PCIDMap;
    static {
	Map<PropertyChangeID, String> aMap = new HashMap<PropertyChangeID, String>();
	aMap.put(PropertyChangeID.ADDRESS_CHANGE, "AddressChange");
	aMap.put(PropertyChangeID.CONFIG_CHANGE, "ConfigChange");
	aMap.put(PropertyChangeID.PROFILE_SELECT, "ProfileSelect");
	aMap.put(PropertyChangeID.HORN, "HornSound");
	aMap.put(PropertyChangeID.BELL, "BellSound");
	aMap.put(PropertyChangeID.NOTCH, "EngineNotch");
	aMap.put(PropertyChangeID.COUPLER, "CouplerSound");
	aMap.put(PropertyChangeID.BRAKE, "BrakeSound");
	aMap.put(PropertyChangeID.ESTART, "EngineStart");
	aMap.put(PropertyChangeID.DELETE, "DeleteDecoder");
	PCIDMap = Collections.unmodifiableMap(aMap);
    }

    String address;

    Border tb;
    JLabel addressLabel;
    JButton configButton;
    JButton optionButton;
    JButton deleteButton;

    JPanel soundsPanel;
    JPanel configPanel;

    private VSDConfig config;

    /** Constructor */
    public VSDControl() {
	super();
	initComponents("");
    }

    /** Constructor
     *
     * @param title (String) : Window title
     */
    public VSDControl(String title) {
	super();
	address = title;
	config = new VSDConfig();
	initComponents(title);
    }

    public VSDControl(VSDConfig c) {
	super();
	config = c;
	address = config.getLocoAddress().toString();
	initComponents(address);
    }

    static public JPanel generateBlank() {
	VSDControl temp = new VSDControl("");
	JLabel jl = new JLabel(rb.getString("BlankVSDControlLabel"));
	jl.setMinimumSize(temp.getPreferredSize());
	jl.setPreferredSize(temp.getPreferredSize());
	jl.setHorizontalAlignment(SwingConstants.CENTER);
	JPanel jp = new JPanel();
	jp.setLayout(new BorderLayout());
	jp.add(jl, BorderLayout.CENTER);
	jl.setMinimumSize(temp.getPreferredSize());
	jp.setPreferredSize(temp.getPreferredSize());
	jp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
							BorderFactory.createLoweredBevelBorder()));
	return(jp);
    }

    private GridBagConstraints setConstraints(int x, int y) {
	return(setConstraints(x, y, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2), GridBagConstraints.LINE_START));
    }
    /*
    private GridBagConstraints setConstraints(int x, int y, int fill) {
	return(setConstraints(x, y, fill, new Insets(2,2,2,2), GridBagConstraints.LINE_START));
    }
    */
    private GridBagConstraints setConstraints(int x, int y, int fill, Insets ins, int anchor) {
	GridBagConstraints gbc1 = new GridBagConstraints();
	gbc1.insets = ins;
	gbc1.gridx = x;
	gbc1.gridy = y;
	gbc1.weightx = 100.0;
	gbc1.weighty = 100.0;
	gbc1.gridwidth = 1;
	gbc1.anchor = anchor;
	gbc1.fill = fill;
	
	return(gbc1);
    }

    /** Initialize the GUI components */
    protected void initComponents(String title) {
	// Create the border.
	// Could make this a titled border with the loco address as the title...
	//tb = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
	tb = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
						BorderFactory.createLoweredBevelBorder());

	this.setBorder(tb);

	this.setLayout(new GridBagLayout());
	
	// Create the buttons and slider
	soundsPanel = new JPanel();
	soundsPanel.setLayout(new GridBagLayout());
	addressLabel = new JLabel(address);

	configPanel = new JPanel();
	configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.PAGE_AXIS));
	configButton = new JButton(rb.getString("ConfigButtonLabel"));
	optionButton = new JButton(rb.getString("OptionsButtonLabel"));
	deleteButton = new JButton(rb.getString("DeleteButtonLabel"));
	configPanel.add(configButton); // maybe don't allow this anymore.
	configPanel.add(Box.createHorizontalGlue());
	configPanel.add(optionButton);
	configPanel.add(Box.createHorizontalGlue());
	configPanel.add(deleteButton);
	
	JPanel alPanel = new JPanel();
	alPanel.setLayout(new BoxLayout(alPanel, BoxLayout.PAGE_AXIS));
	alPanel.add(addressLabel);
	alPanel.add(new JLabel(config.getProfileName()));
	
	// Add them to the panel
	this.add(alPanel, new GridBagConstraints(0, 0, 1, 2, 100.0, 100.0, 
						      GridBagConstraints.LINE_START,
						      GridBagConstraints.NONE,
						      new Insets(2,2,2,2),
						      0, 0));
	this.add(soundsPanel, setConstraints(2,0));
	this.add(configPanel, setConstraints(3,0));
	
	optionButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    optionButtonPressed(e);
		}
		
	    });
	configButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    configButtonPressed(e);
		}
		
	    });
	deleteButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    deleteButtonPressed(e);
		}
		
	    });

	this.setVisible(true);
    }

    /** Add buttons for the selected Profile's defined sounds */
    public void addSoundButtons(ArrayList<SoundEvent> elist) {
	soundsPanel.removeAll();
	for (SoundEvent e : elist) {
	    if (e.getButton() != null) {
		log.debug("adding button " + e.getButton().toString());
		JComponent jc = e.getButton(); 
		GridBagConstraints gbc = new GridBagConstraints();
		// Force the EngineSoundEvent to the second row.
		if (e instanceof EngineSoundEvent) {
		    gbc.gridy = 1;
		    gbc.gridwidth = elist.size() - 1;
		    gbc.fill = GridBagConstraints.NONE;
		    gbc.anchor = GridBagConstraints.LINE_START;
		    soundsPanel.add(jc, gbc);
		} else {
		    gbc.gridy = 0;
		    soundsPanel.add(jc, gbc);
		}
	    }
	}
    }

    /** Handle "Option" button presses */
    protected void optionButtonPressed(ActionEvent e) {
	log.debug("("+address+") Option Button Pressed");
	JOptionPane.showMessageDialog(this, rb.getString("OptionButtonPressedMessage"));
    }

    /** Handle "Config" button presses */
    protected void configButtonPressed(ActionEvent e) {
	log.debug("("+address+") Config Button Pressed");
	VSDConfigDialog d = new VSDConfigDialog(this, rb.getString("ConfigDialogTitlePrefix") + " " + this.address, config);
	d.addPropertyChangeListener(new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
		    log.debug("property change name " + event.getPropertyName() + " old " + event.getOldValue() + " new " + event.getNewValue());
		    configDialogPropertyChange(event);
		}
		
	    });
    }

    /** Handle "Delete" button presses */
    protected void deleteButtonPressed(ActionEvent e) {
	log.debug("("+address+") Delete Button Pressed");
	firePropertyChange(PropertyChangeID.DELETE, address, address);
    }

    /** Callback for the Config Dialog */
    protected void configDialogPropertyChange(PropertyChangeEvent event) {
	log.debug("internal config dialog handler");
	firePropertyChange(PropertyChangeID.CONFIG_CHANGE, event.getOldValue(), event.getNewValue());
    }

    // VSDecoderManager Events

    /** Add a listener for this Pane's property change events */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	List<PropertyChangeListener> l = Arrays.asList(listenerList.getListeners(PropertyChangeListener.class));
	if (!l.contains(listener))
	    listenerList.add(PropertyChangeListener.class, listener);
    }

    /** Remove a listener */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
	listenerList.remove(PropertyChangeListener.class, listener);
    }

    /** Fire a property change from this object */
    // NOTE: should this be public???
    public void firePropertyChange(PropertyChangeID id, Object oldProp, Object newProp) {
	String pcname;

	// map the property change ID
	pcname = PCIDMap.get(id);
	// Fire the actual PropertyChangeEvent
	firePropertyChange(new PropertyChangeEvent(this, pcname, oldProp, newProp));
    }

    /** Fire a property change from this object */
    void firePropertyChange(PropertyChangeEvent evt) {
	//Object[] listeners = listenerList.getListenerList();

	for (PropertyChangeListener l : listenerList.getListeners(PropertyChangeListener.class)) {
	    l.propertyChange(evt);
	}
    }


    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDControl.class.getName());
}
