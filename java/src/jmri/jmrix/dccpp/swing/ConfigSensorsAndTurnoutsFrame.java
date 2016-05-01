package jmri.jmrix.dccpp.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import jmri.jmrix.dccpp.DCCppListener;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.jmrix.dccpp.DCCppSensorManager;
import jmri.jmrix.dccpp.DCCppTrafficController;
import jmri.jmrix.dccpp.DCCppTurnoutManager;
import jmri.util.JmriJFrame;
import jmri.util.WindowMenu;
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
 * @author			Mark Underwood Copyright (C) 2011
 */
public class ConfigSensorsAndTurnoutsFrame extends JmriJFrame implements DCCppListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // Uncomment this when we add labels...
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
        Mnemonics.put("SensorTab", KeyEvent.VK_E); // NOI18N
        Mnemonics.put("TurnoutTab", KeyEvent.VK_T); // NOI18N
        Mnemonics.put("OutputTab", KeyEvent.VK_O); // NOI18N
        Mnemonics.put("AddButton", KeyEvent.VK_A); // NOI18N
        Mnemonics.put("CloseButton", KeyEvent.VK_O); // NOI18N
        Mnemonics.put("SaveButton", KeyEvent.VK_S); // NOI18N
    }

    protected EventListenerList listenerList = new javax.swing.event.EventListenerList();

    private DCCppTrafficController tc;

    private JTabbedPane tabbedPane;
    private JPanel sensorPanel;

    private SensorTableModel sensorModel;
    private TurnoutTableModel turnoutModel;
    private OutputTableModel outputModel;
    private JTable sensorTable;
    private JTable turnoutTable;
    private JTable outputTable;
    private TableRowSorter<TableModel> sensorSorter;
    private TableRowSorter<TableModel> turnoutSorter;
    private TableRowSorter<TableModel> outputSorter;

    private List<JMenu> menuList;
    private enum CurrentTab { SENSOR, TURNOUT, OUTPUT }
    private CurrentTab cTab;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "2D array of different types passed as complex parameter. "
            + "Better to switch to passing use-specific objects rather than "
            + "papering this over with a deep copy of the arguments. "
            + "In any case, there's no risk of exposure here.")
    public ConfigSensorsAndTurnoutsFrame(DCCppSensorManager sm,
                                         DCCppTurnoutManager tm,
                                         DCCppTrafficController t) {
        super(false, false);
        tc = t;
        initGui();
    }

    private void initGui() {

        // NOTE: Look at jmri.jmrit.vsdecoder.swing.ManageLocationsFrame
        // for how to add a tab for turnouts and other things.

        this.setTitle(Bundle.getMessage("FieldManageSensorsTurnoutsFrameTitle"));
        this.buildMenu();

        // Panel for managing sensors
        sensorPanel = new JPanel();
        sensorPanel.setLayout(new GridBagLayout());

        JButton addButton = new JButton(Bundle.getMessage("ButtonAddSensor"));
        addButton.setToolTipText(Bundle.getMessage("ToolTipButtonMSFAdd"));
        addButton.setMnemonic(Mnemonics.get("AddButton")); // NOI18N
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addButtonPressed(e);
            }
        });

        JButton closeButton = new JButton(Bundle.getMessage("ButtonClose"));
        closeButton.setToolTipText(Bundle.getMessage("ToolTipButtonMSFClose"));
        closeButton.setMnemonic(Mnemonics.get("CloseButton")); // NOI18N
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeButtonPressed(e);
            }
        });
        JButton saveButton = new JButton(Bundle.getMessage("ButtonSaveSensors"));
        saveButton.setToolTipText(Bundle.getMessage("ToolTipButtonMSFSave"));
        saveButton.setMnemonic(Mnemonics.get("SaveButton")); // NOI18N
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveButtonPressed(e);
            }
        });


        JScrollPane sensorScrollPanel = new JScrollPane();
        sensorModel = new SensorTableModel();
        sensorTable = new JTable(sensorModel);
        sensorTable.setFillsViewportHeight(true);
        sensorScrollPanel.getViewport().add(sensorTable);
        sensorTable.setPreferredScrollableViewportSize(new Dimension(520, 200));
        sensorTable.getColumn(Bundle.getMessage("FieldTableDeleteColumn")).setCellRenderer(new ButtonRenderer());
        sensorTable.getColumn(Bundle.getMessage("FieldTableDeleteColumn")).setCellEditor(
            new ButtonEditor(new JCheckBox(), sensorTable));
        sensorTable.removeColumn(sensorTable.getColumn("isNew"));
        sensorTable.removeColumn(sensorTable.getColumn("isDirty"));
        sensorTable.removeColumn(sensorTable.getColumn("isDelete"));
        sensorTable.setAutoCreateRowSorter(true);
        sensorSorter = new TableRowSorter<>(sensorTable.getModel());
        sensorTable.setRowSorter(sensorSorter);
        List<RowSorter.SortKey> sensorSortKeys = new ArrayList<>();
        //int columnIndexToSort = 1;
        sensorSortKeys.add(new RowSorter.SortKey(sensorTable.getColumn(Bundle.getMessage("FieldTableIndexColumn")).getModelIndex(), SortOrder.ASCENDING));
        sensorSorter.setSortKeys(sensorSortKeys);
        sensorSorter.sort();
        sensorSorter.setSortable(sensorTable.getColumn(Bundle.getMessage("FieldTableDeleteColumn")).getModelIndex(), false);


        JScrollPane turnoutScrollPanel = new JScrollPane();
        turnoutModel = new TurnoutTableModel();
        turnoutTable = new JTable(turnoutModel);
        turnoutTable.setFillsViewportHeight(true);
        turnoutScrollPanel.getViewport().add(turnoutTable);
        turnoutTable.setPreferredScrollableViewportSize(new Dimension(520, 200));
        turnoutTable.getColumn(Bundle.getMessage("FieldTableDeleteColumn")).setCellRenderer(new ButtonRenderer());
        turnoutTable.getColumn(Bundle.getMessage("FieldTableDeleteColumn")).setCellEditor(
            new ButtonEditor(new JCheckBox(), turnoutTable));
        turnoutTable.removeColumn(turnoutTable.getColumn("isNew"));
        turnoutTable.removeColumn(turnoutTable.getColumn("isDirty"));
        turnoutTable.removeColumn(turnoutTable.getColumn("isDelete"));
        turnoutTable.setAutoCreateRowSorter(true);
        turnoutSorter = new TableRowSorter<>(turnoutTable.getModel());
        turnoutTable.setRowSorter(turnoutSorter);
        List<RowSorter.SortKey> turnoutSortKeys = new ArrayList<>();
        //int columnIndexToSort = 1;
        turnoutSortKeys.add(new RowSorter.SortKey(sensorTable.getColumn(Bundle.getMessage("FieldTableIndexColumn")).getModelIndex(), SortOrder.ASCENDING));
        turnoutSorter.setSortKeys(turnoutSortKeys);
        turnoutSorter.setSortable(sensorTable.getColumn(Bundle.getMessage("FieldTableDeleteColumn")).getModelIndex(), false);
        turnoutSorter.sort();

        JScrollPane outputScrollPanel = new JScrollPane();
        outputModel = new OutputTableModel();
        outputTable = new JTable(outputModel);
        outputTable.setFillsViewportHeight(true);
        outputScrollPanel.getViewport().add(outputTable);
        outputTable.setPreferredScrollableViewportSize(new Dimension(520, 200));
        outputTable.getColumn(Bundle.getMessage("FieldTableDeleteColumn")).setCellRenderer(new ButtonRenderer());
        outputTable.getColumn(Bundle.getMessage("FieldTableDeleteColumn")).setCellEditor(
            new ButtonEditor(new JCheckBox(), outputTable));
        outputTable.removeColumn(outputTable.getColumn("isNew"));
        outputTable.removeColumn(outputTable.getColumn("isDirty"));
        outputTable.removeColumn(outputTable.getColumn("isDelete"));
        outputTable.setAutoCreateRowSorter(true);
        outputSorter = new TableRowSorter<>(outputTable.getModel());
        outputTable.setRowSorter(outputSorter);
        List<RowSorter.SortKey> outputSortKeys = new ArrayList<>();
        //int columnIndexToSort = 1;
        outputSortKeys.add(new RowSorter.SortKey(sensorTable.getColumn(Bundle.getMessage("FieldTableIndexColumn")).getModelIndex(), SortOrder.ASCENDING));
        outputSorter.setSortKeys(outputSortKeys);
        outputSorter.setSortable(sensorTable.getColumn(Bundle.getMessage("FieldTableDeleteColumn")).getModelIndex(), false);
        outputSorter.sort();

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(Bundle.getMessage("FieldSensorsTabTitle"), sensorScrollPanel);
        tabbedPane.setToolTipTextAt(0, Bundle.getMessage("ToolTipSensorTab"));
        tabbedPane.setMnemonicAt(0, Mnemonics.get("SensorTab")); // NOI18N
        tabbedPane.addTab(Bundle.getMessage("FieldTurnoutsTabTitle"), turnoutScrollPanel);
        tabbedPane.setToolTipTextAt(0, Bundle.getMessage("ToolTipTurnoutTab"));
        tabbedPane.setMnemonicAt(0, Mnemonics.get("TurnoutTab")); // NOI18N
        tabbedPane.addTab(Bundle.getMessage("FieldOutputsTabTitle"), outputScrollPanel);
        tabbedPane.setToolTipTextAt(0, Bundle.getMessage("ToolTipOutputTab"));
        tabbedPane.setMnemonicAt(0, Mnemonics.get("OutputTab")); // NOI18N
        cTab = CurrentTab.SENSOR;
        tabbedPane.setSelectedIndex(0);
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                switch(tabbedPane.getSelectedIndex()) {
                    case 2:
                        // Set Add to "Add Output"
                        cTab = CurrentTab.OUTPUT;
                        addButton.setText(Bundle.getMessage("ButtonAddOutput"));
                        saveButton.setText(Bundle.getMessage("ButtonSaveOutputs"));
                        log.debug("Current Tab is: {}", tabbedPane.getSelectedIndex());
                        break;
                    case 1:
                        // Set Add to "Add Turnout"
                        cTab = CurrentTab.TURNOUT;
                        addButton.setText(Bundle.getMessage("ButtonAddTurnout"));
                        saveButton.setText(Bundle.getMessage("ButtonSaveTurnouts"));
                        log.debug("Current Tab is: {}", tabbedPane.getSelectedIndex());
                        break;
                    case 0:
                    default:
                        // Set Add to "Add Sensor"
                        cTab = CurrentTab.SENSOR;
                        addButton.setText(Bundle.getMessage("ButtonAddSensor"));
                        saveButton.setText(Bundle.getMessage("ButtonSaveSensors"));
                        log.debug("Current Tab is: {}", tabbedPane.getSelectedIndex());
                }
            }
        });

        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        JPanel buttonPane2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        //buttonPane2.setLayout(new BoxLayout(buttonPane2, BoxLayout.LINE_AXIS));

        buttonPane.add(addButton);
        buttonPane.add(saveButton);
        buttonPane2.add(closeButton);

        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.getContentPane().add(tabbedPane);
        this.getContentPane().add(buttonPane);
        this.getContentPane().add(buttonPane2);
        this.pack();
        this.setVisible(true);
    }

    private void buildMenu() {
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));

        //fileMenu.add(new LoadVSDFileAction(Bundle.getMessage("MenuItemLoadVSDFile")));
        //fileMenu.add(new StoreXmlVSDecoderAction(Bundle.getMessage("MenuItemSaveProfile")));
        //fileMenu.add(new LoadXmlVSDecoderAction(Bundle.getMessage("MenuItemLoadProfile")));

        JMenu editMenu = new JMenu(Bundle.getMessage("MenuEdit"));
        //editMenu.add(new VSDPreferencesAction(Bundle.getMessage("MenuItemEditPreferences")));

        //fileMenu.getItem(1).setEnabled(false); // disable XML store
        //fileMenu.getItem(2).setEnabled(false); // disable XML load

        menuList = new ArrayList<JMenu>(3);

        menuList.add(fileMenu);
        menuList.add(editMenu);

        this.setJMenuBar(new JMenuBar());
        this.getJMenuBar().add(fileMenu);
        this.getJMenuBar().add(editMenu);
        //this.addHelpMenu("package.jmri.jmrit.vsdecoder.swing.ManageLocationsFrame", true); // NOI18N

    }

    // DCCppListener Methods
    public void message(DCCppReply r) {
        // When we get a SensorDefReply message, add the
        // sensor information to the data map for the model.
        if (r.isSensorDefReply()) {
            Vector<Object> v = new Vector<Object>();
            v.add(r.getSensorDefNumInt());
            v.add(r.getSensorDefPinInt());
            v.add(r.getSensorDefPullupBool());
            //v.add("Delete");
            sensorModel.insertData(v, false);
            sensorSorter.sort();
        } else if (r.isTurnoutDefReply()) {
            Vector<Object> v = new Vector<Object>();
            v.add(r.getTurnoutDefNumInt());
            v.add(r.getTurnoutDefAddrInt());
            v.add(r.getTurnoutDefSubAddrInt());
            turnoutModel.insertData(v, false);
            turnoutSorter.sort();
        } else if (r.isOutputListReply()) {
            Vector<Object> v = new Vector<Object>();
            v.add(r.getOutputNumInt());
            v.add(r.getOutputListPinInt());
            v.add((r.getOutputListIFlagInt() & 0x01) == 1); // (bool) Invert
            v.add((r.getOutputListIFlagInt() & 0x02) == 2); // (bool) Restore State
            v.add((r.getOutputListIFlagInt() & 0x04) == 4); // (bool) Force High
            v.add(r.getOutputListStateInt());
            outputModel.insertData(v, false);
            outputSorter.sort();
        }
    }

    public void message(DCCppMessage m) {
        // Do nothing
    }

    public void notifyTimeout(DCCppMessage m) {
        // Do nothing
    }

    /**
     * Add a standard help menu, including window specific help item.
     *
     * @param ref    JHelp reference for the desired window-specific help page
     * @param direct true if the help menu goes directly to the help system,
     *               e.g. there are no items in the help menu
     *
     * WARNING: BORROWED FROM JmriJFrame.
     */
    public void addHelpMenu(String ref, boolean direct) {
        // only works if no menu present?
        JMenuBar bar = getJMenuBar();
        if (bar == null) {
            bar = new JMenuBar();
        }
        // add Window menu
        bar.add(new WindowMenu(this)); // * GT 28-AUG-2008 Added window menu
        // add Help menu
        jmri.util.HelpUtil.helpMenu(bar, ref, direct);
        setJMenuBar(bar);
    }

    private void addButtonPressed(ActionEvent e) {
        if (cTab == CurrentTab.SENSOR) {
            Vector<Object> v = new Vector<Object>();
            v.add(0);     // Index
            v.add(0);     // Pin
            v.add(false); // Pullup
            sensorModel.insertData(v, true);
        } else if (cTab == CurrentTab.TURNOUT) {
            Vector<Object> v = new Vector<Object>();
            v.add(0); // Index
            v.add(0); // Address
            v.add(0); // Subaddress
            turnoutModel.insertData(v, true);
        } else if (cTab == CurrentTab.OUTPUT) {
            Vector<Object> v = new Vector<Object>();
            v.add(0); // Index
            v.add(0); // Pin
            v.add(false); // Invert
            v.add(false); // Restore state
            v.add(false); // Force high/low
            outputModel.insertData(v, true);
        }
    }

    private void saveButtonPressed(ActionEvent e) {
        int value = JOptionPane.showConfirmDialog(null, Bundle.getMessage("FieldMCFSaveDialogConfirmMessage"),
                Bundle.getMessage("FieldMCFSaveDialogTitle"),
                JOptionPane.YES_NO_OPTION);
        if (sensorTable.getCellEditor() != null) {
            sensorTable.getCellEditor().stopCellEditing();
        }
        if (turnoutTable.getCellEditor() != null) {
            turnoutTable.getCellEditor().stopCellEditing();
        }
        if (outputTable.getCellEditor() != null) {
            outputTable.getCellEditor().stopCellEditing();
        }
        if (value == JOptionPane.YES_OPTION) {
            saveTableValues();
            //OperationsXml.save();
        }
        // TODO: Disabled? Why? Can this go away?
        /*if (Setup.isCloseWindowOnSaveEnabled()) {
            dispose();
        }*/
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "WMI_WRONG_MAP_ITERATOR", justification = "only in slow debug")
    private void saveTableValues() {
        if (cTab == CurrentTab.SENSOR) {
            for (int i = 0; i < sensorModel.getRowData().size(); i++) {

                Vector<Object> r = (Vector<Object>)sensorModel.getRowData().elementAt(i);
                boolean isnew = (boolean)r.elementAt(4);
                boolean isdirty = (boolean)r.elementAt(5);
                boolean isdelete = (boolean)r.elementAt(6);
                int row = sensorModel.getRowData().indexOf(r);
                //if (sensorModel.isNewRow(row)) {
                if (isnew) {
                    tc.sendDCCppMessage(DCCppMessage.makeSensorAddMsg((int)r.elementAt(0),
                                                                      (int)r.elementAt(1),
                                                                      ((boolean)r.elementAt(2) ? 1 : 0)), this);
                    sensorModel.setNewRow(row, false);
                //} else if (sensorModel.isMarkedForDelete(row)) {
                } else if (isdelete) {
                    tc.sendDCCppMessage(DCCppMessage.makeSensorDeleteMsg((int)r.elementAt(0)), this);
                    //log.debug("Sending: " + m);
                    sensorModel.getRowData().remove(r);
                //} else if (sensorModel.isDirtyRow(row)) {
                } else if (isdirty) {
                    // Send a Delete, then an Add (for now).
                    tc.sendDCCppMessage(DCCppMessage.makeSensorDeleteMsg((int)r.elementAt(0)), this);
                    //log.debug("Sending: " + m);
                    // WARNING: Conversions here are brittle. Be careful.
                    tc.sendDCCppMessage(DCCppMessage.makeSensorAddMsg((int)r.elementAt(0),
                                                                      (int)r.elementAt(1),
                                                                      ((boolean)r.elementAt(2) ? 1 : 0)), this);
                    //log.debug("Sending: " + m);
                    sensorModel.setNewRow(row, false);
                    sensorModel.setDirtyRow(row, false);
                }
            }
        } else if (cTab == CurrentTab.TURNOUT) {
            for (int i = 0; i < turnoutModel.getRowData().size(); i++) {

                Vector<Object> r = (Vector<Object>)turnoutModel.getRowData().elementAt(i);
                boolean isnew = (boolean)r.elementAt(4);
                boolean isdirty = (boolean)r.elementAt(5);
                boolean isdelete = (boolean)r.elementAt(6);
                int row = turnoutModel.getRowData().indexOf(r);
                //if (sensorModel.isNewRow(row)) {
                if (isnew) {
                    // WARNING: Conversions here are brittle. Be careful.
                    tc.sendDCCppMessage(DCCppMessage.makeTurnoutAddMsg((int)r.elementAt(0),
                            (int)r.elementAt(1), (int)r.elementAt(2)), this);
                    turnoutModel.setNewRow(row, false);
                //} else if (sensorModel.isMarkedForDelete(row)) {
                } else if (isdelete) {
                    String m = "T " + Integer.toString((int)r.elementAt(0));
                    tc.sendDCCppMessage(DCCppMessage.parseDCCppMessage(m), this);
                    log.debug("Sending: " + m);
                    turnoutModel.getRowData().remove(r);
                //} else if (sensorModel.isDirtyRow(row)) {
                } else if (isdirty) {
                    tc.sendDCCppMessage(DCCppMessage.makeTurnoutDeleteMsg((int)r.elementAt(0)), this);
                    // Send a Delete, then an Add (for now).
                    // WARNING: Conversions here are brittle. Be careful.
                    tc.sendDCCppMessage(DCCppMessage.makeTurnoutAddMsg((int)r.elementAt(0),
                            (int)r.elementAt(1), (int)r.elementAt(2)), this);
                    turnoutModel.setNewRow(row, false);
                    turnoutModel.setDirtyRow(row, false);
                }
            }
        } else if (cTab == CurrentTab.OUTPUT) {
            for (int i = 0; i < outputModel.getRowData().size(); i++) {

                Vector<Object> r = (Vector<Object>)outputModel.getRowData().elementAt(i);
                boolean isnew = (boolean)r.elementAt(6);
                boolean isdirty = (boolean)r.elementAt(7);
                boolean isdelete = (boolean)r.elementAt(8);
                int row = outputModel.getRowData().indexOf(r);
                //if (sensorModel.isNewRow(row)) {
                if (isnew) {
                    // WARNING: Conversions here are brittle. Be careful.
                    int f = ((boolean)r.elementAt(2) ? 1 : 0); // Invert
                    f += ((boolean)r.elementAt(3) ? 2 : 0); // Restore
                    f += ((boolean)r.elementAt(4) ? 4 : 0); // Force
                    tc.sendDCCppMessage(DCCppMessage.makeOutputAddMsg((int)r.elementAt(0),
                            (int)r.elementAt(1), f), this);
                    outputModel.setNewRow(row, false);
                //} else if (sensorModel.isMarkedForDelete(row)) {
                } else if (isdelete) {
                    tc.sendDCCppMessage(DCCppMessage.makeOutputDeleteMsg((int)r.elementAt(0)), this);
                    outputModel.getRowData().remove(r);
                //} else if (sensorModel.isDirtyRow(row)) {
                } else if (isdirty) {
                    // Send a Delete, then an Add (for now).
                    tc.sendDCCppMessage(DCCppMessage.makeOutputDeleteMsg((int)r.elementAt(0)), this);
                    int f = ((boolean)r.elementAt(2) ? 1 : 0); // Invert
                    f += ((boolean)r.elementAt(3) ? 2 : 0); // Restore
                    f += ((boolean)r.elementAt(4) ? 4 : 0); // Force
                    tc.sendDCCppMessage(DCCppMessage.makeOutputAddMsg((int)r.elementAt(0),
                            (int)r.elementAt(1), f), this);
                    outputModel.setNewRow(row, false);
                    outputModel.setDirtyRow(row, false);
                }
            }
        }
        // TODO: Move this to the Close button
        //tc.sendDCCppMessage(new DCCppMessage("E"), this);
        //log.debug("Sending: <E> (Write To EEPROM)");
        //sensorModel.fireTableDataChanged();
    }

    private void closeButtonPressed(ActionEvent e) {
        // If clicked while editing, stop the cell editor(s)
        if (sensorTable.getCellEditor() != null) {
            sensorTable.getCellEditor().stopCellEditing();
        }
        if (turnoutTable.getCellEditor() != null) {
            turnoutTable.getCellEditor().stopCellEditing();
        }
        if (outputTable.getCellEditor() != null) {
            outputTable.getCellEditor().stopCellEditing();
        }

        // If clicked while changes not saved to BaseStation, offer
        // the option of saving.
        if (sensorModel.isDirty() || turnoutModel.isDirty() || outputModel.isDirty()) {
            int value = JOptionPane.showConfirmDialog(null, Bundle.getMessage("FieldMCFSaveDialogConfirmMessage"),
                    Bundle.getMessage("FieldMCFSaveDialogTitle"),
                    JOptionPane.YES_NO_OPTION);
            if (value == JOptionPane.YES_OPTION) {
                saveTableValues();
            }

            // Offer to write the changes to EEPROM
            value = JOptionPane.showConfirmDialog(null, Bundle.getMessage("FieldMCFCloseDialogConfirmMessage"),
                    Bundle.getMessage("FieldMCFCloseDialogTitle"),
                    JOptionPane.YES_NO_OPTION);

            if (value == JOptionPane.YES_OPTION) {
                tc.sendDCCppMessage(DCCppMessage.parseDCCppMessage("E"), this);
                log.debug("Sending: <E> (Write To EEPROM)");
                sensorModel.fireTableDataChanged();
            }

        } else {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("FieldMCFCloseNoChangesDialog"));
        }

        // Close the window
        dispose();
    }

    static private Logger log = LoggerFactory.getLogger(ConfigSensorsAndTurnoutsFrame.class.getName());

    /**
     * Private class to serve as TableModel for Reporters and Ops Locations
     */
    @SuppressWarnings("unused")
    private static class SensorTableModel extends AbstractTableModel {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        // These get internationalized at runtime in the constructor below.
        private String[] columnNames = new String[7];
        private Vector<Vector<Object>> rowData = new Vector<Vector<Object>>();
        //private Vector isNew = new Vector();
        //private Vector isDirty = new Vector();
        //private Vector markDelete = new Vector();

        public SensorTableModel() {
            super();
            // Use i18n-ized column titles.
            //columnNames[0] = Bundle.getMessage("FieldTableNameColumn");
            columnNames[0] = Bundle.getMessage("FieldTableIndexColumn");
            columnNames[1] = Bundle.getMessage("FieldTablePinColumn");
            columnNames[2] = Bundle.getMessage("FieldTablePullupColumn");
            columnNames[3] = Bundle.getMessage("FieldTableDeleteColumn");
            columnNames[4] = "isNew";       // hidden column // NOI18N
            columnNames[5] = "isDirty";     // hidden column // NOI18N
            columnNames[6] = "isDelete";    // hidden column // NOI18N
            rowData = new Vector<Vector<Object>>();
        }

        /**
         * Deprecated
         * Use insertData(Vector, Boolean) instead
         * @param values
         * @param isnew
         */
        // Note: May be obsoleted by insertData(Vector v)
        @Deprecated
        public void insertData(Object[] values, boolean isnew) {
            Vector<Object> v = new Vector<Object>();
            for (int i = 0; i < values.length; i++) {
                v.add(values[i]);
            }
            //v.add("Delete"); // TODO: Fix this
            //v.add(isnew);
            //v.add(false);
            //v.add(false);
            insertData(v, isnew);
        }

        public boolean isNewRow(int row) {
            //return((boolean) isNew.elementAt(row));
            return((boolean)((Vector<Object>)rowData.elementAt(row)).elementAt(4));
        }

        public void setNewRow(int row, boolean n) {
            //isNew.setElementAt(n, row);
            ((Vector<Object>)rowData.elementAt(row)).setElementAt(n, 4);
        }

        public boolean isDirtyRow(int row) {
            //return((boolean)isDirty.elementAt(row));
            return((boolean)((Vector<Object>)rowData.elementAt(row)).elementAt(5));
        }

        public void setDirtyRow(int row, boolean d) {
            //isDirty.setElementAt(d, row);
            ((Vector<Object>)rowData.elementAt(row)).setElementAt(d, 5);
        }

        public boolean isMarkedForDelete(int row) {
            //return((boolean)markDelete.elementAt(row));
            return((boolean)((Vector<Object>)rowData.elementAt(row)).elementAt(6));
        }

        public void markForDelete(int row, boolean mark) {
            //markDelete.setElementAt(mark, row);
            ((Vector<Object>)rowData.elementAt(row)).setElementAt(mark, 6);
        }

        public boolean isDirty() {
            for (int i = 0; i < rowData.size(); i++) {
                if (isDirtyRow(i)) {
                    return(true);
                }
            }
            return(false);
        }

        public boolean contains(Vector<Object> v) {
            Iterator<Vector<Object>> it = rowData.iterator();
            while(it.hasNext()) {
                Vector<Object> r = (Vector<Object>)it.next();
                if (r.firstElement() == v.firstElement()) {
                    return(true);
                }
            }
            return(false);
        }

        public void insertData(Vector<Object> v, boolean isnew) {
            if (!rowData.contains(v)) {
                v.add("Delete");
                v.add(isnew); // is new
                v.add(false); // is dirty (no)
                v.add(false); // is marked for delete (of course not)
                rowData.add(v);
            }
            fireTableDataChanged();
        }

        public Vector<Vector<Object>> getRowData() {
            return(rowData);
        }

        public String getColumnName(int col) {
            return columnNames[col].toString();
        }

        public int getRowCount() {
            return rowData.size();
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public Object getValueAt(int row, int col) {
            return(((Vector<Object>)rowData.elementAt(row)).elementAt(col));
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public void setValueAt(Object value, int row, int col) {
            ((Vector<Object>)rowData.elementAt(row)).setElementAt(value, col);
            if (col < 3) {
                // Only set dirty if data changed, not state
                // Data is in columns 0-2
                setDirtyRow(row, true);
            }
            fireTableCellUpdated(row, col);
        }

        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                case 1:
                    return Integer.class;
                case 2:
                    return Boolean.class;
                case 3:
                    return ButtonEditor.class;
                case 4:
                case 5:
                case 6:
                    return Boolean.class;
                default:
                    return super.getColumnClass(columnIndex);
            }
        }
    }

    /**
     * Private class to serve as TableModel for Reporters and Ops Locations
     */
    @SuppressWarnings("unused")
    private static class TurnoutTableModel extends AbstractTableModel {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        // These get internationalized at runtime in the constructor below.
        private String[] columnNames = new String[7];
        private Vector<Vector<Object>> rowData = new Vector<Vector<Object>>();

        public TurnoutTableModel() {
            super();
            // Use i18n-ized column titles.
            //columnNames[0] = Bundle.getMessage("FieldTableNameColumn");
            columnNames[0] = Bundle.getMessage("FieldTableIndexColumn");
            columnNames[1] = Bundle.getMessage("FieldTableAddressColumn");
            columnNames[2] = Bundle.getMessage("FieldTableSubaddrColumn");
            columnNames[3] = Bundle.getMessage("FieldTableDeleteColumn");
            columnNames[4] = "isNew";        // hidden column // NOI18N
            columnNames[5] = "isDirty";      // hidden column // NOI18N
            columnNames[6] = "isDelete";     // hidden column // NOI18N
            rowData = new Vector<Vector<Object>>();
        }

        /**
         * Deprecated
         * Use insertData(Vector, Boolean) instead
         * @param values
         * @param isnew
         */
        // Note: May be obsoleted by insertData(Vector v)
        @Deprecated
        public void insertData(Object[] values, boolean isnew) {
            Vector<Object> v = new Vector<Object>();
            for (int i = 0; i < values.length; i++) {
                v.add(values[i]);
            }
            insertData(v, isnew);
        }

        public boolean isNewRow(int row) {
            //return((boolean) isNew.elementAt(row));
            return((boolean)((Vector<Object>)rowData.elementAt(row)).elementAt(4));
        }

        public void setNewRow(int row, boolean n) {
            //isNew.setElementAt(n, row);
            ((Vector<Object>)rowData.elementAt(row)).setElementAt(n, 4);
        }

        public boolean isDirtyRow(int row) {
            //return((boolean)isDirty.elementAt(row));
            return((boolean)((Vector<Object>)rowData.elementAt(row)).elementAt(5));
        }

        public void setDirtyRow(int row, boolean d) {
            //isDirty.setElementAt(d, row);
            ((Vector<Object>)rowData.elementAt(row)).setElementAt(d, 5);
        }

        public boolean isMarkedForDelete(int row) {
            //return((boolean)markDelete.elementAt(row));
            return((boolean)((Vector<Object>)rowData.elementAt(row)).elementAt(6));
        }

        public void markForDelete(int row, boolean mark) {
            //markDelete.setElementAt(mark, row);
            ((Vector<Object>)rowData.elementAt(row)).setElementAt(mark, 6);
        }

        public boolean isDirty() {
            for (int i = 0; i < rowData.size(); i++) {
                if (isDirtyRow(i)) {
                    return(true);
                }
            }
            return(false);
        }

        public boolean contains(Vector<Object> v) {
            Iterator<Vector<Object>> it = rowData.iterator();
            while(it.hasNext()) {
                Vector<Object> r = it.next();
                if (r.firstElement() == v.firstElement()) {
                    return(true);
                }
            }
            return(false);
        }

        public void insertData(Vector<Object> v, boolean isnew) {
            if (!rowData.contains(v)) {
                v.add("Delete");
                v.add(isnew); // is new
                v.add(false); // is dirty (no)
                v.add(false); // is marked for delete (of course not)
                rowData.add(v);
            }
            fireTableDataChanged();
        }

        public Vector<Vector<Object>> getRowData() {
            return(rowData);
        }

        public String getColumnName(int col) {
            return columnNames[col].toString();
        }

        public int getRowCount() {
            return rowData.size();
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public Object getValueAt(int row, int col) {
            return(((Vector<Object>)rowData.elementAt(row)).elementAt(col));
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public void setValueAt(Object value, int row, int col) {
            ((Vector<Object>)((Vector<Object>)rowData.elementAt(row))).setElementAt(value, col);
            if (col < 3) {
                // Only set dirty if data changed, not state
                // Data is in columns 0-2
                setDirtyRow(row, true);
            }
            fireTableCellUpdated(row, col);
        }

        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                case 1:
                case 2:
                    return Integer.class;
                case 3:
                    return ConfigSensorsAndTurnoutsFrame.ButtonEditor.class;
                case 4:
                case 5:
                case 6:
                    return Boolean.class;
                default:
                    return super.getColumnClass(columnIndex);
            }
        }
    }

        /**
     * Private class to serve as TableModel for Reporters and Ops Locations
     */
    @SuppressWarnings("unused")
    private static class OutputTableModel extends AbstractTableModel {

        /**
             *
             */
            private static final long serialVersionUID = 1L;
        // These get internationalized at runtime in the constructor below.
        private String[] columnNames = new String[9];
        private Vector<Vector<Object>> rowData = new Vector<Vector<Object>>();

        public OutputTableModel() {
            super();
            // Use i18n-ized column titles.
            //columnNames[0] = Bundle.getMessage("FieldTableNameColumn");
            columnNames[0] = Bundle.getMessage("FieldTableIndexColumn");
            columnNames[1] = Bundle.getMessage("FieldTablePinColumn");
            columnNames[2] = Bundle.getMessage("FieldTableInvertColumn");
            columnNames[3] = Bundle.getMessage("FieldTableOutputRestoreStateColumn");
            columnNames[4] = Bundle.getMessage("FieldTableOutputForceToColumn");
            columnNames[5] = Bundle.getMessage("FieldTableDeleteColumn");
            columnNames[6] = "isNew";        // hidden column // NOI18N
            columnNames[7] = "isDirty";      // hidden column // NOI18N
            columnNames[8] = "isDelete";     // hidden column // NOI18N
            rowData = new Vector<Vector<Object>>();
        }

        /**
         * Deprecated
         * Use insertData(Vector, Boolean) instead
         * @param values
         * @param isnew
         */
        // Note: May be obsoleted by insertData(Vector v)
        @Deprecated
        public void insertData(Object[] values, boolean isnew) {
            Vector<Object> v = new Vector<Object>();
            for (int i = 0; i < values.length; i++) {
                v.add(values[i]);
            }
            insertData(v, isnew);
        }

        public boolean isNewRow(int row) {
            //return((boolean) isNew.elementAt(row));
            return((boolean)((Vector<Object>)rowData.elementAt(row)).elementAt(6));
        }

        public void setNewRow(int row, boolean n) {
            //isNew.setElementAt(n, row);
            ((Vector<Object>)rowData.elementAt(row)).setElementAt(n, 6);
        }

        public boolean isDirtyRow(int row) {
            //return((boolean)isDirty.elementAt(row));
            return((boolean)((Vector<Object>)rowData.elementAt(row)).elementAt(7));
        }

        public void setDirtyRow(int row, boolean d) {
            //isDirty.setElementAt(d, row);
            ((Vector<Object>)rowData.elementAt(row)).setElementAt(d, 7);
        }

        public boolean isMarkedForDelete(int row) {
            //return((boolean)markDelete.elementAt(row));
            return((boolean)((Vector<Object>)rowData.elementAt(row)).elementAt(8));
        }

        public void markForDelete(int row, boolean mark) {
            //markDelete.setElementAt(mark, row);
            ((Vector<Object>)rowData.elementAt(row)).setElementAt(mark, 8);
        }

        public boolean isDirty() {
            for (int i = 0; i < rowData.size(); i++) {
                if (isDirtyRow(i)) {
                    return(true);
                }
            }
            return(false);
        }

        public boolean contains(Vector<Object> v) {
            Iterator<Vector<Object>> it = rowData.iterator();
            while(it.hasNext()) {
                Vector<Object> r = it.next();
                if (r.firstElement() == v.firstElement()) {
                    return(true);
                }
            }
            return(false);
        }

        public void insertData(Vector<Object> v, boolean isnew) {
            if (!rowData.contains(v)) {
                v.add("Delete");
                v.add(isnew); // is new
                v.add(false); // is dirty (no)
                v.add(false); // is marked for delete (of course not)
                rowData.add(v);
            }
            fireTableDataChanged();
        }

        public Vector<Vector<Object>> getRowData() {
            return(rowData);
        }

        public String getColumnName(int col) {
            return columnNames[col].toString();
        }

        public int getRowCount() {
            return rowData.size();
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public Object getValueAt(int row, int col) {
            return(((Vector<Object>)rowData.elementAt(row)).elementAt(col));
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public void setValueAt(Object value, int row, int col) {
            ((Vector<Object>)((Vector<Object>)rowData.elementAt(row))).setElementAt(value, col);
            if (col < 5) {
                // Only set dirty if data changed, not state
                // Data is in columns 0-2
                setDirtyRow(row, true);
            }
            fireTableCellUpdated(row, col);
        }

        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                case 1:
                    return Integer.class;
                case 2:
                case 3:
                case 4:
                    return Boolean.class;
                case 5:
                    return ConfigSensorsAndTurnoutsFrame.ButtonEditor.class;
                case 6:
                case 7:
                case 8:
                    return Boolean.class;
                default:
                    return super.getColumnClass(columnIndex);
            }
        }
    }


    static class ButtonRenderer extends JButton implements TableCellRenderer {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        protected JButton button;
        private String label;
        private boolean isPushed;
        private JTable table;

        public ButtonEditor(JCheckBox checkBox, JTable t) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            table = t;
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                int sel = table.getEditingRow();
                SensorTableModel model = (SensorTableModel) table.getModel();
                int idx = (int)model.getValueAt(sel,0);
                if (model.isMarkedForDelete(sel)) {
                    model.markForDelete(sel, false);
                    log.debug("UnDelete sensor {}", idx);
                    JOptionPane.showMessageDialog(button, "Sensor " + Integer.toString(idx) +
                                                " Not Marked for Deletion");
                } else {
                    model.markForDelete(sel, true);
                    log.debug("Delete sensor {}", idx);
                    JOptionPane.showMessageDialog(button, "Sensor " + Integer.toString(idx) +
                                                " Marked for Deletion");
                }
            }
            isPushed = false;
            return label;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

}
