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
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.jmrit.vsdecoder.LoadVSDFileAction;
import jmri.jmrit.vsdecoder.VSDConfig;
import jmri.jmrit.vsdecoder.VSDManagerEvent;
import jmri.jmrit.vsdecoder.VSDManagerListener;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration dialog for setting up a new VSDecoder
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
 */
public class VSDConfigDialog extends JDialog {

    private static final String CONFIG_PROPERTY = "Config";

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

    private RosterEntry rosterEntrySelected;
    private boolean is_auto_loading;
    private boolean is_viewing;

    /**
     * Constructor
     *
     * @param parent Ancestor panel
     * @param title  title for the dialog
     * @param c      Config object to be set by the dialog
     * @param ial    Is Auto Loading
     * @param viewing Viewing mode flag
     */
    public VSDConfigDialog(JPanel parent, String title, VSDConfig c, boolean ial, boolean viewing) {
        super(SwingUtilities.getWindowAncestor(parent), title);
        config = c;
        is_auto_loading = ial;
        is_viewing = viewing;
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
            log.debug("Index out of bounds setting up tabbed Pane", iobe);
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

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent ae) {
        dispose();
    }

    /**
     * Handle the "Close" (or "OK") button action
     */
    private void closeButtonActionPerformed(java.awt.event.ActionEvent ae) {
        if (profileComboBox.getSelectedItem() == null) {
            log.debug("Profile item selected: {}", profileComboBox.getSelectedItem());
            JOptionPane.showMessageDialog(null, "Please select a valid Profile");
            rosterSaveButton.setEnabled(false);
            closeButton.setEnabled(false);
        } else {
            config.setProfileName(profileComboBox.getSelectedItem().toString());
            log.debug("Profile item selected: {}", config.getProfileName());

            config.setLocoAddress(addressSelector.getAddress());
            if (getSelectedRosterItem() != null) {
                config.setRosterEntry(getSelectedRosterItem());
                // decoder volume
                String dv = config.getRosterEntry().getAttribute("VSDecoder_Volume");
                if (dv !=null && !dv.isEmpty()) {
                    config.setVolume(Float.parseFloat(dv));
                }
                log.debug("Decoder volume in config: {}", config.getVolume());
            } else {
                config.setRosterEntry(null);
            }
            firePropertyChange(CONFIG_PROPERTY, config, null); // open the new VSDControl
            dispose();
        }
    }

    // class NullComboBoxItem
    //
    // little object to insert into profileComboBox when it's empty
    static class NullProfileBoxItem {
        @Override
        public String toString() {
            return Bundle.getMessage("NoLocoSelectedText");
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
     * ActionEventListener function for rosterSelector
     * Chooses a RosterEntry from the list and loads its relevant info.
     * If all VSD Infos are provided, close the Config Dialog.
     */
    private void rosterItemSelectAction(ActionEvent e) {
        if (getSelectedRosterItem() != null) {
            log.debug("Roster Entry selected... {}", getSelectedRosterItem().getId());
            setRosterEntry(getSelectedRosterItem());
            enableProfileStuff(true);

            log.debug("profile ComboBox selected item: {}", profileComboBox.getSelectedItem());
            // undo the close button enable if there's no profile selected (this would
            // be when selecting a RosterEntry that doesn't have predefined VSD info)
            if ((profileComboBox.getSelectedIndex() == -1)
                    || (profileComboBox.getSelectedItem() instanceof NullProfileBoxItem)) {
                rosterSaveButton.setEnabled(false);
                closeButton.setEnabled(false);
                log.warn("No Profile found");
            } else {
                closeButton.doClick(); // All done
            }
        }
    }

    // Roster Entry via Auto-Load from VSDManagerFrame
    void setRosterItem(RosterEntry s) {
        rosterEntrySelected = s;
        log.debug("Auto-Load selected roster id: {}, profile: {}", rosterEntrySelected.getId(),
                rosterEntrySelected.getAttribute("VSDecoder_Profile"));
        rosterItemSelectAction(null); // trigger the next step for Auto-Load (works, but does not seem to be implemented correctly)
    }

    private RosterEntry getRosterItem() {
        return rosterEntrySelected;
    }

    private RosterEntry getSelectedRosterItem() {
        // Used by Auto-Load and non Auto-Load
        if ((is_auto_loading || is_viewing) && getRosterItem() != null) {
            rosterEntrySelected = getRosterItem();
        } else {
            if (rosterSelector.getSelectedRosterEntries().length != 0) {
                rosterEntrySelected = rosterSelector.getSelectedRosterEntries()[0];
            } else {
                rosterEntrySelected = null;
            }
        }
        return rosterEntrySelected;
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
                log.warn("Path not selected.  Ignore Save button press.");
                return;
            } else {
                int value = JOptionPane.showConfirmDialog(null,
                        MessageFormat.format(Bundle.getMessage("UpdateRoster"),
                        new Object[]{r.titleString()}),
                        Bundle.getMessage("SaveRoster?"), JOptionPane.YES_NO_OPTION);
                if (value == JOptionPane.YES_OPTION) {
                    r.putAttribute("VSDecoder_Path", path);
                    r.putAttribute("VSDecoder_Profile", profile);
                    if (r.getAttribute("VSDecoder_LaunchThrottle") == null) {
                        r.putAttribute("VSDecoder_LaunchThrottle", "no");
                    }
                    if (r.getAttribute("VSDecoder_Volume") == null) {
                        // convert Float to String without decimal places
                        r.putAttribute("VSDecoder_Volume", String.valueOf(config.DEFAULT_VOLUME));
                    }
                    r.updateFile(); // write and update timestamp
                    log.info("Roster Media updated for {}", r.getDisplayName());
                    closeButton.doClick(); // All done
                } else {
                    log.info("Roster Media not saved");
                }
            }
        }
    }

