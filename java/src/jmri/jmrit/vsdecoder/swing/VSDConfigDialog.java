package jmri.jmrit.vsdecoder.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.jmrit.vsdecoder.LoadVSDFileAction;
import jmri.jmrit.vsdecoder.VSDConfig;
import jmri.jmrit.vsdecoder.VSDManagerEvent;
import jmri.jmrit.vsdecoder.VSDManagerListener;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration dialog for setting up a new VSDecoder
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
 * @author   Mark Underwood Copyright (C) 2011
 */
public class VSDConfigDialog extends JDialog {

    public static final String CONFIG_PROPERTY = "Config";

    // Map of Mnemonic KeyEvent values to GUI Components
    private static final Map<String, Integer> Mnemonics = new HashMap<>();

    static {
        Mnemonics.put("RosterTab", KeyEvent.VK_R);
        Mnemonics.put("ManualTab", KeyEvent.VK_M);
        Mnemonics.put("AddressSet", KeyEvent.VK_T);
        Mnemonics.put("ProfileLoad", KeyEvent.VK_L);
        Mnemonics.put("RosterSave", KeyEvent.VK_S);
        Mnemonics.put("CloseButton", KeyEvent.VK_O);
        Mnemonics.put("CancelButton", KeyEvent.VK_C);
    }

    // GUI Elements
    private javax.swing.JLabel addressLabel;
    private javax.swing.JButton addressSetButton;
    private DccLocoAddressSelector addressSelector;
    private RosterEntrySelectorPanel rosterSelector;
    private javax.swing.JLabel rosterLabel;
    private javax.swing.JButton rosterSaveButton;
    private javax.swing.JComboBox<Object> profileComboBox;
    private javax.swing.JButton profileLoadButton;
    private javax.swing.JPanel rosterPanel;
    private javax.swing.JPanel profilePanel;
    private javax.swing.JPanel addressPanel;
    private javax.swing.JTabbedPane locoSelectPanel;
    private javax.swing.JButton closeButton;

    private NullProfileBoxItem loadProfilePrompt; // dummy profileComboBox entry
    private VSDConfig config; // local reference to the config being constructed by this dialog
    private RosterEntry rosterEntry; // local reference to the selected RosterEntry

    /**
     * Constructor
     *
     * @param parent Ancestor panel
     * @param title  title for the dialog
     * @param c      Config object to be set by the dialog
     */
    public VSDConfigDialog(JPanel parent, String title, VSDConfig c) {
        super(SwingUtilities.getWindowAncestor(parent), title);
        config = c;
        VSDecoderManager.instance().addEventListener(new VSDManagerListener() {
            @Override
            public void eventAction(VSDManagerEvent evt) {
                vsdecoderManagerEventAction(evt);
            }
        });
        initComponents();
    }

