package jmri.jmrit.vsdecoder.swing;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import jmri.Block;
import jmri.BlockManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import jmri.util.JmriJFrame;
import jmri.util.PhysicalLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to manage Reporters, Blocks, Locations and Listener attributes.
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
public class ManageLocationsFrame extends JmriJFrame {

    // Map of Mnemonic KeyEvent values to GUI Components
    private static final Map<String, Integer> Mnemonics = new HashMap<>();

    static {
        Mnemonics.put("RoomMode", KeyEvent.VK_R); // NOI18N
        Mnemonics.put("HeadphoneMode", KeyEvent.VK_H); // NOI18N
        Mnemonics.put("ReporterTab", KeyEvent.VK_E); // NOI18N
        Mnemonics.put("OpsTab", KeyEvent.VK_P); // NOI18N
        Mnemonics.put("ListenerTab", KeyEvent.VK_L); // NOI18N
        Mnemonics.put("BlockTab", KeyEvent.VK_B); // NOI18N
        Mnemonics.put("CloseButton", KeyEvent.VK_O); // NOI18N
        Mnemonics.put("SaveButton", KeyEvent.VK_S); // NOI18N
    }

    private JTabbedPane tabbedPane;
    private JPanel listenerPanel;
    private JPanel reporterPanel;
    private JPanel opsPanel;
    private JPanel blockPanel;

    private Object[][] reporterData;  // positions of Reporters
    private Object[][] opsData;       // positions of Operations Locations
    private Object[][] locData;       // positions of Listener Locations
    private Object[][] blockData;     // positions of Blocks
    private ManageLocationsTableModel.ReporterBlockTableModel reporterModel;
    private ManageLocationsTableModel.LocationTableModel opsModel;
    private ManageLocationsTableModel.ListenerTableModel locModel;
    private ManageLocationsTableModel.ReporterBlockTableModel blockModel;
    private ListeningSpot listenerLoc;

    private HashMap<String, PhysicalLocation> data;

