package jmri.jmrix.dccpp.swing;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.swing.event.EventListenerList;
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
public class ConfigBaseStationFrame extends JmriJFrame implements DCCppListener {

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

    private final DCCppTrafficController tc;

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

    private enum CurrentTab {
        SENSOR, TURNOUT, OUTPUT
    }
    private CurrentTab cTab;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "2D array of different types passed as complex parameter. "
            + "Better to switch to passing use-specific objects rather than "
            + "papering this over with a deep copy of the arguments. "
            + "In any case, there's no risk of exposure here.")
    public ConfigBaseStationFrame(DCCppSensorManager sm,
            DCCppTurnoutManager tm,
            DCCppTrafficController t) {
        super(false, false);
        tc = t;
        initGui();
    }

    private void initGui() {

        // NOTE: Look at jmri.jmrit.vsdecoder.swing.ManageLocationsFrame
        // for how to add a tab for turnouts and other things.
        this.setTitle(Bundle.getMessage("FieldManageBaseStationFrameTitle"));
        this.buildMenu();

        // Panel for managing sensors
        sensorPanel = new JPanel();
        sensorPanel.setLayout(new GridBagLayout());

        JButton addButton = new JButton(Bundle.getMessage("ButtonAddSensor"));
        addButton.setToolTipText(Bundle.getMessage("ToolTipButtonMSFAdd"));
        addButton.setMnemonic(Mnemonics.get("AddButton")); // NOI18N
        addButton.addActionListener((ActionEvent e) -> {
            addButtonPressed(e);
        });

        JButton closeButton = new JButton(Bundle.getMessage("ButtonClose"));
        closeButton.setToolTipText(Bundle.getMessage("ToolTipButtonMSFClose"));
        closeButton.setMnemonic(Mnemonics.get("CloseButton")); // NOI18N
        closeButton.addActionListener((ActionEvent e) -> {
            closeButtonPressed(e);
        });
        JButton saveButton = new JButton(Bundle.getMessage("ButtonSaveSensors"));
        saveButton.setToolTipText(Bundle.getMessage("ToolTipButtonMSFSave"));
        saveButton.setMnemonic(Mnemonics.get("SaveButton")); // NOI18N
        saveButton.addActionListener((ActionEvent e) -> {
            saveButtonPressed(e);
        });

        JScrollPane sensorScrollPanel = new JScrollPane();
        sensorModel = new SensorTableModel();
        sensorTable = new JTable(sensorModel);
        sensorTable.setFillsViewportHeight(true);
        sensorScrollPanel.getViewport().add(sensorTable);
        sensorTable.setPreferredScrollableViewportSize(new Dimension(520, 200));
        sensorTable.getColumn(Bundle.getMessage("FieldTableDeleteColumn")).setCellRenderer(new ButtonRenderer());
        sensorTable.removeColumn(sensorTable.getColumn("isNew"));
        sensorTable.removeColumn(sensorTable.getColumn("isDirty"));
        sensorTable.removeColumn(sensorTable.getColumn("isDelete"));
        sensorTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleTableMouseClick(sensorTable, evt);
            }
        });
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
        turnoutTable.removeColumn(turnoutTable.getColumn("isNew"));
        turnoutTable.removeColumn(turnoutTable.getColumn("isDirty"));
        turnoutTable.removeColumn(turnoutTable.getColumn("isDelete"));
        turnoutTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleTableMouseClick(turnoutTable, evt);
            }
        });
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
        outputTable.removeColumn(outputTable.getColumn("isNew"));
        outputTable.removeColumn(outputTable.getColumn("isDirty"));
        outputTable.removeColumn(outputTable.getColumn("isDelete"));
        outputTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleTableMouseClick(outputTable, evt);
            }
        });
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
        tabbedPane.addChangeListener((ChangeEvent e) -> {
            switch (tabbedPane.getSelectedIndex()) {
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
        });

        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel buttonPane2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));

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
        menuList = new ArrayList<>(3);

        menuList.add(fileMenu);
        menuList.add(editMenu);

        this.setJMenuBar(new JMenuBar());
        this.getJMenuBar().add(fileMenu);
        this.getJMenuBar().add(editMenu);
        //this.addHelpMenu("package.jmri.jmrit.vsdecoder.swing.ManageLocationsFrame", true); // NOI18N

    }

    // DCCppListener Methods
    @Override
    public void message(DCCppReply r) {
        // When we get a SensorDefReply message, add the
        // sensor information to the data map for the model.
        if (r.isSensorDefReply()) {
            List<Object> v = new ArrayList<>();
            v.add(r.getSensorDefNumInt());
            v.add(r.getSensorDefPinInt());
            v.add(r.getSensorDefPullupBool());
            //v.add("Delete");
            sensorModel.insertData(v, false);
            sensorSorter.sort();
        } else if (r.isTurnoutDefReply()) {
            List<Object> v = new ArrayList<>();
            v.add(r.getTurnoutDefNumInt());
            v.add(r.getTurnoutDefAddrInt());
            v.add(r.getTurnoutDefSubAddrInt());
            turnoutModel.insertData(v, false);
            turnoutSorter.sort();
        } else if (r.isOutputListReply()) {
            List<Object> v = new ArrayList<>();
            v.add(r.getOutputNumInt());
            v.add(r.getOutputListPinInt());
            v.add((r.getOutputListIFlagInt() & 0x01) == 1); // (bool) Invert
            v.add((r.getOutputListIFlagInt() & 0x02) == 2); // (bool) Restore State
            v.add((r.getOutputListIFlagInt() & 0x04) == 4); // (bool) Force High
            //v.add(r.getOutputListStateInt());
            outputModel.insertData(v, false);
            outputSorter.sort();
        }
    }

    @Override
    public void message(DCCppMessage m) {
        // Do nothing
    }

    @Override
    public void notifyTimeout(DCCppMessage m) {
        // Do nothing
    }

    /**
     * Handle mouse clicks within a table.
     * <p>
     * This is currently the workings behind the "Delete" button in the table.
     *
     * @param table the table where the event occurred
     * @param evt   the mouse click
     */
    private void handleTableMouseClick(JTable table, java.awt.event.MouseEvent evt) {
        int row = table.rowAtPoint(evt.getPoint());
        int col = table.columnAtPoint(evt.getPoint());
        if (row < 0 || col < 0) {
            return;
        }
        DCCppTableModel model = (DCCppTableModel) table.getModel();
        if (col == table.convertColumnIndexToView(model.getDeleteColumn())) {
            // This is a row delete action.  Handle it as such.
            int sel = table.convertRowIndexToModel(row);
            int idx = (int) model.getValueAt(sel, 0);
            int value = JOptionPane.showConfirmDialog(null, "Delete ID " + Integer.toString(idx) + "\nAre you sure?",
                    "Delete Item",
                    JOptionPane.OK_CANCEL_OPTION);
            if (value == JOptionPane.OK_OPTION) {
                model.removeRow(sel);
                log.debug("Delete sensor {}", idx);
            }

        }
    }

    /**
     * Responder for pressing the "Add" button. Response depends on which tab is
     * active.
     *
     * @param e the press event
     */
    private void addButtonPressed(ActionEvent e) {
        if (null != cTab) {
            switch (cTab) {
                case SENSOR: {
                    List<Object> v = new ArrayList<>();
                    v.add(0);     // Index
                    v.add(0);     // Pin
                    v.add(false); // Pullup
                    sensorModel.insertData(v, true);
                    break;
                }
                case TURNOUT: {
                    List<Object> v = new ArrayList<>();
                    v.add(0); // Index
                    v.add(0); // Address
                    v.add(0); // Subaddress
                    turnoutModel.insertData(v, true);
                    break;
                }
                case OUTPUT: {
                    List<Object> v = new ArrayList<>();
                    v.add(0); // Index
                    v.add(0); // Pin
                    v.add(false); // Invert
                    v.add(false); // Restore state
                    v.add(false); // Force high/low
                    outputModel.insertData(v, true);
                    break;
                }
                default:
                    break;
            }
        }
    }

    /**
     * Respond to the user pressing the "Save Sensors/Turnouts/Outputs" button.
     *
     * @param e the button press event
     */
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

    /**
     * Save the values for the currently selected tab.
     */
    private void saveTableValues() {
        if (null != cTab) {
            switch (cTab) {
                case SENSOR:
                    for (int i = 0; i < sensorModel.getRowData().size(); i++) {

                        List<Object> r = sensorModel.getRowData().get(i);
                        boolean isnew = (boolean) r.get(4);
                        boolean isdirty = (boolean) r.get(5);
                        boolean isdelete = (boolean) r.get(6);
                        int row = sensorModel.getRowData().indexOf(r);
                        //if (sensorModel.isNewRow(row)) {
                        if (isnew) {
                            tc.sendDCCppMessage(DCCppMessage.makeSensorAddMsg((int) r.get(0),
                                    (int) r.get(1),
                                    ((boolean) r.get(2) ? 1 : 0)), this);
                            sensorModel.setNewRow(row, false);
                            //} else if (sensorModel.isMarkedForDelete(row)) {
                        } else if (isdelete) {
                            tc.sendDCCppMessage(DCCppMessage.makeSensorDeleteMsg((int) r.get(0)), this);
                            //log.debug("Sending: " + m);
                            sensorModel.getRowData().remove(r);
                            //} else if (sensorModel.isDirtyRow(row)) {
                        } else if (isdirty) {
                            // Send a Delete, then an Add (for now).
                            tc.sendDCCppMessage(DCCppMessage.makeSensorDeleteMsg((int) r.get(0)), this);
                            //log.debug("Sending: " + m);
                            // WARNING: Conversions here are brittle. Be careful.
                            tc.sendDCCppMessage(DCCppMessage.makeSensorAddMsg((int) r.get(0),
                                    (int) r.get(1),
                                    ((boolean) r.get(2) ? 1 : 0)), this);
                            //log.debug("Sending: " + m);
                            sensorModel.setNewRow(row, false);
                            sensorModel.setDirtyRow(row, false);
                        }
                    }
                    break;
                case TURNOUT:
                    for (int i = 0; i < turnoutModel.getRowData().size(); i++) {

                        List<Object> r = turnoutModel.getRowData().get(i);
                        boolean isnew = (boolean) r.get(4);
                        boolean isdirty = (boolean) r.get(5);
                        boolean isdelete = (boolean) r.get(6);
                        int row = turnoutModel.getRowData().indexOf(r);
                        //if (sensorModel.isNewRow(row)) {
                        if (isnew) {
                            // WARNING: Conversions here are brittle. Be careful.
                            tc.sendDCCppMessage(DCCppMessage.makeTurnoutAddMsg((int) r.get(0),
                                    (int) r.get(1), (int) r.get(2)), this);
                            turnoutModel.setNewRow(row, false);
                            //} else if (sensorModel.isMarkedForDelete(row)) {
                        } else if (isdelete) {
                            String m = "T " + Integer.toString((int) r.get(0));
                            tc.sendDCCppMessage(DCCppMessage.parseDCCppMessage(m), this);
                            log.debug("Sending: {}", m);
                            turnoutModel.getRowData().remove(r);
                            //} else if (sensorModel.isDirtyRow(row)) {
                        } else if (isdirty) {
                            tc.sendDCCppMessage(DCCppMessage.makeTurnoutDeleteMsg((int) r.get(0)), this);
                            // Send a Delete, then an Add (for now).
                            // WARNING: Conversions here are brittle. Be careful.
                            tc.sendDCCppMessage(DCCppMessage.makeTurnoutAddMsg((int) r.get(0),
                                    (int) r.get(1), (int) r.get(2)), this);
                            turnoutModel.setNewRow(row, false);
                            turnoutModel.setDirtyRow(row, false);
                        }
                    }
                    break;
                case OUTPUT:
                    for (int i = 0; i < outputModel.getRowData().size(); i++) {

                        List<Object> r = outputModel.getRowData().get(i);
                        boolean isnew = (boolean) r.get(6);
                        boolean isdirty = (boolean) r.get(7);
                        boolean isdelete = (boolean) r.get(8);
                        int row = outputModel.getRowData().indexOf(r);
                        //if (sensorModel.isNewRow(row)) {
                        if (isnew) {
                            // WARNING: Conversions here are brittle. Be careful.
                            int f = ((boolean) r.get(2) ? 1 : 0); // Invert
                            f += ((boolean) r.get(3) ? 2 : 0); // Restore
                            f += ((boolean) r.get(4) ? 4 : 0); // Force
                            tc.sendDCCppMessage(DCCppMessage.makeOutputAddMsg((int) r.get(0),
                                    (int) r.get(1), f), this);
                            outputModel.setNewRow(row, false);
                            //} else if (sensorModel.isMarkedForDelete(row)) {
                        } else if (isdelete) {
                            tc.sendDCCppMessage(DCCppMessage.makeOutputDeleteMsg((int) r.get(0)), this);
                            outputModel.getRowData().remove(r);
                            //} else if (sensorModel.isDirtyRow(row)) {
                        } else if (isdirty) {
                            // Send a Delete, then an Add (for now).
                            tc.sendDCCppMessage(DCCppMessage.makeOutputDeleteMsg((int) r.get(0)), this);
                            int f = ((boolean) r.get(2) ? 1 : 0); // Invert
                            f += ((boolean) r.get(3) ? 2 : 0); // Restore
                            f += ((boolean) r.get(4) ? 4 : 0); // Force
                            tc.sendDCCppMessage(DCCppMessage.makeOutputAddMsg((int) r.get(0),
                                    (int) r.get(1), f), this);
                            outputModel.setNewRow(row, false);
                            outputModel.setDirtyRow(row, false);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        // Offer to write the changes to EEPROM
        int value = JOptionPane.showConfirmDialog(null, Bundle.getMessage("FieldMCFCloseDialogConfirmMessage"),
                Bundle.getMessage("FieldMCFCloseDialogTitle"),
                JOptionPane.YES_NO_OPTION);

        if (value == JOptionPane.YES_OPTION) {
            tc.sendDCCppMessage(DCCppMessage.parseDCCppMessage("E"), this);
            log.debug("Sending: <E> (Write To EEPROM)");
            // These might not actually be necessary...
            sensorModel.fireTableDataChanged();
            turnoutModel.fireTableDataChanged();
            outputModel.fireTableDataChanged();
        }
    }

    /**
     * Respond to the user pressing the "Close" button.
     *
     * @param e the button press event
     */
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

    private final static Logger log = LoggerFactory.getLogger(ConfigBaseStationFrame.class);

    /**
     * Private class to serve as TableModel for Sensors
     */
    @SuppressWarnings("unused")
    private static class SensorTableModel extends DCCppTableModel {

        public SensorTableModel() {
            super(4, 5, 6, 7);
            // Use i18n-ized column titles.
            columnNames = new String[7];
            columnNames[0] = Bundle.getMessage("FieldTableIndexColumn");
            columnNames[1] = Bundle.getMessage("FieldTablePinColumn");
            columnNames[2] = Bundle.getMessage("FieldTablePullupColumn");
            columnNames[3] = Bundle.getMessage("FieldTableDeleteColumn");
            columnNames[4] = "isNew";       // hidden column // NOI18N
            columnNames[5] = "isDirty";     // hidden column // NOI18N
            columnNames[6] = "isDelete";    // hidden column // NOI18N
        }

        @Override
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
    private static class TurnoutTableModel extends DCCppTableModel {

        public TurnoutTableModel() {
            super(4, 5, 6, 7);
            // Use i18n-ized column titles.
            columnNames = new String[7];
            columnNames[0] = Bundle.getMessage("FieldTableIndexColumn");
            columnNames[1] = Bundle.getMessage("FieldTableAddressColumn");
            columnNames[2] = Bundle.getMessage("FieldTableSubaddrColumn");
            columnNames[3] = Bundle.getMessage("FieldTableDeleteColumn");
            columnNames[4] = "isNew";        // hidden column // NOI18N
            columnNames[5] = "isDirty";      // hidden column // NOI18N
            columnNames[6] = "isDelete";     // hidden column // NOI18N
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                case 1:
                case 2:
                    return Integer.class;
                case 3:
                    return ConfigBaseStationFrame.ButtonEditor.class;
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
    private static class OutputTableModel extends DCCppTableModel {

        public OutputTableModel() {
            super(6, 7, 8, 9);
            // Use i18n-ized column titles.
            columnNames = new String[9];
            columnNames[0] = Bundle.getMessage("FieldTableIndexColumn");
            columnNames[1] = Bundle.getMessage("FieldTablePinColumn");
            columnNames[2] = Bundle.getMessage("FieldTableInvertColumn");
            columnNames[3] = Bundle.getMessage("FieldTableOutputRestoreStateColumn");
            columnNames[4] = Bundle.getMessage("FieldTableOutputForceToColumn");
            columnNames[5] = Bundle.getMessage("FieldTableDeleteColumn");
            columnNames[6] = "isNew";        // hidden column // NOI18N
            columnNames[7] = "isDirty";      // hidden column // NOI18N
            columnNames[8] = "isDelete";     // hidden column // NOI18N
        }

        @Override
        public int getDeleteColumn() {
            return (5);
        }

        @Override
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
                    return ConfigBaseStationFrame.ButtonEditor.class;
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

        public ButtonRenderer() {
            super.setOpaque(true);
            super.setSelected(false);
        }

        @Override
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

    /**
     * Button Editor class to replace the DefaultCellEditor in the table for the
     * delete button.
     * <p>
     * NOTE: This isn't actually used anymore except as being a unique class
     * type that can be returned from the TableModel classes for the column that
     * includes the Delete buttons.
     */
    class ButtonEditor extends DefaultCellEditor {

        protected JButton button;
        private String label;
        public ButtonEditor(JCheckBox checkBox, JTable t) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener((ActionEvent e) -> {
                //fireEditingStopped();
            });
        }

        @Override
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
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }
    }

}