    // Probably the last setting step of the manually "Add Decoder" process
    // (but the user also can load a VSD file and then set the address).
    // Enable the OK button (closeButton) and the Roster Save button.
    // note: a selected roster entry sets an Address too
    private void profileComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        // if there's also an Address entered, then enable the OK button.
        if (addressSelector.getAddress() != null
                && !(profileComboBox.getSelectedItem() instanceof NullProfileBoxItem)) {
            closeButton.setEnabled(true);
            // Roster Entry is required to enable the Roster Save button
            if (rosterSelector.getSelectedRosterEntries().length != 0) {
                rosterSaveButton.setEnabled(true);
            }
        }
    }

    private void profileLoadButtonActionPerformed(java.awt.event.ActionEvent evt) {
        LoadVSDFileAction vfa = new LoadVSDFileAction();
        vfa.actionPerformed(evt);
        // Note: This will trigger a PROFILE_LIST_CHANGE event from VSDecoderManager
    }

    /**
     * handle the address "Set" button
     */
    private void addressSetButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // address should be an integer, not a string
        if (addressSelector.getAddress() == null) {
            log.warn("Address is not valid");
        }
        // if a profile is already selected enable the OK button (closeButton)
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
            if (!(profileComboBox.getItemAt(i) instanceof NullProfileBoxItem)) {
                ce_list.add(profileComboBox.getItemAt(i).toString());
            }
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

        // If the combo box isn't empty, enable it and enable it
        if (profileComboBox.getItemCount() > 0) {
            profileComboBox.setEnabled(true);
            // select a profile if roster items are available
            if (getSelectedRosterItem() != null) {
                RosterEntry r = getSelectedRosterItem();
                String profile = r.getAttribute("VSDecoder_Profile");
                log.debug("Trying to set the ProfileComboBox to this Profile: {}", profile);
                if (profile != null) {
                    profileComboBox.setSelectedItem(profile);
                }
            }
        }
    }

    /**
     * setRosterEntry()
     *
     * Respond to the user choosing an entry from the rosterSelector
     * Launch a JMRI throttle (optional)
     */
    private void setRosterEntry(RosterEntry entry) {
        // Update the roster entry local var.
        rosterEntry = entry;

        // Get VSD info from Roster
        String vsd_path = rosterEntry.getAttribute("VSDecoder_Path");
        String vsd_launch_throttle = rosterEntry.getAttribute("VSDecoder_LaunchThrottle");

        log.debug("Roster entry path: {}, LaunchThrottle: {}", vsd_path, vsd_launch_throttle);

        // If the roster entry has VSD info stored, load it.
        if (vsd_path == null || vsd_path.isEmpty()) {
            log.warn("No VSD Path found for Roster Entry \"{}\". Use the \"Save to Roster\" button to add the VSD info.",
                    rosterEntry.getId());
        } else {
            // Load the indicated VSDecoder Profile and update the Profile combo box
            // This will trigger a PROFILE_LIST_CHANGE event from the VSDecoderManager.
            boolean is_loaded = LoadVSDFileAction.loadVSDFile(vsd_path);

            if (is_loaded &&
                    vsd_launch_throttle != null &&
                    vsd_launch_throttle.equals("yes") &&
                    InstanceManager.throttleManagerInstance().getThrottleUsageCount(rosterEntry) == 0) {
                // Launch a JMRI Throttle (if setup by the Roster media attribut and a throttle not already exists).
                jmri.jmrit.throttle.ThrottleFrame tf =
                        InstanceManager.getDefault(jmri.jmrit.throttle.ThrottleFrameManager.class).createThrottleFrame();
                tf.toFront();
                tf.getAddressPanel().setRosterEntry(Roster.getDefault().entryFromTitle(rosterEntry.getId()));
            }
        }

        // Set the Address box from the Roster entry.
        // Do this after the VSDecoder create, so it will see the change.
        addressSelector.setAddress(entry.getDccLocoAddress());
        addressSelector.setEnabled(true);
        addressSetButton.setEnabled(true);
    }

    private static final Logger log = LoggerFactory.getLogger(VSDConfigDialog.class);

}