    private List<JMenu> menuList;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "2D array of different types passed as complex parameter. "
            + "Better to switch to passing use-specific objects rather than "
            + "papering this over with a deep copy of the arguments. "
            + "In any case, there's no risk of exposure here.")
    public ManageLocationsFrame(ListeningSpot listener,
            Object[][] reporters,
            Object[][] ops,
            Object[][] blocks) {
        super(false, false);
        reporterData = reporters;
        opsData = ops;
        listenerLoc = listener;
        blockData = blocks;
        initGui();
    }

    private void initGui() {

        this.setTitle(Bundle.getMessage("FieldManageLocationsFrameTitle"));
        this.buildMenu();
        // Panel for managing listeners
        listenerPanel = new JPanel();
        listenerPanel.setLayout(new BoxLayout(listenerPanel, BoxLayout.Y_AXIS));

        // Audio Mode Buttons
        JRadioButton b1 = new JRadioButton(Bundle.getMessage("ButtonAudioModeRoom"));
        b1.setToolTipText(Bundle.getMessage("ToolTipButtonAudioModeRoom"));
        b1.setMnemonic(Mnemonics.get("RoomMode")); // NOI18N
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modeRadioButtonPressed(e);
            }
        });
        JRadioButton b2 = new JRadioButton(Bundle.getMessage("ButtonAudioModeHeadphone"));
        b2.setMnemonic(Mnemonics.get("HeadphoneMode")); // NOI18N
        b2.setToolTipText(Bundle.getMessage("ToolTipButtonAudioModeHeadphone"));
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modeRadioButtonPressed(e);
            }
        });
        b2.setEnabled(false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(b1);
        bg.add(b2);
        b1.setSelected(true);
        JPanel modePanel = new JPanel();
        modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.LINE_AXIS));
        modePanel.add(new JLabel(Bundle.getMessage("FieldAudioMode")));
        modePanel.add(b1);
        modePanel.add(b2);

        // Build Listener Locations Table
        locData = new Object[1][7];
        locData[0][ManageLocationsTableModel.NAMECOL] = listenerLoc.getName();
        locData[0][ManageLocationsTableModel.USECOL] = true;
        locData[0][ManageLocationsTableModel.XCOL] = listenerLoc.getLocation().x;
        locData[0][ManageLocationsTableModel.YCOL] = listenerLoc.getLocation().y;
        locData[0][ManageLocationsTableModel.ZCOL] = listenerLoc.getLocation().z;
        locData[0][ManageLocationsTableModel.BEARINGCOL] = listenerLoc.getBearing();
        locData[0][ManageLocationsTableModel.AZIMUTHCOL] = listenerLoc.getAzimuth();

        log.debug("Listener: {}", listenerLoc.toString());
        log.debug("  locData:");
        for (int i = 0; i < 7; i++) {
            log.debug("  item {}", locData[0][i]);
        }

        JPanel locPanel = new JPanel();
        locPanel.setLayout(new BoxLayout(locPanel, BoxLayout.LINE_AXIS));
        JScrollPane locScrollPanel = new JScrollPane();
        locModel = new ManageLocationsTableModel.ListenerTableModel(locData);
        JTable locTable = new JTable(locModel);
        locTable.setFillsViewportHeight(true);
        locTable.setPreferredScrollableViewportSize(new Dimension(520, 200));
        locScrollPanel.getViewport().add(locTable);

        listenerPanel.add(modePanel);
        listenerPanel.add(locScrollPanel);

        reporterPanel = new JPanel();
        reporterPanel.setLayout(new GridBagLayout());
        JScrollPane reporterScrollPanel = new JScrollPane();
        reporterModel = new ManageLocationsTableModel.ReporterBlockTableModel(reporterData);
        JTable reporterTable = new JTable(reporterModel);
        reporterTable.setFillsViewportHeight(true);
        reporterTable.setPreferredScrollableViewportSize(new Dimension(540, 200));
        reporterScrollPanel.getViewport().add(reporterTable);

        blockPanel = new JPanel();
        blockPanel.setLayout(new GridBagLayout());
        JScrollPane blockScrollPanel = new JScrollPane();
        blockModel = new ManageLocationsTableModel.ReporterBlockTableModel(blockData);
        JTable blockTable = new JTable(blockModel);
        blockTable.setFillsViewportHeight(true);
        blockTable.setPreferredScrollableViewportSize(new Dimension(540, 200));
        blockScrollPanel.getViewport().add(blockTable);

        opsPanel = new JPanel();
        opsPanel.setLayout(new GridBagLayout());
        opsPanel.revalidate();
        JScrollPane opsScrollPanel = new JScrollPane();
        opsModel = new ManageLocationsTableModel.LocationTableModel(opsData);
        JTable opsTable = new JTable(opsModel);
        opsTable.setFillsViewportHeight(true);
        opsTable.setPreferredScrollableViewportSize(new Dimension(520, 200));
        opsScrollPanel.getViewport().add(opsTable);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(Bundle.getMessage("Reporters"), reporterScrollPanel); // Reporters Tab Title
        tabbedPane.setToolTipTextAt(0, Bundle.getMessage("ToolTipReporterTab"));
        tabbedPane.setMnemonicAt(0, Mnemonics.get("ReporterTab")); // NOI18N
        tabbedPane.addTab(Bundle.getMessage("Blocks"), blockScrollPanel);
        tabbedPane.setToolTipTextAt(1, Bundle.getMessage("ToolTipBlockTab"));
        tabbedPane.setMnemonicAt(1, Mnemonics.get("BlockTab")); // NOI18N
        tabbedPane.addTab(Bundle.getMessage("FieldOpsTabTitle"), opsScrollPanel);
        tabbedPane.setToolTipTextAt(2, Bundle.getMessage("ToolTipOpsTab"));
        tabbedPane.setMnemonicAt(2, Mnemonics.get("OpsTab")); // NOI18N
        tabbedPane.addTab(Bundle.getMessage("FieldListenersTabTitle"), listenerPanel);
        tabbedPane.setToolTipTextAt(3, Bundle.getMessage("ToolTipListenerTab"));
        tabbedPane.setMnemonicAt(3, Mnemonics.get("ListenerTab")); // NOI18N

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        JButton closeButton = new JButton(Bundle.getMessage("ButtonCancel"));
        closeButton.setToolTipText(Bundle.getMessage("ToolTipButtonMLFClose"));
        closeButton.setMnemonic(Mnemonics.get("CloseButton")); // NOI18N
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeButtonPressed(e);
            }
        });
        JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
        saveButton.setToolTipText(Bundle.getMessage("ToolTipButtonMLFSave"));
        saveButton.setMnemonic(Mnemonics.get("SaveButton")); // NOI18N
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveButtonPressed(e);
            }
        });
        buttonPane.add(closeButton);
        buttonPane.add(saveButton);

        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.getContentPane().add(tabbedPane);
        this.getContentPane().add(buttonPane);
        this.pack();
        this.setVisible(true);
    }

    private void buildMenu() {
        JMenu editMenu = new JMenu(Bundle.getMessage("MenuEdit"));
        editMenu.add(new VSDPreferencesAction(Bundle.getMessage("VSDecoderFileMenuPreferences")));

        menuList = new ArrayList<>(1);

        menuList.add(editMenu);

        this.setJMenuBar(new JMenuBar());
        this.getJMenuBar().add(editMenu);
        this.addHelpMenu("package.jmri.jmrit.vsdecoder.swing.ManageLocationsFrame", true); // NOI18N
    }

    private void saveButtonPressed(ActionEvent e) {
        int value = JOptionPane.showConfirmDialog(null, Bundle.getMessage("FieldMLFSaveDialogConfirmMessage"),
                Bundle.getMessage("FieldMLFSaveDialogTitle"),
                JOptionPane.YES_NO_OPTION);
        if (value == JOptionPane.YES_OPTION) {
            saveTableValues();
            OperationsXml.save();
        }
        if (Setup.isCloseWindowOnSaveEnabled()) {
            dispose();
        }
    }

    @SuppressFBWarnings(value = "WMI_WRONG_MAP_ITERATOR", justification = "only in slow debug")
    private void saveTableValues() {
        if ((Boolean) locModel.getValueAt(0, ManageLocationsTableModel.USECOL)) {
            // Don't accept Azimuth value 90 or -90 (they are not in the domain of definition)
            if ((Double) locModel.getValueAt(0, ManageLocationsTableModel.AZIMUTHCOL) != null
                    && ((Double) locModel.getValueAt(0, ManageLocationsTableModel.AZIMUTHCOL) == 90.0d
                    || (Double) locModel.getValueAt(0, ManageLocationsTableModel.AZIMUTHCOL) == -90.0d)) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("FieldTableAzimuthInvalidValue"));
            } else {
                listenerLoc.setLocation((Double) locModel.getValueAt(0, ManageLocationsTableModel.XCOL),
                        (Double) locModel.getValueAt(0, ManageLocationsTableModel.YCOL),
                        (Double) locModel.getValueAt(0, ManageLocationsTableModel.ZCOL));
                listenerLoc.setOrientation((Double) locModel.getValueAt(0, ManageLocationsTableModel.BEARINGCOL),
                        (Double) locModel.getValueAt(0, ManageLocationsTableModel.AZIMUTHCOL));
                VSDecoderManager.instance().getVSDecoderPreferences().save();
                VSDecoderManager.instance().getVSDecoderPreferences().setListenerPosition(listenerLoc);
            }
        }

        data = reporterModel.getDataMap();
        ReporterManager mgr = jmri.InstanceManager.getDefault(ReporterManager.class);
        for (String s : data.keySet()) {
            log.debug("Reporter: {}, Location: {}", s, data.get(s));
            Reporter r = mgr.getByDisplayName(s);
            if (r != null) {
                PhysicalLocation.setBeanPhysicalLocation(data.get(s), r);
            }
        }

        data = blockModel.getDataMap();
        BlockManager bmgr = jmri.InstanceManager.getDefault(BlockManager.class);
        for (String s : data.keySet()) {
            log.debug("Block: {}, Location: {}", s, data.get(s));
            Block b = bmgr.getByDisplayName(s);
            if (b != null) {
                PhysicalLocation.setBeanPhysicalLocation(data.get(s), b);
            }
        }

        data = opsModel.getDataMap();
        LocationManager lmgr = jmri.InstanceManager.getDefault(LocationManager.class);
        for (String s : data.keySet()) {
            log.debug("OpsLocation: {}, Location: {}", s, data.get(s));
            Location l = lmgr.getLocationByName(s);
            l.setPhysicalLocation(data.get(s));
        }
    }

    private void modeRadioButtonPressed(ActionEvent e) {
    }

    private void closeButtonPressed(ActionEvent e) {
        dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(ManageLocationsFrame.class);

}
