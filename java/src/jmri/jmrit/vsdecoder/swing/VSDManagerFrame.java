package jmri.jmrit.vsdecoder.swing;

/** class VSDManagerFrame
 *
 * Main frame for the new GUI VSDecoder Manager Frame
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

import jmri.util.JmriJFrame;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSlider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jmri.util.JmriJFrame;
import jmri.util.WindowMenu;

import jmri.jmrit.vsdecoder.VSDConfig;
import jmri.jmrit.vsdecoder.VSDecoder;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import jmri.jmrit.vsdecoder.swing.VSDSwingBundle;
import jmri.jmrit.vsdecoder.SoundEvent;
import jmri.jmrit.vsdecoder.LoadVSDFileAction;
import jmri.jmrit.vsdecoder.StoreXmlVSDecoderAction;
import jmri.jmrit.vsdecoder.LoadXmlVSDecoderAction;
import jmri.jmrit.vsdecoder.VSDecoderPreferencesAction;

@SuppressWarnings("serial")
public class VSDManagerFrame extends JmriJFrame {

    private static final ResourceBundle rb = VSDSwingBundle.bundle();

    public static enum PropertyChangeID { MUTE, VOLUME_CHANGE, ADD_DECODER, REMOVE_DECODER, CLOSE_WINDOW }

    public static final Map<PropertyChangeID, String> PCIDMap;
    static {
	Map<PropertyChangeID, String> aMap = new HashMap<PropertyChangeID, String>();
	aMap.put(PropertyChangeID.MUTE, "VSDMF:Mute");
	aMap.put(PropertyChangeID.VOLUME_CHANGE, "VSDMF:VolumeChange");
	aMap.put(PropertyChangeID.ADD_DECODER, "VSDMF:AddDecoder");
	aMap.put(PropertyChangeID.REMOVE_DECODER, "VSDMF:RemoveDecoder");
	aMap.put(PropertyChangeID.CLOSE_WINDOW, "VSDMF:CloseWindow");
	PCIDMap = Collections.unmodifiableMap(aMap);
    }

    // Map of Mnemonic KeyEvent values to GUI Components
    private static final Map<String, Integer> Mnemonics = new HashMap<String, Integer>();
	static {
	    // Menu
	    Mnemonics.put("FileMenu", KeyEvent.VK_F);
	    Mnemonics.put("EditMenu", KeyEvent.VK_E);
	    // Other GUI
	    Mnemonics.put("MuteButton", KeyEvent.VK_M);
	    Mnemonics.put("AddButton", KeyEvent.VK_A);
	    Mnemonics.put("CloseButton", KeyEvent.VK_C);

    }


    protected EventListenerList listenerList = new javax.swing.event.EventListenerList();

    JPanel decoderPane;
    JPanel volumePane;
    JPanel decoderBlank;

    private VSDConfig config;

    private List<JMenu> menuList;

    //private List<JMenu> menuList;

    /** Constructor */
    public VSDManagerFrame() {
	super(false, false);
	config = new VSDConfig();
	this.addPropertyChangeListener(VSDecoderManager.instance());
	initGUI();
    }

    public void initComponents() {
	//this.initGUI();
    }

    /** Build the GUI components */
    public void initGUI() {
	log.debug("initGUI");
	this.setTitle(rb.getString("VSDManagerFrameTitle"));
	this.buildMenu();
	this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));

	decoderPane = new JPanel();
	decoderPane.setLayout(new BoxLayout(decoderPane, BoxLayout.PAGE_AXIS));
	decoderBlank = VSDControl.generateBlank();
	decoderPane.add(decoderBlank);

	volumePane = new JPanel();
	volumePane.setLayout(new BoxLayout(volumePane, BoxLayout.LINE_AXIS));
	JToggleButton muteButton = new JToggleButton(rb.getString("MuteButtonLabel"));
	JButton addButton = new JButton(rb.getString("AddButtonLabel"));
	JButton closeButton = new JButton(rb.getString("MgrCloseButtonLabel"));
	JSlider volume = new JSlider(0, 100);
	volume.setMinorTickSpacing(10);
	volume.setPaintTicks(true);
	volume.setValue(80);
	volume.setPreferredSize(new Dimension(200, 20));
	volume.setToolTipText(rb.getString("MgrVolumeToolTip"));
	volume.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    volumeChange(e);
		}
	    });
	volumePane.add(new JLabel(rb.getString("VolumePaneLabel")));
	volumePane.add(volume);
	volumePane.add(muteButton);
	muteButton.setToolTipText(rb.getString("MgrMuteToolTip"));
	muteButton.setMnemonic(Mnemonics.get("MuteButton"));
	muteButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    muteButtonPressed(e);
		}
	    });
	volumePane.add(addButton);
	addButton.setToolTipText(rb.getString("MgrAddButtonToolTip"));
	addButton.setMnemonic(Mnemonics.get("AddButton"));
	addButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    addButtonPressed(e);
		}
	    });
	volumePane.add(closeButton);
	closeButton.setToolTipText(rb.getString("MgrCloseButtonToolTip"));
	closeButton.setMnemonic(Mnemonics.get("CloseButton"));
	closeButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    closeButtonPressed(e);
		}
	    });

	this.add(decoderPane);
	this.add(volumePane);

	log.debug("pane size + " + decoderPane.getPreferredSize());
	this.pack();
	this.setVisible(true);

	log.debug("done...");
    }

    /** Handle "Close" button press */
    protected void closeButtonPressed(ActionEvent e) {
	firePropertyChange(PropertyChangeID.CLOSE_WINDOW, null, null);
	dispose();
    }


    /** Handle "Mute" button press */
    protected void muteButtonPressed(ActionEvent e) {
	JToggleButton b = (JToggleButton)e.getSource();
	log.debug("Mute button pressed. value = " + b.isSelected());
	firePropertyChange(PropertyChangeID.MUTE, !b.isSelected(), b.isSelected());
    }

    /** Handle "Add" button press */
    protected void addButtonPressed(ActionEvent e) {
	log.debug("Add button pressed");
	// Do something here.  Create a new VSDecoder and add it to the window.
	VSDConfigDialog d = new VSDConfigDialog(decoderPane, rb.getString("NewDecoderConfigPaneTitle"), config);
	d.addPropertyChangeListener(new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
		    log.debug("property change name " + event.getPropertyName() + " old " + event.getOldValue() + " new " + event.getNewValue());
		    addButtonPropertyChange(event);
		}
		
	    });
	//firePropertyChange(PropertyChangeID.ADD_DECODER, null, null);
    }

    /** Callback for the Config Dialog */
    protected void addButtonPropertyChange(PropertyChangeEvent event) {
	log.debug("internal config dialog handler");
	VSDecoder newDecoder = VSDecoderManager.instance().getVSDecoder(config);
	if (newDecoder == null) {
	    log.debug("no New Decoder constructed!" + config.toString());
	    return;
	}
	//VSDControl newControl = new VSDControl(config.getLocoAddress().toString());
	VSDControl newControl = new VSDControl(config);
	// Set the Decoder to listen to PropertyChanges from the control
	newControl.addPropertyChangeListener(newDecoder);
	this.addPropertyChangeListener(newDecoder);
	// Set US to listen to PropertyChanges from the control (mainly for DELETE)
	newControl.addPropertyChangeListener(new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
		    log.debug("property change name " + event.getPropertyName() + " old " + event.getOldValue() + " new " + event.getNewValue());
		    vsdControlPropertyChange(event);
		}
	    });
	if (decoderPane.isAncestorOf(decoderBlank))
	    decoderPane.remove(decoderBlank);
	decoderPane.add(newControl);
	newControl.addSoundButtons(new ArrayList<SoundEvent>(newDecoder.getEventList()));
	//debugPrintDecoderList();
	decoderPane.revalidate();
	decoderPane.repaint();

	this.pack();
	//this.setVisible(true);
	// Do we need to make newControl a listener to newDecoder?
	//firePropertyChange(PropertyChangeID.ADD_DECODER, null, newDecoder);
    }

    /** Handle property change event from one of the VSDControls */
    protected void vsdControlPropertyChange(PropertyChangeEvent event) {
	String property = event.getPropertyName();
	if (property.equals(VSDControl.PCIDMap.get(VSDControl.PropertyChangeID.DELETE))) {
	    String ov = (String)event.getOldValue();
	    String nv = (String)event.getNewValue();
	    VSDecoder vsd = VSDecoderManager.instance().getVSDecoderByAddress(nv);
	    if (vsd == null)
		log.debug("VSD is null.");
	    this.removePropertyChangeListener(vsd);
	    log.debug("vsdControlPropertyChange: ID = " + PCIDMap.get(PropertyChangeID.REMOVE_DECODER) + " Old " + ov + " New " + nv);
	    firePropertyChange(PropertyChangeID.REMOVE_DECODER, ov, nv);
	    decoderPane.remove((VSDControl)event.getSource());
	    if (decoderPane.getComponentCount() == 0)
		decoderPane.add(decoderBlank);
	    //debugPrintDecoderList();
	    decoderPane.validate();
	    this.pack();
	    this.setVisible(true);
	}
    }

    /** (Debug only) Print the decoder list */
    /*
    private void debugPrintDecoderList() {
	log.debug("Printing Decoder Lists from VSDManagerFrame...");
	VSDecoderManager.instance().debugPrintDecoderList();
    }
    */

    /** Handle volume slider change */
    protected void volumeChange(ChangeEvent e) {
	JSlider v = (JSlider)e.getSource();
	log.debug("Volume slider moved. value = " + v.getValue());
	firePropertyChange(PropertyChangeID.VOLUME_CHANGE, v.getValue(), v.getValue());
    }

    private void buildMenu() {
	JMenu fileMenu = new JMenu(rb.getString("VSDecoderFileMenu"));
	fileMenu.setMnemonic(Mnemonics.get("FileMenu"));

        fileMenu.add(new LoadVSDFileAction(rb.getString("VSDecoderFileMenuLoadVSDFile" )));
        fileMenu.add(new StoreXmlVSDecoderAction(rb.getString("VSDecoderFileMenuSaveProfile" )));
        fileMenu.add(new LoadXmlVSDecoderAction(rb.getString("VSDecoderFileMenuLoadProfile")));

	JMenu editMenu = new JMenu(rb.getString("VSDecoderEditMenu"));
	editMenu.setMnemonic(Mnemonics.get("EditMenu"));
	editMenu.add(new VSDecoderPreferencesAction(rb.getString("VSDecoderEditMenuPreferences")));

	fileMenu.getItem(1).setEnabled(false); // disable XML store
	fileMenu.getItem(2).setEnabled(false); // disable XML load

	menuList = new ArrayList<JMenu>(3);

	menuList.add(fileMenu);
	menuList.add(editMenu);

	this.setJMenuBar(new JMenuBar());
	this.getJMenuBar().add(fileMenu);
	this.getJMenuBar().add(editMenu);
	this.addHelpMenu("package.jmri.jmrit.vsdecoder.swing.VSDManagerFrame", true); // Fix this... needs to be help for the new frame
	
    }

    /**
     * Add a standard help menu, including window specific help item.
     * @param ref JHelp reference for the desired window-specific help page
     * @param direct true if the help menu goes directly to the help system,
     *        e.g. there are no items in the help menu
     *
     * WARNING: BORROWED FROM JmriJFrame.  
     */
    public void addHelpMenu(String ref, boolean direct) {
        // only works if no menu present?
        JMenuBar bar = getJMenuBar();
        if (bar == null) bar = new JMenuBar();
        // add Window menu
	bar.add(new WindowMenu(this)); // * GT 28-AUG-2008 Added window menu
	// add Help menu
        jmri.util.HelpUtil.helpMenu(bar, ref, direct);
        setJMenuBar(bar);
    }


    /** Handle window close event */
    public void windowClosing(java.awt.event.WindowEvent e) {
	// Call the superclass function
        //super.windowClosing(e);

	log.debug("VSDecoderFrame windowClosing() called... " + e.toString());

	log.debug("Calling decoderPane.windowClosing() directly " + e.toString());
	//decoderPane.windowClosing(e);
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
	if (listener == null)
	    log.warn("No listener!");
	listenerList.remove(PropertyChangeListener.class, listener);
    }

    /** Fire a property change from this object */
    // NOTE: should this be public???
    public void firePropertyChange(PropertyChangeID id, Object oldProp, Object newProp) {
	// map the property change ID
	String pcname = PCIDMap.get(id);
	PropertyChangeEvent pce = new PropertyChangeEvent(this, pcname, oldProp, newProp);
	// Fire the actual PropertyChangeEvent
	log.debug("Firing property change: " + pcname);
	firePropertyChange(pce);
    }

    /** Fire a property change from this object */
    void firePropertyChange(PropertyChangeEvent evt) {
	if (evt == null) 
	    log.warn("EVT is NULL!!");
	for (PropertyChangeListener l : listenerList.getListeners(PropertyChangeListener.class)) {
	    l.propertyChange(evt);
	}
    }

    //public List<JMenu> getMenus() { return menuList; }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDManagerFrame.class.getName());
}