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
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import jmri.Block;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.SignalMastLogic;
import jmri.SignalMastManager;
import jmri.Turnout;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.beantable.RowComboBoxPanel; // access to RowComboBoxPanel() for valid Signal Mast Aspects and other states
import jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.swing.RowSorterUtil;
import jmri.util.SystemNameComparator;
import jmri.util.swing.JmriBeanComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a JFrame to configure Signal Mast Logic Pairs (Source + Destination
 * Masts).
 *
 * @author Kevin Dickerson Copyright (C) 2011
 * @author Egbert Broerse Copyright (C) 2017
 */
public class SignallingPanel extends jmri.util.swing.JmriPanel {

    JmriBeanComboBox sourceMastBox;
    JmriBeanComboBox destMastBox;
    JLabel fixedSourceMastLabel = new JLabel();
    JLabel fixedDestMastLabel = new JLabel();
    JLabel sourceMastLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("SourceMast")));  // NOI18N
    JLabel destMastLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("DestMast")));  // NOI18N
    JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));  // NOI18N
    JButton updateButton = new JButton(Bundle.getMessage("UpdateLogicButton"));  // NOI18N
    JButton applyButton = new JButton(Bundle.getMessage("ButtonApply"));  // NOI18N
    JCheckBox useLayoutEditor = new JCheckBox(Bundle.getMessage("UseLayoutEditorPaths"));  // NOI18N
    JCheckBox useLayoutEditorTurnout = new JCheckBox(Bundle.getMessage("UseTurnoutDetails"));  // NOI18N
    JCheckBox useLayoutEditorBlock = new JCheckBox(Bundle.getMessage("UseBlockDetails"));  // NOI18N
    JCheckBox allowAutoMastGeneration = new JCheckBox(Bundle.getMessage("AllowAutomaticSignalMast"));  // NOI18N
    JCheckBox lockTurnouts = new JCheckBox(Bundle.getMessage("LockTurnouts"));  // NOI18N
    JButton sizer = new JButton("Sizer");  // NOI18N

    // fields to store the items currently being configured
    SignalMast sourceMast;
    SignalMast destMast;
    SignalMastLogic sml;

    SignalMastManager smm = InstanceManager.getDefault(jmri.SignalMastManager.class);

    jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    JFrame jFrame;

    /**
     * Create an empty JPanel to configure a new Signal Mast Logic.
     *
     * @param frame Name for the enclosing JFrame
     */
    public SignallingPanel(JFrame frame) {
        this(null, null, frame);
    }

    /**
     * Create and fill in the JPanel to edit an existing Signal Mast Logic.
     *
     * @param source Bean of Source Signal Mast
     * @param dest   Bean of Destination Signal Mast
     * @param frame  Name for the enclosing JFrame
     */
    public SignallingPanel(SignalMast source, SignalMast dest, JFrame frame) {
        super();
        jFrame = frame;
        JLabel mastSpeed = new JLabel();

        if (source != null) {
            this.sourceMast = source;
            this.sml = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(source);
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
                mastSpeed.setText(Bundle.getMessage("PathSpeed") + " : " + Bundle.getMessage("NoneSet"));  // NOI18N
            } else {
                String speed = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getNamedSpeed(pathSpeed);
                if (speed != null) {
                    mastSpeed.setText(Bundle.getMessage("PathSpeed") + " : " + speed);  // NOI18N
                } else {
                    mastSpeed.setText(Bundle.getMessage("PathSpeed") + " : " + Float.toString(pathSpeed));  // NOI18N
                }
            }
        } else if (dest == null) {
            sml = null;
        }

        sourceMastBox = new JmriBeanComboBox(smm, sourceMast, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
        destMastBox = new JmriBeanComboBox(smm, destMast, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

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
            @Override
            public void actionPerformed(ActionEvent e) {
                if (useLayoutEditor.isSelected()) {
                    try {
                        boolean valid = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlockConnectivityTools().checkValidDest(sourceMastBox.getSelectedBean(),
                                destMastBox.getSelectedBean(), LayoutBlockConnectivityTools.MASTTOMAST);
                        if (!valid) {
                            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorUnReachableDestination"));  // NOI18N
                        }
                    } catch (jmri.JmriException je) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("WarningUnabletoValidate"));  // NOI18N
                    }
                }
            }
        });

        header.add(destPanel);
        header.add(mastSpeed);

        JPanel editor = new JPanel();
        editor.setLayout(new BoxLayout(editor, BoxLayout.Y_AXIS));
        editor.add(useLayoutEditor);

        editor.add(useLayoutEditorTurnout);
        editor.add(useLayoutEditorBlock);
        useLayoutEditorBlock.setVisible(false);
        useLayoutEditorTurnout.setVisible(false);

        useLayoutEditor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                useLayoutEditorBlock.setVisible(useLayoutEditor.isSelected());
                useLayoutEditorTurnout.setVisible(useLayoutEditor.isSelected());
                // Setup for display of all Turnouts, if needed
                boolean valid = false;
                if (useLayoutEditor.isSelected()) {
                    jFrame.pack();
                    if (!InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
                        int response;

                        response = JOptionPane.showConfirmDialog(null, Bundle.getMessage("EnableLayoutBlockRouting"));  // NOI18N
                        if (response == 0) {
                            InstanceManager.getDefault(LayoutBlockManager.class).enableAdvancedRouting(true);
                            JOptionPane.showMessageDialog(null, Bundle.getMessage("LayoutBlockRoutingEnabled"));  // NOI18N
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
                                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorUnReachableDestination"));  // NOI18N
                            }
                        } catch (jmri.JmriException je) {
                            JOptionPane.showMessageDialog(null, Bundle.getMessage("WarningUnabletoValidate"));  // NOI18N
                        }
                    }
                }
            }

        });
        header.add(editor);
        header.add(allowAutoMastGeneration);
        header.add(lockTurnouts);
        JPanel py = new JPanel();
        py.add(new JLabel(Bundle.getMessage("Show")));  // NOI18N
        selGroup = new ButtonGroup();
        allButton = new JRadioButton(Bundle.getMessage("All"), true);  // NOI18N
        selGroup.add(allButton);
        py.add(allButton);
        allButton.addActionListener(new ActionListener() {
            @Override
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
        includedButton = new JRadioButton(Bundle.getMessage("Included"), false);  // NOI18N
        selGroup.add(includedButton);
        py.add(includedButton);
        includedButton.addActionListener(new ActionListener() {
            @Override
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
        py.add(new JLabel("  " + Bundle.getMessage("Elements")));  // NOI18N
        header.add(py);

        containerPanel.add(header, BorderLayout.NORTH);

        JTabbedPane detailsTab = new JTabbedPane();
        detailsTab.add(Bundle.getMessage("Blocks"), buildBlocksPanel());  // NOI18N
        detailsTab.add(Bundle.getMessage("Turnouts"), buildTurnoutPanel());  // NOI18N
        detailsTab.add(Bundle.getMessage("Sensors"), buildSensorPanel());  // NOI18N
        detailsTab.add(Bundle.getMessage("SignalMasts"), buildSignalMastPanel());  // NOI18N

        containerPanel.add(detailsTab, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setLayout(new FlowLayout(FlowLayout.TRAILING));

        //Cancel button
        footer.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelPressed(e);
            }
        });

        //Update button
        footer.add(updateButton);
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePressed(e);
            }
        });
        updateButton.setToolTipText(Bundle.getMessage("UpdateButtonToolTip"));  // NOI18N
        updateButton.setVisible(true);

        //Apply (and Close) button
        footer.add(applyButton);
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyPressed(e);
            }
        });
        applyButton.setToolTipText(Bundle.getMessage("ApplyButtonToolTip"));  // NOI18N
        applyButton.setVisible(true);

        containerPanel.add(footer, BorderLayout.SOUTH);

        add(containerPanel);
        if (sourceMast != null) { // edit an existing SML, fix source mast
            fixedSourceMastLabel.setVisible(true);
            sourceMastBox.setVisible(false);
        } else { // source mast selectable for a new SML
            fixedSourceMastLabel.setVisible(false);
            sourceMastBox.setVisible(true);
        }
        if ((sml != null) && (destMast != null)) {  // edit an existing SML, fix destination mast
            fixedDestMastLabel.setVisible(true);
            destMastBox.setVisible(false);
            useLayoutEditorBlock.setVisible(useLayoutEditor.isSelected());
            useLayoutEditorTurnout.setVisible(useLayoutEditor.isSelected());
            initializeIncludedList();
            editDetails(); // pick up details for an existing SML configuration
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
    List<ManualBlockList> _manualBlockList;
    List<AutoBlockList> _automaticBlockList = new ArrayList<AutoBlockList>();

    TurnoutModel _turnoutModel;
    AutoTurnoutModel _autoTurnoutModel;
    List<ManualTurnoutList> _manualTurnoutList;
    List<AutoTurnoutList> _automaticTurnoutList = new ArrayList<AutoTurnoutList>();

    SensorModel _sensorModel;
    List<ManualSensorList> _manualSensorList;

    SignalMastModel _signalMastModel;
    AutoMastModel _autoSignalMastModel;
    List<ManualSignalMastList> _manualSignalMastList;
    List<AutoSignalMastList> _automaticSignalMastList = new ArrayList<AutoSignalMastList>();

    JPanel p2xb = new JPanel();

    /**
     * Compose GUI for setting up Blocks tab for an SML.
     *
     * @return a JPanel containing the SML control blocks configuration
     *         interface
     */
    JPanel buildBlocksPanel() {
        JPanel blockPanel = new JPanel();
        blockPanel.setLayout(new BoxLayout(blockPanel, BoxLayout.Y_AXIS));

        jmri.BlockManager bm = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
        List<String> systemNameList = bm.getSystemNameList();
        _manualBlockList = new ArrayList<ManualBlockList>(systemNameList.size());
        Iterator<String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
            _manualBlockList.add(new ManualBlockList(bm.getBySystemName(systemName)));
        }

        if ((sml != null) && (destMast != null)) {
            List<Block> blkList = sml.getAutoBlocks(destMast);
            _automaticBlockList = new ArrayList<AutoBlockList>(blkList.size());
            Iterator<Block> iterBlk = blkList.iterator();
            while (iterBlk.hasNext()) {
                Block blk = iterBlk.next();

                AutoBlockList blockitem = new AutoBlockList(blk);
                blockitem.setState(sml.getAutoBlockState(blk, destMast));

                _automaticBlockList.add(blockitem);
            }
        }
        JPanel p2xc = new JPanel();  // this hides a field
        JPanel p2xcSpace = new JPanel();
        p2xcSpace.setLayout(new BoxLayout(p2xcSpace, BoxLayout.Y_AXIS));
        p2xcSpace.add(new JLabel("XXX"));  // NOI18N
        p2xc.add(p2xcSpace);

        JPanel p21c = new JPanel();
        p21c.setLayout(new BoxLayout(p21c, BoxLayout.Y_AXIS));
        p21c.add(new JLabel(Bundle.getMessage("LabelSelectChecked", Bundle.getMessage("Blocks"))));  // NOI18N
        p2xc.add(p21c);

        _blockModel = new BlockModel();
        JTable manualBlockTable = new JTable(_blockModel);
        TableRowSorter<BlockModel> manualBlockSorter = new TableRowSorter<>(_blockModel);
        // configure row height for comboBox
        manualBlockTable.setRowHeight(sizer.getPreferredSize().height - 2); // row height has to be greater than for plain tables
        manualBlockSorter.setComparator(BlockModel.SNAME_COLUMN, new SystemNameComparator());
        RowSorterUtil.setSortOrder(manualBlockSorter, BlockModel.SNAME_COLUMN, SortOrder.ASCENDING);
        _blockModel.configStateColumn(manualBlockTable); // create static comboBox in State column
        manualBlockTable.setRowSorter(manualBlockSorter);
        manualBlockTable.setRowSelectionAllowed(false);
        manualBlockTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 100));
        // JComboBox<String> stateCCombo = new JComboBox<>(); // moved to ManualBlockTable class

        TableColumnModel _manualBlockColumnModel = manualBlockTable.getColumnModel();
        TableColumn includeColumnC = _manualBlockColumnModel.
                getColumn(BlockModel.INCLUDE_COLUMN);
        includeColumnC.setResizable(false);
        includeColumnC.setMinWidth(9 * Bundle.getMessage("Include").length()); // was fixed 60  // NOI18N
        includeColumnC.setMaxWidth(includeColumnC.getMinWidth() + 5);

        TableColumn sNameColumnC = _manualBlockColumnModel.
                getColumn(BlockModel.SNAME_COLUMN);
        sNameColumnC.setResizable(true);
        sNameColumnC.setMinWidth(75);
        sNameColumnC.setMaxWidth(95);

        TableColumn stateColumnC = _manualBlockColumnModel.
                getColumn(BlockModel.STATE_COLUMN);
        //stateColumnC.setCellEditor(new DefaultCellEditor(stateCCombo)); // moved to ManualBlockTable class
        stateColumnC.setResizable(false);
        stateColumnC.setMinWidth(9 * Math.max(SET_TO_UNOCCUPIED.length(), SET_TO_OCCUPIED.length()) + 40);
        stateColumnC.setMaxWidth(stateColumnC.getMinWidth() + 10); // was fixed 100
        // remaining space is filled by UserName
        _manualBlockScrollPane = new JScrollPane(manualBlockTable);
        p2xc.add(_manualBlockScrollPane, BorderLayout.CENTER);
        blockPanel.add(p2xc);
        p2xc.setVisible(true);

        ROW_HEIGHT = manualBlockTable.getRowHeight();
        p2xcSpace.setVisible(false);

        JPanel p2xaSpace = new JPanel();
        p2xaSpace.setLayout(new BoxLayout(p2xaSpace, BoxLayout.Y_AXIS));
        p2xaSpace.add(new JLabel("XXX"));  // NOI18N
        p2xb.add(p2xaSpace);

        JPanel p21a = new JPanel();
        p21a.setLayout(new BoxLayout(p21a, BoxLayout.Y_AXIS));
        p21a.add(new JLabel(Bundle.getMessage("LabelAutogenerated", Bundle.getMessage("Blocks"))));  // NOI18N
        p2xb.add(p21a);

        _autoBlockModel = new AutoBlockModel();
        JTable autoBlockTable = new JTable(_autoBlockModel);
        TableRowSorter<AutoBlockModel> autoBlockSorter = new TableRowSorter<>(_autoBlockModel);
        autoBlockSorter.setComparator(AutoBlockModel.SNAME_COLUMN, new SystemNameComparator());
        RowSorterUtil.setSortOrder(autoBlockSorter, AutoBlockModel.SNAME_COLUMN, SortOrder.ASCENDING);
        autoBlockTable.setRowSorter(autoBlockSorter);
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

    /**
     * Compose GUI for setting up the Turnouts tab for an SML.
     *
     * @return a JPanel containing the SML control turnouts configuration
     *         interface
     */
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
            List<Turnout> turnList = sml.getAutoTurnouts(destMast);
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
        p2xcSpace.add(new JLabel("XXX"));  // NOI18N
        p2xt.add(p2xcSpace);

        JPanel p21c = new JPanel();
        p21c.setLayout(new BoxLayout(p21c, BoxLayout.Y_AXIS));
        p21c.add(new JLabel(Bundle.getMessage("LabelSelectChecked", Bundle.getMessage("Turnouts"))));  // NOI18N
        p2xt.add(p21c);

        _turnoutModel = new TurnoutModel();
        JTable manualTurnoutTable = new JTable(_turnoutModel);
        TableRowSorter<TurnoutModel> manualTurnoutSorter = new TableRowSorter<>(_turnoutModel);
        // configure row height for comboBox
        manualTurnoutTable.setRowHeight(sizer.getPreferredSize().height - 2); // row height has to be greater than for plain tables
        manualTurnoutSorter.setComparator(TurnoutModel.SNAME_COLUMN, new SystemNameComparator());
        RowSorterUtil.setSortOrder(manualTurnoutSorter, TurnoutModel.SNAME_COLUMN, SortOrder.ASCENDING);
        _turnoutModel.configStateColumn(manualTurnoutTable); // create static comboBox in State column
        manualTurnoutTable.setRowSorter(manualTurnoutSorter);
        manualTurnoutTable.setRowSelectionAllowed(false);
        manualTurnoutTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 100));
        // JComboBox<String> stateCCombo = new JComboBox<>(); // moved to ManualTurnoutTable class

        TableColumnModel _manualTurnoutColumnModel = manualTurnoutTable.getColumnModel();
        TableColumn includeColumnC = _manualTurnoutColumnModel.
                getColumn(TurnoutModel.INCLUDE_COLUMN);
        includeColumnC.setResizable(false);
        includeColumnC.setMinWidth(9 * Bundle.getMessage("Include").length()); // was fixed 60  // NOI18N
        includeColumnC.setMaxWidth(includeColumnC.getMinWidth() + 5);

        TableColumn sNameColumnC = _manualTurnoutColumnModel.
                getColumn(TurnoutModel.SNAME_COLUMN);
        sNameColumnC.setResizable(true);
        sNameColumnC.setMinWidth(75);
        sNameColumnC.setMaxWidth(95);

        TableColumn stateColumnC = _manualTurnoutColumnModel.
                getColumn(TurnoutModel.STATE_COLUMN);
        // stateColumnC.setCellEditor(new DefaultCellEditor(stateCCombo)); // moved to ManualTurnoutTable class
        stateColumnC.setResizable(false);
        log.debug("L = " + SET_TO_ANY.length());
        stateColumnC.setMinWidth(9 * Math.max(SET_TO_ANY.length(), SET_TO_CLOSED.length()) + 30);
        stateColumnC.setMaxWidth(stateColumnC.getMinWidth() + 10); // was fixed 100
        // remaining space is filled by UserName
        _manualTurnoutScrollPane = new JScrollPane(manualTurnoutTable);
        p2xt.add(_manualTurnoutScrollPane, BorderLayout.CENTER);
        turnoutPanel.add(p2xt);
        p2xt.setVisible(true);

        ROW_HEIGHT = manualTurnoutTable.getRowHeight();
        p2xcSpace.setVisible(false);

        JPanel p2xaSpace = new JPanel();
        p2xaSpace.setLayout(new BoxLayout(p2xaSpace, BoxLayout.Y_AXIS));
        p2xaSpace.add(new JLabel("XXX"));  // NOI18N
        p2xa.add(p2xaSpace);

        JPanel p21a = new JPanel();
        p21a.setLayout(new BoxLayout(p21a, BoxLayout.Y_AXIS));
        p21a.add(new JLabel(Bundle.getMessage("LabelAutogenerated", Bundle.getMessage("Turnouts"))));  // NOI18N
        p2xa.add(p21a);

        _autoTurnoutModel = new AutoTurnoutModel();
        JTable autoTurnoutTable = new JTable(_autoTurnoutModel);
        TableRowSorter<AutoTurnoutModel> autoTurnoutSorter = new TableRowSorter<>(_autoTurnoutModel);
        autoTurnoutSorter.setComparator(AutoTurnoutModel.SNAME_COLUMN, new SystemNameComparator());
        RowSorterUtil.setSortOrder(autoTurnoutSorter, AutoTurnoutModel.SNAME_COLUMN, SortOrder.ASCENDING);
        autoTurnoutTable.setRowSorter(autoTurnoutSorter);
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
        stateColumnA.setResizable(false);
        stateColumnA.setMinWidth(90);
        stateColumnA.setMaxWidth(100);

        _autoTurnoutScrollPane = new JScrollPane(autoTurnoutTable);
        p2xa.add(_autoTurnoutScrollPane, BorderLayout.CENTER);
        turnoutPanel.add(p2xa);
        p2xa.setVisible(true);

        ROW_HEIGHT = autoTurnoutTable.getRowHeight();
        p2xaSpace.setVisible(false);

        return turnoutPanel;
    }

    /**
     * Compose GUI for setting up the Sensors tab for an SML.
     *
     * @return a JPanel containing the SML control sensors configuration
     *         interface
     */
    JPanel buildSensorPanel() {
        JPanel sensorPanel = new JPanel();
        sensorPanel.setLayout(new BoxLayout(sensorPanel, BoxLayout.Y_AXIS));

        jmri.SensorManager bm = jmri.InstanceManager.sensorManagerInstance();
        List<String> systemNameList = bm.getSystemNameList();
        _manualSensorList = new ArrayList<ManualSensorList>(systemNameList.size());
        Iterator<String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
            Sensor ss = bm.getBySystemName(systemName);
            if (ss != null) {
                String userName = ss.getUserName();
                _manualSensorList.add(new ManualSensorList(systemName, userName));
            } else {
                log.error("Failed to get sensor {}", systemName);  // NOI18N
            }
        }

        p2xs = new JPanel();
        JPanel p2xsSpace = new JPanel();
        p2xsSpace.setLayout(new BoxLayout(p2xsSpace, BoxLayout.Y_AXIS));
        p2xsSpace.add(new JLabel("XXX"));  // NOI18N
        p2xs.add(p2xsSpace);

        JPanel p21c = new JPanel();
        p21c.setLayout(new BoxLayout(p21c, BoxLayout.Y_AXIS));
        p21c.add(new JLabel(Bundle.getMessage("LabelSelectChecked", Bundle.getMessage("Sensors"))));  // NOI18N
        p2xs.add(p21c);

        _sensorModel = new SensorModel();
        JTable manualSensorTable = new JTable(_sensorModel);
        TableRowSorter<SensorModel> manualSensorSorter = new TableRowSorter<>(_sensorModel);
        // configure row height for comboBox
        manualSensorTable.setRowHeight(sizer.getPreferredSize().height - 2); // row height has to be greater than for plain tables
        manualSensorSorter.setComparator(SensorModel.SNAME_COLUMN, new SystemNameComparator());
        RowSorterUtil.setSortOrder(manualSensorSorter, SensorModel.SNAME_COLUMN, SortOrder.ASCENDING);
        _sensorModel.configStateColumn(manualSensorTable); // create static comboBox in State column
        manualSensorTable.setRowSorter(manualSensorSorter);
        manualSensorTable.setRowSelectionAllowed(false);
        manualSensorTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 100));
        //stateCCombo = new JComboBox<>(); // moved to ManualSensorTable class

        TableColumnModel _manualSensorColumnModel = manualSensorTable.getColumnModel();
        TableColumn includeColumnC = _manualSensorColumnModel.
                getColumn(SensorModel.INCLUDE_COLUMN);
        includeColumnC.setResizable(false);
        includeColumnC.setMinWidth(9 * Bundle.getMessage("Include").length()); // was fixed 60  // NOI18N
        includeColumnC.setMaxWidth(includeColumnC.getMinWidth() + 5);

        TableColumn sNameColumnC = _manualSensorColumnModel.
                getColumn(SensorModel.SNAME_COLUMN);
        sNameColumnC.setResizable(true);
        sNameColumnC.setMinWidth(75);
        sNameColumnC.setMaxWidth(95);

        TableColumn stateColumnC = _manualSensorColumnModel.
                getColumn(SensorModel.STATE_COLUMN);
        stateColumnC.setResizable(false);
        stateColumnC.setMinWidth(9 * SET_TO_INACTIVE.length() + 30);
        stateColumnC.setMaxWidth(stateColumnC.getMinWidth() + 10); // was fixed 100
        // remaining space is filled by UserName
        _manualSensorScrollPane = new JScrollPane(manualSensorTable);
        p2xs.add(_manualSensorScrollPane, BorderLayout.CENTER);

        sensorPanel.add(p2xs);
        p2xs.setVisible(true);

        ROW_HEIGHT = manualSensorTable.getRowHeight();
        p2xsSpace.setVisible(false);

        return sensorPanel;
    }

    JPanel p2xsm = new JPanel();

    /**
     * Compose GUI for setting up the Signal Masts tab for an SML.
     *
     * @return a JPanel containing the SML control signal masts configuration
     *         interface
     */
    JPanel buildSignalMastPanel() {
        JPanel SignalMastPanel = new JPanel();
        SignalMastPanel.setLayout(new BoxLayout(SignalMastPanel, BoxLayout.Y_AXIS));

        jmri.SignalMastManager bm = jmri.InstanceManager.getDefault(jmri.SignalMastManager.class);
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
        p2xmSpace.add(new JLabel("XXX"));  // NOI18N
        p2xm.add(p2xmSpace);

        JPanel p21c = new JPanel();
        p21c.setLayout(new BoxLayout(p21c, BoxLayout.Y_AXIS));
        p21c.add(new JLabel(Bundle.getMessage("LabelSelectChecked", Bundle.getMessage("SignalMasts"))));  // NOI18N
        p2xm.add(p21c);

        _signalMastModel = new SignalMastModel();
        TableRowSorter<SignalMastModel> sorter = new TableRowSorter<>(_signalMastModel);
        JTable manualSignalMastTable = new JTable(_signalMastModel); // don't use makeTable() since 4.7.1
        // configure (extra) row height for comboBox
        manualSignalMastTable.setRowHeight(sizer.getPreferredSize().height - 2);
        // row height has to be greater than plain tables to properly show comboBox shape, but tightened a bit over preferred
        _signalMastModel.configStateColumn(manualSignalMastTable); // create mast (row) specific comboBox in Aspect column
        manualSignalMastTable.setRowSorter(sorter);
        manualSignalMastTable.setRowSelectionAllowed(false);
        manualSignalMastTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 100));

        TableColumnModel _manualSignalMastColumnModel = manualSignalMastTable.getColumnModel();
        TableColumn includeColumnC = _manualSignalMastColumnModel.
                getColumn(SignalMastModel.INCLUDE_COLUMN);
        includeColumnC.setResizable(false);
        includeColumnC.setMinWidth(9 * Bundle.getMessage("Include").length()); // was fixed 60  // NOI18N
        includeColumnC.setMaxWidth(includeColumnC.getMinWidth() + 5);
        TableColumn sNameColumnC = _manualSignalMastColumnModel.
                getColumn(SignalMastModel.SNAME_COLUMN);
        sNameColumnC.setResizable(true);
        sNameColumnC.setMinWidth(75);
        sNameColumnC.setMaxWidth(95);

        TableColumn stateColumnC = _manualSignalMastColumnModel.
                getColumn(SensorModel.STATE_COLUMN);
        stateColumnC.setResizable(false);
        stateColumnC.setMinWidth(9 * ("Diverging Approach Medium").length() + 20);  // NOI18N
        stateColumnC.setMaxWidth(stateColumnC.getMinWidth() + 10); // was fixed 100
        // remaining space is filled by UserName
        _manualSignalMastScrollPane = new JScrollPane(manualSignalMastTable);
        p2xm.add(_manualSignalMastScrollPane, BorderLayout.CENTER);
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
        p21a.add(new JLabel(Bundle.getMessage("LabelAutogenerated", Bundle.getMessage("SignalMasts"))));  // NOI18N
        p2xsm.add(p21a);

        _autoSignalMastModel = new AutoMastModel();
        JTable autoMastTable = new JTable(_autoSignalMastModel);
        TableRowSorter<AutoMastModel> autoMastSorter = new TableRowSorter<>(_autoSignalMastModel);
        autoMastSorter.setComparator(AutoMastModel.SNAME_COLUMN, new SystemNameComparator());
        RowSorterUtil.setSortOrder(autoMastSorter, AutoMastModel.SNAME_COLUMN, SortOrder.ASCENDING);
        autoMastTable.setRowSorter(autoMastSorter);
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
        stateColumnA.setResizable(false);
        stateColumnA.setMinWidth(90);
        stateColumnA.setMaxWidth(100);

        _autoSignalMastScrollPane = new JScrollPane(autoMastTable);
        p2xsm.add(_autoSignalMastScrollPane, BorderLayout.CENTER);
        SignalMastPanel.add(p2xsm);
        p2xsm.setVisible(true);

        ROW_HEIGHT = autoMastTable.getRowHeight();
        p2xaSpace.setVisible(false);

        return SignalMastPanel;
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    /**
     * Update changes in SML when Update button is pressed in the Edit Logic -
     * Add Logic pane.
     *
     * @param e the event heard
     */
    void updatePressed(ActionEvent e) {
        sourceMast = (SignalMast) sourceMastBox.getSelectedBean();
        destMast = (SignalMast) destMastBox.getSelectedBean();
        boolean smlPairAdded = false;
        destOK = true;

        if (sourceMast == destMast || fixedSourceMastLabel.getText().equals(destMast.getDisplayName())) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSignalMastIdentical"));  // NOI18N
            destOK = false;
            log.debug("Destination Mast check failed, keep pane open");  // NOI18N
            return;
        }
        if ((sml == null) && (useLayoutEditor.isSelected())) {
            boolean valid = false;
            try {
                valid = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlockConnectivityTools().checkValidDest(sourceMast,
                        destMast, LayoutBlockConnectivityTools.MASTTOMAST);
                if (!valid) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorUnReachableDestination"));  // NOI18N
                    return;
                }
            } catch (jmri.JmriException je) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("WarningUnabletoValidate"));  // NOI18N
            }
        }

        if (sml == null) { // a new SML directly from the SML Table
            sml = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).newSignalMastLogic(sourceMast);
            // check if a similar SML pair already exists when in Add New session
            if (!sml.getDestinationList().contains(destMast)) { // not yet defined as a pair
                smlPairAdded = true;
                sml.setDestinationMast(destMast);
            } else {
                // show replace/update dialog
                int mes = JOptionPane.showConfirmDialog(null, Bundle.getMessage("WarningExistingPair"), // NOI18N
                        Bundle.getMessage("WarningTitle"), // NOI18N
                        JOptionPane.YES_NO_OPTION);
                if (mes == JOptionPane.NO_OPTION) {
                    return;
                }
            }
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
            Block blk = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getBlock(_includedManualBlockList.get(i).getSysName());
            hashBlocks.put(blk, _includedManualBlockList.get(i).getState());
        }
        sml.setBlocks(hashBlocks, destMast);

        Hashtable<NamedBeanHandle<Turnout>, Integer> hashTurnouts = new Hashtable<NamedBeanHandle<Turnout>, Integer>();
        for (int i = 0; i < _includedManualTurnoutList.size(); i++) {
            String turnoutName = _includedManualTurnoutList.get(i).getDisplayName();
            Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().getTurnout(_includedManualTurnoutList.get(i).getDisplayName());
            NamedBeanHandle<Turnout> namedTurnout = nbhm.getNamedBeanHandle(turnoutName, turnout);
            hashTurnouts.put(namedTurnout, _includedManualTurnoutList.get(i).getState());
            // no specific value, just show the current turnout state as selection in comboBox.
            // for existing SML pair, will be updated to show present setting by editDetails()
        }
        sml.setTurnouts(hashTurnouts, destMast);

        Hashtable<NamedBeanHandle<Sensor>, Integer> hashSensors = new Hashtable<NamedBeanHandle<Sensor>, Integer>();
        for (int i = 0; i < _includedManualSensorList.size(); i++) {
            String sensorName = _includedManualSensorList.get(i).getDisplayName();
            Sensor sensor = jmri.InstanceManager.sensorManagerInstance().getSensor(_includedManualSensorList.get(i).getDisplayName());
            NamedBeanHandle<Sensor> namedSensor = nbhm.getNamedBeanHandle(sensorName, sensor);
            hashSensors.put(namedSensor, _includedManualSensorList.get(i).getState());
            // no specific value, just show the current sensor state as selection in comboBox.
            // for existing SML pair, will be updated to show present setting by editDetails()
        }
        sml.setSensors(hashSensors, destMast);

        Hashtable<SignalMast, String> hashSignalMasts = new Hashtable<SignalMast, String>();
        for (int i = 0; i < _includedManualSignalMastList.size(); i++) {
            if (_includedManualSignalMastList.get(i).getMast() == sourceMast || _includedManualSignalMastList.get(i).getMast() == destMast) {
                // warn user that control mast is either source or destination mast of this pair, but allow as a valid choice
                int mes = JOptionPane.showConfirmDialog(null, java.text.MessageFormat.format(Bundle.getMessage("SignalMastCriteriaOwn"), // NOI18N
                        new Object[]{_includedManualSignalMastList.get(i).getMast().getDisplayName()}),
                        Bundle.getMessage("SignalMastCriteriaOwnTitle"), // NOI18N
                        JOptionPane.YES_NO_OPTION);
                if (mes == 0) { // Yes
                    hashSignalMasts.put(_includedManualSignalMastList.get(i).getMast(), _includedManualSignalMastList.get(i).getSetToState());
                } else { // No
                    _includedManualSignalMastList.get(i).setIncluded(false); // deselect "Included" checkBox for signal mast in manualSignalList
                    initializeIncludedList();
                    _signalMastModel.fireTableDataChanged();
                }
            } else {
                hashSignalMasts.put(_includedManualSignalMastList.get(i).getMast(), _includedManualSignalMastList.get(i).getSetToState());
            }
        }
        sml.setMasts(hashSignalMasts, destMast);

        sml.allowTurnoutLock(lockTurnouts.isSelected(), destMast);
        sml.initialise(destMast);
        if (smlPairAdded) {
            log.debug("New SML");  // NOI18N
            firePropertyChange("newDestination", null, destMastBox.getSelectedBean()); // to show new SML in underlying table  // NOI18N
        }
    }

    private boolean destOK = true; // false indicates destMast and sourceMast are identical

    /**
     * When Apply button is pressed, call updatePressed and afterwards close the
     * edit pane.
     *
     * @param e the event heard
     */
    void applyPressed(ActionEvent e) {
        updatePressed(e); // store edits
        if (destOK) { // enable user to correct configuration if warned the destMast in incorrect by skipping pane closing
            cancelPressed(e); // close panel signaling acceptance of edits/Apply to the user
        }
    }

    /**
     * Clean up when Cancel button is pressed.
     *
     * @param e the event heard
     */
    void cancelPressed(ActionEvent e) {
        jFrame.setVisible(false);
        jFrame.dispose();
        jFrame = null;
    }

    @Override
    public void initComponents() {

    }

    int blockModeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, blockInputModeValues, blockInputModes);

        if (result < 0) {
            log.warn("unexpected mode string in blockMode: " + mode);  // NOI18N
            throw new IllegalArgumentException();
        }
        return result;
    }

    void setBlockModeBox(int mode, JComboBox<String> box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, blockInputModeValues, blockInputModes);
        box.setSelectedItem(result);
    }

    private static String[] blockInputModes = new String[]{Bundle.getMessage("UnOccupied"), Bundle.getMessage("Occupied")};  // NOI18N
    private static int[] blockInputModeValues = new int[]{Block.UNOCCUPIED, Block.OCCUPIED};

    /**
     * Create new lists of control items configured as part of an SML.
     */
    void initializeIncludedList() {
        _includedManualBlockList = new ArrayList<ManualBlockList>();
        for (int i = 0; i < _manualBlockList.size(); i++) {
            if (_manualBlockList.get(i).isIncluded()) {
                _includedManualBlockList.add(_manualBlockList.get(i));
            }
        }

        if ((sml != null) && (destMast != null)) {
            List<Block> blkList = sml.getAutoBlocks(destMast);
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
            List<Turnout> turnList = sml.getAutoTurnouts(destMast);
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
            List<SignalMast> mastList = sml.getAutoMasts(destMast);
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

    /**
     * Free up resources when no longer used.
     */
    @Override
    public void dispose() {
    }

    ButtonGroup selGroup = null;
    JRadioButton allButton = null;
    JRadioButton includedButton = null;

    private boolean showAll = true;   // false indicates show only included items

    private static String SET_TO_ACTIVE = Bundle.getMessage("SensorStateActive");  // NOI18N
    private static String SET_TO_INACTIVE = Bundle.getMessage("SensorStateInactive");  // NOI18N
    private static String SET_TO_CLOSED = jmri.InstanceManager.turnoutManagerInstance().getClosedText();
    private static String SET_TO_THROWN = jmri.InstanceManager.turnoutManagerInstance().getThrownText();

    private static String SET_TO_UNOCCUPIED = Bundle.getMessage("UnOccupied");  // NOI18N
    private static String SET_TO_OCCUPIED = Bundle.getMessage("Occupied");  // NOI18N
    private static String SET_TO_ANY = Bundle.getMessage("AnyState");  // NOI18N

    private static int ROW_HEIGHT;

    /**
     * Cancels "Show Included Only" option
     */
    void cancelIncludedOnly() {
        if (!showAll) {
            allButton.doClick();
        }
    }

    /**
     * Update items in a comboBox to select a destination signal mast for the
     * SML.
     *
     * @deprecated 4.7.1
     *
     * @param box    comboBox to fill/update
     * @param select the item (mast) in the comboBox to set as the selected
     *               item; null for no selection
     */
    @Deprecated
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

    /**
     * Fill in existing SML configuration on the edit panel
     */
    void editDetails() {
        int setRow = 0;
        for (int i = _manualBlockList.size() - 1; i >= 0; i--) {
            ManualBlockList block = _manualBlockList.get(i);
            String tSysName = block.getSysName();
            Block blk = InstanceManager.getDefault(jmri.BlockManager.class).getBlock(tSysName);
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

    private List<ManualBlockList> _includedManualBlockList;
    private List<ManualTurnoutList> _includedManualTurnoutList;
    private List<ManualSensorList> _includedManualSensorList;
    private List<ManualSignalMastList> _includedManualSignalMastList;

    /**
     * Abstract class implemented during edit of an SML.
     */
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

    /**
     * A series of Lists to store all SML properties during Edit.
     */
    /**
     * A paired list of manually configurable Layout Blocks and a corresponding
     * Set To State used during edit of an SML.
     */
    private class ManualBlockList extends SignalMastElement {

        ManualBlockList(Block block) {
            this.block = block;
        }
        Block block;

        @Override
        String getSysName() {
            return block.getSystemName();
        }

        @Override
        String getUserName() {
            return block.getUserName();
        }

        boolean getPermissiveWorking() {
            return block.getPermissiveWorking();
        }

        String getBlockSpeed() {
            return block.getBlockSpeed();
        }

        @Override
        String getSetToState() {
            switch (_setToState) {
                case Block.OCCUPIED:
                    return SET_TO_OCCUPIED;
                case Block.UNOCCUPIED:
                    return SET_TO_UNOCCUPIED;
                default:
                    // fall out
                    break;
            }
            return SET_TO_ANY;
        }

        @Override
        void setSetToState(String state) {
            if (SET_TO_UNOCCUPIED.equals(state)) {
                _setToState = Block.UNOCCUPIED;
            } else if (SET_TO_OCCUPIED.equals(state)) {
                _setToState = Block.OCCUPIED;
            } else {
                _setToState = 0x03; // AnyState
            }
        }
    }

    /**
     * A paired list of automatically configured Layout Blocks and a
     * corresponding Set To State used during edit of an SML.
     */
    private class AutoBlockList extends ManualBlockList {

        AutoBlockList(Block block) {
            super(block);
        }

        @Override
        void setSetToState(String state) {
        }
    }

    /**
     * A paired list of manually configurable Turnouts and a corresponding Set
     * To State used during edit of an SML.
     */
    private class ManualTurnoutList extends SignalMastElement {

        ManualTurnoutList(String sysName, String userName) {
            super(sysName, userName);
        }

        @Override
        String getSetToState() {
            switch (_setToState) {
                case Turnout.THROWN:
                    return SET_TO_THROWN;
                case Turnout.CLOSED:
                    return SET_TO_CLOSED;
                default:
                    // fall out
                    break;
            }
            return SET_TO_ANY;
        }

        @Override
        void setSetToState(String state) {
            if (SET_TO_THROWN.equals(state)) {
                _setToState = Turnout.THROWN;
            } else if (SET_TO_CLOSED.equals(state)) {
                _setToState = Turnout.CLOSED;
            } else {
                _setToState = 0x00; // AnyState is not correctly returned with Turnouts
            }
        }
    }

    /**
     * A paired list of automatically configured Turnouts and a corresponding
     * Set To State used during edit of an SML.
     */
    private class AutoTurnoutList extends ManualTurnoutList {

        AutoTurnoutList(String sysName, String userName) {
            super(sysName, userName);
        }

        @Override
        void setSetToState(String state) {
        }
    }

    /**
     * A paired list of manually configured Sensors and a corresponding Set To
     * State used during edit of an SML.
     */
    private class ManualSensorList extends SignalMastElement {

        ManualSensorList(String sysName, String userName) {
            super(sysName, userName);
        }

        @Override
        String getSetToState() {
            switch (_setToState) {
                case Sensor.INACTIVE:
                    return SET_TO_INACTIVE;
                case Sensor.ACTIVE:
                    return SET_TO_ACTIVE;
                default:
                    // fall out
                    break;
            }
            return "";
        }

        @Override
        void setSetToState(String state) {
            if (SET_TO_INACTIVE.equals(state)) {
                _setToState = Sensor.INACTIVE;
            } else if (SET_TO_ACTIVE.equals(state)) {
                _setToState = Sensor.ACTIVE;
            } // do not provide other choices like "OnChange"
        }
    }

    /**
     * A paired list of manually configured Signal Masts and a corresponding Set
     * To State used during edit of an SML.
     */
    private class ManualSignalMastList extends SignalMastElement {

        ManualSignalMastList(SignalMast s) {
            mast = s;
        }

        String _setToAspect = "";

        SignalMast mast;

        SignalMast getMast() {
            return mast;
        }

        @Override
        String getSysName() {
            return mast.getSystemName();
        }

        @Override
        String getUserName() {
            return mast.getUserName();
        }

        @Override
        String getSetToState() {
            return _setToAspect;
        }

        @Override
        void setSetToState(String state) {
            _setToAspect = state;
        }
    }

    /**
     * A paired list of automatically configured Signal Masts and a
     * corresponding Set To State used during edit of an SML.
     */
    private class AutoSignalMastList extends ManualSignalMastList {

        AutoSignalMastList(SignalMast s) {
            super(s);
        }

        @Override
        void setSetToState(String state) {
        }

        void setState(String state) {
            _setToAspect = state;
        }
    }

    /**
     * A series of TableModels to display and edit configurations for
     * SignalMastLogic (SML) on the Tabs.
     */
    abstract class TableModel extends AbstractTableModel implements PropertyChangeListener {

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == INCLUDE_COLUMN) {
                return Boolean.class;
            } else if (c == STATE_COLUMN) {
                return RowComboBoxPanel.class; // Use a JPanel containing a custom State ComboBox
            } else {
                return String.class;
            }
        }

        /**
         * Respond to change from bean. Prevent State change during edit.
         *
         * @param e A property change of any bean
         */
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {  // NOI18N
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            }
        }

        /**
         * Remove references to and from this object, so that it can eventually
         * be garbage-collected.
         */
        public void dispose() {
            jmri.InstanceManager.turnoutManagerInstance().removePropertyChangeListener(this);
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case SNAME_COLUMN:
                    return Bundle.getMessage("ColumnSystemName");  // NOI18N
                case UNAME_COLUMN:
                    return Bundle.getMessage("ColumnUserName");  // NOI18N
                case INCLUDE_COLUMN:
                    return Bundle.getMessage("Include");  // NOI18N
                case STATE_COLUMN:
                    return Bundle.getMessage("ColumnState"); // pick up via SignallingBundle as it is a different "State" label than non-signal tables  // NOI18N
                default:
                    return "unknown";  // NOI18N
            }
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return ((c == INCLUDE_COLUMN) || (c == STATE_COLUMN));
        }

        /**
         * Customize the State column to show an appropriate ComboBox of
         * available options.
         *
         * @param table a JTable of beans
         */
        protected void configStateColumn(JTable table) {
            // have the state column hold a JPanel with a JComboBox for States
            // add extras, override BeanTableDataModel
            log.debug("Bean configStateColumn (I am {})", super.toString());  // NOI18N
            table.setDefaultEditor(RowComboBoxPanel.class, new StateComboBoxPanel());
            table.setDefaultRenderer(RowComboBoxPanel.class, new StateComboBoxPanel()); // use same class for the renderer
            // Set more things?
        }

        /**
         * Provide a table cell renderer looking like a JComboBox as an
         * editor/renderer for the manual tables on all except the Masts tab.
         * <p>
         * This is a lightweight version of the
         * {@link jmri.jmrit.beantable.RowComboBoxPanel} RowComboBox cell editor
         * class, some of the hashtables not needed here since we only need
         * identical options for all rows in a colomn.
         *
         * @see SignalMastModel.AspectComboBoxPanel for a full application with
         * row specific comboBox choices.
         */
        public class StateComboBoxPanel extends RowComboBoxPanel {

            @Override
            protected final void eventEditorMousePressed() {
                this.editor.add(getEditorBox(table.convertRowIndexToModel(this.currentRow))); // add editorBox to JPanel
                this.editor.revalidate();
                SwingUtilities.invokeLater(this.comboBoxFocusRequester);
                log.debug("eventEditorMousePressed in row: {})", this.currentRow);  // NOI18N
            }

            /**
             * Call the method in the surrounding method for the
             * SignalHeadTable.
             *
             * @param row the user clicked on in the table
             * @return an appropriate combobox for this signal head
             */
            @Override
            protected JComboBox getEditorBox(int row) {
                return getStateEditorBox(row);
            }

        }

        // Methods to display STATE_COLUMN ComboBox in tables.
        // All row values are in terms of the Model, not the Table as displayed.
        // Hashtables for Editors; none used for Renderers
        /**
         * Provide a static JComboBox element to display inside the JPanel
         * CellEditor. When not yet present, create, store and return a new one.
         *
         * @param row Index number (in TableDataModel)
         * @return A combobox containing the valid aspect names for this mast
         */
        JComboBox getStateEditorBox(int row) {
            // create dummy comboBox, override in extended classes for each bean
            JComboBox<String> editCombo = new JComboBox<>();
            editCombo.addItem(Bundle.getMessage("None"));  // NOI18N
            return editCombo;
        }
        // end of methods to display STATE_COLUMN ComboBox

        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;
        public static final int INCLUDE_COLUMN = 2;
        public static final int STATE_COLUMN = 3;
    }

    /**
     * TableModel for selecting SML control Blocks and Block Set To State.
     */
    class BlockModel extends TableModel {

        BlockModel() {
            jmri.InstanceManager.getDefault(jmri.BlockManager.class).addPropertyChangeListener(this);
        }

        @Override
        public int getRowCount() {
            if (showAll) {
                return _manualBlockList.size();
            } else {
                return _includedManualBlockList.size();
            }
        }

        public static final int SPEED_COLUMN = 4;
        public static final int PERMISSIVE_COLUMN = 5;

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public Object getValueAt(int r, int c) {
            List<ManualBlockList> blockList = null;
            if (showAll) {
                blockList = _manualBlockList;
            } else {
                blockList = _includedManualBlockList;
            }
            // some error checking
            if (r >= blockList.size()) {
                log.debug("row index is greater than block list");  // NOI18N
                return "error";  // NOI18N
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(blockList.get(r).isIncluded());
                case SNAME_COLUMN:
                    return blockList.get(r).getSysName();
                case UNAME_COLUMN:
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

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == PERMISSIVE_COLUMN) {
                return Boolean.class;
            }
            return super.getColumnClass(c);
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case SPEED_COLUMN:
                    return Bundle.getMessage("ColumnSpeed");  // NOI18N
                case PERMISSIVE_COLUMN:
                    return Bundle.getMessage("ColumnPermissive");  // NOI18N
                default:
                    // fall out
                    break;
            }
            return super.getColumnName(col);
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            List<ManualBlockList> blockList = null;
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
                    log.debug("State = " + type);  // NOI18N
                    blockList.get(r).setSetToState((String) type);
                    break;
                default:
                    break;
            }
        }

        /**
         * Provide a static JComboBox element to display inside the JPanel
         * CellEditor. When not yet present, create, store and return a new one.
         *
         * @param row Index number (in TableDataModel)
         * @return A combobox containing the valid aspect names for this mast
         */
        @Override
        JComboBox getStateEditorBox(int row) {
            // create dummy comboBox, override in extended classes for each bean
            JComboBox<String> editCombo = new JComboBox<>();
            editCombo.addItem(SET_TO_UNOCCUPIED);
            editCombo.addItem(SET_TO_OCCUPIED);
            editCombo.addItem(SET_TO_ANY);
            return editCombo;
        }

    }

    /**
     * TableModel for selecting SML control Turnouts and Turnout Set To State.
     */
    class TurnoutModel extends TableModel {

        TurnoutModel() {
            jmri.InstanceManager.turnoutManagerInstance().addPropertyChangeListener(this);
        }

        @Override
        public int getRowCount() {
            if (showAll) {
                return _manualTurnoutList.size();
            } else {
                return _includedManualTurnoutList.size();
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            List<ManualTurnoutList> turnoutList = null;
            if (showAll) {
                turnoutList = _manualTurnoutList;
            } else {
                turnoutList = _includedManualTurnoutList;
            }
            // some error checking
            if (r >= turnoutList.size()) {
                log.debug("row index is greater than turnout list");  // NOI18N
                return "error";  // NOI18N
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(turnoutList.get(r).isIncluded());
                case SNAME_COLUMN:
                    return turnoutList.get(r).getSysName();
                case UNAME_COLUMN:
                    return turnoutList.get(r).getUserName();
                case STATE_COLUMN:
                    // initial answer is 'Thrown', never null or empty
                    return turnoutList.get(r).getSetToState();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            List<ManualTurnoutList> turnoutList = null;
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
                    log.debug("State = " + type);  // NOI18N
                    if ((String) type != null) {
                        turnoutList.get(r).setSetToState((String) type);
                        fireTableRowsUpdated(r, r); // use new value
                    }
                    break;
                default:
                    break;
            }
        }

        /**
         * Provide a static JComboBox element to display inside the JPanel
         * CellEditor. When not yet present, create, store and return a new one.
         *
         * @param row Index number (in TableDataModel)
         * @return A combobox containing the valid aspect names for this mast
         */
        @Override
        JComboBox getStateEditorBox(int row) {
            // create dummy comboBox, override in extended classes for each bean
            JComboBox<String> editCombo = new JComboBox<>();
            editCombo.addItem(SET_TO_THROWN);
            editCombo.addItem(SET_TO_CLOSED);
            editCombo.addItem(SET_TO_ANY);
            return editCombo;
        }

    }

    /**
     * TableModel for selecting SML control Sensors and Sensor Set To State.
     */
    class SensorModel extends TableModel {

        SensorModel() {
            InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
        }

        @Override
        public int getRowCount() {
            if (showAll) {
                return _manualSensorList.size();
            } else {
                return _includedManualSensorList.size();
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            List<ManualSensorList> sensorList = null;
            if (showAll) {
                sensorList = _manualSensorList;
            } else {
                sensorList = _includedManualSensorList;
            }
            // some error checking
            if (r >= sensorList.size()) {
                log.debug("row index is greater than sensor list");  // NOI18N
                return null;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(sensorList.get(r).isIncluded());
                case SNAME_COLUMN:
                    return sensorList.get(r).getSysName();
                case UNAME_COLUMN:
                    return sensorList.get(r).getUserName();
                case STATE_COLUMN:
                    return sensorList.get(r).getSetToState();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            List<ManualSensorList> sensorList = null;
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

        /**
         * Provide a static JComboBox element to display inside the JPanel
         * CellEditor. When not yet present, create, store and return a new one.
         *
         * @param row Index number (in TableDataModel)
         * @return A combobox containing the valid aspect names for this mast
         */
        @Override
        JComboBox getStateEditorBox(int row) {
            // create dummy comboBox, override in extended classes for each bean
            JComboBox<String> editCombo = new JComboBox<>();
            editCombo.addItem(SET_TO_INACTIVE);
            editCombo.addItem(SET_TO_ACTIVE);
            return editCombo;
        }

    }

    /**
     * Set up table for selecting Signal Masts and an Aspect on each mast
     * Updated for TableRowSorter
     */
    class SignalMastModel extends TableModel {

        SignalMastModel() {
            jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).addPropertyChangeListener(this);
        }

        @Override
        public int getRowCount() {
            if (showAll) {
                return _manualSignalMastList.size();
            } else {
                return _includedManualSignalMastList.size();
            }
        }

        @Override
        public Object getValueAt(int r, int c) { // get values from objects to display in table cells
            List<ManualSignalMastList> signalMastList = null;
            if (showAll) {
                signalMastList = _manualSignalMastList;
            } else {
                signalMastList = _includedManualSignalMastList;
            }
            // some error checking
            if (r >= signalMastList.size()) {
                log.debug("row index is greater than mast list");  // NOI18N
                return "error";  // NOI18N
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(signalMastList.get(r).isIncluded());
                case SNAME_COLUMN:
                    return signalMastList.get(r).getSysName();
                case UNAME_COLUMN:
                    return signalMastList.get(r).getUserName();
                case STATE_COLUMN:
                    try {
                        return signalMastList.get(r).getSetToState();
                    } catch (java.lang.NullPointerException e) {
                        //Aspect not set
                        log.debug("Aspect for mast {} not set", r);  // NOI18N
                        return Bundle.getMessage("BeanStateUnknown"); // use place holder string in table  // NOI18N
                    }
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) { // store (new) choices in mast
            List<ManualSignalMastList> signalMastList = null;
            if (showAll) {
                signalMastList = _manualSignalMastList;
            } else {
                signalMastList = _includedManualSignalMastList;
            }
            switch (c) {
                case STATE_COLUMN:
                    if ((String) type != null) {
                        //convertRowIndexToModel(row) not needed
                        log.debug("setValueAt (rowConverted={}; value={})", r, type);  // NOI18N
                        signalMastList.get(r).setSetToState((String) type);
                        fireTableRowsUpdated(r, r);
                    }
                    break;
                case INCLUDE_COLUMN:
                    signalMastList.get(r).setIncluded(((Boolean) type).booleanValue());
                    break;
                default:
                    break;
            }
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == STATE_COLUMN) {
                return RowComboBoxPanel.class; // Use a JPanel containing a custom State ComboBox
            }
            return super.getColumnClass(c);
        }

        public String getValue(String name) { // called by Table Cell Renderer
            SignalMast sm = InstanceManager.getDefault(jmri.SignalMastManager.class).getBySystemName(name);
            if (sm != null) {
                return sm.getAspect(); // return _manualSignalMastList.get(sm).getSetToState(); // unspecific, have to translate sm to index in model
            } else {
                return null; // reporting "Unknown" seems useless for this application
            }
        }

        @Override
        public String getColumnName(int col) {
            if (col == STATE_COLUMN) {
                return Bundle.getMessage("ColumnAspect"); // cf. line 1356 for general/other bean types  // NOI18N
            } else {
                return super.getColumnName(col);
            }
        }

        /**
         * Customize the SignalMast State (Appearance) column to show an
         * appropriate ComboBox of available Aspects.
         *
         * @param table a JTable of Signal Masts
         */
        @Override
        protected void configStateColumn(JTable table) {
            // have the state column hold a JPanel with a JComboBox for Aspects
            // add extras, override BeanTableDataModel
            log.debug("Mast configStateColumn (I am {})", super.toString());  // NOI18N
            table.setDefaultEditor(RowComboBoxPanel.class, new AspectComboBoxPanel());
            table.setDefaultRenderer(RowComboBoxPanel.class, new AspectComboBoxPanel()); // use same class for the renderer
            // Set more things?
        }

        /**
         * A row specific Aspect combobox cell editor/renderer.
         * <p>
         * This is a full version of the
         * {@link jmri.jmrit.beantable.RowComboBoxPanel} RowComboBox cell editor
         * class, including all hashtables and row specific comboBox choices.
         *
         * @see StateComboBoxPanel for a lightweight application when all that's
         * needed are identical options for all rows in a colomn.
         */
        public class AspectComboBoxPanel extends RowComboBoxPanel {

            @Override
            protected final void eventEditorMousePressed() {
                this.editor.add(getEditorBox(table.convertRowIndexToModel(this.currentRow))); // add eb to JPanel
                this.editor.revalidate();
                SwingUtilities.invokeLater(this.comboBoxFocusRequester);
                log.debug("eventEditorMousePressed in row: {}; me = {})", this.currentRow, this.toString());  // NOI18N
            }

            /**
             * Call method {@link #getAspectEditorBox(int)} in the surrounding
             * method for the SignalMastTable
             *
             * @param row Index of the row clicked in the table
             * @return an appropriate combobox for this signal mast
             */
            @Override
            protected JComboBox getEditorBox(int row) {
                return getAspectEditorBox(row);
            }

        }

        // Methods to display STATE_COLUMN (aspect) ComboBox in the Signal Mast Manual Table
        // Derived from the SignalMastJTable class (deprecated since 4.5.5):
        // All row values are in terms of the Model, not the Table as displayed.
        /**
         * Clear the old aspect comboboxes and force them to be rebuilt
         *
         * @param row Index of the signal mast (in TableDataModel) to be rebuilt
         *            in the Hashtables
         */
        public void clearAspectVector(int row) {
            boxMap.remove(this.getValueAt(row, SNAME_COLUMN));
            editorMap.remove(this.getValueAt(row, SNAME_COLUMN));
        }

        // Hashtables for Editors; none used for Renderers
        /**
         * Provide a JComboBox element to display inside the JPanel CellEditor.
         * When not yet present, create, store and return a new one.
         *
         * @param row Index number (in TableDataModel)
         * @return A combobox containing the valid aspect names for this mast
         */
        JComboBox getAspectEditorBox(int row) {
            JComboBox editCombo = editorMap.get(this.getValueAt(row, SNAME_COLUMN));
            if (editCombo == null) {
                // create a new one with correct aspects
                editCombo = new JComboBox<String>(getAspectVector(row)); // show it
                editorMap.put(this.getValueAt(row, SNAME_COLUMN), editCombo); // and store it
            }
            return editCombo;
        }
        Hashtable<Object, JComboBox> editorMap = new Hashtable<Object, JComboBox>();

        /**
         * Holds a Hashtable of valid aspects per signal mast used by
         * getAspectEditorBox()
         *
         * @param row Index number (in TableDataModel)
         * @return The Vector of valid aspect names for this mast to show in the
         *         JComboBox
         */
        Vector<String> getAspectVector(int row) {
            Vector<String> comboaspects = boxMap.get(this.getValueAt(row, SNAME_COLUMN));
            if (comboaspects == null) {
                // create a new one with right aspects
                Vector<String> v = InstanceManager.getDefault(jmri.SignalMastManager.class)
                        .getSignalMast((String) this.getValueAt(row, SNAME_COLUMN)).getValidAspects();
                comboaspects = v;
                boxMap.put(this.getValueAt(row, SNAME_COLUMN), comboaspects); // and store it
            }
            return comboaspects;
        }

        Hashtable<Object, Vector<String>> boxMap = new Hashtable<Object, Vector<String>>();

        // end of methods to display STATE_COLUMN (Aspect) ComboBox
        /**
         * Create a compact control Signal Mast table.
         *
         * @deprecated since 4.5.7, use {@link #getAspectEditorBox(int) }
         *
         * @param model the selected SignalMastModel
         * @return JTable contaning interface to configure a signal mast
         */
        @Deprecated
        protected JTable makeJTable(SignalMastModel model) {
            return new JTable(model) {

                @Override
                public TableCellRenderer getCellRenderer(int row, int column) {
                    if (column == STATE_COLUMN) {
                        return getRenderer(row);
                    } else {
                        return super.getCellRenderer(row, column);
                    }
                }

                /**
                 * @deprecated since 4.5.7
                 */
                @Deprecated
                @Override
                public TableCellEditor getCellEditor(int row, int column) {
                    if (column == STATE_COLUMN) {
                        return getEditor(row);
                    } else {
                        return super.getCellEditor(row, column);
                    }
                }

                /**
                 * @deprecated since 4.5.7
                 */
                @Deprecated
                TableCellRenderer getRenderer(int row) {
                    TableCellRenderer retval = rendererMap.get(getModel().getValueAt(row, SNAME_COLUMN));
                    if (retval == null) {
                        // create a new one with right aspects
                        retval = new MyComboBoxRenderer(getAspectVector(row));
                        rendererMap.put(getModel().getValueAt(row, SNAME_COLUMN), retval);
                    }
                    return retval;
                }
                Hashtable<Object, TableCellRenderer> rendererMap = new Hashtable<Object, TableCellRenderer>();

                /**
                 * @deprecated since 4.5.7
                 */
                @Deprecated
                TableCellEditor getEditor(int row) {
                    TableCellEditor retval = editorMap.get(getModel().getValueAt(row, SNAME_COLUMN));
                    if (retval == null) {
                        // create a new one with right aspects
                        retval = new MyComboBoxEditor(getAspectVector(row));
                        editorMap.put(getModel().getValueAt(row, SNAME_COLUMN), retval);
                    }
                    return retval;
                }
                Hashtable<Object, TableCellEditor> editorMap = new Hashtable<Object, TableCellEditor>();

                /**
                 * @deprecated since 4.5.7
                 */
                @Deprecated
                Vector<String> getAspectVector(int row) {
                    Vector<String> retval = boxMap.get(getModel().getValueAt(row, SNAME_COLUMN));
                    if (retval == null) {
                        // create a new one with right aspects
                        Vector<String> v = InstanceManager.getDefault(jmri.SignalMastManager.class)
                                .getSignalMast((String) getModel().getValueAt(row, SNAME_COLUMN)).getValidAspects();
                        v.add(0, "");
                        retval = v;
                        boxMap.put(getModel().getValueAt(row, SNAME_COLUMN), retval);
                    }
                    return retval;
                }
                Hashtable<Object, Vector<String>> boxMap = new Hashtable<Object, Vector<String>>();
            };
        }

    }

    /**
     * A series of autoTableModels to display - but not edit - configurations on
     * the Edit SML Tabs that are autogenerated from layout Editor information.
     */
    abstract class AutoTableModel extends AbstractTableModel implements PropertyChangeListener {

        AutoTableModel() {
            smlValid();
        }

        public void smlValid() {
            if (sml != null) {
                sml.addPropertyChangeListener(this);
            }
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        /**
         * Remove references to and from this object, so that it can eventually
         * be garbage-collected.
         */
        public void dispose() {
            jmri.InstanceManager.turnoutManagerInstance().removePropertyChangeListener(this);
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case SNAME_COLUMN:
                    return Bundle.getMessage("ColumnSystemName");  // NOI18N
                case UNAME_COLUMN:
                    return Bundle.getMessage("ColumnUserName");  // NOI18N
                case STATE_COLUMN:
                    return Bundle.getMessage("ColumnAspect"); // pick up via SignallingBundle as it is a different "State" label than non-signal tables  // NOI18N

                default:
                    return "unknown";  // NOI18N
            }
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;
        public static final int STATE_COLUMN = 2;
    }

    /**
     * TableModel to display - but not edit - Auto Layout Blocks on the Edit SML
     * Blocks Tab.
     */
    class AutoBlockModel extends AutoTableModel {

        AutoBlockModel() {
            if (sml != null) {
                sml.addPropertyChangeListener(this);
            }
        }

        public static final int SPEED_COLUMN = 3;
        public static final int PERMISSIVE_COLUMN = 4;

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case SPEED_COLUMN:
                    return Bundle.getMessage("ColumnSpeed");  // NOI18N
                case PERMISSIVE_COLUMN:
                    return Bundle.getMessage("ColumnPermissive");  // NOI18N
                default:
                    // fall out
                    break;
            }
            return super.getColumnName(col);
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("autoblocks")) {  // NOI18N
                // a new NamedBean is available in the manager
                initializeIncludedList();
                fireTableDataChanged();
            }
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == PERMISSIVE_COLUMN) {
                return Boolean.class;
            }
            return super.getColumnClass(c);
        }

        @Override
        public int getRowCount() {
            return _automaticBlockList.size();
        }

        @Override
        public Object getValueAt(int r, int c) {
            switch (c) {
                case SNAME_COLUMN:
                    return _automaticBlockList.get(r).getSysName();
                case UNAME_COLUMN:
                    return _automaticBlockList.get(r).getUserName();
                case STATE_COLUMN:
                    return _automaticBlockList.get(r).getSetToState();
                case SPEED_COLUMN:
                    return _automaticBlockList.get(r).getBlockSpeed();
                case PERMISSIVE_COLUMN:
                    return _automaticBlockList.get(r).getPermissiveWorking();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    }

    /**
     * TableModel to display - but not edit - Auto Turnouts on the Edit SML
     * Turnouts Tab.
     */
    class AutoTurnoutModel extends AutoTableModel {

        AutoTurnoutModel() {
            super();
        }

        @Override
        public int getRowCount() {
            return _automaticTurnoutList.size();
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("autoturnouts")) {  // NOI18N
                // a new NamedBean is available in the manager
                initializeIncludedList();
                fireTableDataChanged();
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            switch (c) {
                case SNAME_COLUMN:
                    return _automaticTurnoutList.get(r).getSysName();
                case UNAME_COLUMN:
                    return _automaticTurnoutList.get(r).getUserName();
                case STATE_COLUMN:
                    return _automaticTurnoutList.get(r).getSetToState();
                default:
                    return null;
            }
        }
    }

    /**
     * TableModel to display - but not edit - Auto Signal Masts on the Edit SML
     * Signal Masts Tab.
     */
    class AutoMastModel extends AutoTableModel {

        AutoMastModel() {
            super();
        }

        @Override
        public int getRowCount() {
            return _automaticSignalMastList.size();
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("automasts")) {  // NOI18N
                // a new NamedBean is available in the manager
                initializeIncludedList();
                fireTableDataChanged();
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            switch (c) {
                case SNAME_COLUMN:
                    return _automaticSignalMastList.get(r).getSysName();
                case UNAME_COLUMN:
                    return _automaticSignalMastList.get(r).getUserName();
                case STATE_COLUMN:
                    return _automaticSignalMastList.get(r).getSetToState();
                default:
                    return null;
            }
        }
    }

    /**
     * Class to provide a cell editor with a drop down list of signal mast
     * aspects.
     *
     * @deprecated since 4.7.1, use
     * {@link SignalMastModel#getAspectEditorBox(int)}
     */
    @Deprecated
    public static class MyComboBoxEditor extends DefaultCellEditor {

        public MyComboBoxEditor(Vector<String> items) {
            super(new JComboBox<String>(items));
        }
    }

    /**
     * Class to provide a cell renderer looking like a drop down list showing
     * the current value.
     *
     * @deprecated since 4.7.1, use
     * {@link SignalMastModel#getAspectEditorBox(int)}
     */
    @Deprecated
    public static class MyComboBoxRenderer extends JComboBox<String> implements TableCellRenderer {

        public MyComboBoxRenderer(Vector<String> items) {
            super(items);
        }

        @Override
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

    private final static Logger log = LoggerFactory.getLogger(SignallingPanel.class);
}
