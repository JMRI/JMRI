package jmri.jmrit.signalling;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.Block;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.SignalMastLogic;
import jmri.SignalMastManager;
import jmri.Turnout;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.util.com.sun.TableSorter;
import jmri.util.swing.JmriBeanComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 */
public class SignallingPanel extends jmri.util.swing.JmriPanel {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.SignallingBundle");

    JmriBeanComboBox sourceMastBox;
    JmriBeanComboBox destMastBox;
    JLabel fixedSourceMastLabel = new JLabel();
    JLabel fixedDestMastLabel = new JLabel();
    JLabel sourceMastLabel = new JLabel(rb.getString("SourceMast")+":");
    JLabel destMastLabel = new JLabel(rb.getString("DestMast")+":");

    JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
    JButton updateButton = new JButton(rb.getString("UpdateLogic"));
    JCheckBox useLayoutEditor = new JCheckBox(rb.getString("UseLayoutEditorPaths"));
    JCheckBox useLayoutEditorTurnout = new JCheckBox(rb.getString("UseTurnoutDetails"));
    JCheckBox useLayoutEditorBlock = new JCheckBox(rb.getString("UseBlockDetails"));
    JCheckBox allowAutoMastGeneration = new JCheckBox(rb.getString("AllowAutomaticSignalMast"));
    JCheckBox lockTurnouts = new JCheckBox(rb.getString("LockTurnouts"));

    SignalMast sourceMast;
    SignalMast destMast;
    SignalMastLogic sml;

    SignalMastManager smm = InstanceManager.signalMastManagerInstance();

    jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    JFrame jFrame;

    public SignallingPanel(JFrame frame) {
        this(null, null, frame);
    }

    public SignallingPanel(SignalMast source, SignalMast dest, JFrame frame) {
        super();
        jFrame = frame;
        JLabel mastSpeed = new JLabel();

        if (source != null) {
            this.sourceMast = source;
            this.sml = InstanceManager.signalMastLogicManagerInstance().getSignalMastLogic(source);
            fixedSourceMastLabel = new JLabel(sourceMast.getDisplayName());
            if (dest != null) {
                frame.setTitle(source.getDisplayName() + " to " + dest.getDisplayName());
            }
        }
        if ((dest != null) && (sml != null)) {
            this.destMast = dest;
            if (!sml.isDestinationValid(dest)) {
                sml.setDestinationMast(dest);
            }
            fixedDestMastLabel = new JLabel(destMast.getDisplayName());
            useLayoutEditor.setSelected(sml.useLayoutEditor(destMast));
            useLayoutEditorTurnout.setSelected(sml.useLayoutEditorTurnouts(destMast));
            useLayoutEditorBlock.setSelected(sml.useLayoutEditorBlocks(destMast));
            allowAutoMastGeneration.setSelected(sml.allowAutoMaticSignalMastGeneration(destMast));
            lockTurnouts.setSelected(sml.isTurnoutLockAllowed(destMast));

            Float pathSpeed = sml.getMaximumSpeed(dest);
            if (pathSpeed == 0.0f) {
                mastSpeed.setText(rb.getString("PathSpeed") + " : " + rb.getString("NoneSet"));
            } else {
                String speed = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getNamedSpeed(pathSpeed);
                if (speed != null) {
                    mastSpeed.setText(rb.getString("PathSpeed") + " : " + speed);
                } else {
                    mastSpeed.setText(rb.getString("PathSpeed") + " : " + Float.toString(pathSpeed));
                }
            }
        } else if (dest == null) {
            sml = null;
        }

        sourceMastBox = new JmriBeanComboBox(smm, sourceMast, JmriBeanComboBox.DISPLAYNAME);
        destMastBox = new JmriBeanComboBox(smm, destMast, JmriBeanComboBox.DISPLAYNAME);
        //signalMastCombo(sourceMastBox, sourceMast);
        //signalMastCombo(destMastBox, destMast);

        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JPanel sourcePanel = new JPanel();
        sourcePanel.add(sourceMastLabel);
        sourcePanel.add(sourceMastBox);
        sourcePanel.add(fixedSourceMastLabel);

        header.add(sourcePanel);

        JPanel destPanel = new JPanel();
        destPanel.add(destMastLabel);
        destPanel.add(destMastBox);
        destPanel.add(fixedDestMastLabel);

        destMastBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (useLayoutEditor.isSelected()) {
                    try {
                        boolean valid = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlockConnectivityTools().checkValidDest(sourceMastBox.getSelectedBean(),
                                destMastBox.getSelectedBean(), LayoutBlockConnectivityTools.MASTTOMAST);
                        if (!valid) {
                            JOptionPane.showMessageDialog(null, rb.getString("ErrorUnReachableDestination"));
                        }
                    } catch (jmri.JmriException je) {
                        JOptionPane.showMessageDialog(null, rb.getString("WarningUnabletoValidate"));
                    }
                }
            }
        });

        header.add(destPanel);
        header.add(mastSpeed);

//        JPanel srcSigSpeed = new JPanel();
        JPanel editor = new JPanel();
        editor.setLayout(new BoxLayout(editor, BoxLayout.Y_AXIS));
        editor.add(useLayoutEditor);

        editor.add(useLayoutEditorTurnout);
        editor.add(useLayoutEditorBlock);
        useLayoutEditorBlock.setVisible(false);
        useLayoutEditorTurnout.setVisible(false);

        useLayoutEditor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                useLayoutEditorBlock.setVisible(useLayoutEditor.isSelected());
                useLayoutEditorTurnout.setVisible(useLayoutEditor.isSelected());
                // Setup for display of all Turnouts, if needed
                boolean valid = false;
                if (useLayoutEditor.isSelected()) {
                    jFrame.pack();
                    if (!InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
                        int response;

                        response = JOptionPane.showConfirmDialog(null, rb.getString("EnableLayoutBlockRouting"));
                        if (response == 0) {
                            InstanceManager.getDefault(LayoutBlockManager.class).enableAdvancedRouting(true);
                            JOptionPane.showMessageDialog(null, rb.getString("LayoutBlockRoutingEnabled"));
                        }
                    }

                    if ((sml != null) && (destMast != null)) {
                        try {
                            sml.useLayoutEditor(useLayoutEditor.isSelected(), destMast);
                        } catch (jmri.JmriException je) {
                            JOptionPane.showMessageDialog(null, je.toString());
                        }
                        try {
                            valid = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlockConnectivityTools().checkValidDest(sourceMastBox.getSelectedBean(),
                                    destMastBox.getSelectedBean(), LayoutBlockConnectivityTools.MASTTOMAST);
                            if (!valid) {
                                JOptionPane.showMessageDialog(null, rb.getString("ErrorUnReachableDestination"));
                            }
                        } catch (jmri.JmriException je) {
                            JOptionPane.showMessageDialog(null, rb.getString("WarningUnabletoValidate"));
                        }
                    }
                }
            }

        });
        header.add(editor);
        header.add(allowAutoMastGeneration);
        header.add(lockTurnouts);
        JPanel py = new JPanel();
        py.add(new JLabel(Bundle.getMessage("Show")));
        selGroup = new ButtonGroup();
        allButton = new JRadioButton(Bundle.getMessage("All"), true);
        selGroup.add(allButton);
        py.add(allButton);
        allButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Setup for display of all Turnouts, if needed
                if (!showAll) {
                    showAll = true;
                    _blockModel.fireTableDataChanged();
                    _turnoutModel.fireTableDataChanged();
                    _signalMastModel.fireTableDataChanged();
                    _sensorModel.fireTableDataChanged();
                }
            }
        });
        includedButton = new JRadioButton(Bundle.getMessage("Included"), false);
        selGroup.add(includedButton);
        py.add(includedButton);
        includedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Setup for display of included Turnouts only, if needed
                if (showAll) {
                    showAll = false;
                    initializeIncludedList();
                    _blockModel.fireTableDataChanged();
                    _turnoutModel.fireTableDataChanged();
                    _signalMastModel.fireTableDataChanged();
                    _sensorModel.fireTableDataChanged();
                }
            }
        });
        py.add(new JLabel("  " + Bundle.getMessage("Elements")));
        header.add(py);

        containerPanel.add(header, BorderLayout.NORTH);

