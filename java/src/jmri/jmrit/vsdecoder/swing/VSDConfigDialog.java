package jmri.jmrit.vsdecoder.swing;

/** class VSDConfigDialog
 * 
 * Configuration dialog for setting up a new VSDecoder
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

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.text.MessageFormat;

import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.jmrit.vsdecoder.*;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.IndexedCvTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.jmrit.XmlFile;


public class VSDConfigDialog extends JDialog {

    private static final ResourceBundle rb = VSDecoderBundle.bundle();

    public static final String CONFIG_PROPERTY = "Config";

    // GUI Elements
    private javax.swing.JLabel addressLabel;
    private javax.swing.JButton addressSetButton;
    private DccLocoAddressSelector addressSelector;
    private RosterEntrySelectorPanel rosterSelector;
    private javax.swing.JLabel rosterLabel;
    private javax.swing.JButton rosterSaveButton;
    private javax.swing.JComboBox profileComboBox;
    private javax.swing.JButton profileLoadButton;
    private javax.swing.JLabel profileLabel;
    private javax.swing.JPanel rosterPanel;
    private javax.swing.JPanel profilePanel;
    private javax.swing.JPanel addressPanel;
    private javax.swing.JTabbedPane locoSelectPanel;
    private javax.swing.JButton closeButton;

    private NullProfileBoxItem loadProfilePrompt; // dummy profileComboBox entry
    private VSDConfig config; // local reference to the config being constructed by this dialog
    private RosterEntry rosterEntry; // local reference to the selected RosterEntry

    /** Constructor
     *
     * @param JPanel parent : parent panel
     * @param String title  : title for the dialog
     * @param VSDConfig c   : Config object to be set by the dialog
     */
    public VSDConfigDialog(JPanel parent, String title, VSDConfig c) {
	super(SwingUtilities.getWindowAncestor(parent), title);
	config = c;
	VSDecoderManager.instance().addEventListener(new VSDManagerListener() {
		public void eventAction(VSDManagerEvent evt) {
		    vsdecoderManagerEventAction(evt);
		}
	    });
	initComponents();
    }

    /** Init the GUI components */
    protected void initComponents() {
	//setLayout(new BorderLayout(10, 10));
	this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
	
	// Tabbed pane for loco select (Roster or Manual)
	locoSelectPanel = new JTabbedPane();
	TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Select Loco");
	title.setTitlePosition(TitledBorder.DEFAULT_POSITION);
	locoSelectPanel.setBorder(title);

	// Roster Tab and Address Tab
	rosterPanel = new JPanel();
	rosterPanel.setLayout(new BoxLayout(rosterPanel, BoxLayout.LINE_AXIS));
        addressPanel = new JPanel();
        addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.LINE_AXIS));
	locoSelectPanel.addTab("Roster", rosterPanel);
	locoSelectPanel.addTab("Manual", addressPanel);

	// Roster Tab components
	rosterSelector = new RosterEntrySelectorPanel();
	rosterSelector.setNonSelectedItem(rb.getString("EmptyRosterBox"));
	//rosterComboBox.setToolTipText("tool tip for roster box");
	rosterSelector.addPropertyChangeListener("selectedRosterEntries", new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                rosterItemSelectAction(null);
            }
        });
        rosterPanel.add(rosterSelector);
        rosterLabel = new javax.swing.JLabel();
	rosterLabel.setText(rb.getString("RosterLabel"));

	// Address Tab Components
        addressLabel = new javax.swing.JLabel();
	addressSelector = new DccLocoAddressSelector();
	addressSetButton = new javax.swing.JButton();
	addressSetButton.setText(rb.getString("AddressSetButtonLabel"));
	addressSetButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    addressSetButtonActionPerformed(evt);
		}
	    });
	addressSetButton.setEnabled(false);
	addressSetButton.setToolTipText(rb.getString("AddressSetButtonToolTip"));

	addressPanel.add(addressSelector.getCombinedJPanel());
	addressPanel.add(addressSetButton);
	addressPanel.add(addressLabel);

	// Profile select Pane
	profilePanel = new JPanel();
	profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.PAGE_AXIS));
	profileComboBox = new javax.swing.JComboBox();
	profileLabel = new javax.swing.JLabel();
	profileLoadButton = new JButton(rb.getString("LoadVSDFileButtonLabel"));
	profileLoadButton.setEnabled(false);
	
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
	profileComboBox.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    profileComboBoxActionPerformed(evt);
		}
	    });
        profilePanel.add(profileComboBox);
	profilePanel.add(profileLoadButton);
	profileLoadButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    profileLoadButtonActionPerformed(evt);
		}
	    });

        profileLabel.setText(rb.getString("SoundProfileLabel"));

	rosterSaveButton = new javax.swing.JButton();
	rosterSaveButton.setText(rb.getString("ConfigSaveButtonLabel"));
	rosterSaveButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    rosterSaveButtonAction(e);
		}
	    });
	rosterSaveButton.setEnabled(false); // temporarily disable this until we update the RosterEntry
	rosterSaveButton.setToolTipText(rb.getString("RosterSaveButtonToolTip"));


	JPanel cbPanel = new JPanel();
	closeButton = new JButton(rb.getString("CloseButtonLabel"));
	closeButton.setEnabled(false);
	closeButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    closeButtonActionPerformed(e);
		}
	    });

	JButton cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    cancelButtonActionPerformed(evt);
		}
	    });
	cbPanel.add(cancelButton);
	cbPanel.add(closeButton);


	this.add(locoSelectPanel);
	this.add(profilePanel);
	this.add(rosterSaveButton);
	this.add(cbPanel);
	this.pack();
	this.setVisible(true);

    }

    public void cancelButtonActionPerformed(java.awt.event.ActionEvent ae) {
	dispose();
    }

    /** Handle the "Close" (or "OK") button action */
    public void closeButtonActionPerformed(java.awt.event.ActionEvent ae) {
	if (profileComboBox.getSelectedItem() != null)
	    config.setProfileName(profileComboBox.getSelectedItem().toString());
	else
	    config.setProfileName("");
	config.setLocoAddress(addressSelector.getAddress());
	if (rosterSelector.getSelectedRosterEntries().length > 0)
	    config.setRosterEntry(rosterSelector.getSelectedRosterEntries()[0]);
	else
	    config.setRosterEntry(null);

	firePropertyChange(CONFIG_PROPERTY, null, config);
	dispose();
    }
    

    // class NullComboBoxItem
    //
    // little object to insert into profileComboBox when it's empty
    static class NullProfileBoxItem {
        @Override
	public String toString() {
	    return(rb.getString("NoLocoSelectedText"));
	}
    }

    private void enableProfileStuff(Boolean t) {
	closeButton.setEnabled(t);
	profileComboBox.setEnabled(t);
	profileLoadButton.setEnabled(t);
	rosterSaveButton.setEnabled(t);
    }

    /** rosterItemSelectAction()
     *
     * ActionEventListener function for rosterSelector
     * Chooses a RosterEntry from the list and loads its relevant info.
     */
    private void rosterItemSelectAction(ActionEvent e) {
        if (rosterSelector.getSelectedRosterEntries().length != 0) {
            log.debug("Roster Entry selected...");
            setRosterEntry(rosterSelector.getSelectedRosterEntries()[0]);
	    enableProfileStuff(true);
        }
    }

    /** rosterSaveButtonAction()
     *
     * ActionEventListener method for rosterSaveButton
     * Writes VSDecoder info to the RosterEntry.
     */
    private void rosterSaveButtonAction(ActionEvent e) {
        log.debug("rosterSaveButton pressed");
        if (rosterSelector.getSelectedRosterEntries().length != 0) {
            RosterEntry r = rosterSelector.getSelectedRosterEntries()[0];
	    String profile = profileComboBox.getSelectedItem().toString();
	    String path = VSDecoderManager.instance().getProfilePath(profile);
	    if ((path == null) || (profile == null)) {
		log.debug("Path and/or Profile not selected.  Ignore Save button press.");
		return;
	    } else {
		r.setOpen(true);
		r.putAttribute("VSDecoder_Path", path);
		r.putAttribute("VSDecoder_Profile",profile);
		int value = JOptionPane.showConfirmDialog(null,
							  MessageFormat.format(rb.getString("UpdateRoster"),
									       new Object[] { r.titleString() }), 
							  rb.getString("SaveRoster?"), JOptionPane.YES_NO_OPTION);
		if (value == JOptionPane.YES_OPTION) {
		    storeFile(r);
		}
		r.setOpen(false);
	    }

            // Need to write RosterEntry to file.
        }
    }

    private void profileComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void profileLoadButtonActionPerformed(java.awt.event.ActionEvent evt) {
	LoadVSDFileAction vfa = new LoadVSDFileAction();
	vfa.actionPerformed(evt);
	// Note: This will trigger a PROFILE_LIST_CHANGE event from VSDecoderManager
    }

    /** handle the address "Set" button. */
    private void addressSetButtonActionPerformed(java.awt.event.ActionEvent evt) {
    }

    /** handle profile list changes from the VSDecoderManager */
    @SuppressWarnings("unchecked")
    private void vsdecoderManagerEventAction(VSDManagerEvent evt) {
	if (evt.getType() == VSDManagerEvent.EventType.PROFILE_LIST_CHANGE) {
	    log.debug("Received Profile List Change Event");
	    updateProfileList((ArrayList<String>)evt.getData());
	}
    }

    /** Update the profile combo box */
    private void updateProfileList(ArrayList<String> s) {
	// There's got to be a more efficient way to do this.
	// Most of this is about merging the new array list with
	// the entries already in the ComboBox.

	if (s == null)
	    return;

	// This is a bit tedious...
	// Pull all of the existing names from the Profile ComboBox
	ArrayList<String> ce_list = new ArrayList<String>();
	for (int i = 0; i < profileComboBox.getItemCount(); i++) {
	    ce_list.add(profileComboBox.getItemAt(i).toString());
	}

	// Cycle through the list provided as "s" and add only
	// those profiles that aren't already there.
	Iterator<String> itr = s.iterator();
	while (itr.hasNext()) {
	    String st = itr.next();
	    if (!ce_list.contains(st)) {
		log.debug("added item " + st);
		profileComboBox.addItem(st);
	    }
	}

	// If the combo box isn't empty, enable it and enable the
	// Roster Save button.
	if (profileComboBox.getItemCount() > 0) {
	    profileComboBox.setEnabled(true);
	    // Enable the roster save button if roster items are available.
	    if (rosterSelector.getSelectedRosterEntries().length > 0) {
		RosterEntry r = rosterSelector.getSelectedRosterEntries()[0];
		String profile = r.getAttribute("VSDecoder_Profile");
		log.debug("Trying to set the ProfileComboBox to this Profile: " + profile);
		if (profile != null) {
		    profileComboBox.setSelectedItem(profile);
		}
		rosterSaveButton.setEnabled(true);
	    }
	}
    }

    /** setRosterEntry()
     *
     * Respond to the user choosing an entry from the rosterSelector
     */
    public void setRosterEntry(RosterEntry entry){
	String vsd_path;
	String vsd_profile;

	// Update the roster entry local var.
	rosterEntry = entry;

	// Get VSD info from Roster.
	vsd_path = rosterEntry.getAttribute("VSDecoder_Path");
	vsd_profile = rosterEntry.getAttribute("VSDecoder_Profile");

	log.debug("Roster entry: profile = " + vsd_profile + " path = " + vsd_path);

	// If the roster entry has VSD info stored, load it.
	if ((vsd_path != null) && (vsd_profile != null)) {
	    // Load the indicated VSDecoder Profile and update the Profile combo box
	    // This will trigger a PROFILE_LIST_CHANGE event from the VSDecoderManager.
	    VSDecoderManager.instance().loadProfiles(vsd_path);
	}

	// Set the Address box from the Roster entry
	// Do this after the VSDecoder create, so it will see the change.
	addressSelector.setAddress(entry.getDccLocoAddress());
	addressSelector.setEnabled(true);
	addressSetButton.setEnabled(true);
    }

    /** Write roster settings to the Roster file */
    protected boolean storeFile(RosterEntry _rosterEntry) {
        log.debug("storeFile starts");
	// We need to create a programmer, a cvTableModel, an iCvTableModel, and a variableTableModel.
	// Doesn't matter which, so we'll use the LocoNet programmer.
	Programmer p = InstanceManager.programmerManagerInstance().getGlobalProgrammer();
	CvTableModel cvModel = new CvTableModel(null, p);
	IndexedCvTableModel iCvModel = new IndexedCvTableModel(null, p);
	VariableTableModel variableModel = new VariableTableModel(null, new String[]  {"Name", "Value"}, cvModel, iCvModel);
	
	// Now, in theory we can call _rosterEntry.writeFile...
        if (_rosterEntry.getFileName() != null) {
            // set the loco file name in the roster entry
            _rosterEntry.readFile();  // read, but don't yet process
	    _rosterEntry.loadCvModel(cvModel, iCvModel);
        }

        // id has to be set!
        if (_rosterEntry.getId().equals("")) {
            log.debug("storeFile without a filename; issued dialog");
            return false;
        }
        
        // if there isn't a filename, store using the id
        _rosterEntry.ensureFilenameExists();

        // create the RosterEntry to its file
        _rosterEntry.writeFile(cvModel, iCvModel, variableModel ); // where to get the models???

        // mark this as a success
        variableModel.setFileDirty(false);

        // and store an updated roster file
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        Roster.writeRosterFile();

        return true;
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDConfigDialog.class.getName());
    
}