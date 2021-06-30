package jmri.jmrit.vsdecoder.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.vsdecoder.LoadVSDFileAction;
import jmri.jmrit.vsdecoder.SoundEvent;
import jmri.jmrit.vsdecoder.VSDConfig;
import jmri.jmrit.vsdecoder.VSDecoder;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main frame for the GUI VSDecoder Manager.
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
public class VSDManagerFrame extends JmriJFrame {

    public static final String MUTE = "VSDMF:Mute"; // NOI18N
    public static final String VOLUME_CHANGE = "VSDMF:VolumeChange"; // NOI18N
    public static final String REMOVE_DECODER = "VSDMF:RemoveDecoder"; // NOI18N
    public static final String CLOSE_WINDOW = "VSDMF:CloseWindow"; // NOI18N

    // Map of Mnemonic KeyEvent values to GUI Components
    private static final Map<String, Integer> Mnemonics = new HashMap<>();

    static {
        // Menu
        Mnemonics.put("FileMenu", KeyEvent.VK_F);
        Mnemonics.put("EditMenu", KeyEvent.VK_E);
        // Other GUI
        Mnemonics.put("MuteButton", KeyEvent.VK_M);
        Mnemonics.put("AddButton", KeyEvent.VK_A);
    }

    private int master_volume;

    JPanel decoderPane;
    JPanel volumePane;
    JPanel decoderBlank;

    private VSDConfig config;
    private VSDConfigDialog cd;
    private List<JMenu> menuList;
    private boolean is_auto_loading;
    private boolean is_viewing;
    private List<VSDecoder> vsdlist;

    /**
     * Constructor
     */
    public VSDManagerFrame() {
        super(false, false);
        this.addPropertyChangeListener(VSDecoderManager.instance());
        is_auto_loading = VSDecoderManager.instance().getVSDecoderPreferences().isAutoLoadingDefaultVSDFile();
        is_viewing = VSDecoderManager.instance().getVSDecoderList().isEmpty() ? false : true;
        initGUI();
    }

    @Override
    public void initComponents() {
        //this.initGUI();
    }