    /**
     * Init the GUI components
     */
    protected void initComponents() {
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));

        // Tabbed pane for loco select (Roster or Manual)
        locoSelectPanel = new JTabbedPane();
        TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(),
                Bundle.getMessage("LocoTabbedPaneTitle"));
        title.setTitlePosition(TitledBorder.DEFAULT_POSITION);
        locoSelectPanel.setBorder(title);

        // Roster Tab and Address Tab
        rosterPanel = new JPanel();
        rosterPanel.setLayout(new BoxLayout(rosterPanel, BoxLayout.LINE_AXIS));
        addressPanel = new JPanel();
        addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.LINE_AXIS));
        locoSelectPanel.addTab(Bundle.getMessage("RosterLabel"), rosterPanel); // tab name
        locoSelectPanel.addTab(Bundle.getMessage("LocoTabbedPaneManualTab"), addressPanel);
        //NOTE: There appears to be a bug in Swing that doesn't let Mnemonics work on a JTabbedPane when a sibling component
        // has the focus.  Oh well.
        try {
            locoSelectPanel.setToolTipTextAt(locoSelectPanel.indexOfTab(Bundle.getMessage("RosterLabel")), Bundle.getMessage("LTPRosterTabToolTip"));
            locoSelectPanel.setMnemonicAt(locoSelectPanel.indexOfTab(Bundle.getMessage("RosterLabel")), Mnemonics.get("RosterTab"));
            locoSelectPanel.setToolTipTextAt(locoSelectPanel.indexOfTab(Bundle.getMessage("LocoTabbedPaneManualTab")), Bundle.getMessage("LTPManualTabToolTip"));
            locoSelectPanel.setMnemonicAt(locoSelectPanel.indexOfTab(Bundle.getMessage("LocoTabbedPaneManualTab")), Mnemonics.get("ManualTab"));
        } catch (IndexOutOfBoundsException iobe) {
            log.debug("Index out of bounds setting up tabbed Pane: {}", iobe);
            // Ignore out-of-bounds exception.  We just won't have mnemonics or tool tips this go round
        }
        // Roster Tab components
        rosterSelector = new RosterEntrySelectorPanel();
        rosterSelector.setNonSelectedItem(Bundle.getMessage("EmptyRosterBox"));
        rosterSelector.setToolTipText(Bundle.getMessage("LTPRosterSelectorToolTip"));
        //rosterComboBox.setToolTipText("tool tip for roster box");
        rosterSelector.addPropertyChangeListener("selectedRosterEntries", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                rosterItemSelectAction(null);
            }
        });
        rosterPanel.add(rosterSelector);
        rosterLabel = new javax.swing.JLabel();
        rosterLabel.setText(Bundle.getMessage("RosterLabel"));

        // Address Tab Components
        addressLabel = new javax.swing.JLabel();
        addressSelector = new DccLocoAddressSelector();
        addressSelector.setToolTipText(Bundle.getMessage("LTPAddressSelectorToolTip", Bundle.getMessage("ButtonSet")));
        addressSetButton = new javax.swing.JButton();
        addressSetButton.setText(Bundle.getMessage("ButtonSet"));
        addressSetButton.setEnabled(true);
        addressSetButton.setToolTipText(Bundle.getMessage("AddressSetButtonToolTip"));
        addressSetButton.setMnemonic(Mnemonics.get("AddressSet"));
        addressSetButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressSetButtonActionPerformed(evt);
            }
        });

        addressPanel.add(addressSelector.getCombinedJPanel());
        addressPanel.add(addressSetButton);
        addressPanel.add(addressLabel);

        // Profile select Pane
        profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.PAGE_AXIS));
        profileComboBox = new javax.swing.JComboBox<>();
        profileComboBox.setToolTipText(Bundle.getMessage("ProfileComboBoxToolTip"));
        profileLoadButton = new JButton(Bundle.getMessage("VSDecoderFileMenuLoadVSDFile"));
        profileLoadButton.setToolTipText(Bundle.getMessage("ProfileLoadButtonToolTip"));
        profileLoadButton.setMnemonic(Mnemonics.get("ProfileLoad"));
        profileLoadButton.setEnabled(true);
        TitledBorder title2 = BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(),
                Bundle.getMessage("ProfileSelectorPaneTitle"));
        title.setTitlePosition(TitledBorder.DEFAULT_POSITION);
        profilePanel.setBorder(title2);

        profileComboBox.setModel(new javax.swing.DefaultComboBoxModel<>());
        // Add any already-loaded profile names
        ArrayList<String> sl = VSDecoderManager.instance().getVSDProfileNames();
        if (sl.isEmpty()) {
            profileComboBox.setEnabled(false);
        } else {
            profileComboBox.setEnabled(true);
        }
        updateProfileList(sl);
        profileComboBox.addItem((loadProfilePrompt = new NullProfileBoxItem()));
        profileComboBox.setSelectedItem(loadProfilePrompt);
        profileComboBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileComboBoxActionPerformed(evt);
            }
        });
        profilePanel.add(profileComboBox);
        profilePanel.add(profileLoadButton);
        profileLoadButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileLoadButtonActionPerformed(evt);
            }
        });

        rosterSaveButton = new javax.swing.JButton();
        rosterSaveButton.setText(Bundle.getMessage("ConfigSaveButtonLabel"));
        rosterSaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rosterSaveButtonAction(e);
            }
        });
        rosterSaveButton.setEnabled(false); // temporarily disable this until we update the RosterEntry
        rosterSaveButton.setToolTipText(Bundle.getMessage("RosterSaveButtonToolTip"));
        rosterSaveButton.setMnemonic(Mnemonics.get("RosterSave"));

        JPanel cbPanel = new JPanel();
        closeButton = new JButton(Bundle.getMessage("ButtonOK"));
        closeButton.setEnabled(false);
        closeButton.setToolTipText(Bundle.getMessage("CD_CloseButtonToolTip"));
        closeButton.setMnemonic(Mnemonics.get("CloseButton"));
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                closeButtonActionPerformed(e);
            }
        });

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.setToolTipText(Bundle.getMessage("CD_CancelButtonToolTip"));
        cancelButton.setMnemonic(Mnemonics.get("CancelButton"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        cbPanel.add(cancelButton);
        cbPanel.add(rosterSaveButton);
        cbPanel.add(closeButton);

        this.add(locoSelectPanel);
        this.add(profilePanel);
        //this.add(rosterSaveButton);
        this.add(cbPanel);
        this.pack();
        this.setVisible(true);

    }

    public void cancelButtonActionPerformed(java.awt.event.ActionEvent ae) {
        dispose();
    }

    /**
     * Handle the "Close" (or "OK") button action
     */
    public void closeButtonActionPerformed(java.awt.event.ActionEvent ae) {
        if (profileComboBox.getSelectedItem() != null) {
            config.setProfileName(profileComboBox.getSelectedItem().toString());
        } else {
            config.setProfileName("");
        }
        config.setLocoAddress(addressSelector.getAddress());
        if (rosterSelector.getSelectedRosterEntries().length > 0) {
            config.setRosterEntry(rosterSelector.getSelectedRosterEntries()[0]);
        } else {
            config.setRosterEntry(null);
        }

        firePropertyChange(CONFIG_PROPERTY, null, config);
        dispose();
    }

    // class NullComboBoxItem
    //
    // little object to insert into profileComboBox when it's empty
    static class NullProfileBoxItem {

        @Override
        public String toString() {
            return (Bundle.getMessage("NoLocoSelectedText"));
        }
    }

    private void enableProfileStuff(Boolean t) {
        closeButton.setEnabled(t);
        profileComboBox.setEnabled(t);
        profileLoadButton.setEnabled(t);
        rosterSaveButton.setEnabled(t);
    }

    /**
     * rosterItemSelectAction()
     *
     * ActionEventListener function for rosterSelector Chooses a RosterEntry
     * from the list and loads its relevant info.
     */
    private void rosterItemSelectAction(ActionEvent e) {
        if (rosterSelector.getSelectedRosterEntries().length != 0) {
            log.debug("Roster Entry selected...");
            setRosterEntry(rosterSelector.getSelectedRosterEntries()[0]);
            enableProfileStuff(true);
            // undo the close button enable if there's no profile selected
            // (this would be when selecting a RosterEntry that doesn't have
            // predefined VSD info.
            if ((profileComboBox.getSelectedIndex() == -1)
                    || (profileComboBox.getSelectedItem() instanceof NullProfileBoxItem)) {
                closeButton.setEnabled(false);
            }
        }
    }

    /**
     * rosterSaveButtonAction()
     *
     * ActionEventListener method for rosterSaveButton Writes VSDecoder info to
     * the RosterEntry.
     */
    private void rosterSaveButtonAction(ActionEvent e) {
        log.debug("rosterSaveButton pressed");
        if (rosterSelector.getSelectedRosterEntries().length != 0) {
            RosterEntry r = rosterSelector.getSelectedRosterEntries()[0];
            String profile = profileComboBox.getSelectedItem().toString();
            String path = VSDecoderManager.instance().getProfilePath(profile);
            if (path == null) {
                log.debug("Path not selected.  Ignore Save button press.");
                return;
            } else {
                r.setOpen(true);
                r.putAttribute("VSDecoder_Path", path);
                r.putAttribute("VSDecoder_Profile", profile);
                int value = JOptionPane.showConfirmDialog(null,
                        MessageFormat.format(Bundle.getMessage("UpdateRoster"),
                                new Object[]{r.titleString()}),
                        Bundle.getMessage("SaveRoster?"), JOptionPane.YES_NO_OPTION);
                if (value == JOptionPane.YES_OPTION) {
                    storeFile(r);
                }
                r.setOpen(false);
            }

            // Need to write RosterEntry to file.
        }
    }

    private void profileComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        // if there's also an Address entered, then enable the OK button.
        if (addressSelector.getAddress() != null) {
            closeButton.setEnabled(true);
        }
    }

    private void profileLoadButtonActionPerformed(java.awt.event.ActionEvent evt) {
        LoadVSDFileAction vfa = new LoadVSDFileAction();
        vfa.actionPerformed(evt);
        // Note: This will trigger a PROFILE_LIST_CHANGE event from VSDecoderManager
    }

    /**
     * handle the address "Set" button.
     */
    private void addressSetButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // If there's also a Profile selected, enable the OK button.
        if ((profileComboBox.getSelectedIndex() != -1)
                && (!(profileComboBox.getSelectedItem() instanceof NullProfileBoxItem))) {
            closeButton.setEnabled(true);
        }
    }

    /**
     * handle profile list changes from the VSDecoderManager
     */
    @SuppressWarnings("unchecked")
    private void vsdecoderManagerEventAction(VSDManagerEvent evt) {
        if (evt.getType() == VSDManagerEvent.EventType.PROFILE_LIST_CHANGE) {
            log.debug("Received Profile List Change Event");
            updateProfileList((ArrayList<String>) evt.getData());
        }
    }

    /**
     * Update the profile combo box
     */
    private void updateProfileList(ArrayList<String> s) {
        // There's got to be a more efficient way to do this.
        // Most of this is about merging the new array list with
        // the entries already in the ComboBox.

        if (s == null) {
            return;
        }

        // This is a bit tedious...
        // Pull all of the existing names from the Profile ComboBox
        ArrayList<String> ce_list = new ArrayList<>();
        for (int i = 0; i < profileComboBox.getItemCount(); i++) {
            ce_list.add(profileComboBox.getItemAt(i).toString());
        }

        // Cycle through the list provided as "s" and add only
        // those profiles that aren't already there.
        Iterator<String> itr = s.iterator();
        while (itr.hasNext()) {
            String st = itr.next();
            if (!ce_list.contains(st)) {
                log.debug("added item {}", st);
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
                log.debug("Trying to set the ProfileComboBox to this Profile: {}", profile);
                if (profile != null) {
                    profileComboBox.setSelectedItem(profile);
                }
                rosterSaveButton.setEnabled(true);
            }
        }
    }

    /**
     * setRosterEntry()
     *
     * Respond to the user choosing an entry from the rosterSelector
     */
    public void setRosterEntry(RosterEntry entry) {
        String vsd_path;
        String vsd_profile;

        // Update the roster entry local var.
        rosterEntry = entry;

        // Get VSD info from Roster.
        vsd_path = rosterEntry.getAttribute("VSDecoder_Path");
        vsd_profile = rosterEntry.getAttribute("VSDecoder_Profile");

        log.debug("Roster entry: profile: {}, path: {}", vsd_profile, vsd_path);

        // If the roster entry has VSD info stored, load it.
        if (vsd_path == null || vsd_path.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No VSD info found in Roster Media - couldn't load VSD File. Use the \"Save to Roster\" button to add the VSD info.");
        } else {
            // Load the indicated VSDecoder Profile and update the Profile combo box
            // This will trigger a PROFILE_LIST_CHANGE event from the VSDecoderManager.
            LoadVSDFileAction.loadVSDFile(vsd_path);
        }

        // Set the Address box from the Roster entry
        // Do this after the VSDecoder create, so it will see the change.
        addressSelector.setAddress(entry.getDccLocoAddress());
        addressSelector.setEnabled(true);
        addressSetButton.setEnabled(true);
    }

    /**
     * Write roster settings to the Roster file
     */
    protected boolean storeFile(RosterEntry _rosterEntry) {
        log.debug("storeFile starts");
        // We need to create a programmer, a cvTableModel, and a variableTableModel.
        // Doesn't matter which, so we'll use the Global programmer.
        Programmer p = InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer();
        CvTableModel cvModel = new CvTableModel(null, p);
        VariableTableModel variableModel = new VariableTableModel(null, new String[]{"Name", "Value"}, cvModel);

        // Now, in theory we can call _rosterEntry.writeFile...
        if (_rosterEntry.getFileName() != null) {
            // set the loco file name in the roster entry
            _rosterEntry.readFile();  // read, but don't yet process
            _rosterEntry.loadCvModel(variableModel, cvModel);
        }

        // id has to be set!
        if (_rosterEntry.getId().isEmpty()) {
            log.debug("storeFile without a filename; issued dialog");
            return false;
        }

        // if there isn't a filename, store using the id
        _rosterEntry.ensureFilenameExists();

        // create the RosterEntry to its file
        _rosterEntry.writeFile(cvModel, variableModel); // where to get the models???

        // mark this as a success
        variableModel.setFileDirty(false);

        // and store an updated roster file
        Roster.getDefault().writeRoster();

        return true;
    }

    private static final Logger log = LoggerFactory.getLogger(VSDConfigDialog.class);

}
