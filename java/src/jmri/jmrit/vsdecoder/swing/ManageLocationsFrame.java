package jmri.jmrit.vsdecoder.swing;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
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
import javax.swing.event.EventListenerList;
import javax.swing.table.AbstractTableModel;
import jmri.Block;
import jmri.BlockManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.vsdecoder.LoadVSDFileAction;
import jmri.jmrit.vsdecoder.LoadXmlVSDecoderAction;
import jmri.jmrit.vsdecoder.StoreXmlVSDecoderAction;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import jmri.util.JmriJFrame;
import jmri.util.PhysicalLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author   Mark Underwood Copyright (C) 2011
 */
public class ManageLocationsFrame extends JmriJFrame {

    public static enum PropertyChangeID {

        MUTE, VOLUME_CHANGE, ADD_DECODER, REMOVE_DECODER
    }

    public static final Map<PropertyChangeID, String> PCIDMap;

    static {
        Map<PropertyChangeID, String> aMap = new HashMap<PropertyChangeID, String>();
        aMap.put(PropertyChangeID.MUTE, "VSDMF:Mute"); // NOI18N
        aMap.put(PropertyChangeID.VOLUME_CHANGE, "VSDMF:VolumeChange"); // NOI18N
        aMap.put(PropertyChangeID.ADD_DECODER, "VSDMF:AddDecoder"); // NOI18N
        aMap.put(PropertyChangeID.REMOVE_DECODER, "VSDMF:RemoveDecoder"); // NOI18N
        PCIDMap = Collections.unmodifiableMap(aMap);
    }

    // Map of Mnemonic KeyEvent values to GUI Components
    private static final Map<String, Integer> Mnemonics = new HashMap<String, Integer>();

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

    protected EventListenerList listenerList = new javax.swing.event.EventListenerList();

    private JTabbedPane tabbedPane;
    private JPanel listenerPanel;
    private JPanel reporterPanel;
    private JPanel opsPanel;
    private JPanel blockPanel;

    private Object[][] reporterData;  // positions of Reporters
    private Object[][] opsData;       // positions of Operations Locations
    private Object[][] locData;       // positions of Listener Locations
    private Object[][] blockData;     // positions of Blocks
    private LocationTableModel reporterModel;
    private LocationTableModel opsModel;
    private ListenerTableModel locModel;
    private LocationTableModel blockModel;
    private ListeningSpot listenerLoc;

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
        locData[0][0] = listenerLoc.getName();
        locData[0][1] = true;
        locData[0][2] = listenerLoc.getLocation().x;
        locData[0][3] = listenerLoc.getLocation().y;
        locData[0][4] = listenerLoc.getLocation().z;
        locData[0][5] = listenerLoc.getBearing();
        locData[0][6] = listenerLoc.getAzimuth();

        log.debug("Listener:" + listenerLoc.toString());
        log.debug("locData:");
        for (int i = 0; i < 7; i++) {
            log.debug("" + locData[0][i]);
        }

        JPanel locPanel = new JPanel();
        locPanel.setLayout(new BoxLayout(locPanel, BoxLayout.LINE_AXIS));
        JScrollPane locScrollPanel = new JScrollPane();
        locModel = new ListenerTableModel(locData);
        JTable locTable = new JTable(locModel);
        locTable.setFillsViewportHeight(true);
        locTable.setPreferredScrollableViewportSize(new Dimension(520, 200));

        locScrollPanel.getViewport().add(locTable);

        listenerPanel.add(modePanel);
        listenerPanel.add(locScrollPanel);

        reporterPanel = new JPanel();
        reporterPanel.setLayout(new GridBagLayout());
        JScrollPane reporterScrollPanel = new JScrollPane();
        reporterModel = new LocationTableModel(reporterData);
        JTable reporterTable = new JTable(reporterModel);
        reporterTable.setFillsViewportHeight(true);
        reporterScrollPanel.getViewport().add(reporterTable);
        reporterTable.setPreferredScrollableViewportSize(new Dimension(520, 200));