//        JPanel sensorPanel = new JPanel();
//        JPanel signalMastPanel = new JPanel();
        JTabbedPane detailsTab = new JTabbedPane();
        detailsTab.add(Bundle.getMessage("Blocks"), buildBlocksPanel());
        detailsTab.add(Bundle.getMessage("Turnouts"), buildTurnoutPanel());
        detailsTab.add(Bundle.getMessage("Sensors"), buildSensorPanel());
        detailsTab.add(Bundle.getMessage("SignalMasts"), buildSignalMastPanel());

        containerPanel.add(detailsTab, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setLayout(new FlowLayout(FlowLayout.TRAILING));

        //Cancel button
        footer.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed(e);
            }
        });

        //Update button
        footer.add(updateButton);
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updatePressed(e);
            }
        });
        updateButton.setToolTipText(rb.getString("UpdateButtonToolTip"));
        updateButton.setVisible(true);

        containerPanel.add(footer, BorderLayout.SOUTH);

        add(containerPanel);
        if (sourceMast != null) {
            fixedSourceMastLabel.setVisible(true);
            sourceMastBox.setVisible(false);
        } else {
            fixedSourceMastLabel.setVisible(false);
            sourceMastBox.setVisible(true);
        }
        if ((sml != null) && (destMast != null)) {
            fixedDestMastLabel.setVisible(true);
            destMastBox.setVisible(false);
            useLayoutEditorBlock.setVisible(useLayoutEditor.isSelected());
            useLayoutEditorTurnout.setVisible(useLayoutEditor.isSelected());
            initializeIncludedList();
            editDetails();
        } else {
            useLayoutEditorBlock.setVisible(useLayoutEditor.isSelected());
            useLayoutEditorTurnout.setVisible(useLayoutEditor.isSelected());
            fixedDestMastLabel.setVisible(false);
            destMastBox.setVisible(true);
        }
    }

    JScrollPane _manualBlockScrollPane;
    JScrollPane _autoBlockScrollPane;
    JScrollPane _manualTurnoutScrollPane;
    JScrollPane _manualSignalMastScrollPane;
    JScrollPane _autoSignalMastScrollPane;
    JScrollPane _autoTurnoutScrollPane;

    JScrollPane _manualSensorScrollPane;

    JPanel p2xc = null;
    JPanel p2xt = null;
    JPanel p2xs = null;
    JPanel p2xm = null;

    BlockModel _blockModel;
    AutoBlockModel _autoBlockModel;
    ArrayList<ManualBlockList> _manualBlockList;
    ArrayList<AutoBlockList> _automaticBlockList = new ArrayList<AutoBlockList>();

    TurnoutModel _turnoutModel;
    AutoTurnoutModel _autoTurnoutModel;
    ArrayList<ManualTurnoutList> _manualTurnoutList;
    ArrayList<AutoTurnoutList> _automaticTurnoutList = new ArrayList<AutoTurnoutList>();

    SensorModel _sensorModel;
    ArrayList<ManualSensorList> _manualSensorList;

    SignalMastModel _signalMastModel;
    AutoMastModel _autoSignalMastModel;
    ArrayList<ManualSignalMastList> _manualSignalMastList;
    ArrayList<AutoSignalMastList> _automaticSignalMastList = new ArrayList<AutoSignalMastList>();

    JPanel p2xb = new JPanel();

    JPanel buildBlocksPanel() {
        JPanel blockPanel = new JPanel();
        blockPanel.setLayout(new BoxLayout(blockPanel, BoxLayout.Y_AXIS));

        jmri.BlockManager bm = jmri.InstanceManager.blockManagerInstance();
        List<String> systemNameList = bm.getSystemNameList();
        _manualBlockList = new ArrayList<ManualBlockList>(systemNameList.size());
        Iterator<String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
            //String userName = bm.getBySystemName(systemName).getUserName();
            _manualBlockList.add(new ManualBlockList(bm.getBySystemName(systemName)));
        }

        if ((sml != null) && (destMast != null)) {
            ArrayList<Block> blkList = sml.getAutoBlocks(destMast);
            _automaticBlockList = new ArrayList<AutoBlockList>(blkList.size());
            Iterator<Block> iterBlk = blkList.iterator();
            while (iterBlk.hasNext()) {
                Block blk = iterBlk.next();

                AutoBlockList blockitem = new AutoBlockList(blk);
                blockitem.setState(sml.getAutoBlockState(blk, destMast));

                _automaticBlockList.add(blockitem);
            }
        }
        JPanel p2xc = new JPanel();  //this hides a field
        //p2xc = new JPanel();
        JPanel p2xcSpace = new JPanel();
        p2xcSpace.setLayout(new BoxLayout(p2xcSpace, BoxLayout.Y_AXIS));
        p2xcSpace.add(new JLabel("XXX"));
        p2xc.add(p2xcSpace);

        JPanel p21c = new JPanel();
        p21c.setLayout(new BoxLayout(p21c, BoxLayout.Y_AXIS));
        p21c.add(new JLabel(Bundle.getMessage("LabelSelectChecked", Bundle.getMessage("Blocks"))));
        p2xc.add(p21c);

        _blockModel = new BlockModel();
        JTable manualBlockTable = jmri.util.JTableUtil.sortableDataModel(_blockModel);
        try {
            jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter) manualBlockTable.getModel());
            tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            tmodel.setSortingStatus(BlockModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
        } catch (ClassCastException e3) {
        }  // if not a sortable table model
        manualBlockTable.setRowSelectionAllowed(false);
        manualBlockTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 100));
        JComboBox<String> stateCCombo = new JComboBox<String>();
        stateCCombo.addItem(SET_TO_UNOCCUPIED);
        stateCCombo.addItem(SET_TO_OCCUPIED);
        stateCCombo.addItem(SET_TO_ANY);

        TableColumnModel _manualBlockColumnModel = manualBlockTable.getColumnModel();
        TableColumn includeColumnC = _manualBlockColumnModel.
                getColumn(BlockModel.INCLUDE_COLUMN);
        includeColumnC.setResizable(false);
        includeColumnC.setMinWidth(50);
        includeColumnC.setMaxWidth(60);
        TableColumn sNameColumnC = _manualBlockColumnModel.
                getColumn(BlockModel.SNAME_COLUMN);
        sNameColumnC.setResizable(true);
        sNameColumnC.setMinWidth(75);
        sNameColumnC.setMaxWidth(95);

        TableColumn stateColumnC = _manualBlockColumnModel.
                getColumn(BlockModel.STATE_COLUMN);
        stateColumnC.setCellEditor(new DefaultCellEditor(stateCCombo));
        stateColumnC.setResizable(false);
        stateColumnC.setMinWidth(90);
        stateColumnC.setMaxWidth(100);

        _manualBlockScrollPane = new JScrollPane(manualBlockTable);
        p2xc.add(_manualBlockScrollPane, BorderLayout.CENTER);
        //contentPane.add(p2xc);
        blockPanel.add(p2xc);
        p2xc.setVisible(true);

        ROW_HEIGHT = manualBlockTable.getRowHeight();
        p2xcSpace.setVisible(false);

        JPanel p2xaSpace = new JPanel();
        p2xaSpace.setLayout(new BoxLayout(p2xaSpace, BoxLayout.Y_AXIS));
        p2xaSpace.add(new JLabel("XXX"));
        p2xb.add(p2xaSpace);

        JPanel p21a = new JPanel();
        p21a.setLayout(new BoxLayout(p21a, BoxLayout.Y_AXIS));
        p21a.add(new JLabel(Bundle.getMessage("LabelAutogenerated", Bundle.getMessage("Blocks"))));
        p2xb.add(p21a);

        _autoBlockModel = new AutoBlockModel();
        JTable autoBlockTable = jmri.util.JTableUtil.sortableDataModel(_autoBlockModel);
        try {
            jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter) autoBlockTable.getModel());
            tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            tmodel.setSortingStatus(AutoBlockModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
        } catch (ClassCastException e3) {
        }  // if not a sortable table model
        autoBlockTable.setRowSelectionAllowed(false);
        autoBlockTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 100));

        TableColumnModel _autoBlockColumnModel = autoBlockTable.getColumnModel();
        TableColumn sNameColumnA = _autoBlockColumnModel.
                getColumn(AutoBlockModel.SNAME_COLUMN);
        sNameColumnA.setResizable(true);
        sNameColumnA.setMinWidth(75);
        sNameColumnA.setMaxWidth(95);

        TableColumn stateColumnA = _autoBlockColumnModel.
                getColumn(AutoBlockModel.STATE_COLUMN);
        //stateColumnA.setCellEditor(new DefaultCellEditor(stateCCombo));
        stateColumnA.setResizable(false);
        stateColumnA.setMinWidth(90);
        stateColumnA.setMaxWidth(100);

        _autoBlockScrollPane = new JScrollPane(autoBlockTable);
        p2xb.add(_autoBlockScrollPane, BorderLayout.CENTER);
        blockPanel.add(p2xb);
        p2xb.setVisible(true);

        ROW_HEIGHT = autoBlockTable.getRowHeight();
        p2xaSpace.setVisible(false);

        return blockPanel;

    }

    JPanel p2xa = new JPanel();

    JPanel buildTurnoutPanel() {
        JPanel turnoutPanel = new JPanel();
        turnoutPanel.setLayout(new BoxLayout(turnoutPanel, BoxLayout.Y_AXIS));

        jmri.TurnoutManager bm = jmri.InstanceManager.turnoutManagerInstance();
        List<String> systemNameList = bm.getSystemNameList();
        _manualTurnoutList = new ArrayList<ManualTurnoutList>(systemNameList.size());
        Iterator<String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
            String userName = bm.getBySystemName(systemName).getUserName();
            _manualTurnoutList.add(new ManualTurnoutList(systemName, userName));
        }

        if ((sml != null) && (destMast != null)) {
            ArrayList<Turnout> turnList = sml.getAutoTurnouts(destMast);
            _automaticTurnoutList = new ArrayList<AutoTurnoutList>(turnList.size());
            Iterator<Turnout> iterTurn = turnList.iterator();
            while (iterTurn.hasNext()) {
                Turnout turn = iterTurn.next();
                String systemName = turn.getSystemName();
                String userName = turn.getUserName();
                AutoTurnoutList turnitem = new AutoTurnoutList(systemName, userName);
                turnitem.setState(sml.getAutoTurnoutState(turn, destMast));
                _automaticTurnoutList.add(turnitem);
            }
        }

        p2xt = new JPanel();
        JPanel p2xcSpace = new JPanel();
        p2xcSpace.setLayout(new BoxLayout(p2xcSpace, BoxLayout.Y_AXIS));
        p2xcSpace.add(new JLabel("XXX"));
        p2xt.add(p2xcSpace);

        JPanel p21c = new JPanel();
        p21c.setLayout(new BoxLayout(p21c, BoxLayout.Y_AXIS));
        p21c.add(new JLabel(Bundle.getMessage("LabelSelectChecked", Bundle.getMessage("Turnouts"))));
        p2xt.add(p21c);

        _turnoutModel = new TurnoutModel();
        JTable manualTurnoutTable = jmri.util.JTableUtil.sortableDataModel(_turnoutModel);
        try {
            jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter) manualTurnoutTable.getModel());
            tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            tmodel.setSortingStatus(TurnoutModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
        } catch (ClassCastException e3) {
        }  // if not a sortable table model
        manualTurnoutTable.setRowSelectionAllowed(false);
        manualTurnoutTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 100));
        JComboBox<String> stateCCombo = new JComboBox<String>();
        stateCCombo.addItem(SET_TO_THROWN);
        stateCCombo.addItem(SET_TO_CLOSED);
        stateCCombo.addItem(SET_TO_ANY);

        TableColumnModel _manualTurnoutColumnModel = manualTurnoutTable.getColumnModel();
        TableColumn includeColumnC = _manualTurnoutColumnModel.
                getColumn(TurnoutModel.INCLUDE_COLUMN);
        includeColumnC.setResizable(false);
        includeColumnC.setMinWidth(50);
        includeColumnC.setMaxWidth(60);
        TableColumn sNameColumnC = _manualTurnoutColumnModel.
                getColumn(TurnoutModel.SNAME_COLUMN);
        sNameColumnC.setResizable(true);
        sNameColumnC.setMinWidth(75);
        sNameColumnC.setMaxWidth(95);

        TableColumn stateColumnC = _manualTurnoutColumnModel.
                getColumn(TurnoutModel.STATE_COLUMN);
        stateColumnC.setCellEditor(new DefaultCellEditor(stateCCombo));
        stateColumnC.setResizable(false);
        stateColumnC.setMinWidth(90);
        stateColumnC.setMaxWidth(100);

        _manualTurnoutScrollPane = new JScrollPane(manualTurnoutTable);
        p2xt.add(_manualTurnoutScrollPane, BorderLayout.CENTER);
        //contentPane.add(p2xc);
        turnoutPanel.add(p2xt);
        p2xt.setVisible(true);

        ROW_HEIGHT = manualTurnoutTable.getRowHeight();
        p2xcSpace.setVisible(false);

        JPanel p2xaSpace = new JPanel();
        p2xaSpace.setLayout(new BoxLayout(p2xaSpace, BoxLayout.Y_AXIS));
        p2xaSpace.add(new JLabel("XXX"));
        p2xa.add(p2xaSpace);

        JPanel p21a = new JPanel();
        p21a.setLayout(new BoxLayout(p21a, BoxLayout.Y_AXIS));
        p21a.add(new JLabel(Bundle.getMessage("LabelAutogenerated", Bundle.getMessage("Turnouts"))));
        p2xa.add(p21a);

        _autoTurnoutModel = new AutoTurnoutModel();
        JTable autoTurnoutTable = jmri.util.JTableUtil.sortableDataModel(_autoTurnoutModel);
        try {
            jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter) autoTurnoutTable.getModel());
            tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            tmodel.setSortingStatus(AutoTurnoutModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
        } catch (ClassCastException e3) {
        }  // if not a sortable table model
        autoTurnoutTable.setRowSelectionAllowed(false);
        autoTurnoutTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 100));

        TableColumnModel _autoTurnoutColumnModel = autoTurnoutTable.getColumnModel();
        TableColumn sNameColumnA = _autoTurnoutColumnModel.
                getColumn(AutoTurnoutModel.SNAME_COLUMN);
        sNameColumnA.setResizable(true);
        sNameColumnA.setMinWidth(75);
        sNameColumnA.setMaxWidth(95);

        TableColumn stateColumnA = _autoTurnoutColumnModel.
                getColumn(AutoTurnoutModel.STATE_COLUMN);
        //stateColumnA.setCellEditor(new DefaultCellEditor(stateCCombo));
        stateColumnA.setResizable(false);
        stateColumnA.setMinWidth(90);
        stateColumnA.setMaxWidth(100);

        _autoTurnoutScrollPane = new JScrollPane(autoTurnoutTable);
        p2xa.add(_autoTurnoutScrollPane, BorderLayout.CENTER);
        //contentPane.add(p2xa);
        turnoutPanel.add(p2xa);
        p2xa.setVisible(true);

        ROW_HEIGHT = autoTurnoutTable.getRowHeight();
        p2xaSpace.setVisible(false);

        return turnoutPanel;
    }

    JPanel buildSensorPanel() {
        JPanel sensorPanel = new JPanel();
        sensorPanel.setLayout(new BoxLayout(sensorPanel, BoxLayout.Y_AXIS));

        jmri.SensorManager bm = jmri.InstanceManager.sensorManagerInstance();
        List<String> systemNameList = bm.getSystemNameList();
        _manualSensorList = new ArrayList<ManualSensorList>(systemNameList.size());
        Iterator<String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
            String userName = bm.getBySystemName(systemName).getUserName();
            _manualSensorList.add(new ManualSensorList(systemName, userName));
        }

        p2xs = new JPanel();
        JPanel p2xsSpace = new JPanel();
        p2xsSpace.setLayout(new BoxLayout(p2xsSpace, BoxLayout.Y_AXIS));
        p2xsSpace.add(new JLabel("XXX"));
        p2xs.add(p2xsSpace);

        JPanel p21c = new JPanel();
        p21c.setLayout(new BoxLayout(p21c, BoxLayout.Y_AXIS));
        p21c.add(new JLabel(Bundle.getMessage("LabelSelectChecked", Bundle.getMessage("Sensors"))));
        p2xs.add(p21c);

        _sensorModel = new SensorModel();
        JTable manualSensorTable = jmri.util.JTableUtil.sortableDataModel(_sensorModel);
        try {
            jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter) manualSensorTable.getModel());
            tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            tmodel.setSortingStatus(SensorModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
        } catch (ClassCastException e3) {
        }  // if not a sortable table model
        manualSensorTable.setRowSelectionAllowed(false);
        manualSensorTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 100));
        JComboBox<String> stateCCombo = new JComboBox<String>();
        stateCCombo.addItem(SET_TO_INACTIVE);
        stateCCombo.addItem(SET_TO_ACTIVE);

        TableColumnModel _manualSensorColumnModel = manualSensorTable.getColumnModel();
        TableColumn includeColumnC = _manualSensorColumnModel.
                getColumn(SensorModel.INCLUDE_COLUMN);
        includeColumnC.setResizable(false);
        includeColumnC.setMinWidth(50);
        includeColumnC.setMaxWidth(60);
        TableColumn sNameColumnC = _manualSensorColumnModel.
                getColumn(SensorModel.SNAME_COLUMN);
        sNameColumnC.setResizable(true);
        sNameColumnC.setMinWidth(75);
        sNameColumnC.setMaxWidth(95);

        TableColumn stateColumnC = _manualSensorColumnModel.
                getColumn(SensorModel.STATE_COLUMN);
        stateColumnC.setCellEditor(new DefaultCellEditor(stateCCombo));
        stateColumnC.setResizable(false);
        stateColumnC.setMinWidth(90);
        stateColumnC.setMaxWidth(100);

        _manualSensorScrollPane = new JScrollPane(manualSensorTable);
        p2xs.add(_manualSensorScrollPane, BorderLayout.CENTER);

        sensorPanel.add(p2xs);
        p2xs.setVisible(true);

        ROW_HEIGHT = manualSensorTable.getRowHeight();
        p2xsSpace.setVisible(false);

        return sensorPanel;
    }

    JPanel p2xsm = new JPanel();

    JPanel buildSignalMastPanel() {
        JPanel SignalMastPanel = new JPanel();
        SignalMastPanel.setLayout(new BoxLayout(SignalMastPanel, BoxLayout.Y_AXIS));

        jmri.SignalMastManager bm = jmri.InstanceManager.signalMastManagerInstance();
        List<String> systemNameList = bm.getSystemNameList();
        _manualSignalMastList = new ArrayList<ManualSignalMastList>(systemNameList.size());
        Iterator<String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
            _manualSignalMastList.add(new ManualSignalMastList(bm.getBySystemName(systemName)));
        }

        p2xm = new JPanel();
        JPanel p2xmSpace = new JPanel();
        p2xmSpace.setLayout(new BoxLayout(p2xmSpace, BoxLayout.Y_AXIS));
        p2xmSpace.add(new JLabel("XXX"));
        p2xm.add(p2xmSpace);

        JPanel p21c = new JPanel();
        p21c.setLayout(new BoxLayout(p21c, BoxLayout.Y_AXIS));
        p21c.add(new JLabel(Bundle.getMessage("LabelSelectChecked", Bundle.getMessage("SignalMasts"))));
        p2xm.add(p21c);

        _signalMastModel = new SignalMastModel();

        TableSorter sorter = new TableSorter(_signalMastModel);
        JTable manualSignalMastTable = _signalMastModel.makeJTable(sorter);
        sorter.setTableHeader(manualSignalMastTable.getTableHeader());

        manualSignalMastTable.setRowSelectionAllowed(false);
        manualSignalMastTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 100));

        TableColumnModel _manualSignalMastColumnModel = manualSignalMastTable.getColumnModel();
        TableColumn includeColumnC = _manualSignalMastColumnModel.
                getColumn(SignalMastModel.INCLUDE_COLUMN);
        includeColumnC.setResizable(false);
        includeColumnC.setMinWidth(50);
        includeColumnC.setMaxWidth(60);
        TableColumn sNameColumnC = _manualSignalMastColumnModel.
                getColumn(SignalMastModel.SNAME_COLUMN);
        sNameColumnC.setResizable(true);
        sNameColumnC.setMinWidth(75);
        sNameColumnC.setMaxWidth(95);

        _manualSignalMastScrollPane = new JScrollPane(manualSignalMastTable);
        p2xm.add(_manualSignalMastScrollPane, BorderLayout.CENTER);
        //contentPane.add(p2xm);
        SignalMastPanel.add(p2xm);
        p2xm.setVisible(true);

        ROW_HEIGHT = manualSignalMastTable.getRowHeight();
        p2xmSpace.setVisible(false);

        JPanel p2xaSpace = new JPanel();
        p2xaSpace.setLayout(new BoxLayout(p2xaSpace, BoxLayout.Y_AXIS));
        p2xaSpace.add(new JLabel("XXX"));
        p2xsm.add(p2xaSpace);

        JPanel p21a = new JPanel();
        p21a.setLayout(new BoxLayout(p21a, BoxLayout.Y_AXIS));
        p21a.add(new JLabel(Bundle.getMessage("LabelAutogenerated", Bundle.getMessage("SignalMasts"))));
        p2xsm.add(p21a);

        _autoSignalMastModel = new AutoMastModel();
        JTable autoMastTable = jmri.util.JTableUtil.sortableDataModel(_autoSignalMastModel);
        try {
            jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter) autoMastTable.getModel());
            tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            tmodel.setSortingStatus(AutoMastModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
        } catch (ClassCastException e3) {
        }  // if not a sortable table model
        autoMastTable.setRowSelectionAllowed(false);
        autoMastTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 100));

        TableColumnModel _autoMastColumnModel = autoMastTable.getColumnModel();
        TableColumn sNameColumnA = _autoMastColumnModel.
                getColumn(AutoMastModel.SNAME_COLUMN);
        sNameColumnA.setResizable(true);
        sNameColumnA.setMinWidth(75);
        sNameColumnA.setMaxWidth(95);

        TableColumn stateColumnA = _autoMastColumnModel.
                getColumn(AutoMastModel.STATE_COLUMN);
        //stateColumnA.setCellEditor(new DefaultCellEditor(stateCCombo));
        stateColumnA.setResizable(false);
        stateColumnA.setMinWidth(90);
        stateColumnA.setMaxWidth(100);

        _autoSignalMastScrollPane = new JScrollPane(autoMastTable);
        p2xsm.add(_autoSignalMastScrollPane, BorderLayout.CENTER);
        //contentPane.add(p2xa);
        SignalMastPanel.add(p2xsm);
        p2xsm.setVisible(true);

        ROW_HEIGHT = autoMastTable.getRowHeight();
        p2xaSpace.setVisible(false);

        return SignalMastPanel;
    }

    void cancelPressed(ActionEvent e) {
        jFrame.setVisible(false);
        jFrame.dispose();
        jFrame = null;
    }
    void updatePressed(ActionEvent e) {
        sourceMast = (SignalMast) sourceMastBox.getSelectedBean();
        destMast = (SignalMast) destMastBox.getSelectedBean();

        if ((sml == null) && (useLayoutEditor.isSelected())) {
            boolean valid = false;
            try {
                valid = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlockConnectivityTools().checkValidDest(sourceMast,
                        destMast, LayoutBlockConnectivityTools.MASTTOMAST);
                if (!valid) {
                    JOptionPane.showMessageDialog(null, rb.getString("ErrorUnReachableDestination"));
                    return;
                }
            } catch (jmri.JmriException je) {
                JOptionPane.showMessageDialog(null, rb.getString("WarningUnabletoValidate"));
            }
        }

        if (sml == null) {
            sml = InstanceManager.signalMastLogicManagerInstance().newSignalMastLogic(sourceMast);
            sml.setDestinationMast(destMast);
            fixedSourceMastLabel.setText(sourceMast.getDisplayName());
            fixedDestMastLabel.setText(destMast.getDisplayName());
            sourceMastBox.setVisible(false);
            destMastBox.setVisible(false);
            fixedSourceMastLabel.setVisible(true);
            fixedDestMastLabel.setVisible(true);
            _autoTurnoutModel.smlValid();
            _autoBlockModel.smlValid();
            _autoSignalMastModel.smlValid();
        }
        initializeIncludedList();
        sml.allowAutoMaticSignalMastGeneration(allowAutoMastGeneration.isSelected(), destMast);
        boolean layouteditorgen = true;
        try {
            sml.useLayoutEditor(useLayoutEditor.isSelected(), destMast);
        } catch (jmri.JmriException je) {
            JOptionPane.showMessageDialog(null, je.toString());
            layouteditorgen = false;
        }

        try {
            if (useLayoutEditor.isSelected()) {
                sml.useLayoutEditorDetails(useLayoutEditorTurnout.isSelected(), useLayoutEditorBlock.isSelected(), destMast);
            }
        } catch (jmri.JmriException ji) {
            if (layouteditorgen) {
                JOptionPane.showMessageDialog(null, ji.toString());
            }
        }
        Hashtable<Block, Integer> hashBlocks = new Hashtable<Block, Integer>();
        for (int i = 0; i < _includedManualBlockList.size(); i++) {
            Block blk = jmri.InstanceManager.blockManagerInstance().getBlock(_includedManualBlockList.get(i).getSysName());
            hashBlocks.put(blk, _includedManualBlockList.get(i).getState());
        }
        sml.setBlocks(hashBlocks, destMast);

        Hashtable<NamedBeanHandle<Turnout>, Integer> hashTurnouts = new Hashtable<NamedBeanHandle<Turnout>, Integer>();
        for (int i = 0; i < _includedManualTurnoutList.size(); i++) {
            String turnoutName = _includedManualTurnoutList.get(i).getDisplayName();
            Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().getTurnout(_includedManualTurnoutList.get(i).getDisplayName());
            NamedBeanHandle<Turnout> namedTurnout = nbhm.getNamedBeanHandle(turnoutName, turnout);
            hashTurnouts.put(namedTurnout, _includedManualTurnoutList.get(i).getState());
        }
        sml.setTurnouts(hashTurnouts, destMast);

        Hashtable<NamedBeanHandle<Sensor>, Integer> hashSensors = new Hashtable<NamedBeanHandle<Sensor>, Integer>();
        for (int i = 0; i < _includedManualSensorList.size(); i++) {
            String sensorName = _includedManualSensorList.get(i).getDisplayName();
            Sensor sensor = jmri.InstanceManager.sensorManagerInstance().getSensor(_includedManualSensorList.get(i).getDisplayName());
            NamedBeanHandle<Sensor> namedSensor = nbhm.getNamedBeanHandle(sensorName, sensor);
            hashSensors.put(namedSensor, _includedManualSensorList.get(i).getState());
        }
        sml.setSensors(hashSensors, destMast);

        Hashtable<SignalMast, String> hashSignalMast = new Hashtable<SignalMast, String>();
        for (int i = 0; i < _includedManualSignalMastList.size(); i++) {
            if (_includedManualSignalMastList.get(i).getMast() == sourceMast || _includedManualSignalMastList.get(i).getMast() == destMast) {
                int mes = JOptionPane.showConfirmDialog(null, java.text.MessageFormat.format(rb.getString("SignalMastCriteriaOwn"),
                        new Object[]{_includedManualSignalMastList.get(i).getMast().getDisplayName()}),
                        rb.getString("SignalMastCriteriaOwnTitle"),
                        JOptionPane.YES_NO_OPTION);
                if (mes == 0) {
                    hashSignalMast.put(_includedManualSignalMastList.get(i).getMast(), _includedManualSignalMastList.get(i).getSetToState());
                } else {
                    _includedManualSignalMastList.get(i).setIncluded(false);
                    initializeIncludedList();
                    _signalMastModel.fireTableDataChanged();
                }
            } else {
                hashSignalMast.put(_includedManualSignalMastList.get(i).getMast(), _includedManualSignalMastList.get(i).getSetToState());
            }

        }
        sml.setMasts(hashSignalMast, destMast);
        sml.allowTurnoutLock(lockTurnouts.isSelected(), destMast);
        sml.initialise(destMast);
    }

    public void initComponents() {

    }

    int blockModeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, blockInputModeValues, blockInputModes);

        if (result < 0) {
            log.warn("unexpected mode string in sensorMode: " + mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    void setBlockModeBox(int mode, JComboBox<String> box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, blockInputModeValues, blockInputModes);
        box.setSelectedItem(result);
    }

    private static String[] blockInputModes = new String[]{rb.getString("UnOccupied"), rb.getString("Occupied")};
    private static int[] blockInputModeValues = new int[]{Block.UNOCCUPIED, Block.OCCUPIED};

    void initializeIncludedList() {
        _includedManualBlockList = new ArrayList<ManualBlockList>();
        for (int i = 0; i < _manualBlockList.size(); i++) {
            if (_manualBlockList.get(i).isIncluded()) {
                _includedManualBlockList.add(_manualBlockList.get(i));
            }
        }

        if ((sml != null) && (destMast != null)) {
            ArrayList<Block> blkList = sml.getAutoBlocks(destMast);
            _automaticBlockList = new ArrayList<AutoBlockList>(blkList.size());
            Iterator<Block> iter = blkList.iterator();
            while (iter.hasNext()) {
                Block blk = iter.next();
                AutoBlockList newABlk = new AutoBlockList(blk);
                _automaticBlockList.add(newABlk);
                newABlk.setState(sml.getAutoBlockState(blk, destMast));
            }
        }

        _includedManualTurnoutList = new ArrayList<ManualTurnoutList>();
        for (int i = 0; i < _manualTurnoutList.size(); i++) {
            if (_manualTurnoutList.get(i).isIncluded()) {
                _includedManualTurnoutList.add(_manualTurnoutList.get(i));
            }
        }

        if ((sml != null) && (destMast != null)) {
            ArrayList<Turnout> turnList = sml.getAutoTurnouts(destMast);
            _automaticTurnoutList = new ArrayList<AutoTurnoutList>(turnList.size());
            Iterator<Turnout> iter = turnList.iterator();
            while (iter.hasNext()) {
                Turnout turn = iter.next();
                String systemName = turn.getSystemName();
                String userName = turn.getUserName();
                AutoTurnoutList newAturn = new AutoTurnoutList(systemName, userName);
                _automaticTurnoutList.add(newAturn);
                newAturn.setState(sml.getAutoTurnoutState(turn, destMast));
            }
        }

        _includedManualSensorList = new ArrayList<ManualSensorList>();
        for (int i = 0; i < _manualSensorList.size(); i++) {
            if (_manualSensorList.get(i).isIncluded()) {
                _includedManualSensorList.add(_manualSensorList.get(i));
            }
        }

        _includedManualSignalMastList = new ArrayList<ManualSignalMastList>();
        for (int i = 0; i < _manualSignalMastList.size(); i++) {
            if (_manualSignalMastList.get(i).isIncluded()) {
                _includedManualSignalMastList.add(_manualSignalMastList.get(i));
            }
        }

        if ((sml != null) && (destMast != null)) {
            ArrayList<SignalMast> mastList = sml.getAutoMasts(destMast);
            _automaticSignalMastList = new ArrayList<AutoSignalMastList>(mastList.size());
            Iterator<SignalMast> iter = mastList.iterator();
            while (iter.hasNext()) {
                SignalMast mast = iter.next();
                AutoSignalMastList newAmast = new AutoSignalMastList(mast);
                _automaticSignalMastList.add(newAmast);
                newAmast.setState(sml.getAutoSignalMastState(mast, destMast));
            }
        }
    }

    // to free resources when no longer used
    public void dispose() {

    }

    ButtonGroup selGroup = null;
    JRadioButton allButton = null;
    JRadioButton includedButton = null;

    private boolean showAll = true;   // false indicates show only included items

    private static String SET_TO_ACTIVE = rb.getString("SensorActive");
    private static String SET_TO_INACTIVE = rb.getString("SensorInactive");
    private static String SET_TO_CLOSED = jmri.InstanceManager.turnoutManagerInstance().getClosedText();
    private static String SET_TO_THROWN = jmri.InstanceManager.turnoutManagerInstance().getThrownText();

    private static String SET_TO_UNOCCUPIED = rb.getString("UnOccupied");
    private static String SET_TO_OCCUPIED = rb.getString("Occupied");
    private static String SET_TO_ANY = rb.getString("AnyState");

    private static int ROW_HEIGHT;

    /**
     * Cancels included only option
     */
    void cancelIncludedOnly() {
        if (!showAll) {
            allButton.doClick();
        }
    }

    void signalMastCombo(JComboBox<String> box, SignalMast select) {
        box.removeAllItems();
        List<String> nameList = smm.getSystemNameList();
        String[] displayList = new String[nameList.size()];
        for (int i = 0; i < nameList.size(); i++) {
            SignalMast sm = smm.getBySystemName(nameList.get(i));
            displayList[i] = sm.getDisplayName();
        }
        java.util.Arrays.sort(displayList);
        for (int i = 0; i < displayList.length; i++) {
            box.addItem(displayList[i]);
            if ((select != null) && (displayList[i].equals(select.getDisplayName()))) {
                box.setSelectedIndex(i);
            }
        }
    }

    void editDetails() {
        int setRow = 0;
        for (int i = _manualBlockList.size() - 1; i >= 0; i--) {
            ManualBlockList block = _manualBlockList.get(i);
            String tSysName = block.getSysName();
            Block blk = InstanceManager.blockManagerInstance().getBlock(tSysName);
            if (sml.isBlockIncluded(blk, destMast)) {
                block.setIncluded(true);
                block.setState(sml.getBlockState(blk, destMast));
                setRow = i;
            } else {
                block.setIncluded(false);
                block.setState(Block.UNOCCUPIED);
            }
        }
        setRow -= 1;
        if (setRow < 0) {
            setRow = 0;
        }
        _manualBlockScrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
        _blockModel.fireTableDataChanged();

        setRow = 0;
        for (int i = _manualTurnoutList.size() - 1; i >= 0; i--) {
            ManualTurnoutList turnout = _manualTurnoutList.get(i);
            String tSysName = turnout.getSysName();
            Turnout turn = InstanceManager.turnoutManagerInstance().getTurnout(tSysName);
            if (sml.isTurnoutIncluded(turn, destMast)) {
                turnout.setIncluded(true);
                turnout.setState(sml.getTurnoutState(turn, destMast));
                setRow = i;
            } else {
                turnout.setIncluded(false);
                turnout.setState(Turnout.CLOSED);
            }
        }
        setRow -= 1;
        if (setRow < 0) {
            setRow = 0;
        }
        _manualSensorScrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
        _sensorModel.fireTableDataChanged();

        setRow = 0;
        for (int i = _manualSensorList.size() - 1; i >= 0; i--) {
            ManualSensorList sensor = _manualSensorList.get(i);
            String tSysName = sensor.getSysName();
            Sensor sen = InstanceManager.sensorManagerInstance().getSensor(tSysName);
            if (sml.isSensorIncluded(sen, destMast)) {
                sensor.setIncluded(true);
                sensor.setState(sml.getSensorState(sen, destMast));
                setRow = i;
            } else {
                sensor.setIncluded(false);
                sensor.setState(Sensor.INACTIVE);
            }
        }
        setRow -= 1;
        if (setRow < 0) {
            setRow = 0;
        }
        _manualSensorScrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
        _sensorModel.fireTableDataChanged();

        setRow = 0;
        for (int i = _manualSignalMastList.size() - 1; i >= 0; i--) {
            ManualSignalMastList mast = _manualSignalMastList.get(i);
            SignalMast sigMast = _manualSignalMastList.get(i).getMast();
            if (sml.isSignalMastIncluded(sigMast, destMast)) {
                mast.setIncluded(true);
                mast.setSetToState(sml.getSignalMastState(sigMast, destMast));
                setRow = i;
            } else {
                mast.setIncluded(false);
            }
        }
        setRow -= 1;
        if (setRow < 0) {
            setRow = 0;
        }
        _manualSignalMastScrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
        _signalMastModel.fireTableDataChanged();

    }

//    private ArrayList <AutoBlockList> _autoBlockList;
    private ArrayList<ManualBlockList> _includedManualBlockList;
    private ArrayList<ManualTurnoutList> _includedManualTurnoutList;
    private ArrayList<ManualSensorList> _includedManualSensorList;
    private ArrayList<ManualSignalMastList> _includedManualSignalMastList;

    private abstract class SignalMastElement {

        String _sysName;
        String _userName;
        boolean _included;
        int _setToState;

        SignalMastElement() {

        }

        SignalMastElement(String sysName, String userName) {
            _sysName = sysName;
            _userName = userName;
            _included = false;
            _setToState = Sensor.INACTIVE;
        }

        String getSysName() {
            return _sysName;
        }

        String getUserName() {
            return _userName;
        }

        String getDisplayName() {
            String name = getUserName();
            if (name != null && name.length() > 0) {
                return name;
            } else {
                return getSysName();
            }
        }

        boolean isIncluded() {
            return _included;
        }

        void setIncluded(boolean include) {
            _included = include;
        }

        abstract String getSetToState();

        abstract void setSetToState(String state);

        int getState() {
            return _setToState;
        }

        void setState(int state) {
            _setToState = state;
        }
    }

    private class ManualBlockList extends SignalMastElement {

        ManualBlockList(Block block) {
            this.block = block;
        }
        Block block;

        String getSysName() {
            return block.getSystemName();
        }

        String getUserName() {
            return block.getUserName();
        }

        boolean getPermissiveWorking() {
            return block.getPermissiveWorking();
        }

        String getBlockSpeed() {
            return block.getBlockSpeed();
        }

        String getSetToState() {
            switch (_setToState) {
                case Block.OCCUPIED:
                    return SET_TO_OCCUPIED;
                case Block.UNOCCUPIED:
                    return SET_TO_UNOCCUPIED;
            }
            return SET_TO_ANY;
        }

        void setSetToState(String state) {
            if (SET_TO_UNOCCUPIED.equals(state)) {
                _setToState = Block.UNOCCUPIED;
            } else if (SET_TO_OCCUPIED.equals(state)) {
                _setToState = Block.OCCUPIED;
            } else {
                _setToState = 0x03;
            }
        }
    }

    private class AutoBlockList extends ManualBlockList {

        AutoBlockList(Block block) {
            super(block);
        }

        void setSetToState(String state) {
        }
    }

    private class ManualTurnoutList extends SignalMastElement {

        ManualTurnoutList(String sysName, String userName) {
            super(sysName, userName);
        }

        String getSetToState() {
            switch (_setToState) {
                case Turnout.THROWN:
                    return SET_TO_THROWN;
                case Turnout.CLOSED:
                    return SET_TO_CLOSED;
            }
            return SET_TO_ANY;
        }

        void setSetToState(String state) {
            if (SET_TO_THROWN.equals(state)) {
                _setToState = Turnout.THROWN;
            } else if (SET_TO_CLOSED.equals(state)) {
                _setToState = Turnout.CLOSED;
            } else {
                _setToState = 0x00;
            }
        }
    }

    private class AutoTurnoutList extends ManualTurnoutList {

        AutoTurnoutList(String sysName, String userName) {
            super(sysName, userName);
        }

        void setSetToState(String state) {
        }
    }

    private class ManualSensorList extends SignalMastElement {

        ManualSensorList(String sysName, String userName) {
            super(sysName, userName);
        }

        String getSetToState() {
            switch (_setToState) {
                case Sensor.INACTIVE:
                    return SET_TO_INACTIVE;
                case Sensor.ACTIVE:
                    return SET_TO_ACTIVE;
            }
            return "";
        }

        void setSetToState(String state) {
            if (SET_TO_INACTIVE.equals(state)) {
                _setToState = Sensor.INACTIVE;
            } else if (SET_TO_ACTIVE.equals(state)) {
                _setToState = Sensor.ACTIVE;
            }
        }
    }

    private class ManualSignalMastList extends SignalMastElement {

        ManualSignalMastList(SignalMast s) {
            mast = s;
        }

        String _setToAspect = "";

        SignalMast mast;

        SignalMast getMast() {
            return mast;
        }

        String getSysName() {
            return mast.getSystemName();
        }

        String getUserName() {
            return mast.getUserName();
        }

        String getSetToState() {
            return _setToAspect;
        }

        void setSetToState(String state) {
            _setToAspect = state;
        }
    }

    private class AutoSignalMastList extends ManualSignalMastList {

        AutoSignalMastList(SignalMast s) {
            super(s);
        }

        void setSetToState(String state) {
        }

        void setState(String state) {
            _setToAspect = state;
        }
    }

    abstract class TableModel extends AbstractTableModel implements PropertyChangeListener {

        /**
         *
         */
        private static final long serialVersionUID = 7361250471794011296L;

        public Class<?> getColumnClass(int c) {
            if (c == INCLUDE_COLUMN) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            }
        }

        public void dispose() {
            jmri.InstanceManager.turnoutManagerInstance().removePropertyChangeListener(this);
        }

        public String getColumnName(int col) {
            switch (col) {
                case SNAME_COLUMN:
                    return rb.getString("ColumnSystemName");
                case UNAME_COLUMN:
                    return rb.getString("ColumnUserName");
                case INCLUDE_COLUMN:
                    return rb.getString("ColumnInclude");
                case STATE_COLUMN:
                    return rb.getString("ColumnState");
                default:
                    return "unknown";
            }
        }

        public int getColumnCount() {
            return 4;
        }

        public boolean isCellEditable(int r, int c) {
            return ((c == INCLUDE_COLUMN) || (c == STATE_COLUMN));
        }

        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;
        public static final int INCLUDE_COLUMN = 2;
        public static final int STATE_COLUMN = 3;
    }

    class BlockModel extends TableModel {

        /**
         *
         */
        private static final long serialVersionUID = -7997858302507580484L;

        BlockModel() {
            jmri.InstanceManager.blockManagerInstance().addPropertyChangeListener(this);
        }

        public int getRowCount() {
            if (showAll) {
                return _manualBlockList.size();
            } else {
                return _includedManualBlockList.size();
            }
        }

        public static final int SPEED_COLUMN = 4;
        public static final int PERMISSIVE_COLUMN = 5;

        public int getColumnCount() {
            return 6;
        }

        public Object getValueAt(int r, int c) {
            ArrayList<ManualBlockList> blockList = null;
            if (showAll) {
                blockList = _manualBlockList;
            } else {
                blockList = _includedManualBlockList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(blockList.get(r).isIncluded());
                case SNAME_COLUMN:  // slot number
                    return blockList.get(r).getSysName();
                case UNAME_COLUMN:  //
                    return blockList.get(r).getUserName();
                case STATE_COLUMN:
                    return blockList.get(r).getSetToState();
                case SPEED_COLUMN:
                    return blockList.get(r).getBlockSpeed();
                case PERMISSIVE_COLUMN:
                    return blockList.get(r).getPermissiveWorking();
                default:
                    return "";
            }
        }

        public Class<?> getColumnClass(int c) {
            if (c == PERMISSIVE_COLUMN) {
                return Boolean.class;
            }
            return super.getColumnClass(c);
        }

        public String getColumnName(int col) {
            switch (col) {
                case SPEED_COLUMN:
                    return rb.getString("ColumnSpeed");
                case PERMISSIVE_COLUMN:
                    return rb.getString("ColumnPermissive");
            }
            return super.getColumnName(col);
        }

        public void setValueAt(Object type, int r, int c) {
            ArrayList<ManualBlockList> blockList = null;
            if (showAll) {
                blockList = _manualBlockList;
            } else {
                blockList = _includedManualBlockList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    blockList.get(r).setIncluded(((Boolean) type).booleanValue());
                    break;
                case STATE_COLUMN:
                    blockList.get(r).setSetToState((String) type);
                    break;
                default:
                    break;
            }
        }
    }

    class TurnoutModel extends TableModel {

        /**
         *
         */
        private static final long serialVersionUID = 8155668809998527577L;

        TurnoutModel() {
            jmri.InstanceManager.turnoutManagerInstance().addPropertyChangeListener(this);
        }

        public int getRowCount() {
            if (showAll) {
                return _manualTurnoutList.size();
            } else {
                return _includedManualTurnoutList.size();
            }
        }

        public Object getValueAt(int r, int c) {
            ArrayList<ManualTurnoutList> turnoutList = null;
            if (showAll) {
                turnoutList = _manualTurnoutList;
            } else {
                turnoutList = _includedManualTurnoutList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(turnoutList.get(r).isIncluded());
                case SNAME_COLUMN:  // slot number
                    return turnoutList.get(r).getSysName();
                case UNAME_COLUMN:  //
                    return turnoutList.get(r).getUserName();
                case STATE_COLUMN:
                    return turnoutList.get(r).getSetToState();
                default:
                    return null;
            }
        }

        public void setValueAt(Object type, int r, int c) {
            ArrayList<ManualTurnoutList> turnoutList = null;
            if (showAll) {
                turnoutList = _manualTurnoutList;
            } else {
                turnoutList = _includedManualTurnoutList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    turnoutList.get(r).setIncluded(((Boolean) type).booleanValue());
                    break;
                case STATE_COLUMN:
                    turnoutList.get(r).setSetToState((String) type);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Set up table for selecting Sensors and Sensor State
     */
    class SensorModel extends TableModel {

        /**
         *
         */
        private static final long serialVersionUID = 1976471188325126562L;

        SensorModel() {
            InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
        }

        public int getRowCount() {
            if (showAll) {
                return _manualSensorList.size();
            } else {
                return _includedManualSensorList.size();
            }
        }

        public Object getValueAt(int r, int c) {
            ArrayList<ManualSensorList> sensorList = null;
            if (showAll) {
                sensorList = _manualSensorList;
            } else {
                sensorList = _includedManualSensorList;
            }
            // some error checking
            if (r >= sensorList.size()) {
                log.debug("row is greater than turnout list size");
                return null;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(sensorList.get(r).isIncluded());
                case SNAME_COLUMN:  // slot number
                    return sensorList.get(r).getSysName();
                case UNAME_COLUMN:  //
                    return sensorList.get(r).getUserName();
                case STATE_COLUMN:  //
                    return sensorList.get(r).getSetToState();
                default:
                    return null;
            }
        }

        public void setValueAt(Object type, int r, int c) {
            ArrayList<ManualSensorList> sensorList = null;
            if (showAll) {
                sensorList = _manualSensorList;
            } else {
                sensorList = _includedManualSensorList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    sensorList.get(r).setIncluded(((Boolean) type).booleanValue());
                    break;
                case STATE_COLUMN:
                    sensorList.get(r).setSetToState((String) type);
                    break;
                default:
                    break;
            }
        }
    }

    class SignalMastModel extends TableModel {

        /**
         *
         */
        private static final long serialVersionUID = 3218114528313492508L;

        SignalMastModel() {
            jmri.InstanceManager.signalMastManagerInstance().addPropertyChangeListener(this);
        }

        public int getRowCount() {
            if (showAll) {
                return _manualSignalMastList.size();
            } else {
                return _includedManualSignalMastList.size();
            }
        }

        public Object getValueAt(int r, int c) {
            ArrayList<ManualSignalMastList> signalMastList = null;
            if (showAll) {
                signalMastList = _manualSignalMastList;
            } else {
                signalMastList = _includedManualSignalMastList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(signalMastList.get(r).isIncluded());
                case SNAME_COLUMN:  // slot number
                    return signalMastList.get(r).getSysName();
                case UNAME_COLUMN:  //
                    return signalMastList.get(r).getUserName();
                case STATE_COLUMN:
                    return signalMastList.get(r).getSetToState();
                default:
                    return null;
            }
        }

        public String getValue(String name) {
            return InstanceManager.signalMastManagerInstance().getBySystemName(name).getAspect();
        }

        public String getColumnName(int col) {
            if (col == STATE_COLUMN) {
                return rb.getString("ColumnAspect");
            } else {
                return super.getColumnName(col);
            }
        }

        public void setValueAt(Object type, int r, int c) {
            ArrayList<ManualSignalMastList> signalMastList = null;

            if (showAll) {
                signalMastList = _manualSignalMastList;
            } else {
                signalMastList = _includedManualSignalMastList;
            }

            switch (c) {
                case STATE_COLUMN:
                    signalMastList.get(r).setSetToState((String) type);
                    fireTableRowsUpdated(r, r);
                    break;
                case INCLUDE_COLUMN:
                    signalMastList.get(r).setIncluded(((Boolean) type).booleanValue());
                    break;
                default:
                    break;
            }
        }

        TableSorter sorter;

        protected JTable makeJTable(TableSorter srtr) {
            this.sorter = srtr;
            return new JTable(sorter) {
                /**
                 *
                 */
                private static final long serialVersionUID = 5719838067969573916L;

                public boolean editCellAt(int row, int column, java.util.EventObject e) {
                    boolean res = super.editCellAt(row, column, e);
                    java.awt.Component c = this.getEditorComponent();
                    if (c instanceof javax.swing.JTextField) {
                        ((JTextField) c).selectAll();
                    }
                    return res;
                }

                public TableCellRenderer getCellRenderer(int row, int column) {
                    if (column == STATE_COLUMN) {
                        return getRenderer(row);
                    } else {
                        return super.getCellRenderer(row, column);
                    }
                }

                public TableCellEditor getCellEditor(int row, int column) {
                    if (column == STATE_COLUMN) {
                        return getEditor(row);
                    } else {
                        return super.getCellEditor(row, column);
                    }
                }

                TableCellRenderer getRenderer(int row) {
                    TableCellRenderer retval = rendererMap.get(sorter.getValueAt(row, SNAME_COLUMN));
                    if (retval == null) {
                        // create a new one with right aspects
                        retval = new MyComboBoxRenderer(getAspectVector(row));
                        rendererMap.put(sorter.getValueAt(row, SNAME_COLUMN), retval);
                    }
                    return retval;
                }
                Hashtable<Object, TableCellRenderer> rendererMap = new Hashtable<Object, TableCellRenderer>();

                TableCellEditor getEditor(int row) {
                    TableCellEditor retval = editorMap.get(sorter.getValueAt(row, SNAME_COLUMN));
                    if (retval == null) {
                        // create a new one with right aspects
                        retval = new MyComboBoxEditor(getAspectVector(row));
                        editorMap.put(sorter.getValueAt(row, SNAME_COLUMN), retval);
                    }
                    return retval;
                }
                Hashtable<Object, TableCellEditor> editorMap = new Hashtable<Object, TableCellEditor>();

                Vector<String> getAspectVector(int row) {
                    Vector<String> retval = boxMap.get(sorter.getValueAt(row, SNAME_COLUMN));
                    if (retval == null) {
                        // create a new one with right aspects
                        Vector<String> v = InstanceManager.signalMastManagerInstance()
                                .getSignalMast((String) sorter.getValueAt(row, SNAME_COLUMN)).getValidAspects();
                        v.add(0, "");
                        retval = v;
                        boxMap.put(sorter.getValueAt(row, SNAME_COLUMN), retval);
                    }
                    return retval;
                }
                Hashtable<Object, Vector<String>> boxMap = new Hashtable<Object, Vector<String>>();
            };
        }

    }

    abstract class AutoTableModel extends AbstractTableModel implements PropertyChangeListener {

        /**
         *
         */
        private static final long serialVersionUID = -272666720986232529L;

        AutoTableModel() {
            smlValid();
        }

        public void smlValid() {
            if (sml != null) {
                sml.addPropertyChangeListener(this);
            }
        }

        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        public void dispose() {
            jmri.InstanceManager.turnoutManagerInstance().removePropertyChangeListener(this);
        }

        public String getColumnName(int col) {
            switch (col) {
                case SNAME_COLUMN:
                    return rb.getString("ColumnSystemName");
                case UNAME_COLUMN:
                    return rb.getString("ColumnUserName");
                case STATE_COLUMN:
                    return rb.getString("ColumnState");

                default:
                    return "unknown";
            }
        }

        public int getColumnCount() {
            return 3;
        }

        public boolean isCellEditable(int r, int c) {
            return false;
        }

        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;
        public static final int STATE_COLUMN = 2;
    }

    class AutoBlockModel extends AutoTableModel {

        /**
         *
         */
        private static final long serialVersionUID = -1271144423340776473L;

        AutoBlockModel() {
            if (sml != null) {
                sml.addPropertyChangeListener(this);
            }
        }

        public static final int SPEED_COLUMN = 3;
        public static final int PERMISSIVE_COLUMN = 4;

        public int getColumnCount() {
            return 5;
        }

        public String getColumnName(int col) {
            switch (col) {
                case SPEED_COLUMN:
                    return rb.getString("ColumnSpeed");
                case PERMISSIVE_COLUMN:
                    return rb.getString("ColumnPermissive"); /*AbstractTableAction.rb.getString("ColumnUserName"); //"User Name";*/

            }
            return super.getColumnName(col);
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("autoblocks")) {
                // a new NamedBean is available in the manager
                initializeIncludedList();
                fireTableDataChanged();
            }
        }

        public Class<?> getColumnClass(int c) {
            if (c == PERMISSIVE_COLUMN) {
                return Boolean.class;
            }
            return super.getColumnClass(c);
        }

        public int getRowCount() {
            return _automaticBlockList.size();
        }

        public Object getValueAt(int r, int c) {
            switch (c) {
                case SNAME_COLUMN:  // slot number
                    return _automaticBlockList.get(r).getSysName();
                case UNAME_COLUMN:  //
                    return _automaticBlockList.get(r).getUserName();
                case STATE_COLUMN:
                    return _automaticBlockList.get(r).getSetToState();
                case SPEED_COLUMN:
                    return _automaticBlockList.get(r).getBlockSpeed();
                case PERMISSIVE_COLUMN:
                    //return new Boolean(_automaticBlockList.get(r).getPermissiveWorking());
                    return _automaticBlockList.get(r).getPermissiveWorking();
                default:
                    return null;
            }
        }

        public void setValueAt(Object type, int r, int c) {
        }

        public boolean isCellEditable(int r, int c) {
            return false;
        }
    }

    class AutoTurnoutModel extends AutoTableModel {

        /**
         *
         */
        private static final long serialVersionUID = 3406404811666081205L;

        AutoTurnoutModel() {
            super();
        }

        public int getRowCount() {
            return _automaticTurnoutList.size();
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("autoturnouts")) {
                // a new NamedBean is available in the manager
                initializeIncludedList();
                fireTableDataChanged();
            }
        }

        public Object getValueAt(int r, int c) {
            switch (c) {
                case SNAME_COLUMN:  // slot number
                    return _automaticTurnoutList.get(r).getSysName();
                case UNAME_COLUMN:  //
                    return _automaticTurnoutList.get(r).getUserName();
                case STATE_COLUMN:
                    return _automaticTurnoutList.get(r).getSetToState();
                default:
                    return null;
            }
        }
    }

    class AutoMastModel extends AutoTableModel {

        /**
         *
         */
        private static final long serialVersionUID = -6641207901154262227L;

        AutoMastModel() {
            super();
        }

        public int getRowCount() {
            return _automaticSignalMastList.size();
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("automasts")) {
                // a new NamedBean is available in the manager
                initializeIncludedList();
                fireTableDataChanged();
            }
        }

        public Object getValueAt(int r, int c) {
            switch (c) {
                case SNAME_COLUMN:  // slot number
                    return _automaticSignalMastList.get(r).getSysName();
                case UNAME_COLUMN:  //
                    return _automaticSignalMastList.get(r).getUserName();
                case STATE_COLUMN:
                    return _automaticSignalMastList.get(r).getSetToState();
                default:
                    return null;
            }
        }
    }

    public static class MyComboBoxEditor extends DefaultCellEditor {

        /**
         *
         */
        private static final long serialVersionUID = 5009970368789179788L;

        public MyComboBoxEditor(Vector<String> items) {
            super(new JComboBox<String>(items));
        }
    }

    public static class MyComboBoxRenderer extends JComboBox<String> implements TableCellRenderer {

        /**
         *
         */
        private static final long serialVersionUID = 1441539863361877122L;

        public MyComboBoxRenderer(Vector<String> items) {
            super(items);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }

            // Select the current value
            setSelectedItem(value);
            return this;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SignallingPanel.class.getName());
}

/* @(#)StatusPane.java */
