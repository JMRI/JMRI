package jmri.jmrit.vsdecoder;

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
 * @version			$Revision$
 */

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.util.swing.*;

import java.io.File;
import jmri.jmrit.XmlFile;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Arrays;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.TitledBorder;


/**
 * Virtual Sound Decoder for playing sounds off of LocoNet messages.
 * Based on the LocoMon tool by Bob Jacobsen
 * @author	   Mark Underwood   Copyright (C) 2011
 * @version   $Revision$
 */
@SuppressWarnings("serial")
public class VSDecoderPane extends JmriPanel {

    //private static final ResourceBundle vsdBundle = VSDecoderBundle.bundle();

    public static enum PropertyChangeID { ADDRESS_CHANGE, PROFILE_SELECT, MUTE, VOLUME_CHANGE }

    private static final Map<PropertyChangeID, String> PCIDMap;
    static {
	Map<PropertyChangeID, String> aMap = new HashMap<PropertyChangeID, String>();
	aMap.put(PropertyChangeID.ADDRESS_CHANGE, "AddressChange");
	aMap.put(PropertyChangeID.PROFILE_SELECT, "ProfileSelect");
	aMap.put(PropertyChangeID.MUTE, "Mute");
	aMap.put(PropertyChangeID.VOLUME_CHANGE, "VolumeChange");
	PCIDMap = Collections.unmodifiableMap(aMap);
    }

    String decoder_id;
    VSDecoderManager decoder_mgr;

    final static String BASICPANEL = "Basic";
    final static String COMBOPANEL = "Sounds";
    final static String OPTIONPANEL = "Options";

    // GUI Components

    private VSDecoderFrame parent;

    private JTabbedPane tabbedPane;
    private VSDConfigPanel configPanel;
    private JPanel soundsPanel;
    private JPanel optionPanel;
    private JPanel volumePanel;

    private static String VSDecoderFileLocation = null;

    //private List<JMenu> menuList;

    public VSDecoderPane(VSDecoderFrame p) {
        super();
	parent = p;
	decoder_mgr = VSDecoderManager.instance();
    }

    
    public VSDecoderFrame getFrame() { return(parent); }

    
    // getHelpTarget()
    //
    // Return a reference to the help file
    public String getHelpTarget() { return "package.jmri.jmrix.vsdecoder.VSDecoderPane"; }

    // getTitle()
    //
    // Return a suggested title for the enclosing frame.
    public String getTitle() { 
        return VSDecoderBundle.bundle().getString("WindowTitle");
    }

    // getDefaultVSDecoderFolder()
    //
    // Return a string default default path for the VSD files
    public static String getDefaultVSDecoderFolder() {
        if (VSDecoderFileLocation == null)
            return XmlFile.prefsDir()+"vsdecoder"+File.separator ;
        return VSDecoderFileLocation;
    }



    public void init() {
	// Does nothing.  Here for completeness.
    }
    
    public void initContext(Object context) {
	// Does nothing.  Here for completeness.
    }