        blockPanel = new JPanel();
        blockPanel.setLayout(new GridBagLayout());
        JScrollPane blockScrollPanel = new JScrollPane();
        blockModel = new LocationTableModel(blockData);
        JTable blockTable = new JTable(blockModel);
        blockTable.setFillsViewportHeight(true);
        blockScrollPanel.getViewport().add(blockTable);
        blockTable.setPreferredScrollableViewportSize(new Dimension(520, 200));

        opsPanel = new JPanel();
        opsPanel.setLayout(new GridBagLayout());
        opsPanel.revalidate();
        JScrollPane opsScrollPanel = new JScrollPane();
        opsModel = new LocationTableModel(opsData);
        JTable opsTable = new JTable(opsModel);
        opsTable.setFillsViewportHeight(true);
        opsTable.setPreferredScrollableViewportSize(new Dimension(520, 200));

        opsScrollPanel.getViewport().add(opsTable);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(Bundle.getMessage("Reporters"), reporterScrollPanel); // Reporters Tab Title
        tabbedPane.setToolTipTextAt(0, Bundle.getMessage("ToolTipReporterTab"));
        tabbedPane.setMnemonicAt(0, Mnemonics.get("ReporterTab")); // NOI18N
        tabbedPane.addTab(Bundle.getMessage("Blocks"), blockScrollPanel);
        tabbedPane.setToolTipTextAt(0, Bundle.getMessage("ToolTipBlockTab"));
        tabbedPane.setMnemonicAt(0, Mnemonics.get("BlockTab")); // NOI18N
        tabbedPane.addTab(Bundle.getMessage("FieldOpsTabTitle"), opsScrollPanel);
        tabbedPane.setToolTipTextAt(1, Bundle.getMessage("ToolTipOpsTab"));
        tabbedPane.setMnemonicAt(1, Mnemonics.get("OpsTab")); // NOI18N
        tabbedPane.addTab(Bundle.getMessage("FieldListenersTabTitle"), listenerPanel);
        tabbedPane.setToolTipTextAt(2, Bundle.getMessage("ToolTipListenerTab"));
        tabbedPane.setMnemonicAt(2, Mnemonics.get("ListenerTab")); // NOI18N

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
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));

        fileMenu.add(new LoadVSDFileAction(Bundle.getMessage("VSDecoderFileMenuLoadVSDFile")));
        fileMenu.add(new StoreXmlVSDecoderAction(Bundle.getMessage("VSDecoderFileMenuSaveProfile")));
        fileMenu.add(new LoadXmlVSDecoderAction(Bundle.getMessage("VSDecoderFileMenuLoadProfile")));

        JMenu editMenu = new JMenu(Bundle.getMessage("MenuEdit"));
        editMenu.add(new VSDPreferencesAction(Bundle.getMessage("VSDecoderFileMenuPreferences")));

        fileMenu.getItem(1).setEnabled(false); // disable XML store
        fileMenu.getItem(2).setEnabled(false); // disable XML load

        menuList = new ArrayList<JMenu>(3);

        menuList.add(fileMenu);
        menuList.add(editMenu);

        this.setJMenuBar(new JMenuBar());
        this.getJMenuBar().add(fileMenu);
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
        if ((Boolean) locModel.getValueAt(0, 1)) {
            listenerLoc.setLocation((Double) locModel.getValueAt(0, 2),
                    (Double) locModel.getValueAt(0, 3),
                    (Double) locModel.getValueAt(0, 4));
            listenerLoc.setOrientation((Double) locModel.getValueAt(0, 5),
                    (Double) locModel.getValueAt(0, 6));
            VSDecoderManager.instance().getVSDecoderPreferences().setListenerPosition(listenerLoc);
        }

        HashMap<String, PhysicalLocation> data = reporterModel.getDataMap();
        ReporterManager mgr = jmri.InstanceManager.getDefault(jmri.ReporterManager.class);
        for (String s : data.keySet()) {
            log.debug("Reporter: " + s + " Location: " + data.get(s));
            Reporter r = mgr.getByDisplayName(s);
            PhysicalLocation.setBeanPhysicalLocation(data.get(s), r);
        }

        data = blockModel.getDataMap();
        BlockManager bmgr = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
        for (String s : data.keySet()) {
            log.debug("Block: " + s + " Location: " + data.get(s));
            Block b = bmgr.getByDisplayName(s);
            PhysicalLocation.setBeanPhysicalLocation(data.get(s), b);
        }

        data = opsModel.getDataMap();
        LocationManager lmgr = LocationManager.instance();
        for (String s : data.keySet()) {
            log.debug("OpsLocation: " + s + " Location: " + data.get(s));
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

    /**
     * Private class to serve as TableModel for Reporters and Ops Locations
     */
    private static class LocationTableModel extends AbstractTableModel {

        // These get internationalized at runtime in the constructor below.
        private String[] columnNames = new String[6];
        private Object[][] rowData;

        public LocationTableModel(Object[][] dataMap) {
            super();
            // Use i18n-ized column titles.
            columnNames[0] = Bundle.getMessage("Name");
            columnNames[1] = Bundle.getMessage("FieldTableUseColumn");
            columnNames[2] = Bundle.getMessage("FieldTableXColumn");
            columnNames[3] = Bundle.getMessage("FieldTableYColumn");
            columnNames[4] = Bundle.getMessage("FieldTableZColumn");
            columnNames[5] = Bundle.getMessage("FieldTableIsTunnelColumn");
            rowData = dataMap;
        }

        public HashMap<String, PhysicalLocation> getDataMap() {
            // Includes only the ones with the checkbox made
            HashMap<String, PhysicalLocation> retv = new HashMap<String, PhysicalLocation>();
            for (Object[] row : rowData) {
                if ((Boolean) row[1]) {
                    retv.put((String) row[0],
                            new PhysicalLocation((Float) row[2], (Float) row[3], (Float) row[4], (Boolean) row[5]));
                }
            }
            return (retv);
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getRowCount() {
            return rowData.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            return rowData[row][col];
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            rowData[row][col] = value;
            fireTableCellUpdated(row, col);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 1:
                case 5:
                    return Boolean.class;
                case 4:
                case 3:
                case 2:
                    return Float.class;
                case 0:
                default:
                    return super.getColumnClass(columnIndex);
            }
        }
    }

    /**
     * Private class for use as TableModel for Listener Locations
     */
    static private class ListenerTableModel extends AbstractTableModel {

        // These get internationalized at runtime in the constructor below.
        private String[] columnNames = new String[7];
        private Object[][] rowData = null;

        public ListenerTableModel(Object[][] dataMap) {
            super();
            // Use i18n-ized column titles.
            columnNames[0] = Bundle.getMessage("Name");
            columnNames[1] = Bundle.getMessage("FieldTableUseColumn");
            columnNames[2] = Bundle.getMessage("FieldTableXColumn");
            columnNames[3] = Bundle.getMessage("FieldTableYColumn");
            columnNames[4] = Bundle.getMessage("FieldTableZColumn");
            columnNames[5] = Bundle.getMessage("FieldTableBearingColumn");
            columnNames[6] = Bundle.getMessage("FieldTableAzimuthColumn");
            rowData = dataMap;
        }

        @SuppressWarnings("unused")
        public HashMap<String, ListeningSpot> getDataMap() {
            // Includes only the ones with the checkbox made
            HashMap<String, ListeningSpot> retv = new HashMap<String, ListeningSpot>();
            ListeningSpot spot = null;
            for (Object[] row : rowData) {
                if ((Boolean) row[1]) {
                    spot = new ListeningSpot();
                    spot.setName((String) row[0]);
                    spot.setLocation((Double) row[2], (Double) row[3], (Double) row[4]);
                    spot.setOrientation((Double) row[5], (Double) row[6]);
                    retv.put((String) row[0], spot);
                }
            }
            return (retv);
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getRowCount() {
            return rowData.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            return rowData[row][col];
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            rowData[row][col] = value;
            fireTableCellUpdated(row, col);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 1:
                    return Boolean.class;
                case 6:
                case 5:
                case 4:
                case 3:
                case 2:
                    return Double.class;
                case 0:
                default:
                    return super.getColumnClass(columnIndex);
            }
        }

    }
}
