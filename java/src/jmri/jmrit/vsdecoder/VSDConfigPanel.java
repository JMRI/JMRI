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
import jmri.DccLocoAddress;

public class VSDConfigPanel extends JmriPanel {

    private static final ResourceBundle vsdecoderBundle = VSDecoderBundle.bundle();

    // Local References
    VSDecoderManager decoder_mgr; // local reference to the VSDecoderManager instance
    VSDecoderPane main_pane;      // local reference to the parent VSDecoderPane
    private RosterEntry rosterEntry; // local reference to the selected RosterEntry

    // GUI Elements
    private javax.swing.JLabel addressLabel;
    private javax.swing.JTextField addressTextBox;
    private javax.swing.JButton addressSetButton;
    private javax.swing.JComboBox rosterComboBox;
    private javax.swing.JLabel rosterLabel;
    private javax.swing.JButton rosterSaveButton;
    private javax.swing.JComboBox profileComboBox;
    private javax.swing.JLabel profileLabel;

    // Panels for the tabbed pane.
    private javax.swing.JPanel rosterPanel;
    private javax.swing.JPanel profilePanel;
    private javax.swing.JPanel addressPanel;

    // Local variables
    private boolean profile_selected;  // true if a user has selected a Profile
    private NullComboBoxItem loadProfilePrompt; // dummy profileComboBox entry

    // CONSTRUCTORS

    public VSDConfigPanel() {
	this(null, null);
    }

    public VSDConfigPanel(String dec, VSDecoderPane dad) {
	super();
	main_pane = dad;
	profile_selected = false;
	decoder_mgr = VSDecoderManager.instance();
	initComponents();
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



    // Read the addressTextBox and update the VSDecoder's address.
    protected void updateAddress() {
	// Simulates the clicking of the address Set button
	VSDecoder dec = main_pane.getDecoder();
	try {
	    int addr = Integer.parseInt(addressTextBox.getText());
	    main_pane.firePropertyChange(VSDecoderPane.PropertyChangeID.ADDRESS_CHANGE,
			       dec.getAddress(), new DccLocoAddress(addr, true));
	} catch(NumberFormatException e) {
	    // Address box does not contain an integer... do nothing.
	}
    }

    // GUI EVENT HANDLING METHODS

    // Respond to a change in the VSDecoderManager's profile list.
    public void handleDecoderListChange() {
	log.warn("Handling the decoder list change");
	ArrayList<String> sl = VSDecoderManager.instance().getVSDProfileNames();
	this.setProfileList(sl);
    }

    // Perform the actual work of changing the profileComboBox's contents
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
	    // A decoder is already selected, and is the default decoder...
	    if (((vsd = main_pane.getDecoder()) != null) && vsd.isDefault()) {
		log.debug("decoder " + st + " : " + vsd);
		log.debug("item is default " + st);
		profileComboBox.setSelectedItem(st);
		profileComboBox.removeItem(loadProfilePrompt);
		default_set = true;
		profile_selected = true;
		// Imitate a button click.  Don't care the contents of the ActionEvent - the called
		// function doesn't use the contents.
		
		updateAddress();
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
		    if (e.getType() == VSDecoderManager.EventType.DECODER_LIST_CHANGE) {
			log.debug("Received Decoder List Change Event");
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
	


        profileComboBox.setModel(new javax.swing.DefaultComboBoxModel());
	profileComboBox.addItem((loadProfilePrompt = new NullComboBoxItem()));
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
	VSDecoder dec;

	// Update the roster entry local var.
	rosterEntry = entry;

	// Get VSD info from Roster.
	vsd_path = rosterEntry.getAttribute("VSDecoder_Path");
	vsd_profile = rosterEntry.getAttribute("VSDecoder_Profile");

	log.debug("Roster entry: profile = " + vsd_profile + " path = " + vsd_path);

	// If the roster entry has VSD info stored, load it.
	if ((vsd_path != null) && (vsd_profile != null)) {
	    // Load the indicated VSDecoder Profile and update the Profile combo box
	    dec = VSDecoderManager.instance().getVSDecoder(vsd_profile, vsd_path);
	    if (dec != null) {
		log.debug("VSDecoder loaded from file: " + dec.getProfileName());
		main_pane.setDecoder(dec);
		profileComboBox.setSelectedItem(dec.getProfileName());
		profileComboBox.setEnabled(true);
		profile_selected = true;
	    }
	}

	// Update the roster combo box
	//rosterComboBox.setSelectedItem(entry);
	//addrSelector.setAddress(entry.getDccLocoAddress());

	// Set the Address box from the Roster entry
	main_pane.setAddress(entry.getDccLocoAddress());
	addressTextBox.setText(""+entry.getDccLocoAddress().getNumber());
	addressTextBox.setEnabled(true);
	addressSetButton.setEnabled(true);
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
	    r.putAttribute("VSDecoder_Path", main_pane.getDecoder().getVSDFilePath());
	    r.putAttribute("VSDecoder_Profile", profileComboBox.getSelectedItem().toString());
	}
    }

    // User chose a Profile from the profileComboBox.
    private void profileComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
	int addr;
	VSDecoder dec;

	// If the user selected an item, remove the load prompt item
	if (!profile_selected)
	    if (profileComboBox.getSelectedItem() == loadProfilePrompt) {
		// User has selected the "Choose" entry, which doesn't exist
		return;
	    } else {
		// User has chosen a profile for the first time.
		profileComboBox.removeItem(loadProfilePrompt);  // remove the "choose" thing.
		profile_selected = true;
	}

	dec = main_pane.getDecoder();
	log.debug("Profile selected. New = " + profileComboBox.getSelectedItem() + "Decoder = " + dec);
	if (dec != null) {
	    dec.disable();  // disable the previous decoder
	}
	log.debug("Getting selected decoder from VSDecoderManager.");
	dec = main_pane.getDecoder(profileComboBox.getSelectedItem().toString());
	main_pane.setDecoder(dec);
	log.debug("Decoder = " + dec);
	if (dec != null) {
	    log.debug("Decoder name: " + dec.getProfileName());
	    updateAddress();
	    // Either way, enable the address text box and set button.
	    addressSetButton.setEnabled(true);
	    addressTextBox.setEnabled(true);
	    // Do something.
	} else {
	    log.warn("NULL POINTER returned from VSDecoderManager.");
	}
    }

    private void addressBoxActionPerformed(java.awt.event.ActionEvent evt) {
	// Don't do anything just yet...
    }//GEN-LAST:event_jButton1ActionPerformed

    private void addressSetButtonActionPerformed(java.awt.event.ActionEvent evt) {
	VSDecoder dec;
	String address_text = addressTextBox.getText();
	updateAddress();
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDConfigPanel.class.getName());
    
}