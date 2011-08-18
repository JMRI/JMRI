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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.swing.JmriPanel;

public class VSDConfigPanel extends JmriPanel {

    private static final ResourceBundle vsdecoderBundle = VSDecoderBundle.bundle();

    VSDecoder decoder;
    VSDecoderManager decoder_mgr;
    VSDecoderPane main_pane;

    private javax.swing.JLabel addressLabel;
    private javax.swing.JTextField addressTextBox;
    private javax.swing.JButton addressSetButton;
    private javax.swing.JComboBox rosterComboBox;
    private javax.swing.JLabel rosterLabel;
    private javax.swing.JButton rosterSaveButton;
    private javax.swing.JComboBox profileComboBox;
    private javax.swing.JLabel profileLabel;

    private javax.swing.JPanel rosterPanel;
    private javax.swing.JPanel profilePanel;
    private javax.swing.JPanel addressPanel;

    private boolean profile_selected;

    private RosterEntry rosterEntry;

    public VSDConfigPanel() {
	this(null, null);
    }

    public VSDConfigPanel(VSDecoder dec, VSDecoderPane dad) {
	super();
	decoder = dec;
	main_pane = dad;
	profile_selected = false;
	decoder_mgr = VSDecoderManager.instance();
	initComponents();
    }

    public void handleDecoderListChange() {
	log.warn("Handling the decoder list change");
	ArrayList<String> sl = VSDecoderManager.instance().getVSDProfileNames();
	this.setProfileList(sl);
    }

    public void setDecoder(VSDecoder dec) {
	decoder = dec;
    }

    public VSDecoder getDecoder() {
	return(decoder);
    }

    public void setMainPane(VSDecoderPane dad) {
	main_pane = dad;
    }

    public VSDecoderPane getMainPane() {
	return(main_pane);
    }

    public void init() {}

    @Override
    public void initContext(Object context) {
	initComponents();
    }

    public void setProfileList(ArrayList<String> s) {
	VSDecoder vsd;
	boolean default_set = false;

	log.warn("updating the profile list.");
	
	profileComboBox.setModel(new DefaultComboBoxModel());
	Iterator itr = s.iterator();
	while (itr.hasNext()) {

	    String st = (String)itr.next();
	    log.debug("added item " + st);
	    profileComboBox.addItem(st);
	    vsd = VSDecoderManager.instance().getVSDecoder(st);
	    log.debug("decoder " + st + " : " + vsd);
	    if (((vsd = VSDecoderManager.instance().getVSDecoder(st)) != null) && vsd.isDefault()) {
		log.debug("item is default " + st);
		profileComboBox.setSelectedItem(st);
		default_set = true;
		profile_selected = true;
		// Imitate a button click.  Don't care the contents of the ActionEvent - the called
		// function doesn't use the contents.
		profileComboBoxActionPerformed(new ActionEvent(profileComboBox, 0, null));
	    }

	} 
	if ((!default_set) && (!profile_selected))  {
	    profileComboBox.insertItemAt("Choose a Profile", 0);
	    profileComboBox.setSelectedIndex(0);
	}

	if (profileComboBox.getItemCount() > 0) {
	    profileComboBox.setEnabled(true);
	    // Enable the roster save button if roster items are available.
	    if (rosterComboBox.getItemCount() > 0)
		rosterSaveButton.setEnabled(true);
	}
	
	revalidate();
	repaint();
    }

