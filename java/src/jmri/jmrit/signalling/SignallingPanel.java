package jmri.jmrit.signalling;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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
import jmri.NamedBean.DisplayOptions;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.beantable.RowComboBoxPanel;
import jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.swing.NamedBeanComboBox;
import jmri.swing.RowSorterUtil;
import jmri.util.swing.JmriPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a JFrame to configure Signal Mast Logic Pairs (Source + Destination
 * Masts).
 *
 * @author Kevin Dickerson Copyright (C) 2011
 * @author Egbert Broerse Copyright (C) 2017, 2018, 2019
 */
public class SignallingPanel extends JmriPanel {

    private NamedBeanComboBox<SignalMast> sourceMastBox;
    private NamedBeanComboBox<SignalMast> destMastBox;
    private JLabel fixedSourceMastLabel = new JLabel();
    private JLabel fixedDestMastLabel = new JLabel();
    private static final JLabel sourceMastLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("SourceMast")), JLabel.TRAILING);  // NOI18N
    private static final JLabel destMastLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("DestMast")), JLabel.TRAILING);  // NOI18N
    private JCheckBox useLayoutEditor = new JCheckBox(Bundle.getMessage("UseLayoutEditorPaths"));  // NOI18N
    private JCheckBox useLayoutEditorTurnout = new JCheckBox(Bundle.getMessage("UseTurnoutDetails"));  // NOI18N
    private JCheckBox useLayoutEditorBlock = new JCheckBox(Bundle.getMessage("UseBlockDetails"));  // NOI18N
    private JCheckBox allowAutoMastGeneration = new JCheckBox(Bundle.getMessage("AllowAutomaticSignalMast"));  // NOI18N
    private JCheckBox lockTurnouts = new JCheckBox(Bundle.getMessage("LockTurnouts"));  // NOI18N
    private static final JButton sizer = new JButton("Sizer");  // NOI18N

    // fields to store the items currently being configured
    private SignalMast sourceMast;
    private SignalMast destMast;
    private SignalMastLogic sml;

    private jmri.NamedBeanHandleManager nbhm = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    private JFrame jFrame;

    // Size of the individual bean tables inside the shared pane
    private static final Dimension TABLESIZEPREFERRED = new Dimension(720, 200);

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
     * @see SignallingFrame
     * @param source Bean of Source Signal Mast
     * @param dest   Bean of Destination Signal Mast
     * @param frame  Name for the enclosing JFrame
     */
    public SignallingPanel(SignalMast source, SignalMast dest, JFrame frame) {
        super();
        jFrame = frame;
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));  // NOI18N
        JButton updateButton = new JButton(Bundle.getMessage("UpdateLogicButton"));  // NOI18N
        JButton applyButton = new JButton(Bundle.getMessage("ButtonApply"));  // NOI18N
        JLabel mastSpeed = new JLabel();

        if (source != null) {
            this.sourceMast = source;
            this.sml = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(source);
            fixedSourceMastLabel = new JLabel(sourceMast.getDisplayName());
            // if (dest != null) {
            //   frame.setTitle(source.getDisplayName() + " to " + dest.getDisplayName());
            // }
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

            float pathSpeed = sml.getMaximumSpeed(dest);
            if (pathSpeed == 0.0f) {
                mastSpeed.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("PathSpeed")) + " " + Bundle.getMessage("NoneSet"));  // NOI18N
            } else {
                String speed = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getNamedSpeed(pathSpeed);
                if (speed != null) {
                    mastSpeed.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("PathSpeed")) + " " + speed);  // NOI18N
                } else {
                    mastSpeed.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("PathSpeed")) + " " + pathSpeed);  // NOI18N
                }
            }
        } else if (dest == null) {
            sml = null;
        }

        SignalMastManager smm = InstanceManager.getDefault(jmri.SignalMastManager.class);
        sourceMastBox = new NamedBeanComboBox<>(smm, sourceMast, DisplayOptions.DISPLAYNAME);
        sourceMastBox.setMaximumSize(sourceMastBox.getPreferredSize());
        destMastBox = new NamedBeanComboBox<>(smm, destMast, DisplayOptions.DISPLAYNAME);
        destMastBox.setMaximumSize(destMastBox.getPreferredSize());

        // directly add sub-panes onto JFrame's content pane to allow resizing (2018)
        Container contentPane = frame.getContentPane();

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JPanel mastGrid = new JPanel();
        GridLayout layout = new GridLayout(2, 2, 10, 0); // (int rows, int cols, int hgap, int vgap)
        mastGrid.setLayout(layout);
        // row 1
        mastGrid.add(sourceMastLabel);

        JPanel sourcePanel = new JPanel();
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.X_AXIS));
        sourcePanel.add(sourceMastBox);
        sourcePanel.add(fixedSourceMastLabel);
        mastGrid.add(sourcePanel);
        // row 2
        mastGrid.add(destMastLabel);

        JPanel destPanel = new JPanel();
        destPanel.setLayout(new BoxLayout(destPanel, BoxLayout.X_AXIS));
        destPanel.add(destMastBox);
        destPanel.add(fixedDestMastLabel);

        destMastBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (useLayoutEditor.isSelected()) {
                    try {
                        boolean valid = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlockConnectivityTools().checkValidDest(sourceMastBox.getSelectedItem(),
                                destMastBox.getSelectedItem(), LayoutBlockConnectivityTools.MASTTOMAST);
                        if (!valid) {
                            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorUnReachableDestination"));
                        }
                    } catch (jmri.JmriException je) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("WarningUnableToValidate"));
                    }
                }
            }
        });

        mastGrid.add(destPanel);
        header.add(mastGrid);

        header.add(mastSpeed);

        JPanel editor = new JPanel();
        editor.setLayout(new BoxLayout(editor, BoxLayout.Y_AXIS));
        useLayoutEditor.setAlignmentX(Component.LEFT_ALIGNMENT);
        editor.add(useLayoutEditor);

        JPanel useLayoutEditorSubPanel = new JPanel(); // indent 2 options connected to LayoutEditor choice
        useLayoutEditorSubPanel.setLayout(new BoxLayout(useLayoutEditorSubPanel, BoxLayout.Y_AXIS));
        useLayoutEditorSubPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        useLayoutEditorSubPanel.add(useLayoutEditorTurnout);
        useLayoutEditorSubPanel.add(useLayoutEditorBlock);
        editor.add(useLayoutEditorSubPanel);
        useLayoutEditorSubPanel.setVisible(false);

        useLayoutEditor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                useLayoutEditorSubPanel.setVisible(useLayoutEditor.isSelected());
                // Setup for display of all Turnouts, if needed
                boolean valid;
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
                            valid = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlockConnectivityTools().checkValidDest(sourceMastBox.getSelectedItem(),
                                    destMastBox.getSelectedItem(), LayoutBlockConnectivityTools.MASTTOMAST);
                            if (!valid) {
                                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorUnReachableDestination"));
                            }
                        } catch (jmri.JmriException je) {
                            JOptionPane.showMessageDialog(null, Bundle.getMessage("WarningUnableToValidate"));
                        }
                    }
                }
            }
        });
        header.add(editor);
        header.add(allowAutoMastGeneration);
        header.add(lockTurnouts);

        // selection radiobuttons for All/Included items
        JPanel py = new JPanel();
        py.add(new JLabel(Bundle.getMessage("Show")));  // NOI18N
        ButtonGroup selGroup = new ButtonGroup();
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
        JRadioButton includedButton = new JRadioButton(Bundle.getMessage("Included"), false);  // NOI18N
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
        contentPane.add(header);

        // build_x_Panel() returns a JScrollFrame
        JTabbedPane detailsTab = new JTabbedPane();
        detailsTab.add(Bundle.getMessage("Blocks"), buildBlocksPanel());  // NOI18N
        detailsTab.add(Bundle.getMessage("Turnouts"), buildTurnoutPanel());  // NOI18N
        detailsTab.add(Bundle.getMessage("Sensors"), buildSensorPanel());  // NOI18N
        detailsTab.add(Bundle.getMessage("SignalMasts"), buildSignalMastPanel());  // NOI18N

        JScrollPane detailsScrollPane = new JScrollPane(detailsTab); // make set of 1-2 tables scrollable on smaller screens
        contentPane.add(detailsScrollPane);

        JPanel footer = new JPanel();
        footer.setLayout(new FlowLayout(FlowLayout.TRAILING));

        // Cancel button
        footer.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelPressed(e);
            }
        });

        // Update button
        footer.add(updateButton);
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePressed(e);
            }
        });
        updateButton.setToolTipText(Bundle.getMessage("UpdateButtonToolTip"));  // NOI18N
        updateButton.setVisible(true);

        // Apply (and Close) button
        footer.add(applyButton);
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyPressed(e);
            }
        });
        applyButton.setToolTipText(Bundle.getMessage("ApplyButtonToolTip"));  // NOI18N
        applyButton.setVisible(true);

        contentPane.add(Box.createVerticalGlue()); // glue above buttons
        contentPane.add(footer);

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
            useLayoutEditorSubPanel.setVisible(useLayoutEditor.isSelected());
            initializeIncludedList();
            editDetails(); // pick up details for an existing SML configuration
        } else {
            useLayoutEditorSubPanel.setVisible(useLayoutEditor.isSelected());
            fixedDestMastLabel.setVisible(false);
            destMastBox.setVisible(true);
        }
    }

    private JScrollPane _manualBlockScrollPane;
    private JScrollPane _manualSignalMastScrollPane;
    private JScrollPane _manualSensorScrollPane;

    private BlockModel _blockModel;
    private AutoBlockModel _autoBlockModel;
    private List<ManualBlockList> _manualBlockList;
    private List<AutoBlockList> _automaticBlockList = new ArrayList<>();

    private TurnoutModel _turnoutModel;
    private AutoTurnoutModel _autoTurnoutModel;
    private List<ManualTurnoutList> _manualTurnoutList;
    private List<AutoTurnoutList> _automaticTurnoutList = new ArrayList<>();

    private SensorModel _sensorModel;
    private List<ManualSensorList> _manualSensorList;

    private SignalMastModel _signalMastModel;
    private AutoMastModel _autoSignalMastModel;
    private List<ManualSignalMastList> _manualSignalMastList;
    private List<AutoSignalMastList> _automaticSignalMastList = new ArrayList<>();

    private JPanel p2xb = new JPanel();

    /**
     * Compose GUI for setting up Blocks tab for an SML.
     *
     * @return a JPanel containing the SML control blocks configuration
     *         interface
     */
    private JPanel buildBlocksPanel() {
        JPanel blockPanel = new JPanel();
        blockPanel.setLayout(new BoxLayout(blockPanel, BoxLayout.Y_AXIS));

        jmri.BlockManager bm = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
        _manualBlockList = new ArrayList<>();
        for (Block b : bm.getNamedBeanSet()) {
            _manualBlockList.add(new ManualBlockList(b));
        }

        if ((sml != null) && (destMast != null)) {
            List<Block> blkList = sml.getAutoBlocks(destMast);
            _automaticBlockList = new ArrayList<>(blkList.size());
            for (Block blk : blkList) {
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
        RowSorterUtil.setSortOrder(manualBlockSorter, BlockModel.SNAME_COLUMN, SortOrder.ASCENDING);
        _blockModel.configStateColumn(manualBlockTable); // create static comboBox in State column
        manualBlockTable.setRowSorter(manualBlockSorter);
        manualBlockTable.setRowSelectionAllowed(false);
        manualBlockTable.setPreferredScrollableViewportSize(TABLESIZEPREFERRED);
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

        setRowHeight(manualBlockTable.getRowHeight());
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
        RowSorterUtil.setSortOrder(autoBlockSorter, AutoBlockModel.SNAME_COLUMN, SortOrder.ASCENDING);
        autoBlockTable.setRowSorter(autoBlockSorter);
        autoBlockTable.setRowSelectionAllowed(false);
        autoBlockTable.setPreferredScrollableViewportSize(TABLESIZEPREFERRED);

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

        JScrollPane _autoBlockScrollPane = new JScrollPane(autoBlockTable);
        p2xb.add(_autoBlockScrollPane, BorderLayout.CENTER);
        blockPanel.add(p2xb);
        p2xb.setVisible(true);

        setRowHeight(autoBlockTable.getRowHeight());
        p2xaSpace.setVisible(false);

        return blockPanel;
    }

    private JPanel p2xa = new JPanel();

    /**
     * Compose GUI for setting up the Turnouts tab for an SML.
     *
     * @return a JPanel containing the SML control turnouts configuration
     *         interface
     */
    private JPanel buildTurnoutPanel() {
        JPanel turnoutPanel = new JPanel();
        turnoutPanel.setLayout(new BoxLayout(turnoutPanel, BoxLayout.Y_AXIS));

        jmri.TurnoutManager bm = jmri.InstanceManager.turnoutManagerInstance();
        _manualTurnoutList = new ArrayList<>();
        for (Turnout b : bm.getNamedBeanSet()) {
            String systemName = b.getSystemName();
            String userName = b.getUserName();
            _manualTurnoutList.add(new ManualTurnoutList(systemName, userName));
        }

        if ((sml != null) && (destMast != null)) {
            List<Turnout> turnList = sml.getAutoTurnouts(destMast);
            _automaticTurnoutList = new ArrayList<>(turnList.size());
            for (Turnout turn : turnList) {
                String systemName = turn.getSystemName();
                String userName = turn.getUserName();
                AutoTurnoutList turnItem = new AutoTurnoutList(systemName, userName);
                turnItem.setState(sml.getAutoTurnoutState(turn, destMast));
                _automaticTurnoutList.add(turnItem);
            }
        }

        JPanel p2xt = new JPanel();
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
        RowSorterUtil.setSortOrder(manualTurnoutSorter, TurnoutModel.SNAME_COLUMN, SortOrder.ASCENDING);
        _turnoutModel.configStateColumn(manualTurnoutTable); // create static comboBox in State column
        manualTurnoutTable.setRowSorter(manualTurnoutSorter);
        manualTurnoutTable.setRowSelectionAllowed(false);
        manualTurnoutTable.setPreferredScrollableViewportSize(TABLESIZEPREFERRED);
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
        log.debug("L = {}", SET_TO_ANY.length());
        stateColumnC.setMinWidth(9 * Math.max(SET_TO_ANY.length(), SET_TO_CLOSED.length()) + 30);
        stateColumnC.setMaxWidth(stateColumnC.getMinWidth() + 10); // was fixed 100
        // remaining space is filled by UserName
        JScrollPane _manualTurnoutScrollPane = new JScrollPane(manualTurnoutTable);
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
        RowSorterUtil.setSortOrder(autoTurnoutSorter, AutoTurnoutModel.SNAME_COLUMN, SortOrder.ASCENDING);
        autoTurnoutTable.setRowSorter(autoTurnoutSorter);
        autoTurnoutTable.setRowSelectionAllowed(false);
        autoTurnoutTable.setPreferredScrollableViewportSize(TABLESIZEPREFERRED);

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

        JScrollPane _autoTurnoutScrollPane = new JScrollPane(autoTurnoutTable);
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
    private JPanel buildSensorPanel() {
        JPanel sensorPanel = new JPanel();
        sensorPanel.setLayout(new BoxLayout(sensorPanel, BoxLayout.Y_AXIS));

        jmri.SensorManager bm = jmri.InstanceManager.sensorManagerInstance();
        _manualSensorList = new ArrayList<>();
        for (Sensor ss : bm.getNamedBeanSet()) {
            String systemName = ss.getSystemName();
            String userName = ss.getUserName();
            _manualSensorList.add(new ManualSensorList(systemName, userName));
        }

        JPanel p2xs = new JPanel();
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
        RowSorterUtil.setSortOrder(manualSensorSorter, SensorModel.SNAME_COLUMN, SortOrder.ASCENDING);
        _sensorModel.configStateColumn(manualSensorTable); // create static comboBox in State column
        manualSensorTable.setRowSorter(manualSensorSorter);
        manualSensorTable.setRowSelectionAllowed(false);
        manualSensorTable.setPreferredScrollableViewportSize(TABLESIZEPREFERRED);
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

    private JPanel p2xsm = new JPanel();

    /**
     * Compose GUI for setting up the Signal Masts tab for an SML.
     *
     * @return a JPanel containing the SML control signal masts configuration
     *         interface
     */
    private JPanel buildSignalMastPanel() {
        JPanel SignalMastPanel = new JPanel(); // TODO make this a shared variable
        SignalMastPanel.setLayout(new BoxLayout(SignalMastPanel, BoxLayout.Y_AXIS));

        jmri.SignalMastManager bm = jmri.InstanceManager.getDefault(jmri.SignalMastManager.class);
        _manualSignalMastList = new ArrayList<>();
        for (SignalMast m : bm.getNamedBeanSet()) {
            _manualSignalMastList.add(new ManualSignalMastList(m));
        }

        JPanel p2xm = new JPanel();
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
        JTable manualSignalMastTable = new JTable(_signalMastModel);
        // configure (extra) row height for comboBox
        manualSignalMastTable.setRowHeight(sizer.getPreferredSize().height - 2);
        // row height has to be greater than plain tables to properly show comboBox shape, but tightened a bit over preferred
        _signalMastModel.configStateColumn(manualSignalMastTable); // create mast (row) specific comboBox in Aspect column
        manualSignalMastTable.setRowSorter(sorter);
        manualSignalMastTable.setRowSelectionAllowed(false);
        manualSignalMastTable.setPreferredScrollableViewportSize(TABLESIZEPREFERRED);

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
        RowSorterUtil.setSortOrder(autoMastSorter, AutoMastModel.SNAME_COLUMN, SortOrder.ASCENDING);
        autoMastTable.setRowSorter(autoMastSorter);
        autoMastTable.setRowSelectionAllowed(false);
        autoMastTable.setPreferredScrollableViewportSize(TABLESIZEPREFERRED);

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

        JScrollPane _autoSignalMastScrollPane = new JScrollPane(autoMastTable);
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
    private void updatePressed(ActionEvent e) {
        sourceMast = sourceMastBox.getSelectedItem();
        destMast = destMastBox.getSelectedItem();
        boolean smlPairAdded = false;
        destOK = true;

        // TODO bind all these dialogs to parent SignalMastPanel (make that a shared var) for gui test to find them
        if ((sourceMastBox.getSelectedItem() == null) || (destMastBox.getSelectedItem() == null)) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSignalMastNull",
                    Bundle.getMessage("SourceMast"), Bundle.getMessage("DestMast")));
            destOK = false;
            log.debug("No Source or Destination Mast selected, keep pane open");  // NOI18N
            return;
        }
        if (sourceMast == destMast || fixedSourceMastLabel.getText().equals(destMast.getDisplayName())) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSignalMastIdentical"));
            destOK = false;
            log.debug("Destination Mast check failed, keep pane open");  // NOI18N
            return;
        }
        if ((sml == null) && (useLayoutEditor.isSelected())) {
            boolean valid;
            try {
                valid = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlockConnectivityTools().checkValidDest(sourceMast,
                        destMast, LayoutBlockConnectivityTools.MASTTOMAST);
                if (!valid) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorUnReachableDestination"));
                    return;
                }
            } catch (jmri.JmriException je) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("WarningUnableToValidate"));
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
                int mes = JOptionPane.showConfirmDialog(null, Bundle.getMessage("WarningExistingPair"),
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
        boolean layoutEditorGen = true;
        try {
            sml.useLayoutEditor(useLayoutEditor.isSelected(), destMast);
        } catch (jmri.JmriException je) {
            JOptionPane.showMessageDialog(null, je.toString());
            layoutEditorGen = false;
        }

        try {
            if (useLayoutEditor.isSelected()) {
                sml.useLayoutEditorDetails(useLayoutEditorTurnout.isSelected(), useLayoutEditorBlock.isSelected(), destMast);
            }
        } catch (jmri.JmriException ji) {
            if (layoutEditorGen) {
                JOptionPane.showMessageDialog(null, ji.toString());
            }
        }
        Hashtable<Block, Integer> hashBlocks = new Hashtable<>();
        for (ManualBlockList mbl : _includedManualBlockList) {
            Block blk = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getBlock(mbl.getSysName());
            if (blk != null) {
                hashBlocks.put(blk, mbl.getState());
            }
        }
        sml.setBlocks(hashBlocks, destMast);

        Hashtable<NamedBeanHandle<Turnout>, Integer> hashTurnouts = new Hashtable<>();
        for (ManualTurnoutList mtl : _includedManualTurnoutList) {
            String turnoutName = mtl.getDisplayName();
            Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
            if (turnout != null) {
                NamedBeanHandle<Turnout> namedTurnout = nbhm.getNamedBeanHandle(turnoutName, turnout);
                hashTurnouts.put(namedTurnout, mtl.getState());
            }
            // no specific value, just show the current turnout state as selection in comboBox.
            // for existing SML pair, will be updated to show present setting by editDetails()
        }
        sml.setTurnouts(hashTurnouts, destMast);

        Hashtable<NamedBeanHandle<Sensor>, Integer> hashSensors = new Hashtable<>();
        for (ManualSensorList msl : _includedManualSensorList) {
            String sensorName = msl.getDisplayName();
            Sensor sensor = jmri.InstanceManager.sensorManagerInstance().getSensor(msl.getDisplayName());
            if (sensor != null) {
                NamedBeanHandle<Sensor> namedSensor = nbhm.getNamedBeanHandle(sensorName, sensor);
                hashSensors.put(namedSensor, msl.getState());
            }
            // no specific value, just show the current sensor state as selection in comboBox.
            // for existing SML pair, will be updated to show present setting by editDetails()
        }
        sml.setSensors(hashSensors, destMast);

        Hashtable<SignalMast, String> hashSignalMasts = new Hashtable<>();
        for (ManualSignalMastList msml : _includedManualSignalMastList) {
            if (msml.getMast() == sourceMast || msml.getMast() == destMast) {
                // warn user that control mast is either source or destination mast of this pair, but allow as a valid choice
                int mes = JOptionPane.showConfirmDialog(null, java.text.MessageFormat.format(Bundle.getMessage("SignalMastCriteriaOwn"), // NOI18N
                        msml.getMast().getDisplayName()),
                        Bundle.getMessage("SignalMastCriteriaOwnTitle"), // NOI18N
                        JOptionPane.YES_NO_OPTION);
                if (mes == 0) { // Yes
                    hashSignalMasts.put(msml.getMast(), msml.getSetToState());
                } else { // No
                    msml.setIncluded(false); // deselect "Included" checkBox for signal mast in manualSignalList
                    initializeIncludedList();
                    _signalMastModel.fireTableDataChanged();
                }
            } else {
                hashSignalMasts.put(msml.getMast(), msml.getSetToState());
            }
        }
        sml.setMasts(hashSignalMasts, destMast);

        sml.allowTurnoutLock(lockTurnouts.isSelected(), destMast);
        sml.initialise(destMast);
        if (smlPairAdded) {
            log.debug("New SML");  // NOI18N
            firePropertyChange("newDestination", null, destMastBox.getSelectedItem()); // to show new SML in underlying table  // NOI18N
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
        if (destOK) { // enable user to correct configuration if warned the destMast is incorrect by skipping pane closing
            cancelPressed(e); // close panel signaling acceptance of edits/Apply to the user
        }
    }

    /**
     * Clean up when Cancel button is pressed.
     *
     * @param e the event heard
     */
    void cancelPressed(ActionEvent e) {
        if (jFrame != null) {
            jFrame.setVisible(false);
            jFrame.dispose();
        }
        jFrame = null;
    }

    int blockModeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, blockInputModeValues, blockInputModes);

        if (result < 0) {
            log.warn("unexpected mode string in blockMode: {}", mode);  // NOI18N
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
    private void initializeIncludedList() {
        _includedManualBlockList = new ArrayList<>();
        for (ManualBlockList mbl : _manualBlockList) {
            if (mbl.isIncluded()) {
                _includedManualBlockList.add(mbl);
            }
        }

        if ((sml != null) && (destMast != null)) {
            List<Block> blkList = sml.getAutoBlocks(destMast);
            _automaticBlockList = new ArrayList<>(blkList.size());
            for (Block blk : blkList) {
                AutoBlockList newABlk = new AutoBlockList(blk);
                _automaticBlockList.add(newABlk);
                newABlk.setState(sml.getAutoBlockState(blk, destMast));
            }
        }

        _includedManualTurnoutList = new ArrayList<>();
        for (ManualTurnoutList mtl : _manualTurnoutList) {
            if (mtl.isIncluded()) {
                _includedManualTurnoutList.add(mtl);
            }
        }

        if ((sml != null) && (destMast != null)) {
            List<Turnout> turnList = sml.getAutoTurnouts(destMast);
            _automaticTurnoutList = new ArrayList<>(turnList.size());
            for (Turnout turn : turnList) {
                String systemName = turn.getSystemName();
                String userName = turn.getUserName();
                AutoTurnoutList newAturn = new AutoTurnoutList(systemName, userName);
                _automaticTurnoutList.add(newAturn);
                newAturn.setState(sml.getAutoTurnoutState(turn, destMast));
            }
        }

        _includedManualSensorList = new ArrayList<>();
        for (ManualSensorList msl : _manualSensorList) {
            if (msl.isIncluded()) {
                _includedManualSensorList.add(msl);
            }
        }

        _includedManualSignalMastList = new ArrayList<>();
        for (ManualSignalMastList msml : _manualSignalMastList) {
            if (msml.isIncluded()) {
                _includedManualSignalMastList.add(msml);
            }
        }

        if ((sml != null) && (destMast != null)) {
            List<SignalMast> mastList = sml.getAutoMasts(destMast);
            _automaticSignalMastList = new ArrayList<>(mastList.size());
            for (SignalMast mast : mastList) {
                AutoSignalMastList newAmast = new AutoSignalMastList(mast);
                _automaticSignalMastList.add(newAmast);
                newAmast.setState(sml.getAutoSignalMastState(mast, destMast));
            }
        }
    }

    private JRadioButton allButton;

    private boolean showAll = true;   // false indicates show only included items

    private static String SET_TO_ACTIVE = Bundle.getMessage("SensorStateActive");  // NOI18N
    private static String SET_TO_INACTIVE = Bundle.getMessage("SensorStateInactive");  // NOI18N
    private static String SET_TO_CLOSED = jmri.InstanceManager.turnoutManagerInstance().getClosedText();
    private static String SET_TO_THROWN = jmri.InstanceManager.turnoutManagerInstance().getThrownText();

    private static String SET_TO_UNOCCUPIED = Bundle.getMessage("UnOccupied");  // NOI18N
    private static String SET_TO_OCCUPIED = Bundle.getMessage("Occupied");  // NOI18N
    private static String SET_TO_ANY = Bundle.getMessage("AnyState");  // NOI18N

    private static int ROW_HEIGHT;

    private static void setRowHeight(int newHeight) {
        ROW_HEIGHT = newHeight;
    }

    /**
     * Cancels "Show Included Only" option
     */
    void cancelIncludedOnly() {
        if (!showAll) {
            allButton.doClick();
        }
    }

    /**
     * Fill in existing SML configuration on the edit panel
     */
    private void editDetails() {
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

    /*
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
     * A paired list of manually configured Signal Masts and a corresponding Set To
     * State used during edit of an SML.
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
         * identical options for all rows in a column.
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
            protected JComboBox<String> getEditorBox(int row) {
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
        JComboBox<String> getStateEditorBox(int row) {
            // create dummy comboBox, override in extended classes for each bean
            JComboBox<String> editCombo = new JComboBox<>();
            editCombo.addItem(Bundle.getMessage("None"));  // NOI18N
            return editCombo;
        }
        // end of methods to display STATE_COLUMN ComboBox

        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;
        static final int INCLUDE_COLUMN = 2;
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

        private static final int SPEED_COLUMN = 4;
        private static final int PERMISSIVE_COLUMN = 5;

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public Object getValueAt(int r, int c) {
            List<ManualBlockList> blockList;
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
                    return blockList.get(r).isIncluded();
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
            List<ManualBlockList> blockList;
            if (showAll) {
                blockList = _manualBlockList;
            } else {
                blockList = _includedManualBlockList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    blockList.get(r).setIncluded((Boolean) type);
                    break;
                case STATE_COLUMN:
                    log.debug("State = {}", type);  // NOI18N
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
        JComboBox<String> getStateEditorBox(int row) {
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
            List<ManualTurnoutList> turnoutList;
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
                    return turnoutList.get(r).isIncluded();
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
            List<ManualTurnoutList> turnoutList;
            if (showAll) {
                turnoutList = _manualTurnoutList;
            } else {
                turnoutList = _includedManualTurnoutList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    turnoutList.get(r).setIncluded((Boolean) type);
                    break;
                case STATE_COLUMN:
                    log.debug("State = {}", type);  // NOI18N
                    if (type != null) {
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
        JComboBox<String> getStateEditorBox(int row) {
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
            List<ManualSensorList> sensorList;
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
                    return sensorList.get(r).isIncluded();
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
            List<ManualSensorList> sensorList;
            if (showAll) {
                sensorList = _manualSensorList;
            } else {
                sensorList = _includedManualSensorList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    sensorList.get(r).setIncluded((Boolean) type);
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
        JComboBox<String> getStateEditorBox(int row) {
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
            List<ManualSignalMastList> signalMastList;
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
                    return signalMastList.get(r).isIncluded();
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
            List<ManualSignalMastList> signalMastList;
            if (showAll) {
                signalMastList = _manualSignalMastList;
            } else {
                signalMastList = _includedManualSignalMastList;
            }
            switch (c) {
                case STATE_COLUMN:
                    if (type != null) {
                        //convertRowIndexToModel(row) not needed
                        log.debug("setValueAt (rowConverted={}; value={})", r, type);  // NOI18N
                        signalMastList.get(r).setSetToState((String) type);
                        fireTableRowsUpdated(r, r);
                    }
                    break;
                case INCLUDE_COLUMN:
                    signalMastList.get(r).setIncluded((Boolean) type);
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
            protected JComboBox<String> getEditorBox(int row) {
                return getAspectEditorBox(row);
            }
        }

        // Methods to display STATE_COLUMN (aspect) ComboBox in the Signal Mast Manual Table
        // Derived from the SignalMastJTable class (deprecated since 4.5.5):
        // All row values are in terms of the Model, not the Table as displayed.

        /**
         * Provide a JComboBox element to display inside the JPanel CellEditor.
         * When not yet present, create, store and return a new one.
         *
         * @param row Index number (in TableDataModel)
         * @return A combobox containing the valid aspect names for this mast
         */
        JComboBox<String> getAspectEditorBox(int row) {
            JComboBox<String> editCombo = editorMap.get(this.getValueAt(row, SNAME_COLUMN));
            if (editCombo == null) {
                // create a new one with correct aspects
                editCombo = new JComboBox<>(getAspectVector(row)); // show it
                editorMap.put(this.getValueAt(row, SNAME_COLUMN), editCombo); // and store it
            }
            return editCombo;
        }

        // Hashtables for Editors; none used for Renderers
        Hashtable<Object, JComboBox<String>> editorMap = new Hashtable<>();

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
                // create a new one with correct aspects
                comboaspects = InstanceManager.getDefault(jmri.SignalMastManager.class)
                        .getSignalMast((String) this.getValueAt(row, SNAME_COLUMN)).getValidAspects();
                boxMap.put(this.getValueAt(row, SNAME_COLUMN), comboaspects); // and store it
            }
            return comboaspects;
        }

        private Hashtable<Object, Vector<String>> boxMap = new Hashtable<>();

        // end of methods to display STATE_COLUMN (Aspect) ComboBox

    }

    /**
     * A series of autoTableModels to display - but not edit - configurations on
     * the Edit SML Tabs that are autogenerated from layout Editor information.
     */
    abstract class AutoTableModel extends AbstractTableModel implements PropertyChangeListener {

        AutoTableModel() {
            smlValid();
        }

        void smlValid() {
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

        static final int SPEED_COLUMN = 3;
        static final int PERMISSIVE_COLUMN = 4;

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

    private final static Logger log = LoggerFactory.getLogger(SignallingPanel.class);

}