    // initComponents()
    //
    // initialzies the GUI components.
    public void initComponents() {
	log.debug("initComponents()");
	//buildMenu();

	setLayout(new BorderLayout(10, 0));
	setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

	// Add the tabbed pane to the VSDecoderPane.  The tabbedPane will contain all the other panes.
	tabbedPane = new JTabbedPane();
	tabbedPane.setMinimumSize(new Dimension(300, 300));
	add(tabbedPane, BorderLayout.CENTER);

	//-------------------------------------------------------------------
	// configPanel
	// The configPanel holds the stuff for addressing and configuration.
        configPanel = new VSDConfigPanel(decoder_id, this);

	tabbedPane.addTab("Config", configPanel);

	//-------------------------------------------------------------------
	// soundsPanel
	// The optionPanel holds controls for selecting sound options.
	optionPanel = new VSDOptionPanel(decoder_id, this);

	tabbedPane.addTab("Options", optionPanel);
	

	//-------------------------------------------------------------------
	// soundsPanel
	// The soundsPanel holds buttons for specific sounds.
        soundsPanel = new VSDSoundsPanel(decoder_id, this);
	tabbedPane.addTab("Sounds", soundsPanel);

	//-------------------------------------------------------------------
	// volumePanel
	// The volumePanel holds the master volume and mute controls.
	volumePanel = new JPanel();
	volumePanel.setLayout(new BorderLayout(10, 0));
	TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Volume");
	title.setTitlePosition(TitledBorder.DEFAULT_POSITION);
	//volumePanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
	volumePanel.setBorder(title);

	JSlider volume = new JSlider(0, 100);
	volume.setMinorTickSpacing(10);
	volume.setPaintTicks(true);
	volume.setValue(80);
	volume.setPreferredSize(new Dimension(200, 20));
	volume.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    volumeChange(e);
		}
	    });
	volumePanel.add(volume, BorderLayout.LINE_START);

	JToggleButton mute_button = new JToggleButton("Mute");
	mute_button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    muteButtonPressed(e);
		}
		
	    });
	volumePanel.add(mute_button, BorderLayout.LINE_END);

	volumePanel.setPreferredSize(new Dimension(300, 60));
	add(volumePanel, BorderLayout.PAGE_END);

	// Pack and set visible
	parent.pack();
	parent.setVisible(true);
    }

    // PROPERTY CHANGE EVENT FUNCTIONS

    public void muteButtonPressed(ActionEvent e) {
	JToggleButton b = (JToggleButton)e.getSource();
	log.debug("Mute button pressed. value = " + b.isSelected());
	firePropertyChange(PropertyChangeID.MUTE, !b.isSelected(), b.isSelected());
	// do something.
    }

    public void volumeChange(ChangeEvent e) {
	JSlider v = (JSlider)e.getSource();
	log.debug("Volume slider moved. value = " + v.getValue());
	firePropertyChange(PropertyChangeID.VOLUME_CHANGE, v.getValue(), v.getValue());

	// do something
    }

    // VSDecoderManager Events
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	List<PropertyChangeListener> l = Arrays.asList(listenerList.getListeners(PropertyChangeListener.class));
	if (!l.contains(listener))
	    listenerList.add(PropertyChangeListener.class, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
	listenerList.remove(PropertyChangeListener.class, listener);
    }

    public void firePropertyChange(PropertyChangeID id, Object oldProp, Object newProp) {
	String pcname;

	// map the property change ID
	pcname = PCIDMap.get(id);
	// Fire the actual PropertyChangeEvent
	firePropertyChange(new PropertyChangeEvent(this, pcname, oldProp, newProp));
    }

    void firePropertyChange(PropertyChangeEvent evt) {
	//Object[] listeners = listenerList.getListenerList();

	for (PropertyChangeListener l : listenerList.getListeners(PropertyChangeListener.class)) {
	    l.propertyChange(evt);
	}
    }

    // getDecoder()
    //
    // Looks up the currently referenced decoder and returns it.
    public VSDecoder getDecoder() {
	VSDecoder d = VSDecoderManager.instance().getVSDecoderByID(decoder_id);
	addPropertyChangeListener(d);
	return(d);
    }


    // getDecoder(String)
    //
    // Looks up a decoder profile by name and returns that decoder.
    public VSDecoder getDecoder(String profile) {
	VSDecoder d = VSDecoderManager.instance().getVSDecoder(profile);
	addPropertyChangeListener(d);
	return(d);
    }

    // setDecoder()
    //
    // set the Decoder ID and update the soundsPanel
    public void setDecoder(VSDecoder dec) {
	if (dec != null) {
	    // Store the new decoder
	    decoder_id = dec.getID();
	    log.debug("Decoder ID = " + decoder_id + " Decoder = " + dec);
	    // Register the decoder as a listener on our frame... so it can react
	    // to the window closing
	    parent.addWindowListener(new WindowListener() {
		    public void windowActivated(WindowEvent e) {}
		    public void windowClosed(WindowEvent e) {}
		    public void windowClosing(WindowEvent e) { 
			VSDecoderManager.instance().getVSDecoderByID(decoder_id).windowChange(e); 
		    }
		    public void windowDeactivated(WindowEvent e) {}
		    public void windowDeiconified(WindowEvent e) {}
		    public void windowIconified(WindowEvent e) {}
		    public void windowOpened(WindowEvent e) {}
		});
	    // Update the sounds pane
	    tabbedPane.remove(soundsPanel);
	    soundsPanel = new VSDSoundsPanel(decoder_id, this);
	    tabbedPane.addTab("Sounds", soundsPanel);
	    tabbedPane.revalidate();
	    tabbedPane.repaint();
	}
	
    }

    // setAddress()
    //
    // Update the Decoder's address...
    public void setAddress(LocoAddress a) {
	if (a != null) {
	    VSDecoder decoder = VSDecoderManager.instance().getVSDecoderByID(decoder_id);
	    if (decoder != null) {
		decoder.setAddress(a);
		decoder.enable();
	    }
	    this.setTitle(a);
	}
    }

    // setTitle();
    //
    // Update the window title with the given address.
    // Deprecate this eventually
    public void setTitle(DccLocoAddress a) {
	if (a != null) {
	    parent.setTitle("VSDecoder - " + a.toString());
	}
    }

    // New version
    public void setTitle(LocoAddress a) {
	if (a != null) {
	    parent.setTitle("VSDecoder - " + a.toString());
	}
    }

    public void windowClosing(WindowEvent e) {
	log.debug("VSDecoderPane windowClosing() called...");
    }


    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoderPane.class.getName());
}