    @Override
    public void initComponents() {

	// Connect to the VSDecoderManager, so we know when the Profile list changes.
	VSDecoderManager.instance().addEventListener(new VSDManagerListener() {
		public void eventAction(VSDManagerEvent e) {
		    log.warn("Caught Event from VSDManager");
		    if (e.getType() == VSDecoderManager.EventType.DECODER_LIST_CHANGE) {
			log.warn("It's a DECODER_LIST_CHANGE");
			handleDecoderListChange();
		    }
		}
	    });

	// Build the GUI.
	setLayout(new BorderLayout(10, 10));
	
	rosterPanel = new JPanel();
	rosterPanel.setLayout(new BoxLayout(rosterPanel, BoxLayout.LINE_AXIS));
	this.add(rosterPanel, BorderLayout.PAGE_START);
	profilePanel = new JPanel();
	profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.LINE_AXIS));
	this.add(profilePanel, BorderLayout.CENTER);
        addressPanel = new JPanel();
        addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.LINE_AXIS));
	this.add(addressPanel, BorderLayout.PAGE_END);

        //rosterComboBox = new javax.swing.JComboBox();
	rosterComboBox = Roster.instance().fullRosterComboBox();
	rosterComboBox.insertItemAt(new NullComboBoxItem(), 0);
	rosterComboBox.setSelectedIndex(0);
	rosterComboBox.setToolTipText("tool tip for roster box");
	rosterComboBox.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    rosterItemSelected();
		}
	    });
        rosterPanel.add(rosterComboBox);
        rosterLabel = new javax.swing.JLabel();
        rosterLabel.setText("Roster");
        rosterPanel.add(rosterLabel);

	rosterSaveButton = new javax.swing.JButton();
	rosterSaveButton.setText("Save");
	rosterSaveButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    rosterSaveButtonAction(e);
		}
	    });
	rosterSaveButton.setEnabled(false); // temporarily disable this until we update the RosterEntry
	rosterSaveButton.setToolTipText(vsdecoderBundle.getString("RosterSaveButtonToolTip"));
	rosterPanel.add(rosterSaveButton);

        addressTextBox = new javax.swing.JTextField(5);
        addressLabel = new javax.swing.JLabel();
	addressSetButton = new javax.swing.JButton();
	profileComboBox = new javax.swing.JComboBox();
	profileLabel = new javax.swing.JLabel();
	


        profileComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Profiles Loaded"}));
	profile_selected = false;
	profileComboBox.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    profileComboBoxActionPerformed(evt);
		}
	    });
	profileComboBox.setEnabled(false);
        profilePanel.add(profileComboBox);

        profileLabel.setText("Sound Profile");
        profilePanel.add(profileLabel);

        addressTextBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressBoxActionPerformed(evt);
            }
        });
	addressTextBox.setMaximumSize(addressTextBox.getPreferredSize());
        addressPanel.add(addressTextBox);

        addressLabel.setText("Address");
	addressLabel.setMaximumSize(addressLabel.getPreferredSize());
        addressPanel.add(addressLabel);

	addressPanel.add(addressSetButton);
	addressSetButton.setText("Set");
	addressSetButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    addressSetButtonActionPerformed(evt);
		}
	    });
	addressTextBox.setEnabled(false);
	addressSetButton.setEnabled(false);
	addressSetButton.setToolTipText("AddressSetButtonToolTip");

    }

    static class NullComboBoxItem {
        @Override
	public String toString() {
	    //return rb.getString("NoLocoSelected");
	    return("NoLocoSelected");
	}
    }

    /**
     * Set the RosterEntry for this decoder.
     */
    public void setRosterEntry(RosterEntry entry){
	String vsd_path;
	String vsd_profile;

	rosterComboBox.setSelectedItem(entry);
	//addrSelector.setAddress(entry.getDccLocoAddress());
	rosterEntry = entry;
	// Check to see if we have to load a new VSD File
	vsd_path = rosterEntry.getAttribute("VSDecoder_Path");
	vsd_profile = rosterEntry.getAttribute("VSDecoder_Profile");
	if ((vsd_path != null) && (vsd_profile != null)) {
	    // VSD Attributes stored in the roster entry.
	    // Try to see if the profile is already loaded
	    VSDecoder v = VSDecoderManager.instance().getVSDecoder(vsd_profile);
	    if (v == null) {
		// Profile not loaded.  Load from file
		LoadVSDFileAction.loadVSDFile(vsd_path);
		// Try again with the profile
		v = VSDecoderManager.instance().getVSDecoder(vsd_profile);
	    } else {
		// nothing to do.  We found the profile already loaded.
		// note that if you have two VSDfiles with the same profile,
		// this might not pick the right one.
	    }
	    // Note: an else doesn't apply here.  This needs to be run 
	    // regardless of the if above.
	    if (v != null) {
		log.debug("VSDecoder loaded from file: " + v.getProfileName());
		decoder = v;
		profileComboBox.setSelectedItem(v.getProfileName());
		profile_selected = true;
		// Imitate a button click.  Don't care the contents of the ActionEvent - the called
		// function doesn't use the contents.
		profileComboBoxActionPerformed(new ActionEvent(profileComboBox, 0, null));
	    }
	}

	//changeOfAddress();
	if (decoder != null) {
	    decoder.setAddress(entry.getDccLocoAddress());
	    decoder.enable();
	    this.getMainPane().setTitle(decoder.getAddress());
	    addressTextBox.setText(""+entry.getDccLocoAddress().getNumber());
	} 

    }

    // using Roster combo box
    private void rosterItemSelected() {
	if (!(rosterComboBox.getSelectedItem() instanceof NullComboBoxItem)) {
	    log.debug("Roster Item Selected...");
	    String rosterEntryTitle = rosterComboBox.getSelectedItem().toString();
	    setRosterEntry(Roster.instance().entryFromTitle(rosterEntryTitle));
	}
    }

    private void rosterSaveButtonAction(ActionEvent e) {
	log.debug("rosterSaveButton pressed");
	String rosterEntryTitle = rosterComboBox.getSelectedItem().toString();
	RosterEntry r = Roster.instance().entryFromTitle(rosterEntryTitle);
	if (r != null) {
	    r.putAttribute("VSDecoder_Path", decoder.getVSDFilePath());
	    r.putAttribute("VSDecoder_Profile", profileComboBox.getSelectedItem().toString());
	}
    }

    // Note - this gets called directly from setProfileList() when a default profile
    // is loaded from a file.  The contents of the evt parameter in that call are junk.
    // If you change this function to look at the contents of evt, better revise
    // setProfileList() to send useful data.
    private void profileComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
	int addr;

	if (!profile_selected)
	    if (profileComboBox.getSelectedIndex() == 0) {
		// User has selected the "Choose" entry, which doesn't exist
		return;
	    } else {
		// User has chosen a profile for the first time.
		profileComboBox.removeItemAt(0);  // remove the "choose" thing.
		profile_selected = true;
	}

	log.debug("Profile selected. New = " + profileComboBox.getSelectedItem() + "Decoder = " + decoder);
	if (decoder != null) {
	    decoder.disable();  // disable the previous decoder
	}
	log.debug("Getting selected decoder from VSDecoderManager.");
	decoder = decoder_mgr.getVSDecoder(profileComboBox.getSelectedItem().toString(), false);
	log.debug("Decoder = " + decoder);
	if (decoder != null) {
	    log.debug("Decoder name: " + decoder.getProfileName());
	    try {
		addr = Integer.parseInt(addressTextBox.getText());
		decoder.setAddress(addr, true);
		decoder.enable();
	    } catch(NumberFormatException e) {
	    // Address not set.  Do nothing. 
	    } finally {
		// Either way, set the sounds pane.
		if (decoder != null)
		    this.main_pane.setDecoder(decoder);
		addressSetButton.setEnabled(true);
		addressTextBox.setEnabled(true);
	    }
	    // Do something.
	} else {
	    log.warn("NULL POINTER returned from VSDecoderManager.");
	}
    }

    private void addressBoxActionPerformed(java.awt.event.ActionEvent evt) {
        String address_text = addressTextBox.getText();
        decoder.setAddress(Integer.parseInt(address_text), true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void addressSetButtonActionPerformed(java.awt.event.ActionEvent evt) {
	String address_text = addressTextBox.getText();
	if (decoder != null) {
	    try {
		decoder.setAddress(Integer.parseInt(address_text), true);
		decoder.enable();
		this.getMainPane().setTitle(decoder.getAddress());
	    } catch(NumberFormatException e) {
		// Un-set or invalid address value.  Do nothing
	    }
	} 
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDConfigPanel.class.getName());
    
}