    /**
     * Build the GUI components
     */
    private void initGUI() {
        log.debug("initGUI");
        this.setTitle(Bundle.getMessage("VSDManagerFrameTitle"));
        this.buildMenu();
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));

        decoderPane = new JPanel();
        decoderPane.setLayout(new BoxLayout(decoderPane, BoxLayout.PAGE_AXIS));
        decoderBlank = VSDControl.generateBlank();
        decoderPane.add(decoderBlank);

        volumePane = new JPanel();
        volumePane.setLayout(new BoxLayout(volumePane, BoxLayout.LINE_AXIS));
        JToggleButton muteButton = new JToggleButton(Bundle.getMessage("MuteButtonLabel"));
        JButton addButton = new JButton(Bundle.getMessage("AddButtonLabel"));
        final JSlider volume = new JSlider(0, 100);
        volume.setMinorTickSpacing(10);
        volume.setPaintTicks(true);
        master_volume = VSDecoderManager.instance().getMasterVolume();
        volume.setValue(master_volume);
        volume.setPreferredSize(new Dimension(200, 20));
        volume.setToolTipText(Bundle.getMessage("MgrVolumeToolTip"));
        volume.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                volumeChange(e); // slider in real time
            }
        });
        volumePane.add(new JLabel(Bundle.getMessage("VolumePaneLabel")));
        volumePane.add(volume);
        volumePane.add(muteButton);
        muteButton.setToolTipText(Bundle.getMessage("MgrMuteToolTip"));
        muteButton.setMnemonic(Mnemonics.get("MuteButton"));
        muteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                muteButtonPressed(e);
            }
        });
        volumePane.add(addButton);
        addButton.setToolTipText(Bundle.getMessage("MgrAddButtonToolTip"));
        addButton.setMnemonic(Mnemonics.get("AddButton"));
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addButtonPressed(e);
            }
        });

        this.add(decoderPane);
        this.add(volumePane);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                firePropertyChange(CLOSE_WINDOW, null, null);
            }
        });

        log.debug("pane size + {}", decoderPane.getPreferredSize());
        this.pack();
        this.setVisible(true);

        log.debug("done...");

        // first, check Viewing Mode
        if (is_viewing) {
            vsdlist = new ArrayList<>(); // List of VSDecoders with uncomplete configuration (no Roster Entry reference)
            for (VSDecoder vsd : VSDecoderManager.instance().getVSDecoderList()) {
                if (vsd.getRosterEntry() != null) {
                    // VSDecoder configuration is complete and will be listed
                    addButton.doClick(); // simulate an Add-button-click
                    cd.setRosterItem(vsd.getRosterEntry()); // forward the roster entry
                } else {
                    vsdlist.add(vsd); // VSDecoder with uncomplete configuration
                }
            }
            // delete VSDecoder(s) with uncomplete configuration
            for (VSDecoder v : vsdlist) {
                VSDecoderManager.instance().deleteDecoder(v.getAddress().toString());
            }
            // change back to Edit mode
            is_viewing = false;
        } else if (is_auto_loading) {
            // Auto-Load
            log.info("Auto-Loading VSDecoder");
            String vsdRosterGroup = "VSD";
            String msg = "";
            if (Roster.getDefault().getRosterGroupList().contains(vsdRosterGroup)) {
                List<RosterEntry> rosterList;
                rosterList = Roster.getDefault().getEntriesInGroup(vsdRosterGroup);
                if (!rosterList.isEmpty()) {
                    // Allow <max_decoder> roster entries
                    int entry_counter = 1;
                    for (RosterEntry entry : rosterList) {
                        if (entry_counter <= VSDecoderManager.max_decoder) { 
                            addButton.doClick(); // simulate an Add-button-click
                            cd.setRosterItem(entry); // forward the roster entry
                            entry_counter++;
                        } else {
                            msg = "Only " + VSDecoderManager.max_decoder + " Roster Entries allowed. Discarded "
                                    + (rosterList.size() - VSDecoderManager.max_decoder);
                        }
                    }
                } else {
                    msg = "No Roster Entry found in Roster Group " + vsdRosterGroup;
                }
            } else {
                msg = "Roster Group \"" + vsdRosterGroup + "\" not found";
            }
            if (!msg.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Auto-Loading: " + msg);
                log.warn("Auto-Loading VSDecoder aborted");
            }
        }
    }

    /**
     * Handle "Mute" button press.
     * @param e Event that kicked this off.
     */
    protected void muteButtonPressed(ActionEvent e) {
        JToggleButton b = (JToggleButton) e.getSource();
        log.debug("Mute button pressed. value: {}", b.isSelected());
        firePropertyChange(MUTE, !b.isSelected(), b.isSelected());
    }

    /**
     * Handle "Add" button press
     * @param e Event that fired this change
     */
    protected void addButtonPressed(ActionEvent e) {
        log.debug("Add button pressed");

        // If the maximum number of VSDecoders (Controls) is reached, don't create a new Control
        // In Viewing Mode up to 4 existing VSDecoders are possible, so skip the check
        if (! is_viewing && VSDecoderManager.instance().getVSDecoderList().size() >= VSDecoderManager.max_decoder) {
            JOptionPane.showMessageDialog(null, 
                    "VSDecoder cannot be created. Maximal number is " + String.valueOf(VSDecoderManager.max_decoder));
        } else {
            config = new VSDConfig(); // Create a new Config for the new VSDecoder.
            // Do something here.  Create a new VSDecoder and add it to the window.
            cd = new VSDConfigDialog(decoderPane, Bundle.getMessage("NewDecoderConfigPaneTitle"), config, is_auto_loading, is_viewing);
            cd.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    log.debug("property change name {}, old: {}, new: {}", event.getPropertyName(),
                            event.getOldValue(), event.getNewValue());
                    addButtonPropertyChange(event);
                }
            });
        }
    }

    /**
     * Callback for the Config Dialog
     * @param event Event that fired this change
     */
    protected void addButtonPropertyChange(PropertyChangeEvent event) {
        log.debug("internal config dialog handler");
        // If this decoder already exists, don't create a new Control
        // In Viewing Mode up to 4 existing VSDecoders are possible, so skip the check
        if (! is_viewing && VSDecoderManager.instance().getVSDecoderByAddress(config.getLocoAddress().toString()) != null) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("MgrAddDuplicateMessage"));
        } else {
            VSDecoder newDecoder = VSDecoderManager.instance().getVSDecoder(config);
            if (newDecoder == null) {
                log.error("Lost context, VSDecoder is null. Quit JMRI and start over. No New Decoder constructed! Address: {}, profile: {}",
                        config.getLocoAddress(), config.getProfileName());
                return;
            }
            VSDControl newControl = new VSDControl(config);
            // Set the Decoder to listen to PropertyChanges from the control
            newControl.addPropertyChangeListener(newDecoder);
            this.addPropertyChangeListener(newDecoder);
            // Set US to listen to PropertyChanges from the control (mainly for DELETE)
            newControl.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    log.debug("property change name {}, old: {}, new: {}",
                            event.getPropertyName(), event.getOldValue(), event.getNewValue());
                    vsdControlPropertyChange(event);
                }
            });
            if (decoderPane.isAncestorOf(decoderBlank)) {
                decoderPane.remove(decoderBlank);
            }

            decoderPane.add(newControl);
            newControl.addSoundButtons(new ArrayList<SoundEvent>(newDecoder.getEventList()));

            firePropertyChange(VOLUME_CHANGE, master_volume, null);
            log.debug("Master volume set to {}", master_volume);

            decoderPane.revalidate();
            decoderPane.repaint();

            this.pack();
            //this.setVisible(true);
            // Do we need to make newControl a listener to newDecoder?
        }
    }

    /**
     * Handle property change event from one of the VSDControls
     * @param event Event that fired this change
     */
    protected void vsdControlPropertyChange(PropertyChangeEvent event) {
        String property = event.getPropertyName();
        if (property.equals(VSDControl.DELETE)) {
            String ov = (String) event.getOldValue();
            log.debug("vsdControlPropertyChange. ID: {}, old: {}", VSDControl.DELETE, ov);
            VSDecoder vsd = VSDecoderManager.instance().getVSDecoderByAddress(ov);
            if (vsd == null) {
                log.warn("Lost context, VSDecoder is null. Quit JMRI and start over.");
                return;
            }
            if (vsd.getEngineSound().isEngineStarted()) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("MgrDeleteWhenEngineStopped"));
                return;
            } else {
                this.removePropertyChangeListener(vsd);
                log.debug("vsdControlPropertyChange. ID: {}, old: {}", REMOVE_DECODER, ov);
                firePropertyChange(REMOVE_DECODER, ov, null);
                decoderPane.remove((VSDControl) event.getSource());
                if (decoderPane.getComponentCount() == 0) {
                    decoderPane.add(decoderBlank);
                }
                //debugPrintDecoderList();
                decoderPane.revalidate();
                decoderPane.repaint();

                this.pack();
            }
        }
    }

    /**
     * Handle master volume slider change
     * @param event Event that fired this change
     */
    protected void volumeChange(ChangeEvent event) {
        JSlider v = (JSlider) event.getSource();
        log.debug("Volume slider moved. value: {}", v.getValue());
        master_volume = v.getValue();
        firePropertyChange(VOLUME_CHANGE, master_volume, null);
        // todo? do you want to save?
        if (VSDecoderManager.instance().getMasterVolume() != v.getValue()) {
            VSDecoderManager.instance().setMasterVolume(v.getValue());
            VSDecoderManager.instance().getVSDecoderPreferences().save();
            log.debug("VSD Preferences saved");
        }
    }

    private void buildMenu() {
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile")); // uses NamedBeanBundle
        fileMenu.setMnemonic(Mnemonics.get("FileMenu")); // OK to use this different key name for Mnemonics

        fileMenu.add(new LoadVSDFileAction(Bundle.getMessage("VSDecoderFileMenuLoadVSDFile")));

        JMenu editMenu = new JMenu(Bundle.getMessage("MenuEdit"));
        editMenu.setMnemonic(Mnemonics.get("EditMenu")); // OK to use this different key name for Mnemonics
        editMenu.add(new VSDPreferencesAction(Bundle.getMessage("VSDecoderFileMenuPreferences")));

        menuList = new ArrayList<>(2);

        menuList.add(fileMenu);
        menuList.add(editMenu);

        this.setJMenuBar(new JMenuBar());

        this.getJMenuBar().add(fileMenu);
        this.getJMenuBar().add(editMenu);

        this.addHelpMenu("package.jmri.jmrit.vsdecoder.swing.VSDManagerFrame", true);
    }

    private static final Logger log = LoggerFactory.getLogger(VSDManagerFrame.class);

}
