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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.swing.JmriPanel;
import javax.swing.SwingWorker;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.LocoAddress;

@SuppressWarnings("serial")
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
    private DccLocoAddressSelector addressSelector;
    private RosterEntrySelectorPanel rosterSelector;
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
    private NullProfileBoxItem loadProfilePrompt; // dummy profileComboBox entry

    private BusyDialog busy_dialog;

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

    // updateAddress()
    //
    // Read the addressTextBox and broadcast the new address to
    // any listeners.
    @SuppressWarnings("cast")
    protected void updateAddress() {
	// Simulates the clicking of the address Set button
	VSDecoder dec = main_pane.getDecoder();
	/*
	try {
	    int addr = Integer.parseInt(addressTextBox.getText());
	    main_pane.firePropertyChange(VSDecoderPane.PropertyChangeID.ADDRESS_CHANGE,
			       dec.getAddress(), new DccLocoAddress(addr, true));
	    int addr = Integer.parseInt(addressTextBox.getText());
	    main_pane.firePropertyChange(VSDecoderPane.PropertyChangeID.ADDRESS_CHANGE,
			       dec.getAddress(), new DccLocoAddress(addr, true));
	} catch(NumberFormatException e) {
	    // Address box does not contain an integer... do nothing.
	}
	*/
	if (addressSelector.getAddress() != null) {
	    main_pane.firePropertyChange(VSDecoderPane.PropertyChangeID.ADDRESS_CHANGE,
					 dec.getAddress(), addressSelector.getAddress());
	}
    }

    // GUI EVENT HANDLING METHODS

    // handleDecoderListChange()
    //
    // Respond to a change in the VSDecoderManager's profile list.
    // Event listener on VSDecoderManager's profile list.
    @SuppressWarnings("unchecked")
    public void handleDecoderListChange(VSDManagerEvent e) {
	log.warn("Handling the decoder list change");
	//ArrayList<String> sl = (ArrayList<String>)e.getData();
	//this.updateProfileList(sl);
	this.updateProfileList((ArrayList<String>) e.getData());
    }

    public void updateProfileList(ArrayList<String> s) {

	if (s == null)
	    return;

	// This is a bit tedious...
	ArrayList<String> ce_list = new ArrayList<String>();
	for (int i = 0; i < profileComboBox.getItemCount(); i++) {
	    ce_list.add(profileComboBox.getItemAt(i).toString());
	}

	Iterator<String> itr = s.iterator();
	while (itr.hasNext()) {
	    String st = itr.next();
	    if (!ce_list.contains(st)) {
		log.debug("added item " + st);
		profileComboBox.addItem(st);
	    }
	}
	//profileComboBox.removeItem(loadProfilePrompt);

	if (profileComboBox.getItemCount() > 0) {
	    profileComboBox.setEnabled(true);
	    // Enable the roster save button if roster items are available.
	    if (rosterSelector.getSelectedRosterEntries().length > 0)
		rosterSaveButton.setEnabled(true);
	}
	
	revalidate();
	repaint();

    }

    // setProfileList()
    //
    // Perform the actual work of changing the profileComboBox's contents
    @SuppressWarnings("cast")
    public void setProfileList(ArrayList<String> s) {
	VSDecoder vsd;
	boolean default_set = false;

	log.warn("updating the profile list.");
	
	profileComboBox.setModel(new DefaultComboBoxModel());
	Iterator<String> itr = s.iterator();
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
		
		updateAddress();  // why this? Who's listening?  Profile select doesn't change the address...
	    }

	} 
	if ((!default_set) && (!profile_selected))  {
	    profileComboBox.insertItemAt("Choose a Profile", 0);
	    profileComboBox.setSelectedIndex(0);
	}

	if (profileComboBox.getItemCount() > 0) {
	    profileComboBox.setEnabled(true);
	    // Enable the roster save button if roster items are available.
	    if (rosterSelector.getSelectedRosterEntries().length > 0)
		rosterSaveButton.setEnabled(true);
	}

	revalidate();
	repaint();
    }

    // initComponents()
    //
    // Build the GUI components and initialize them.
    @Override
    public void initComponents() {

	// Connect to the VSDecoderManager, so we know when the Profile list changes.
	VSDecoderManager.instance().addEventListener(new VSDManagerListener() {
		public void eventAction(VSDManagerEvent e) {
		    if (e.getType() == VSDecoderManager.EventType.DECODER_LIST_CHANGE) {
			log.debug("Received Decoder List Change Event");
			handleDecoderListChange(e);
		    }
		}
	    });

	// Build the GUI.
	setLayout(new BorderLayout(10, 10));
	
	rosterPanel = new JPanel();
	rosterPanel.setLayout(new BoxLayout(rosterPanel, BoxLayout.LINE_AXIS));
	profilePanel = new JPanel();
	profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.LINE_AXIS));
        addressPanel = new JPanel();
        addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.LINE_AXIS));
	this.add(profilePanel, BorderLayout.PAGE_START);
	this.add(addressPanel, BorderLayout.CENTER);
	this.add(rosterPanel, BorderLayout.PAGE_END);

	rosterSelector = new RosterEntrySelectorPanel();
	rosterSelector.setNonSelectedItem("No Loco Selected");
	//rosterComboBox.setToolTipText("tool tip for roster box");
    rosterSelector.addPropertyChangeListener("selectedRosterEntries", new PropertyChangeListener(){

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                rosterItemSelectAction(null);
            }
        });
        rosterPanel.add(rosterSelector);
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
	addressSelector = new DccLocoAddressSelector();

	profileComboBox = new javax.swing.JComboBox();
	profileLabel = new javax.swing.JLabel();
	


        profileComboBox.setModel(new javax.swing.DefaultComboBoxModel());
	// Add any already-loaded profile names
	ArrayList<String> sl = VSDecoderManager.instance().getVSDProfileNames();
	if (sl.isEmpty())
	    profileComboBox.setEnabled(false);
	else
	    profileComboBox.setEnabled(true);
	updateProfileList(sl);
	profileComboBox.addItem((loadProfilePrompt = new NullProfileBoxItem()));
	profileComboBox.setSelectedItem(loadProfilePrompt);
	profile_selected = false;
	profileComboBox.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    profileComboBoxActionPerformed(evt);
		}
	    });
        profilePanel.add(profileComboBox);

        profileLabel.setText("Sound Profile");
        profilePanel.add(profileLabel);

        addressTextBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressBoxActionPerformed(evt);
            }
        });
	addressTextBox.setMaximumSize(addressTextBox.getPreferredSize());
        //addressPanel.add(addressTextBox);

        addressLabel.setText("Address");
	addressLabel.setMaximumSize(addressLabel.getPreferredSize());
        //addressPanel.add(addressLabel);

	//addressPanel.add(addressSetButton);
	addressSetButton.setText("Set");
	addressSetButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    addressSetButtonActionPerformed(evt);
		}
	    });
	addressTextBox.setEnabled(false);
	addressSetButton.setEnabled(false);
	addressSetButton.setToolTipText("AddressSetButtonToolTip");
	addressPanel.add(addressSelector.getCombinedJPanel());
	addressPanel.add(addressSetButton);
	addressPanel.add(addressLabel);

    }

    // class NullComboBoxItem
    //
    // little object to insert into profileComboBox when it's empty
    static class NullProfileBoxItem {
        @Override
	public String toString() {
	    //return rb.getString("NoLocoSelected");
	    return("Select a profile");
	}
    }

    // setRosterEntry()
    //
    // Respond to the user choosing an entry from the rosterSelector
    public void setRosterEntry(RosterEntry entry){
	String vsd_path;
	String vsd_profile;
	VSDecoder dec;

	// Update the roster entry local var.
	rosterEntry = entry;

	// Set the Address box from the Roster entry
	main_pane.setAddress((LocoAddress)entry.getDccLocoAddress());
	addressSelector.setAddress(entry.getDccLocoAddress());
	addressSelector.setEnabled(true);

	//addressTextBox.setText(""+entry.getDccLocoAddress().getNumber());
	//addressTextBox.setEnabled(true);
	addressSetButton.setEnabled(true);

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
		ArrayList<String> sl = VSDecoderManager.instance().getVSDProfileNames();
		updateProfileList(sl);
		profileComboBox.setSelectedItem(dec.getProfileName());
		profileComboBox.setEnabled(true);
		profile_selected = true;
	    }
	}

    }

    // rosterItemSelectAction()
    //
    // ActionEventListener function for rosterSelector
    // Chooses a RosterEntry from the list and loads its relevant info.
    private void rosterItemSelectAction(ActionEvent e) {
        if (rosterSelector.getSelectedRosterEntries().length != 0) {
            log.debug("Roster Entry selected...");
            setRosterEntry(rosterSelector.getSelectedRosterEntries()[0]);
        }
    }
    
    // rosterSaveButtonAction()
    //
    // ActionEventListener method for rosterSaveButton
    // Writes VSDecoder info to the RosterEntry.
    private void rosterSaveButtonAction(ActionEvent e) {
        log.debug("rosterSaveButton pressed");
        if (rosterSelector.getSelectedRosterEntries().length != 0) {
            RosterEntry r = rosterSelector.getSelectedRosterEntries()[0];
            r.putAttribute("VSDecoder_Path", main_pane.getDecoder().getVSDFilePath());
            r.putAttribute("VSDecoder_Profile", profileComboBox.getSelectedItem().toString());

            // Need to write RosterEntry to file.
        }
    }

    // profileComboBoxActionPerformed()
    //
    // User chose a Profile from the profileComboBox.
    private void profileComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
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
	
	// Get the existing decoder from the main pane...
	dec = main_pane.getDecoder();
	log.debug("Profile selected. New = " + profileComboBox.getSelectedItem() + "Decoder = " + dec);
	if (dec != null) {
	    dec.shutdown();
	    dec.disable();  // disable the previous decoder
	}
	log.debug("Getting selected decoder from VSDecoderManager.");

	// Setup the progress monitor, in case this takes a while
	//dec = main_pane.getDecoder(profileComboBox.getSelectedItem().toString());
	dec = getNewDecoder();
	main_pane.setDecoder(dec);
	//bd.kill();
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

    protected VSDecoder getNewDecoder() {
	VSDecoder rv;
	busy_dialog = new BusyDialog(this.main_pane.getFrame(), "Loading VSD Profile...", false);
	// This takes a little while... so we'll use a SwingWorker
	SwingWorker<VSDecoder, Object> sw = new SwingWorker<VSDecoder, Object>() {
	    @Override
	    public VSDecoder doInBackground() {
		return(main_pane.getDecoder(profileComboBox.getSelectedItem().toString()));
	    }
	    protected void done() {
		busy_dialog.finish();
	    }
	};
	sw.execute();
	busy_dialog.start();
	try {
	    rv = sw.get(); 
	} catch (Exception e) {
	    // Way too loose  Should be more specific about exceptions caught.
	    rv = null;
	}
	return(rv);
    }

    // addressBoxActionPerformed()
    //
    // ActionEventListener for addressSetButton
    // Does nothing.
    private void addressBoxActionPerformed(java.awt.event.ActionEvent evt) {
	// Don't do anything just yet...
	// Probably don't do anything ever...
    }//GEN-LAST:event_jButton1ActionPerformed

    // addressSetButtonActionPerformed()
    //
    // ActionEventListener for addressSetButton
    // User just pressed "set" on a new address.
    private void addressSetButtonActionPerformed(java.awt.event.ActionEvent evt) {
	updateAddress();
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDConfigPanel.class.getName());
    
}