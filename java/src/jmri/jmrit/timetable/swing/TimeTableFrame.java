package jmri.jmrit.timetable.swing;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.*;

import jmri.InstanceManager;
import jmri.Scale;
import jmri.ScaleManager;
import jmri.jmrit.operations.trains.tools.ExportTimetable;
import jmri.jmrit.timetable.*;
import jmri.jmrit.timetable.configurexml.TimeTableXml;
import jmri.util.JmriJFrame;
import jmri.util.swing.SplitButtonColorChooserPanel;

/**
 * Create and maintain timetables.
 * <p>
 * A timetable describes the layout and trains along with the times that each train should be at specified locations.
 *
 *   Logical Schema
 * Layout
 *    Train Types
 *    Segments
 *        Stations
 *    Schedules
 *        Trains
 *           Stops
 *
 * @author Dave Sand Copyright (c) 2018
 */
public class TimeTableFrame extends jmri.util.JmriJFrame {

    public TimeTableFrame() {
    }

    public TimeTableFrame(String tt) {
        super(true, true);
        setTitle(Bundle.getMessage("TitleTimeTable"));  // NOI18N
        InstanceManager.setDefault(TimeTableFrame.class, this);
        _dataMgr = TimeTableDataManager.getDataManager();
        buildComponents();
        createFrame();
        createMenu();
        setEditMode(false);
        setShowReminder(false);
    }

    TimeTableDataManager _dataMgr;
    boolean _isDirty = false;
    boolean _showTrainTimes = false;
    boolean _twoPage = false;

    // ------------ Tree variables ------------
    JTree _timetableTree;
    DefaultTreeModel _timetableModel;
    DefaultMutableTreeNode _timetableRoot;
    TreeSelectionListener _timetableListener;
    TreePath _curTreePath = null;

    // ------------ Tree components ------------
    TimeTableTreeNode _layoutNode = null;
    TimeTableTreeNode _typeHead = null;
    TimeTableTreeNode _typeNode = null;
    TimeTableTreeNode _segmentHead = null;
    TimeTableTreeNode _segmentNode = null;
    TimeTableTreeNode _stationNode = null;
    TimeTableTreeNode _scheduleHead = null;
    TimeTableTreeNode _scheduleNode = null;
    TimeTableTreeNode _trainNode = null;
    TimeTableTreeNode _stopNode = null;
    TimeTableTreeNode _leafNode = null;

    // ------------ Current tree node variables ------------
    TimeTableTreeNode _curNode = null;
    int _curNodeId = 0;
    String _curNodeType = null;
    String _curNodeText = null;
    int _curNodeRow = -1;

    // ------------ Edit detail components ------------
    JPanel _detailGrid = new JPanel();
    JPanel _detailFooter = new JPanel();
    JPanel _gridPanel;  // Child of _detailGrid, contains the current grid labels and fields
    boolean _editActive = false;
    JButton _cancelAction;
    JButton _updateAction;

    // Layout
    JTextField _editLayoutName;
    JComboBox<Scale> _editScale;
    JTextField _editFastClock;
    JTextField _editThrottles;
    JCheckBox _editMetric;
    JLabel _showScaleMK;

    // TrainType
    JTextField _editTrainTypeName;
    JColorChooser _editTrainTypeColor;

    // Segment
    JTextField _editSegmentName;

    // Station
    JTextField _editStationName;
    JTextField _editDistance;
    JCheckBox _editDoubleTrack;
    JSpinner _editSidings;
    JSpinner _editStaging;

    // Schedule
    JTextField _editScheduleName;
    JTextField _editEffDate;
    JSpinner _editStartHour;
    JSpinner _editDuration;

    // Train
    JTextField _editTrainName;
    JTextField _editTrainDesc;
    JComboBox<TrainType> _editTrainType;
    JTextField _editDefaultSpeed;
    JTextField _editTrainStartTime;
    JSpinner _editThrottle;
    JTextArea _editTrainNotes;
    JLabel _showRouteDuration;

    // Stop
    JLabel _showStopSeq;
    JComboBox<TimeTableDataManager.SegmentStation> _editStopStation;
    JTextField _editStopDuration;
    JTextField _editNextSpeed;
    JSpinner _editStagingTrack;
    JTextArea _editStopNotes;
    JLabel _showArriveTime;
    JLabel _showDepartTime;

    // ------------ Button bar components ------------
    JPanel _leftButtonBar;
    JPanel _addButtonPanel;
    JPanel _deleteButtonPanel;
    JPanel _moveButtonPanel;
    JPanel _graphButtonPanel;
    JButton _addButton = new JButton();
    JButton _deleteButton = new JButton();
    JButton _displayButton = new JButton();
    JButton _printButton = new JButton();
    JButton _saveButton = new JButton();

    // ------------ Create Panel and components ------------

    /**
     * Create the main Timetable Window
     * The left side contains the timetable tree.
     * The right side contains the current edit grid.
     */
    private void createFrame() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        // ------------ Body - tree (left side) ------------
        JTree treeContent = buildTree();
        JScrollPane treeScroll = new JScrollPane(treeContent);

        // ------------ Body - detail (right side) ------------
        JPanel detailPane = new JPanel();
        detailPane.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.DARK_GRAY));
        detailPane.setLayout(new BoxLayout(detailPane, BoxLayout.Y_AXIS));

        // ------------ Edit Detail Panel ------------
        makeDetailGrid("EmptyGrid");  // NOI18N

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        _cancelAction = new JButton(Bundle.getMessage("ButtonCancel"));  // NOI18N
        _cancelAction.setToolTipText(Bundle.getMessage("HintCancelButton"));  // NOI18N
        panel.add(_cancelAction);
        _cancelAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelPressed();
            }
        });
        panel.add(Box.createHorizontalStrut(10));

        _updateAction = new JButton(Bundle.getMessage("ButtonUpdate"));  // NOI18N
        _updateAction.setToolTipText(Bundle.getMessage("HintUpdateButton"));  // NOI18N
        panel.add(_updateAction);
        _updateAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePressed();
            }
        });
        _detailFooter.add(panel);

        JPanel detailEdit = new JPanel(new BorderLayout());
        detailEdit.add(_detailGrid, BorderLayout.NORTH);
        detailEdit.add(_detailFooter, BorderLayout.SOUTH);
        detailPane.add(detailEdit);

        JSplitPane bodyPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, detailPane);
        bodyPane.setDividerSize(10);
        bodyPane.setResizeWeight(.35);
        bodyPane.setOneTouchExpandable(true);
        contentPane.add(bodyPane);

        // ------------ Footer ------------
        JPanel footer = new JPanel(new BorderLayout());
        _leftButtonBar = new JPanel();

        // ------------ Add Button ------------
        _addButton = new JButton(Bundle.getMessage("AddLayoutButtonText"));    // NOI18N
        _addButton.setToolTipText(Bundle.getMessage("HintAddButton"));       // NOI18N
        _addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPressed();
            }
        });
        _addButtonPanel = new JPanel();
        _addButtonPanel.add(_addButton);
        _leftButtonBar.add(_addButtonPanel);

        // ------------ Delete Button ------------
        _deleteButton = new JButton(Bundle.getMessage("DeleteLayoutButtonText")); // NOI18N
        _deleteButton.setToolTipText(Bundle.getMessage("HintDeleteButton"));    // NOI18N
        _deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deletePressed();
            }
        });
        _deleteButtonPanel = new JPanel();
        _deleteButtonPanel.add(_deleteButton);
        _deleteButtonPanel.setVisible(false);
        _leftButtonBar.add(_deleteButtonPanel);

        // ------------ Move Buttons ------------
        JLabel moveLabel = new JLabel(Bundle.getMessage("LabelMove"));      // NOI18N

        JButton upButton = new JButton(Bundle.getMessage("ButtonUp"));      // NOI18N
        upButton.setToolTipText(Bundle.getMessage("HintUpButton"));         // NOI18N
        JButton downButton = new JButton(Bundle.getMessage("ButtonDown"));  // NOI18N
        downButton.setToolTipText(Bundle.getMessage("HintDownButton"));     // NOI18N

        upButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downButton.setEnabled(false);
                upButton.setEnabled(false);
                upPressed();
            }
        });

        downButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                upButton.setEnabled(false);
                downButton.setEnabled(false);
                downPressed();
            }
        });

        _moveButtonPanel = new JPanel();
        _moveButtonPanel.add(moveLabel);
        _moveButtonPanel.add(upButton);
        _moveButtonPanel.add(new JLabel("|"));
        _moveButtonPanel.add(downButton);
        _moveButtonPanel.setVisible(false);
        _leftButtonBar.add(_moveButtonPanel);

        // ------------ Graph Buttons ------------
        JLabel graphLabel = new JLabel(Bundle.getMessage("LabelGraph"));      // NOI18N

        _displayButton = new JButton(Bundle.getMessage("ButtonDisplay"));  // NOI18N
        _displayButton.setToolTipText(Bundle.getMessage("HintDisplayButton"));     // NOI18N
        _displayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphPressed("Display");  // NOI18N
            }
        });

        _printButton = new JButton(Bundle.getMessage("ButtonPrint"));  // NOI18N
        _printButton.setToolTipText(Bundle.getMessage("HintPrintButton"));     // NOI18N
        _printButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphPressed("Print");  // NOI18N
            }
        });

        _graphButtonPanel = new JPanel();
        _graphButtonPanel.add(graphLabel);
        _graphButtonPanel.add(_displayButton);
        _graphButtonPanel.add(new JLabel("|"));
        _graphButtonPanel.add(_printButton);
        _leftButtonBar.add(_graphButtonPanel);

        footer.add(_leftButtonBar, BorderLayout.WEST);
        JPanel rightButtonBar = new JPanel();

        // ------------ Save Button ------------
        _saveButton = new JButton(Bundle.getMessage("ButtonSave"));  // NOI18N
        _saveButton.setToolTipText(Bundle.getMessage("HintSaveButton"));     // NOI18N
        _saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePressed();
            }
        });
        JPanel saveButtonPanel = new JPanel();
        saveButtonPanel.add(_saveButton);
        rightButtonBar.add(saveButtonPanel);

        // ------------ Done Button ------------
        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));  // NOI18N
        doneButton.setToolTipText(Bundle.getMessage("HintDoneButton"));     // NOI18N
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                donePressed();
            }
        });
        JPanel doneButtonPanel = new JPanel();
        doneButtonPanel.add(doneButton);
        rightButtonBar.add(doneButtonPanel);

        footer.add(rightButtonBar, BorderLayout.EAST);
        contentPane.add(footer, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                donePressed();
            }
        });
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        pack();
        _addButtonPanel.setVisible(false);
        _deleteButtonPanel.setVisible(false);
        _graphButtonPanel.setVisible(false);
    }

    /**
     * Create a Options/Tools menu.
     * - Option: Show train times on the graph.
     * - Option: Enable two page graph printing.
     * - Tool: Import a SchedGen data file.
     * - Tool: Import a CSV data file.
     * - Tool: Export a CSV data file.
     * Include the standard Windows and Help menu bar items.
     */
    void createMenu() {
        _showTrainTimes = InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                getSimplePreferenceState("jmri.jmrit.timetable:TrainTimes");      // NOI18N

        JCheckBoxMenuItem trainTime = new JCheckBoxMenuItem(Bundle.getMessage("MenuTrainTimes"));  // NOI18N
        trainTime.setSelected(_showTrainTimes);
        trainTime.addActionListener((ActionEvent event) -> {
            _showTrainTimes = trainTime.isSelected();
            InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    setSimplePreferenceState("jmri.jmrit.timetable:TrainTimes", _showTrainTimes);  // NOI18N
        });

        _twoPage = InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                getSimplePreferenceState("jmri.jmrit.timetable:TwoPage");      // NOI18N

        JCheckBoxMenuItem twoPage = new JCheckBoxMenuItem(Bundle.getMessage("MenuTwoPage"));  // NOI18N
        twoPage.setSelected(_twoPage);
        twoPage.addActionListener((ActionEvent event) -> {
            _twoPage = twoPage.isSelected();
            InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    setSimplePreferenceState("jmri.jmrit.timetable:TwoPage", _twoPage);  // NOI18N
        });

        JMenuItem impsgn = new JMenuItem(Bundle.getMessage("MenuImportSgn"));  // NOI18N
        impsgn.addActionListener((ActionEvent event) -> {
            importPressed();
        });

        JMenuItem impcsv = new JMenuItem(Bundle.getMessage("MenuImportCsv"));  // NOI18N
        impcsv.addActionListener((ActionEvent event) -> {
            importCsvPressed();
        });
        
        JMenuItem impopr = new JMenuItem(Bundle.getMessage("MenuImportOperations"));  // NOI18N
        impopr.addActionListener((ActionEvent event) -> {
            importFromOperationsPressed();
        });

        JMenuItem expcsv = new JMenuItem(Bundle.getMessage("MenuExportCsv"));  // NOI18N
        expcsv.addActionListener((ActionEvent event) -> {
            exportCsvPressed();
        });

        JMenu ttMenu = new JMenu(Bundle.getMessage("MenuTimetable"));  // NOI18N
        ttMenu.add(trainTime);
        ttMenu.addSeparator();
        ttMenu.add(twoPage);
        ttMenu.addSeparator();
        ttMenu.add(impsgn);
        ttMenu.add(impcsv);
        ttMenu.add(impopr);
        ttMenu.add(expcsv);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(ttMenu);
        setJMenuBar(menuBar);

        //setup Help menu
        addHelpMenu("html.tools.TimeTable", true);  // NOI18N
    }

    /**
     * Initialize components.
     * Add Focus and Change listeners to activate edit mode.
     * Create the color selector for train types.
     */
    void buildComponents() {
        // Layout
        _editLayoutName = new JTextField(20);
        _editScale = new JComboBox<>();
        _editScale.addItemListener(layoutScaleItemEvent);
        _editFastClock = new JTextField(5);
        _editThrottles = new JTextField(5);
        _editMetric = new JCheckBox();
        _showScaleMK = new JLabel();

        _editLayoutName.addFocusListener(detailFocusEvent);
        _editScale.addFocusListener(detailFocusEvent);
        _editFastClock.addFocusListener(detailFocusEvent);
        _editThrottles.addFocusListener(detailFocusEvent);
        _editMetric.addChangeListener(detailChangeEvent);

        // TrainType
        _editTrainTypeName = new JTextField(20);
        _editTrainTypeColor = new JColorChooser(Color.BLACK);
        _editTrainTypeColor.setPreviewPanel(new JPanel()); // remove the preview panel
        AbstractColorChooserPanel editTypeColorPanels[] = {new SplitButtonColorChooserPanel()};
        _editTrainTypeColor.setChooserPanels(editTypeColorPanels);

        _editTrainTypeName.addFocusListener(detailFocusEvent);
        _editTrainTypeColor.getSelectionModel().addChangeListener(detailChangeEvent);

        // Segment
        _editSegmentName = new JTextField(20);

        _editSegmentName.addFocusListener(detailFocusEvent);

        // Station
        _editStationName = new JTextField(20);
        _editDistance = new JTextField(5);
        _editDoubleTrack = new JCheckBox();
        _editSidings = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));
        _editStaging = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));

        _editStationName.addFocusListener(detailFocusEvent);
        _editDistance.addFocusListener(detailFocusEvent);
        _editDoubleTrack.addChangeListener(detailChangeEvent);
        _editSidings.addChangeListener(detailChangeEvent);
        _editStaging.addChangeListener(detailChangeEvent);

        // Schedule
        _editScheduleName = new JTextField(20);
        _editEffDate = new JTextField(10);
        _editStartHour = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
        _editDuration = new JSpinner(new SpinnerNumberModel(24, 1, 24, 1));

        _editScheduleName.addFocusListener(detailFocusEvent);
        _editEffDate.addFocusListener(detailFocusEvent);
        _editStartHour.addChangeListener(detailChangeEvent);
        _editDuration.addChangeListener(detailChangeEvent);

        // Train
        _editTrainName = new JTextField(10);
        _editTrainDesc = new JTextField(20);
        _editTrainType = new JComboBox<>();
        _editDefaultSpeed = new JTextField(5);
        _editTrainStartTime = new JTextField(5);
        _editThrottle = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));
        _editTrainNotes = new JTextArea(4, 30);
        _showRouteDuration = new JLabel();

        _editTrainName.addFocusListener(detailFocusEvent);
        _editTrainDesc.addFocusListener(detailFocusEvent);
        _editTrainType.addFocusListener(detailFocusEvent);
        _editDefaultSpeed.addFocusListener(detailFocusEvent);
        _editTrainStartTime.addFocusListener(detailFocusEvent);
        _editThrottle.addChangeListener(detailChangeEvent);
        _editTrainNotes.addFocusListener(detailFocusEvent);

        // Stop
        _showStopSeq = new JLabel();
        _editStopStation = new JComboBox<>();
        _editStopDuration = new JTextField(5);
        _editNextSpeed = new JTextField(5);
        _editStagingTrack = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));
        _editStopNotes = new JTextArea(4, 30);
        _showArriveTime = new JLabel();
        _showDepartTime = new JLabel();

        _editStopStation.addFocusListener(detailFocusEvent);
        _editStopStation.addItemListener(stopStationItemEvent);
        _editStopDuration.addFocusListener(detailFocusEvent);
        _editNextSpeed.addFocusListener(detailFocusEvent);
        _editStagingTrack.addChangeListener(detailChangeEvent);
        _editStopNotes.addFocusListener(detailFocusEvent);
    }

    /**
     * Enable edit mode.  Used for JTextFields and JComboBoxs.
     */
    transient FocusListener detailFocusEvent = new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
            if (!_editActive) {
                setEditMode(true);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
        }
    };

    /**
     * Enable edit mode.  Used for JCheckBoxs, JSpinners and JColorChoosers.
     */
    transient ChangeListener detailChangeEvent = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (!_editActive) {
                setEditMode(true);
            }
        }
    };

    /**
     * Change the max spinner value based on the station data.
     * The number of staging tracks varies depending on the selected station.
     */
    transient ItemListener stopStationItemEvent = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                TimeTableDataManager.SegmentStation segmentStation = (TimeTableDataManager.SegmentStation) e.getItem();
                int stagingTracks = _dataMgr.getStation(segmentStation.getStationId()).getStaging();
                Stop stop = _dataMgr.getStop(_curNodeId);
                if (stop.getStagingTrack() <= stagingTracks) {
                    _editStagingTrack.setModel(new SpinnerNumberModel(stop.getStagingTrack(), 0, stagingTracks, 1));
                }
            }
        }
    };

    /**
     * If the custom scale item is selected provide a dialog to set the scale ratio
     */
    transient ItemListener layoutScaleItemEvent = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (_editScale.hasFocus()) {
                    Scale scale = (Scale) _editScale.getSelectedItem();
                    if (scale.getScaleName().equals("CUSTOM")) {  // NOI18N
                        String ans = JOptionPane.showInputDialog(
                                Bundle.getMessage("ScaleRatioChange"),  // NOI18N
                                scale.getScaleRatio()
                                );
                        if (ans != null) {
                            try {
                                double newRatio = Double.parseDouble(ans);
                                scale.setScaleRatio(newRatio);
                            } catch (java.lang.IllegalArgumentException
                                    | java.beans.PropertyVetoException ex) {
                                log.warn("Unable to change custom ratio: {}", ex.getMessage());  // NOI18N
                                JOptionPane.showMessageDialog(null,
                                        Bundle.getMessage("NumberFormatError", ans, "Custom ratio"),  // NOI18N
                                        Bundle.getMessage("WarningTitle"),  // NOI18N
                                        JOptionPane.WARNING_MESSAGE);
                                Layout layout = _dataMgr.getLayout(_curNodeId);
                                _editScale.setSelectedItem(layout.getScale());
                            }
                        }
                    }
                }
            }
        }
    };

    // ------------ Create GridBag panels ------------

    /**
     * Build new GridBag content. The grid panel is hidden, emptied, re-built and
     * made visible.
     *
     * @param gridType The type of grid to create
     */
    void makeDetailGrid(String gridType) {
        _detailGrid.setVisible(false);
        _detailGrid.removeAll();
        _detailFooter.setVisible(true);

        _gridPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.ipadx = 5;

        switch (gridType) {
            case "EmptyGrid":  // NOI18N
                makeEmptyGrid(c);
                _detailFooter.setVisible(false);
                break;

            case "Layout":  // NOI18N
                makeLayoutGrid(c);
                break;

            case "TrainType":  // NOI18N
                makeTrainTypeGrid(c);
                break;

            case "Segment":  // NOI18N
                makeSegmentGrid(c);
                break;

            case "Station":  // NOI18N
                makeStationGrid(c);
                break;

            case "Schedule":  // NOI18N
                makeScheduleGrid(c);
                break;

            case "Train":  // NOI18N
                makeTrainGrid(c);
                break;

            case "Stop":  // NOI18N
                makeStopGrid(c);
                break;

            default:
                log.warn("Invalid grid type: '{}'", gridType);  // NOI18N
                makeEmptyGrid(c);
        }

        _detailGrid.add(_gridPanel);
        _detailGrid.setVisible(true);
    }

    /**
     * This grid is used when there are no edit grids required.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeEmptyGrid(GridBagConstraints c) {
        // Variable type box
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        JLabel rowLabel = new JLabel("This page is intentionally blank");  // NOI18N
        _gridPanel.add(rowLabel, c);
    }

    /**
     * This grid is used to edit Layout data.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeLayoutGrid(GridBagConstraints c) {
        makeGridLabel(0, "LabelLayoutName", "HintLayoutName", c);  // NOI18N
        _gridPanel.add(_editLayoutName, c);

        makeGridLabel(1, "LabelScale", "HintScale", c);  // NOI18N
        _gridPanel.add(_editScale, c);

        makeGridLabel(2, "LabelFastClock", "HintFastClock", c);  // NOI18N
        _gridPanel.add(_editFastClock, c);

        makeGridLabel(3, "LabelThrottles", "HintThrottles", c);  // NOI18N
        _gridPanel.add(_editThrottles, c);

        makeGridLabel(4, "LabelMetric", "HintMetric", c);  // NOI18N
        _gridPanel.add(_editMetric, c);

        makeGridLabel(5, "LabelScaleMK", "HintScaleMK", c);  // NOI18N
        _gridPanel.add(_showScaleMK, c);
    }

    /**
     * This grid is used to edit the Train Type data.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeTrainTypeGrid(GridBagConstraints c) {
        makeGridLabel(0, "LabelTrainTypeName", "HintTrainTypeName", c);  // NOI18N
        _gridPanel.add(_editTrainTypeName, c);

        makeGridLabel(1, "LabelTrainTypeColor", "HintTrainTypeColor", c);  // NOI18N
        _gridPanel.add(_editTrainTypeColor, c);
    }

    /**
     * This grid is used to edit the Segment data.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeSegmentGrid(GridBagConstraints c) {
        makeGridLabel(0, "LabelSegmentName", "HintSegmentName", c);  // NOI18N
        _gridPanel.add(_editSegmentName, c);
    }

    /**
     * This grid is used to edit the Station data.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeStationGrid(GridBagConstraints c) {
        makeGridLabel(0, "LabelStationName", "HintStationName", c);  // NOI18N
        _gridPanel.add(_editStationName, c);

        makeGridLabel(1, "LabelDistance", "HintDistance", c);  // NOI18N
        _gridPanel.add(_editDistance, c);

        makeGridLabel(2, "LabelDoubleTrack", "HintDoubleTrack", c);  // NOI18N
        _gridPanel.add(_editDoubleTrack, c);

        makeGridLabel(3, "LabelSidings", "HintSidings", c);  // NOI18N
        _gridPanel.add(_editSidings, c);

        makeGridLabel(4, "LabelStaging", "HintStaging", c);  // NOI18N
        _gridPanel.add(_editStaging, c);
    }

    /**
     * This grid is used to edit the Schedule data.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeScheduleGrid(GridBagConstraints c) {
        makeGridLabel(0, "LabelScheduleName", "HintScheduleName", c);  // NOI18N
        _gridPanel.add(_editScheduleName, c);

        makeGridLabel(1, "LabelEffDate", "HintEffDate", c);  // NOI18N
        _gridPanel.add(_editEffDate, c);

        makeGridLabel(2, "LabelStartHour", "HintStartHour", c);  // NOI18N
        _gridPanel.add(_editStartHour, c);

        makeGridLabel(3, "LabelDuration", "HintDuration", c);  // NOI18N
        _gridPanel.add(_editDuration, c);
    }

    /**
     * This grid is used to edit the Train data.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeTrainGrid(GridBagConstraints c) {
        makeGridLabel(0, "LabelTrainName", "HintTrainName", c);  // NOI18N
        _gridPanel.add(_editTrainName, c);

        makeGridLabel(1, "LabelTrainDesc", "HintTrainDesc", c);  // NOI18N
        _gridPanel.add(_editTrainDesc, c);

        makeGridLabel(2, "LabelTrainType", "HintTrainType", c);  // NOI18N
        _gridPanel.add(_editTrainType, c);

        makeGridLabel(3, "LabelDefaultSpeed", "HintDefaultSpeed", c);  // NOI18N
        _gridPanel.add(_editDefaultSpeed, c);

        makeGridLabel(4, "LabelTrainStartTime", "HintTrainStartTime", c);  // NOI18N
        _gridPanel.add(_editTrainStartTime, c);

        makeGridLabel(5, "LabelThrottle", "HintThrottle", c);  // NOI18N
        _gridPanel.add(_editThrottle, c);

        makeGridLabel(6, "LabelRouteDuration", "HintRouteDuration", c);  // NOI18N
        _gridPanel.add(_showRouteDuration, c);

        makeGridLabel(7, "LabelTrainNotes", "HintTrainNotes", c);  // NOI18N
        _gridPanel.add(_editTrainNotes, c);
    }

    /**
     * This grid is used to edit the Stop data.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeStopGrid(GridBagConstraints c) {
        makeGridLabel(0, "LabelStopSeq", "HintStopSeq", c);  // NOI18N
        _gridPanel.add(_showStopSeq, c);

        makeGridLabel(1, "LabelStopStation", "HintStopStation", c);  // NOI18N
        _gridPanel.add(_editStopStation, c);

        makeGridLabel(2, "LabelStopDuration", "HintStopDuration", c);  // NOI18N
        _gridPanel.add(_editStopDuration, c);

        makeGridLabel(3, "LabelNextSpeed", "HintNextSpeed", c);  // NOI18N
        _gridPanel.add(_editNextSpeed, c);

        makeGridLabel(4, "LabelStagingTrack", "HintStagingTrack", c);  // NOI18N
        _gridPanel.add(_editStagingTrack, c);

        makeGridLabel(5, "LabelArriveTime", "HintArriveTime", c);  // NOI18N
        _gridPanel.add(_showArriveTime, c);

        makeGridLabel(6, "LabelDepartTime", "HintDepartTime", c);  // NOI18N
        _gridPanel.add(_showDepartTime, c);

        makeGridLabel(7, "LabelStopNotes", "HintStopNotes", c);  // NOI18N
        _gridPanel.add(_editStopNotes, c);
    }

    /**
     * Create the label portion of a grid row.
     * @param row The grid row number.
     * @param label The bundle key for the label text.
     * @param hint The bundle key for the label tool tip.
     * @param c The grid bag contraints object.
     */
    void makeGridLabel(int row, String label, String hint, GridBagConstraints c) {
        c.gridy = row;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel rowLabel = new JLabel(Bundle.getMessage(label));
        rowLabel.setToolTipText(Bundle.getMessage(hint));
        _gridPanel.add(rowLabel, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
    }

    // ------------ Process button bar and tree events ------------

    /**
     * Add new items.
     */
    void addPressed() {
        switch (_curNodeType) {
            case "Layout":     // NOI18N
                addLayout();
                break;

            case "TrainTypes": // NOI18N
                addTrainType();
                break;

            case "Segments":   // NOI18N
                addSegment();
                break;

            case "Segment":    // NOI18N
                addStation();
                break;

            case "Schedules":  // NOI18N
                addSchedule();
                break;

            case "Schedule":   // NOI18N
                addTrain();
                break;

            case "Train":      // NOI18N
                addStop();
                break;

            default:
                log.error("Add called for unsupported node type: '{}'", _curNodeType);  // NOI18N
        }
    }

    /**
     * Create a new Layout object with default values.
     * Add the layout node and the TrainTypes, Segments and Schedules collection nodes.
     */
    void addLayout() {
        Layout newLayout = new Layout();
        setShowReminder(true);

        // Build tree components
        _curNode = new TimeTableTreeNode(newLayout.getLayoutName(), "Layout", newLayout.getLayoutId(), 0);    // NOI18N
        _timetableRoot.add(_curNode);
        _leafNode = new TimeTableTreeNode(buildNodeText("TrainTypes", null, 0), "TrainTypes", 0, 0);    // NOI18N
        _curNode.add(_leafNode);
        _leafNode = new TimeTableTreeNode(buildNodeText("Segments", null, 0), "Segments", 0, 0);    // NOI18N
        _curNode.add(_leafNode);
        _leafNode = new TimeTableTreeNode(buildNodeText("Schedules", null, 0), "Schedules", 0, 0);    // NOI18N
        _curNode.add(_leafNode);
        _timetableModel.nodeStructureChanged(_timetableRoot);

        // Switch to new node
        _timetableTree.setSelectionPath(new TreePath(_curNode.getPath()));
    }

    /**
     * Create a new Train Type object.
     * The default color is black.
     */
    void addTrainType() {
        TimeTableTreeNode layoutNode = (TimeTableTreeNode) _curNode.getParent();
        int layoutId = layoutNode.getId();
        TrainType newType = new TrainType(layoutId);
        setShowReminder(true);

        // Build tree components
        _leafNode = new TimeTableTreeNode(newType.getTypeName(), "TrainType", newType.getTypeId(), 0);    // NOI18N
        _curNode.add(_leafNode);
        _timetableModel.nodeStructureChanged(_curNode);

        // Switch to new node
        _timetableTree.setSelectionPath(new TreePath(_leafNode.getPath()));
    }

    /**
     * Create a new Segment object with default values.
     */
    void addSegment() {
        TimeTableTreeNode layoutNode = (TimeTableTreeNode) _curNode.getParent();
        int layoutId = layoutNode.getId();
        Segment newSegment = new Segment(layoutId);
        setShowReminder(true);

        // Build tree components
        _leafNode = new TimeTableTreeNode(newSegment.getSegmentName(), "Segment", newSegment.getSegmentId(), 0);    // NOI18N
        _curNode.add(_leafNode);
        _timetableModel.nodeStructureChanged(_curNode);

        // Switch to new node
        _timetableTree.setSelectionPath(new TreePath(_leafNode.getPath()));
    }

    /**
     * Create a new Station object with default values.
     */
    void addStation() {
        Station newStation = new Station(_curNodeId);
        setShowReminder(true);

        // Build tree components
        _leafNode = new TimeTableTreeNode(newStation.getStationName(), "Station", newStation.getStationId(), 0);    // NOI18N
        _curNode.add(_leafNode);
        _timetableModel.nodeStructureChanged(_curNode);

        // Switch to new node
        _timetableTree.setSelectionPath(new TreePath(_leafNode.getPath()));
    }

    /**
     * Create a new Schedule object with default values.
     */
    void addSchedule() {
        TimeTableTreeNode layoutNode = (TimeTableTreeNode) _curNode.getParent();
        int layoutId = layoutNode.getId();
        Schedule newSchedule = new Schedule(layoutId);
        setShowReminder(true);

        // Build tree components
        _leafNode = new TimeTableTreeNode(newSchedule.getScheduleName(), "Schedule", newSchedule.getScheduleId(), 0);    // NOI18N
        _curNode.add(_leafNode);
        _timetableModel.nodeStructureChanged(_curNode);

        // Switch to new node
        _timetableTree.setSelectionPath(new TreePath(_leafNode.getPath()));
    }

    void addTrain() {
        Train newTrain = new Train(_curNodeId);
        setShowReminder(true);

        // Build tree components
        _leafNode = new TimeTableTreeNode(newTrain.getTrainName(), "Train", newTrain.getTrainId(), 0);    // NOI18N
        _curNode.add(_leafNode);
        _timetableModel.nodeStructureChanged(_curNode);

        // Switch to new node
        _timetableTree.setSelectionPath(new TreePath(_leafNode.getPath()));
    }

    void addStop() {
        int newSeq = _dataMgr.getStops(_curNodeId, 0, false).size();
        Stop newStop = new Stop(_curNodeId, newSeq + 1);
        setShowReminder(true);

        // Build tree components
        _leafNode = new TimeTableTreeNode(String.valueOf(newSeq + 1), "Stop", newStop.getStopId(), newSeq + 1);    // NOI18N
        _curNode.add(_leafNode);
        _timetableModel.nodeStructureChanged(_curNode);

        // Switch to new node
        _timetableTree.setSelectionPath(new TreePath(_leafNode.getPath()));
    }

    /**
     * Set up the edit environment for the selected node Called from
     * {@link #treeRowSelected}. This takes the place of an actual button.
     */
    void editPressed() {
        switch (_curNodeType) {
            case "Layout":     // NOI18N
                editLayout();
                makeDetailGrid("Layout");  // NOI18N
                break;

            case "TrainType":     // NOI18N
                editTrainType();
                makeDetailGrid("TrainType");  // NOI18N
                break;

            case "Segment":     // NOI18N
                editSegment();
                makeDetailGrid("Segment");  // NOI18N
                break;

            case "Station":     // NOI18N
                editStation();
                makeDetailGrid("Station");  // NOI18N
                break;

            case "Schedule":     // NOI18N
                editSchedule();
                makeDetailGrid("Schedule");  // NOI18N
                break;

            case "Train":     // NOI18N
                editTrain();
                makeDetailGrid("Train");  // NOI18N
                break;

            case "Stop":     // NOI18N
                editStop();
                makeDetailGrid("Stop");  // NOI18N
                break;

            default:
                log.error("Edit called for unsupported node type: '{}'", _curNodeType);  // NOI18N
        }
        setEditMode(false);
    }

    /*
     * Set Layout edit variables and labels
     */
    void editLayout() {
        Layout layout = _dataMgr.getLayout(_curNodeId);
        _editLayoutName.setText(layout.getLayoutName());
        _editFastClock.setText(Integer.toString(layout.getFastClock()));
        _editThrottles.setText(Integer.toString(layout.getThrottles()));
        _editMetric.setSelected(layout.getMetric());
        String unitMeasure = (layout.getMetric())
                ? Bundle.getMessage("LabelRealMeters") // NOI18N
                : Bundle.getMessage("LabelRealFeet"); // NOI18N
        _showScaleMK.setText(String.format("%.2f %s", layout.getScaleMK(), unitMeasure)); // NOI18N

        _editScale.removeAllItems();
        for (Scale scale : ScaleManager.getScales()) {
            _editScale.addItem(scale);
        }
        jmri.util.swing.JComboBoxUtil.setupComboBoxMaxRows(_editScale);
        _editScale.setSelectedItem(layout.getScale());
    }

    /*
     * Set TrainType edit variables and labels
     */
    void editTrainType() {
        TrainType type = _dataMgr.getTrainType(_curNodeId);
        _editTrainTypeName.setText(type.getTypeName());
        _editTrainTypeColor.setColor(Color.decode(type.getTypeColor()));
    }

    /*
     * Set Segment edit variables and labels
     */
    void editSegment() {
        Segment segment = _dataMgr.getSegment(_curNodeId);
        _editSegmentName.setText(segment.getSegmentName());
    }

    /*
     * Set Station edit variables and labels
     */
    void editStation() {
        Station station = _dataMgr.getStation(_curNodeId);
        _editStationName.setText(station.getStationName());
        _editDistance.setText(Double.toString(station.getDistance()));
        _editDoubleTrack.setSelected(station.getDoubleTrack());
        _editSidings.setValue(station.getSidings());
        _editStaging.setValue(station.getStaging());
    }

    /*
     * Set Schedule edit variables and labels
     */
    void editSchedule() {
        Schedule schedule = _dataMgr.getSchedule(_curNodeId);
        _editScheduleName.setText(schedule.getScheduleName());
        _editEffDate.setText(schedule.getEffDate());
        _editStartHour.setValue(schedule.getStartHour());
        _editDuration.setValue(schedule.getDuration());
    }

    /*
     * Set Train edit variables and labels
     */
    void editTrain() {
        Train train = _dataMgr.getTrain(_curNodeId);
        int layoutId = _dataMgr.getSchedule(train.getScheduleId()).getLayoutId();

        _editTrainName.setText(train.getTrainName());
        _editTrainDesc.setText(train.getTrainDesc());
        _editDefaultSpeed.setText(Integer.toString(train.getDefaultSpeed()));
        _editTrainStartTime.setText(String.format("%02d:%02d",  // NOI18N
                train.getStartTime() / 60,
                train.getStartTime() % 60));
        _editThrottle.setModel(new SpinnerNumberModel(train.getThrottle(), 0, _dataMgr.getLayout(layoutId).getThrottles(), 1));
        _editTrainNotes.setText(train.getTrainNotes());
        _showRouteDuration.setText(String.format("%02d:%02d",  // NOI18N
                train.getRouteDuration() / 60,
                train.getRouteDuration() % 60));

        _editTrainType.removeAllItems();
        for (TrainType type : _dataMgr.getTrainTypes(layoutId, true)) {
            _editTrainType.addItem(type);
        }
        jmri.util.swing.JComboBoxUtil.setupComboBoxMaxRows(_editTrainType);
        if (train.getTypeId() > 0) {
            _editTrainType.setSelectedItem(_dataMgr.getTrainType(train.getTypeId()));
        }
    }

    /*
     * Set Stop edit variables and labels
     * The station combo box uses a data manager internal class to present
     * both the segment name and the station name.  This is needed since a station
     * can be in multiple segments.
     */
    void editStop() {
        Stop stop = _dataMgr.getStop(_curNodeId);
        Layout layout = _dataMgr.getLayoutForStop(_curNodeId);

        _showStopSeq.setText(Integer.toString(stop.getSeq()));
        _editStopDuration.setText(Integer.toString(stop.getDuration()));
        _editNextSpeed.setText(Integer.toString(stop.getNextSpeed()));
        _editStopNotes.setText(stop.getStopNotes());
        _showArriveTime.setText(String.format("%02d:%02d",  // NOI18N
                stop.getArriveTime() / 60,
                stop.getArriveTime() % 60));
        _showDepartTime.setText(String.format("%02d:%02d",  // NOI18N
                stop.getDepartTime() / 60,
                stop.getDepartTime() % 60));

        _editStopStation.removeAllItems();
        for (TimeTableDataManager.SegmentStation segmentStation : _dataMgr.getSegmentStations(layout.getLayoutId())) {
            _editStopStation.addItem(segmentStation);
            if (stop.getStationId() == segmentStation.getStationId()) {
                // This also triggers stopStationItemEvent which will set _editStagingTrack
                _editStopStation.setSelectedItem(segmentStation);
            }
        }
        jmri.util.swing.JComboBoxUtil.setupComboBoxMaxRows(_editStopStation);
        setMoveButtons();
    }

    /**
     * Apply the updates to the current node.
     */
    void updatePressed() {
        switch (_curNodeType) {
            case "Layout":     // NOI18N
                updateLayout();
                break;

            case "TrainType":     // NOI18N
                updateTrainType();
                break;

            case "Segment":     // NOI18N
                updateSegment();
                break;

            case "Station":     // NOI18N
                updateStation();
                break;

            case "Schedule":     // NOI18N
                updateSchedule();
                break;

            case "Train":     // NOI18N
                updateTrain();
                break;

            case "Stop":     // NOI18N
                updateStop();
                break;

            default:
                log.warn("Invalid update button press");  // NOI18N
        }
        setEditMode(false);
        _timetableTree.setSelectionPath(_curTreePath);
        _timetableTree.grabFocus();
        editPressed();
    }

    /**
     * Update the layout information.
     * If the fast clock or metric values change, a recalc will be required.
     * The throttles value cannot be less than the highest throttle assigned to a train.
     */
    void updateLayout() {
        Layout layout = _dataMgr.getLayout(_curNodeId);

        // Pre-validate and convert inputs
        String newName = _editLayoutName.getText().trim();
        Scale newScale = (Scale) _editScale.getSelectedItem();
        int newFastClock = parseNumber(_editFastClock, "fast clock");  // NOI18N
        if (newFastClock < 1) {
            newFastClock = layout.getFastClock();
        }
        int newThrottles = parseNumber(_editThrottles, "throttles");  // NOI18N
        if (newThrottles < 0) {
            newThrottles = layout.getThrottles();
        }
        boolean newMetric =_editMetric.isSelected();

        boolean update = false;
        List<String> exceptionList = new ArrayList<>();

        // Perform updates
        if (!layout.getLayoutName().equals(newName)) {
            layout.setLayoutName(newName);
            _curNode.setText(newName);
            _timetableModel.nodeChanged(_curNode);
            update = true;
        }

        if (!layout.getScale().equals(newScale)) {
            try {
                layout.setScale(newScale);
                update = true;
            } catch (IllegalArgumentException ex) {
                exceptionList.add(ex.getMessage());
            }
        }

        if (layout.getFastClock() != newFastClock) {
            try {
                layout.setFastClock(newFastClock);
                update = true;
            } catch (IllegalArgumentException ex) {
                exceptionList.add(ex.getMessage());
            }
        }

        if (layout.getMetric() != newMetric) {
            try {
                layout.setMetric(newMetric);
                update = true;
            } catch (IllegalArgumentException ex) {
                exceptionList.add(ex.getMessage());
            }
        }

        if (layout.getThrottles() != newThrottles) {
            try {
                layout.setThrottles(newThrottles);
                update = true;
            } catch (IllegalArgumentException ex) {
                exceptionList.add(ex.getMessage());
            }
        }

        if (update) {
            setShowReminder(true);
        }

        // Display exceptions if necessary
        if (!exceptionList.isEmpty()) {
            StringBuilder msg = new StringBuilder(Bundle.getMessage("LayoutUpdateErrors"));  // NOI18N
            for (String keyWord : exceptionList) {
                if (keyWord.startsWith(_dataMgr.TIME_OUT_OF_RANGE)) {
                    String[] comps = keyWord.split("~");
                    msg.append(Bundle.getMessage(comps[0], comps[1], comps[2]));
                } else if (keyWord.startsWith(_dataMgr.SCALE_NF)) {
                    String[] scaleMsg = keyWord.split("~");
                    msg.append(Bundle.getMessage(scaleMsg[0], scaleMsg[1]));
                } else {
                    msg.append(String.format("%n%s", Bundle.getMessage(keyWord)));
                    if (keyWord.equals(_dataMgr.THROTTLES_IN_USE)) {
                        // Add the affected trains
                        for (Schedule schedule : _dataMgr.getSchedules(_curNodeId, true)) {
                            for (Train train : _dataMgr.getTrains(schedule.getScheduleId(), 0, true)) {
                                if (train.getThrottle() > newThrottles) {
                                    msg.append(String.format("%n      %s [ %d ]", train.getTrainName(), train.getThrottle()));
                                }
                            }
                        }
                    }
                }
            }
            JOptionPane.showMessageDialog(null,
                    msg.toString(),
                    Bundle.getMessage("WarningTitle"),  // NOI18N
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Update the train type information.
     */
    void updateTrainType() {
        TrainType type = _dataMgr.getTrainType(_curNodeId);

        String newName = _editTrainTypeName.getText().trim();
        Color newColor = _editTrainTypeColor.getColor();
        String newColorHex = jmri.util.ColorUtil.colorToHexString(newColor);

        boolean update = false;

        if (!type.getTypeName().equals(newName)) {
            type.setTypeName(newName);
            _curNode.setText(newName);
            update = true;
        }
        if (!type.getTypeColor().equals(newColorHex)) {
            type.setTypeColor(newColorHex);
            update = true;
        }
        _timetableModel.nodeChanged(_curNode);

        if (update) {
            setShowReminder(true);
        }
    }

    /**
     * Update the segment information.
     */
    void updateSegment() {
        String newName = _editSegmentName.getText().trim();

        Segment segment = _dataMgr.getSegment(_curNodeId);
        if (!segment.getSegmentName().equals(newName)) {
            segment.setSegmentName(newName);
            _curNode.setText(newName);
            setShowReminder(true);
        }
        _timetableModel.nodeChanged(_curNode);
    }

    /**
     * Update the station information.
     * The staging track value cannot be less than any train references.
     */
    void updateStation() {
        Station station = _dataMgr.getStation(_curNodeId);

        // Pre-validate and convert inputs
        String newName = _editStationName.getText().trim();
        double newDistance;
        try {
            newDistance = Double.parseDouble(_editDistance.getText());
        } catch (NumberFormatException ex) {
            log.warn("'{}' is not a valid number for {}", _editDistance.getText(), "station distance");  // NOI18N
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("NumberFormatError", _editDistance.getText(), "station distance"),  // NOI18N
                    Bundle.getMessage("WarningTitle"),  // NOI18N
                    JOptionPane.WARNING_MESSAGE);
            newDistance = station.getDistance();
        }
        boolean newDoubleTrack =_editDoubleTrack.isSelected();
        int newSidings = (int) _editSidings.getValue();
        int newStaging = (int) _editStaging.getValue();

        boolean update = false;
        List<String> exceptionList = new ArrayList<>();

        // Perform updates
        if (!station.getStationName().equals(newName)) {
            station.setStationName(newName);
            _curNode.setText(newName);
            _timetableModel.nodeChanged(_curNode);
            update = true;
        }

        if (newDistance < 0.0) {
            newDistance = station.getDistance();
        }
        if (Math.abs(station.getDistance() - newDistance) > .01 ) {
            try {
                station.setDistance(newDistance);
                update = true;
            } catch (IllegalArgumentException ex) {
                exceptionList.add(ex.getMessage());
            }
        }

        if (station.getDoubleTrack() != newDoubleTrack) {
            station.setDoubleTrack(newDoubleTrack);
            update = true;
        }

        if (station.getSidings() != newSidings) {
            station.setSidings(newSidings);
            update = true;
        }

        if (station.getStaging() != newStaging) {
            try {
                station.setStaging(newStaging);
                update = true;
            } catch (IllegalArgumentException ex) {
                exceptionList.add(ex.getMessage());
            }
        }

        if (update) {
            setShowReminder(true);
        }

        // Display exceptions if necessary
        if (!exceptionList.isEmpty()) {
            StringBuilder msg = new StringBuilder(Bundle.getMessage("StationUpdateErrors"));  // NOI18N
            for (String keyWord : exceptionList) {
                if (keyWord.startsWith(_dataMgr.TIME_OUT_OF_RANGE)) {
                    String[] comps = keyWord.split("~");
                    msg.append(Bundle.getMessage(comps[0], comps[1], comps[2]));
                } else {
                    msg.append(String.format("%n%s", Bundle.getMessage(keyWord)));
                    if (keyWord.equals(_dataMgr.STAGING_IN_USE)) {
                        // Add the affected stops
                        for (Stop stop : _dataMgr.getStops(0, _curNodeId, false)) {
                            if (stop.getStagingTrack() > newStaging) {
                                Train train = _dataMgr.getTrain(stop.getTrainId());
                                msg.append(String.format("%n      %s, %d", train.getTrainName(), stop.getSeq()));
                            }
                        }
                    }
                }
            }
            JOptionPane.showMessageDialog(null,
                    msg.toString(),
                    Bundle.getMessage("WarningTitle"),  // NOI18N
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Update the schedule information.
     * Changes to the schedule times cannot make a train start time or
     * a stop's arrival or departure times invalid.
     */
    void updateSchedule() {
        Schedule schedule = _dataMgr.getSchedule(_curNodeId);

        // Pre-validate and convert inputs
        String newName = _editScheduleName.getText().trim();
        String newEffDate = _editEffDate.getText().trim();
        int newStartHour = (int) _editStartHour.getValue();
        if (newStartHour < 0 || newStartHour > 23) {
            newStartHour = schedule.getStartHour();
        }
        int newDuration = (int) _editDuration.getValue();
        if (newDuration < 1 || newDuration > 24) {
            newDuration = schedule.getDuration();
        }

        boolean update = false;
        List<String> exceptionList = new ArrayList<>();

        // Perform updates
        if (!schedule.getScheduleName().equals(newName)) {
            schedule.setScheduleName(newName);
            update = true;
        }

        if (!schedule.getEffDate().equals(newEffDate)) {
            schedule.setEffDate(newEffDate);
            update = true;
        }

        if (update) {
            _curNode.setText(buildNodeText("Schedule", schedule, 0));  // NOI18N
            _timetableModel.nodeChanged(_curNode);
        }

        if (schedule.getStartHour() != newStartHour) {
            try {
                schedule.setStartHour(newStartHour);
                update = true;
            } catch (IllegalArgumentException ex) {
                exceptionList.add(ex.getMessage());
            }
        }

        if (schedule.getDuration() != newDuration) {
            try {
                schedule.setDuration(newDuration);
                update = true;
            } catch (IllegalArgumentException ex) {
                exceptionList.add(ex.getMessage());
            }
        }

        if (update) {
            setShowReminder(true);
        }

        // Display exceptions if necessary
        if (!exceptionList.isEmpty()) {
            StringBuilder msg = new StringBuilder(Bundle.getMessage("ScheduleUpdateErrors"));  // NOI18N
            for (String keyWord : exceptionList) {
                if (keyWord.startsWith(_dataMgr.TIME_OUT_OF_RANGE)) {
                    String[] comps = keyWord.split("~");
                    msg.append(Bundle.getMessage(comps[0], comps[1], comps[2]));
                } else {
                    msg.append(String.format("%n%s", Bundle.getMessage(keyWord)));
                }
            }
            JOptionPane.showMessageDialog(null,
                    msg.toString(),
                    Bundle.getMessage("WarningTitle"),  // NOI18N
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Update the train information.
     * The train start time has to have a h:mm format and cannot fall outside
     * of the schedules times.
     */
    void updateTrain() {
        Train train = _dataMgr.getTrain(_curNodeId);
        List<String> exceptionList = new ArrayList<>();

        // Pre-validate and convert inputs
        String newName = _editTrainName.getText().trim();
        String newDesc = _editTrainDesc.getText().trim();
        int newType = ((TrainType) _editTrainType.getSelectedItem()).getTypeId();
        int newSpeed = parseNumber(_editDefaultSpeed, "default train speed");  // NOI18N
        if (newSpeed < 0) {
            newSpeed = train.getDefaultSpeed();
        }

        LocalTime newTime;
        int newStart;
        try {
            newTime = LocalTime.parse(_editTrainStartTime.getText().trim(), DateTimeFormatter.ofPattern("H:mm"));  // NOI18N
            newStart = newTime.getHour() * 60 + newTime.getMinute();
        } catch (java.time.format.DateTimeParseException ex) {
            exceptionList.add(_dataMgr.START_TIME_FORMAT + "~" + ex.getParsedString());
            newStart = train.getStartTime();
        }

        int newThrottle = (int) _editThrottle.getValue();
        String newNotes = _editTrainNotes.getText();

        boolean update = false;

        // Perform updates
        if (!train.getTrainName().equals(newName)) {
            train.setTrainName(newName);
            update = true;
        }

        if (!train.getTrainDesc().equals(newDesc)) {
            train.setTrainDesc(newDesc);
            update = true;
        }

        if (update) {
            _curNode.setText(buildNodeText("Train", train, 0));  // NOI18N
            _timetableModel.nodeChanged(_curNode);
        }

        if (train.getTypeId() != newType) {
            train.setTypeId(newType);
            update = true;
        }

        if (train.getDefaultSpeed() != newSpeed) {
            try {
                train.setDefaultSpeed(newSpeed);
                update = true;
            } catch (IllegalArgumentException ex) {
                exceptionList.add(ex.getMessage());
            }
        }

        if (train.getStartTime() != newStart) {
            try {
                train.setStartTime(newStart);
                update = true;
            } catch (IllegalArgumentException ex) {
                exceptionList.add(ex.getMessage());
            }
        }

        if (train.getThrottle() != newThrottle) {
            try {
                train.setThrottle(newThrottle);
                update = true;
            } catch (IllegalArgumentException ex) {
                exceptionList.add(ex.getMessage());
            }
        }

        if (!train.getTrainNotes().equals(newNotes)) {
            train.setTrainNotes(newNotes);
            update = true;
        }

        if (update) {
            setShowReminder(true);
        }

        // Display exceptions if necessary
        if (!exceptionList.isEmpty()) {
            StringBuilder msg = new StringBuilder(Bundle.getMessage("TrainUpdateErrors"));  // NOI18N
            for (String keyWord : exceptionList) {
                log.info("kw = {}", keyWord);
                if (keyWord.startsWith(_dataMgr.TIME_OUT_OF_RANGE)) {
                    String[] comps = keyWord.split("~");
                    msg.append(Bundle.getMessage(comps[0], comps[1], comps[2]));
                } else if (keyWord.startsWith(_dataMgr.START_TIME_FORMAT)) {
                    String[] timeMsg = keyWord.split("~");
                    msg.append(Bundle.getMessage(timeMsg[0], timeMsg[1]));
                } else if (keyWord.startsWith(_dataMgr.START_TIME_RANGE)) {
                    String[] schedMsg = keyWord.split("~");
                    msg.append(Bundle.getMessage(schedMsg[0], schedMsg[1], schedMsg[2]));
                } else {
                    msg.append(String.format("%n%s", Bundle.getMessage(keyWord)));
                }
            }
            JOptionPane.showMessageDialog(null,
                    msg.toString(),
                    Bundle.getMessage("WarningTitle"),  // NOI18N
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Update the stop information.
     */
    void updateStop() {
        Stop stop = _dataMgr.getStop(_curNodeId);

        // Pre-validate and convert inputs
        TimeTableDataManager.SegmentStation stopSegmentStation =
                (TimeTableDataManager.SegmentStation) _editStopStation.getSelectedItem();
        int newStation = stopSegmentStation.getStationId();
        int newDuration = parseNumber(_editStopDuration, "stop duration");  // NOI18N
        if (newDuration < 0) {
            newDuration = stop.getDuration();
        }
        int newSpeed = parseNumber(_editNextSpeed, "next speed");  // NOI18N
        if (newSpeed < 0) {
            newSpeed = stop.getNextSpeed();
        }
        int newStagingTrack = (int) _editStagingTrack.getValue();
        String newNotes = _editStopNotes.getText();

        boolean update = false;
        List<String> exceptionList = new ArrayList<>();

        // Perform updates
        if (stop.getStationId() != newStation) {
            stop.setStationId(newStation);
            _curNode.setText(buildNodeText("Stop", stop, 0));  // NOI18N
            _timetableModel.nodeChanged(_curNode);
            update = true;
        }

        if (stop.getDuration() != newDuration) {
            try {
                stop.setDuration(newDuration);
                update = true;
            } catch (IllegalArgumentException ex) {
                exceptionList.add(ex.getMessage());
            }
        }

        if (stop.getNextSpeed() != newSpeed) {
            try {
                stop.setNextSpeed(newSpeed);
                update = true;
            } catch (IllegalArgumentException ex) {
                exceptionList.add(ex.getMessage());
            }
        }

        if (stop.getStagingTrack() != newStagingTrack) {
            try {
                stop.setStagingTrack(newStagingTrack);
                update = true;
            } catch (IllegalArgumentException ex) {
                exceptionList.add(ex.getMessage());
            }
        }

        if (!stop.getStopNotes().equals(newNotes)) {
            stop.setStopNotes(newNotes);
            update = true;
        }

        if (update) {
            setShowReminder(true);
        }

        // Display exceptions if necessary
        if (!exceptionList.isEmpty()) {
            StringBuilder msg = new StringBuilder(Bundle.getMessage("StopUpdateErrors"));  // NOI18N
            for (String keyWord : exceptionList) {
                if (keyWord.startsWith(_dataMgr.TIME_OUT_OF_RANGE)) {
                    String[] comps = keyWord.split("~");
                    msg.append(Bundle.getMessage(comps[0], comps[1], comps[2]));
                } else {
                    msg.append(String.format("%n%s", Bundle.getMessage(keyWord)));
                }
            }
            JOptionPane.showMessageDialog(null,
                    msg.toString(),
                    Bundle.getMessage("WarningTitle"),  // NOI18N
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Convert text input to an integer.
     * @param textField JTextField containing the probable integer.
     * @param fieldName The name of the field for the dialog.
     * @return the valid number or -1 for an invalid input.
     */
    int parseNumber(JTextField textField, String fieldName) {
        String text = textField.getText().trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            log.warn("'{}' is not a valid number for {}", text, fieldName);  // NOI18N
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("NumberFormatError", text, fieldName),  // NOI18N
                    Bundle.getMessage("WarningTitle"),  // NOI18N
                    JOptionPane.WARNING_MESSAGE);
            return -1;
        }
    }

    /**
     * Process the node delete request.
     */
    void deletePressed() {
        switch (_curNodeType) {
            case "Layout":  // NOI18N
                deleteLayout();
                break;

            case "TrainType":  // NOI18N
                deleteTrainType();
                break;

            case "Segment":  // NOI18N
                deleteSegment();
                break;

            case "Station":  // NOI18N
                deleteStation();
                break;

            case "Schedule":  // NOI18N
                deleteSchedule();
                break;

            case "Train":  // NOI18N
                deleteTrain();
                break;

            case "Stop":
                deleteStop();  // NOI18N
                break;

            default:
                log.error("Delete called for unsupported node type: '{}'", _curNodeType);  // NOI18N
        }
    }

    /**
     * After confirmation, perform a cascade delete of the layout and its components.
     */
    void deleteLayout() {
        Object[] options = {Bundle.getMessage("ButtonNo"), Bundle.getMessage("ButtonYes")};  // NOI18N
        int selectedOption = JOptionPane.showOptionDialog(null,
                Bundle.getMessage("LayoutCascade"), // NOI18N
                Bundle.getMessage("QuestionTitle"),   // NOI18N
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (selectedOption == 0) {
            return;
        }

        // Delete the components
        for (Schedule schedule : _dataMgr.getSchedules(_curNodeId, false)) {
            for (Train train : _dataMgr.getTrains(schedule.getScheduleId(), 0, false)) {
                for (Stop stop : _dataMgr.getStops(train.getTrainId(), 0, false)) {
                    _dataMgr.deleteStop(stop.getStopId());
                }
                _dataMgr.deleteTrain(train.getTrainId());
            }
            _dataMgr.deleteSchedule(schedule.getScheduleId());
        }

        for (Segment segment : _dataMgr.getSegments(_curNodeId, false)) {
            for (Station station : _dataMgr.getStations(segment.getSegmentId(), false)) {
                _dataMgr.deleteStation(station.getStationId());
            }
            _dataMgr.deleteSegment(segment.getSegmentId());
        }

        for (TrainType type : _dataMgr.getTrainTypes(_curNodeId, false)) {
            _dataMgr.deleteTrainType(type.getTypeId());
        }

        // delete the Layout
        _dataMgr.deleteLayout(_curNodeId);
        setShowReminder(true);

        // Update the tree
//         TreePath parentPath = _curTreePath.getParentPath();
        TreeNode parentNode = _curNode.getParent();
        _curNode.removeFromParent();
        _curNode = null;
        _timetableModel.nodeStructureChanged(parentNode);
//         _timetableTree.setSelectionPath(parentPath);
    }

    /**
     * Delete a train type after checking for usage.
     */
    void deleteTrainType() {
        // Check train references
        ArrayList<String> typeReference = new ArrayList<>();
        for (Train train : _dataMgr.getTrains(0, _curNodeId, true)) {
            typeReference.add(train.getTrainName());
        }
        if (!typeReference.isEmpty()) {
            StringBuilder msg = new StringBuilder(Bundle.getMessage("DeleteWarning", _curNodeType));  // NOI18N
            for (String trainName : typeReference) {
                msg.append("\n    " + trainName);  // NOI18N
            }
            JOptionPane.showMessageDialog(null,
                    msg.toString(),
                    Bundle.getMessage("WarningTitle"),  // NOI18N
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        _dataMgr.deleteTrainType(_curNodeId);
        setShowReminder(true);

        // Update the tree
        TreePath parentPath = _curTreePath.getParentPath();
        TimeTableTreeNode parentNode = (TimeTableTreeNode) _curNode.getParent();
        parentNode.remove(_curNode);
        _timetableModel.nodeStructureChanged(parentNode);
        _curNode = null;
        _timetableTree.setSelectionPath(parentPath);
    }

    /**
     * Delete a Segment.
     * If the segment contains inactive stations, provide the option to perform
     * a cascade delete.
     */
    void deleteSegment() {
        List<Station> stationList = new ArrayList<>(_dataMgr.getStations(_curNodeId, true));
        if (!stationList.isEmpty()) {
            // The segment still has stations.  See if any are still used by Stops
            List<Station> activeList = new ArrayList<>();
            for (Station checkActive : stationList) {
                List<Stop> stopList = new ArrayList<>(_dataMgr.getStops(0, checkActive.getStationId(), true));
                if (!stopList.isEmpty()) {
                    activeList.add(checkActive);
                }
            }
            if (!activeList.isEmpty()) {
                // Cannot delete the Segment
                StringBuilder msg = new StringBuilder(Bundle.getMessage("DeleteWarning", _curNodeType));  // NOI18N
                for (Station activeStation : activeList) {
                    msg.append("\n    " + activeStation.getStationName());  // NOI18N
                }
                JOptionPane.showMessageDialog(null,
                        msg.toString(),
                        Bundle.getMessage("WarningTitle"),  // NOI18N
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Present the option to delete the stations and the segment
            Object[] options = {Bundle.getMessage("ButtonNo"), Bundle.getMessage("ButtonYes")};  // NOI18N
            int selectedOption = JOptionPane.showOptionDialog(null,
                    Bundle.getMessage("SegmentCascade"), // NOI18N
                    Bundle.getMessage("QuestionTitle"),   // NOI18N
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            if (selectedOption == 0) {
                return;
            }
            for (Station delStation : stationList) {
                _dataMgr.deleteStation(delStation.getStationId());
            }
        }
        // delete the segment
        _dataMgr.deleteSegment(_curNodeId);
        setShowReminder(true);

        // Update the tree
        TreePath parentPath = _curTreePath.getParentPath();
        TimeTableTreeNode parentNode = (TimeTableTreeNode) _curNode.getParent();
        _curNode.removeFromParent();
        _curNode = null;
        _timetableModel.nodeStructureChanged(parentNode);
        _timetableTree.setSelectionPath(parentPath);
    }

    /**
     * Delete a Station after checking for usage.
     */
    void deleteStation() {
        // Check stop references
        List<String> stopReference = new ArrayList<>();
        for (Stop stop : _dataMgr.getStops(0, _curNodeId, true)) {
            Train train = _dataMgr.getTrain(stop.getTrainId());
            String trainSeq = String.format("%s : %d", train.getTrainName(), stop.getSeq());  // NOI18N
            stopReference.add(trainSeq);
        }
        if (!stopReference.isEmpty()) {
            StringBuilder msg = new StringBuilder(Bundle.getMessage("DeleteWarning", _curNodeType));  // NOI18N
            for (String stopTrainSeq : stopReference) {
                msg.append("\n    " + stopTrainSeq);  // NOI18N
            }
            JOptionPane.showMessageDialog(null,
                    msg.toString(),
                    Bundle.getMessage("WarningTitle"),  // NOI18N
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        _dataMgr.deleteStation(_curNodeId);
        setShowReminder(true);

        // Update the tree
        TreePath parentPath = _curTreePath.getParentPath();
        TimeTableTreeNode parentNode = (TimeTableTreeNode) _curNode.getParent();
        parentNode.remove(_curNode);
        _timetableModel.nodeStructureChanged(parentNode);
        _curNode = null;
        _timetableTree.setSelectionPath(parentPath);
    }

    /**
     * Delete a Schedule.
     * If the schedule contains trains, provide the option to perform
     * a cascade delete of trains and their stops.
     */
    void deleteSchedule() {
        List<Train> trainList = new ArrayList<>(_dataMgr.getTrains(_curNodeId, 0, true));
        if (!trainList.isEmpty()) {
            // The schedule still has trains.
            // Present the option to delete the stops, trains and the schedule
            Object[] options = {Bundle.getMessage("ButtonNo"), Bundle.getMessage("ButtonYes")};  // NOI18N
            int selectedOption = JOptionPane.showOptionDialog(null,
                    Bundle.getMessage("ScheduleCascade"), // NOI18N
                    Bundle.getMessage("QuestionTitle"),   // NOI18N
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            if (selectedOption == 0) {
                return;
            }
            for (Train train : trainList) {
                for (Stop stop : _dataMgr.getStops(train.getTrainId(), 0, false)) {
                    _dataMgr.deleteStop(stop.getStopId());
                }
                _dataMgr.deleteTrain(train.getTrainId());
            }
        }
        // delete the schedule
        _dataMgr.deleteSchedule(_curNodeId);
        setShowReminder(true);

        // Update the tree
        TreePath parentPath = _curTreePath.getParentPath();
        TimeTableTreeNode parentNode = (TimeTableTreeNode) _curNode.getParent();
        _curNode.removeFromParent();
        _curNode = null;
        _timetableModel.nodeStructureChanged(parentNode);
        _timetableTree.setSelectionPath(parentPath);
    }

    /**
     * Delete a Train.
     * If the train contains stops, provide the option to perform
     * a cascade delete of the stops.
     */
    void deleteTrain() {
        List<Stop> stopList = new ArrayList<>(_dataMgr.getStops(_curNodeId, 0, true));
        if (!stopList.isEmpty()) {
            // The trains still has stops.
            // Present the option to delete the stops and the train
            Object[] options = {Bundle.getMessage("ButtonNo"), Bundle.getMessage("ButtonYes")};  // NOI18N
            int selectedOption = JOptionPane.showOptionDialog(null,
                    Bundle.getMessage("TrainCascade"), // NOI18N
                    Bundle.getMessage("QuestionTitle"),   // NOI18N
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            if (selectedOption == 0) {
                return;
            }
            for (Stop stop : stopList) {
                _dataMgr.deleteStop(stop.getStopId());
            }
        }
        // delete the train
        _dataMgr.deleteTrain(_curNodeId);
        setShowReminder(true);

        // Update the tree
        TreePath parentPath = _curTreePath.getParentPath();
        TimeTableTreeNode parentNode = (TimeTableTreeNode) _curNode.getParent();
        _curNode.removeFromParent();
        _curNode = null;
        _timetableModel.nodeStructureChanged(parentNode);
        _timetableTree.setSelectionPath(parentPath);
    }

    /**
     * Delete a Stop.
     */
    void deleteStop() {
        // delete the stop
        _dataMgr.deleteStop(_curNodeId);
        setShowReminder(true);

        // Update the tree
        TreePath parentPath = _curTreePath.getParentPath();
        TimeTableTreeNode parentNode = (TimeTableTreeNode) _curNode.getParent();
        _curNode.removeFromParent();
        _curNode = null;
        _timetableModel.nodeStructureChanged(parentNode);
        _timetableTree.setSelectionPath(parentPath);
    }

    /**
     * Cancel the current node edit.
     */
    void cancelPressed() {
        setEditMode(false);
        _timetableTree.setSelectionPath(_curTreePath);
        _timetableTree.grabFocus();
    }

    /**
     * Move a Stop row up 1 row.
     */
    void upPressed() {
        setShowReminder(true);

        DefaultMutableTreeNode prevNode = _curNode.getPreviousSibling();
        if (prevNode == null || !(prevNode instanceof TimeTableTreeNode)) {
            log.warn("At first node, cannot move up");  // NOI18N
            return;
        }
        int prevStopId = ((TimeTableTreeNode) prevNode).getId();
        Stop prevStop = _dataMgr.getStop(prevStopId);
        prevStop.setSeq(prevStop.getSeq() + 1);
        Stop currStop = _dataMgr.getStop(_curNodeId);
        currStop.setSeq(currStop.getSeq() - 1);
        moveTreeNode("Up");     // NOI18N
    }

    /**
     * Move a Stop row down 1 row.
     */
    void downPressed() {
        setShowReminder(true);

        DefaultMutableTreeNode nextNode = _curNode.getNextSibling();
        if (nextNode == null || !(nextNode instanceof TimeTableTreeNode)) {
            log.warn("At last node, cannot move down");  // NOI18N
            return;
        }
        int nextStopId = ((TimeTableTreeNode) nextNode).getId();
        Stop nextStop = _dataMgr.getStop(nextStopId);
        nextStop.setSeq(nextStop.getSeq() - 1);
        Stop currStop = _dataMgr.getStop(_curNodeId);
        currStop.setSeq(currStop.getSeq() + 1);
        moveTreeNode("Down");     // NOI18N
    }

    /**
     * Move a tree node in response to a up or down request.
     *
     * @param direction The direction of movement, Up or Down
     */
    void moveTreeNode(String direction) {
        // Update the node
        if (direction.equals("Up")) {    // NOI18N
            _curNodeRow -= 1;
        } else {
            _curNodeRow += 1;
        }
        _curNode.setRow(_curNodeRow);
        _timetableModel.nodeChanged(_curNode);

        // Update the sibling
        DefaultMutableTreeNode siblingNode;
        TimeTableTreeNode tempNode;
        if (direction.equals("Up")) {    // NOI18N
            siblingNode = _curNode.getPreviousSibling();
            if (siblingNode instanceof TimeTableTreeNode) {
                tempNode = (TimeTableTreeNode) siblingNode;
                tempNode.setRow(tempNode.getRow() + 1);
            }
        } else {
            siblingNode = _curNode.getNextSibling();
            if (siblingNode instanceof TimeTableTreeNode) {
                tempNode = (TimeTableTreeNode) siblingNode;
                tempNode.setRow(tempNode.getRow() - 1);
            }
        }
        _timetableModel.nodeChanged(siblingNode);

        // Update the tree
        TimeTableTreeNode parentNode = (TimeTableTreeNode) _curNode.getParent();
        parentNode.insert(_curNode, _curNodeRow - 1);
        _timetableModel.nodeStructureChanged(parentNode);
        _timetableTree.setSelectionPath(new TreePath(_curNode.getPath()));
        setMoveButtons();

        // Update times
        _dataMgr.calculateTrain(_dataMgr.getStop(_curNodeId).getTrainId(), true);
    }

    /**
     * Enable/Disable the Up and Down buttons based on the postion in the list.
     */
    void setMoveButtons() {
        if (_curNode == null) {
            return;
        }

        Component[] compList = _moveButtonPanel.getComponents();
        JButton up = (JButton) compList[1];
        JButton down = (JButton) compList[3];

        up.setEnabled(true);
        down.setEnabled(true);

        int rows = _curNode.getSiblingCount();
        if (_curNodeRow < 2) {
            up.setEnabled(false);
        }
        if (_curNodeRow > rows - 1) {
            down.setEnabled(false);
        }

        // Disable move buttons during Variable or Action add or edit processing, or nothing selected
        if (_editActive) {
            up.setEnabled(false);
            down.setEnabled(false);
        }

        _moveButtonPanel.setVisible(true);
    }

    void graphPressed(String graphType) {

        // select a schedule if necessary
        Segment segment = _dataMgr.getSegment(_curNodeId);
        Layout layout = _dataMgr.getLayout(segment.getLayoutId());
        int scheduleId;
        List<Schedule> schedules = _dataMgr.getSchedules(layout.getLayoutId(), true);

        if (schedules.size() == 0) {
            log.warn("no schedule");  // NOI18N
            return;
        } else {
            scheduleId = schedules.get(0).getScheduleId();
            if (schedules.size() > 1) {
                // do selection dialog
                Schedule[] schedArr = new Schedule[schedules.size()];
                schedArr = schedules.toArray(schedArr);
                Schedule schedSelected = (Schedule) JOptionPane.showInputDialog(
                        null,
                        Bundle.getMessage("GraphScheduleMessage"),  // NOI18N
                        Bundle.getMessage("QuestionTitle"),  // NOI18N
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        schedArr,
                        schedArr[0]
                );
                if (schedSelected == null) {
                    log.warn("Schedule not selected, graph request cancelled");  // NOI18N
                    return;
                }
                scheduleId = schedSelected.getScheduleId();
            }
        }

        if (graphType.equals("Display")) {
            TimeTableDisplayGraph graph = new TimeTableDisplayGraph(_curNodeId, scheduleId, _showTrainTimes);

            JmriJFrame f = new JmriJFrame(Bundle.getMessage("TitleTimeTableGraph"), true, true);  // NOI18N
            f.setMinimumSize(new Dimension(600, 300));
            f.getContentPane().add(graph);
            f.pack();
            f.addHelpMenu("html.tools.TimeTable", true);  // NOI18N
            f.setVisible(true);
        }

        if (graphType.equals("Print")) {
            TimeTablePrintGraph print = new TimeTablePrintGraph(_curNodeId, scheduleId, _showTrainTimes, _twoPage);
            print.printGraph();
        }
    }

    JFileChooser fileChooser;
    void importPressed() {
        fileChooser = jmri.jmrit.XmlFile.userFileChooser("SchedGen File", "sgn");  // NOI18N
        int retVal = fileChooser.showOpenDialog(null);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                new TimeTableImport().importSgn(_dataMgr, file);
            } catch (IOException ex) {
                log.error("Import exception: {}", ex);  // NOI18N
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("ImportFailed", "SGN"),  // NOI18N
                        Bundle.getMessage("ErrorTitle"),  // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            savePressed();
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("ImportCompleted", "SGN"),  // NOI18N
                    Bundle.getMessage("MessageTitle"),  // NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    List<String> feedbackList;
    void importCsvPressed() {
        fileChooser = new JFileChooser(jmri.util.FileUtil.getUserFilesPath());
        fileChooser.setFileFilter(new FileNameExtensionFilter("Import File", "csv"));
        int retVal = fileChooser.showOpenDialog(null);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            completeImport(file);
        }
    }
        
    void completeImport(File file) {
        try {
            feedbackList = new TimeTableCsvImport().importCsv(file);
        } catch (IOException ex) {
            log.error("Import exception: {}", ex); // NOI18N
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("ImportCsvFailed", "CVS"), // NOI18N
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (feedbackList.size() > 0) {
            StringBuilder msg = new StringBuilder(Bundle.getMessage("ImportCsvErrors")); // NOI18N
            for (String feedback : feedbackList) {
                msg.append(feedback + "\n");
            }
            JOptionPane.showMessageDialog(null,
                    msg.toString(),
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        savePressed();
        JOptionPane.showMessageDialog(null,
                Bundle.getMessage("ImportCompleted", "CSV"), // NOI18N
                Bundle.getMessage("MessageTitle"), // NOI18N
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    void importFromOperationsPressed() {
        ExportTimetable ex = new ExportTimetable();
        new ExportTimetable().writeOperationsTimetableFile();
        completeImport(ex.getExportFile());
    }

    void exportCsvPressed() {
        // Select layout
        List<Layout> layouts = _dataMgr.getLayouts(true);
        if (layouts.size() == 0) {
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("ExportLayoutError"),  // NOI18N
                    Bundle.getMessage("ErrorTitle"),  // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int layoutId = layouts.get(0).getLayoutId();
        if (layouts.size() > 1) {
            Layout layout = (Layout) JOptionPane.showInputDialog(
                    null,
                    Bundle.getMessage("ExportSelectLayout"),  // NOI18N
                    Bundle.getMessage("QuestionTitle"),  // NOI18N
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    layouts.toArray(),
                    null);
            if (layout == null) return;
            layoutId = layout.getLayoutId();
        }

        // Select segment
        List<Segment> segments = _dataMgr.getSegments(layoutId, true);
        if (segments.size() == 0) {
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("ExportSegmentError"),  // NOI18N
                    Bundle.getMessage("ErrorTitle"),  // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int segmentId = segments.get(0).getSegmentId();
        if (segments.size() > 1) {
            Segment segment = (Segment) JOptionPane.showInputDialog(
                    null,
                    Bundle.getMessage("ExportSelectSegment"),  // NOI18N
                    Bundle.getMessage("QuestionTitle"),  // NOI18N
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    segments.toArray(),
                    null);
            if (segment == null) return;
            segmentId = segment.getSegmentId();
        }

        // Select schedule
        List<Schedule> schedules = _dataMgr.getSchedules(layoutId, true);
        if (schedules.size() == 0) {
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("ExportScheduleError"),  // NOI18N
                    Bundle.getMessage("ErrorTitle"),  // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int scheduleId = schedules.get(0).getScheduleId();
        if (schedules.size() > 1) {
            Schedule schedule = (Schedule) JOptionPane.showInputDialog(
                    null,
                    Bundle.getMessage("ExportSelectSchedule"),  // NOI18N
                    Bundle.getMessage("QuestionTitle"),  // NOI18N
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    schedules.toArray(),
                    null);
            if (schedule == null) return;
            scheduleId = schedule.getScheduleId();
        }

        fileChooser = new JFileChooser(jmri.util.FileUtil.getUserFilesPath());
        fileChooser.setFileFilter(new FileNameExtensionFilter("Export as CSV File", "csv"));  // NOI18N
        int retVal = fileChooser.showSaveDialog(null);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileName = file.getAbsolutePath();
            String fileNameLC = fileName.toLowerCase();
            if (!fileNameLC.endsWith(".csv")) {  // NOI18N
                fileName = fileName + ".csv";  // NOI18N
                file = new File(fileName);
            }
            if (file.exists()) {
                if (JOptionPane.showConfirmDialog(null,
                        Bundle.getMessage("FileOverwriteWarning", file.getName()),  // NOI18N
                        Bundle.getMessage("QuestionTitle"),  // NOI18N
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) {
                    return;
                }
            }


            boolean hasErrors;
            try {
                hasErrors = new TimeTableCsvExport().exportCsv(file, layoutId, segmentId, scheduleId);
            } catch (IOException ex) {
                log.error("Export exception: {}", ex);  // NOI18N
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("ExportFailed"),  // NOI18N
                        Bundle.getMessage("ErrorTitle"),  // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return;
            }




            if (hasErrors) {
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("ExportFailed"),  // NOI18N
                        Bundle.getMessage("ErrorTitle"),  // NOI18N
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("ExportCompleted", file),  // NOI18N
                        Bundle.getMessage("MessageTitle"),  // NOI18N
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Save the current set of timetable data.
     */
    void savePressed() {
        TimeTableXml.doStore();
        setShowReminder(false);
    }

    /**
     * Check for pending updates and close if none or approved.
     */
    void donePressed() {
        if (_isDirty) {
            Object[] options = {Bundle.getMessage("ButtonNo"), Bundle.getMessage("ButtonYes")};  // NOI18N
            int selectedOption = JOptionPane.showOptionDialog(null,
                    Bundle.getMessage("DirtyDataWarning"), // NOI18N
                    Bundle.getMessage("WarningTitle"),   // NOI18N
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            if (selectedOption == 0) {
                return;
            }
        }
        InstanceManager.reset(TimeTableFrame.class);
        dispose();
    }

    // ------------  Tree Content and Navigation ------------

    /**
     * Create the TimeTable tree structure.
     *
     * @return _timetableTree The tree ddefinition with its content
     */
    JTree buildTree() {
        _timetableRoot = new DefaultMutableTreeNode("Root Node");      // NOI18N
        _timetableModel = new DefaultTreeModel(_timetableRoot);
        _timetableTree = new JTree(_timetableModel);

        createTimeTableContent();

        // build the tree GUI
        _timetableTree.expandPath(new TreePath(_timetableRoot));
        _timetableTree.setRootVisible(false);
        _timetableTree.setShowsRootHandles(true);
        _timetableTree.setScrollsOnExpand(true);
        _timetableTree.setExpandsSelectedPaths(true);
        _timetableTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);

        // tree listeners
        _timetableTree.addTreeSelectionListener(_timetableListener = new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (_editActive) {
                    if (e.getNewLeadSelectionPath() != _curTreePath) {
                        _timetableTree.setSelectionPath(e.getOldLeadSelectionPath());
                        showNodeEditMessage();
                    }
                    return;
                }

                _curTreePath = _timetableTree.getSelectionPath();
                if (_curTreePath != null) {
                    Object chkLast = _curTreePath.getLastPathComponent();
                    if (chkLast instanceof TimeTableTreeNode) {
                        treeRowSelected((TimeTableTreeNode) chkLast);
                    }
                }
            }
        });

        return _timetableTree;
    }

    /**
     * Create the tree content.
     * Level 1 -- Layouts
     * Level 2 -- Train Type, Segment and Schedule Containers
     * Level 3 -- Train Types, Segments, Schedules
     * Level 4 -- Stations, Trains
     * Level 5 -- Stops
     */
    void createTimeTableContent() {
        for (Layout l : _dataMgr.getLayouts(true)) {
            _layoutNode = new TimeTableTreeNode(l.getLayoutName(), "Layout", l.getLayoutId(), 0);    // NOI18N
            _timetableRoot.add(_layoutNode);

            _typeHead = new TimeTableTreeNode(buildNodeText("TrainTypes", null, 0), "TrainTypes", 0, 0);    // NOI18N
            _layoutNode.add(_typeHead);
            for (TrainType y : _dataMgr.getTrainTypes(l.getLayoutId(), true)) {
                _typeNode = new TimeTableTreeNode(y.getTypeName(), "TrainType", y.getTypeId(), 0);    // NOI18N
                _typeHead.add(_typeNode);
            }

            _segmentHead = new TimeTableTreeNode(buildNodeText("Segments", null, 0), "Segments", 0, 0);    // NOI18N
            _layoutNode.add(_segmentHead);
            for (Segment sg : _dataMgr.getSegments(l.getLayoutId(), true)) {
                _segmentNode = new TimeTableTreeNode(sg.getSegmentName(), "Segment", sg.getSegmentId(), 0);    // NOI18N
                _segmentHead.add(_segmentNode);
                for (Station st : _dataMgr.getStations(sg.getSegmentId(), true)) {
                    _leafNode = new TimeTableTreeNode(st.getStationName(), "Station", st.getStationId(), 0);    // NOI18N
                    _segmentNode.add(_leafNode);
                }
            }

            _scheduleHead = new TimeTableTreeNode(buildNodeText("Schedules", null, 0), "Schedules", 0, 0);    // NOI18N
            _layoutNode.add(_scheduleHead);
            for (Schedule c : _dataMgr.getSchedules(l.getLayoutId(), true)) {
                _scheduleNode = new TimeTableTreeNode(buildNodeText("Schedule", c, 0), "Schedule", c.getScheduleId(), 0);    // NOI18N
                _scheduleHead.add(_scheduleNode);
                for (Train tr : _dataMgr.getTrains(c.getScheduleId(), 0, true)) {
                    _trainNode = new TimeTableTreeNode(buildNodeText("Train", tr, 0), "Train", tr.getTrainId(), 0);    // NOI18N
                    _scheduleNode.add(_trainNode);
                    for (Stop sp : _dataMgr.getStops(tr.getTrainId(), 0, true)) {
                        _leafNode = new TimeTableTreeNode(buildNodeText("Stop", sp, 0), "Stop", sp.getStopId(), sp.getSeq());    // NOI18N
                        _trainNode.add(_leafNode);
                    }
                }
            }
        }
    }

    /**
     * Create the localized node text display strings based on node type.
     *
     * @param nodeType  The type of the node
     * @param component The object or child object
     * @param idx       Optional index value
     * @return nodeText containing the text to display on the node
     */
    String buildNodeText(String nodeType, Object component, int idx) {
        switch (nodeType) {
            case "TrainTypes":
                return Bundle.getMessage("LabelTrainTypes");  // NOI18N
            case "Segments":
                return Bundle.getMessage("LabelSegments");  // NOI18N
            case "Schedules":
                return Bundle.getMessage("LabelSchedules");  // NOI18N
            case "Schedule":
                Schedule schedule = (Schedule) component;
                return Bundle.getMessage("LabelSchedule", schedule.getScheduleName(), schedule.getEffDate());  // NOI18N
            case "Train":
                Train train = (Train) component;
                return Bundle.getMessage("LabelTrain", train.getTrainName(), train.getTrainDesc());  // NOI18N
            case "Stop":
                Stop stop = (Stop) component;
                int stationId = stop.getStationId();
                return Bundle.getMessage("LabelStop", stop.getSeq(), _dataMgr.getStation(stationId).getStationName());  // NOI18N
            default:
                return "None";  // NOI18N
        }
    }

    /**
     * Change the button row based on the currently selected node type. Invoke
     * edit where appropriate.
     *
     * @param selectedNode The node object
     */
    void treeRowSelected(TimeTableTreeNode selectedNode) {
        // Set the current node variables
        _curNode = selectedNode;
        _curNodeId = selectedNode.getId();
        _curNodeType = selectedNode.getType();
        _curNodeText = selectedNode.getText();
        _curNodeRow = selectedNode.getRow();

        // Reset button bar
        _addButtonPanel.setVisible(false);
        _deleteButtonPanel.setVisible(false);
        _moveButtonPanel.setVisible(false);
        _graphButtonPanel.setVisible(false);

        switch (_curNodeType) {
            case "Layout":     // NOI18N
                _addButton.setText(Bundle.getMessage("AddLayoutButtonText"));  // NOI18N
                _addButtonPanel.setVisible(true);
                _deleteButton.setText(Bundle.getMessage("DeleteLayoutButtonText"));  // NOI18N
                _deleteButtonPanel.setVisible(true);
                editPressed();
                break;

            case "TrainTypes":     // NOI18N
                _addButton.setText(Bundle.getMessage("AddTrainTypeButtonText"));  // NOI18N
                _addButtonPanel.setVisible(true);
                makeDetailGrid("EmptyGrid");  // NOI18N
                break;

            case "TrainType":     // NOI18N
                _deleteButton.setText(Bundle.getMessage("DeleteTrainTypeButtonText"));  // NOI18N
                _deleteButtonPanel.setVisible(true);
                editPressed();
                break;

            case "Segments":     // NOI18N
                _addButton.setText(Bundle.getMessage("AddSegmentButtonText"));  // NOI18N
                _addButtonPanel.setVisible(true);
                makeDetailGrid("EmptyGrid");  // NOI18N
                break;

            case "Segment":     // NOI18N
                _addButton.setText(Bundle.getMessage("AddStationButtonText"));  // NOI18N
                _addButtonPanel.setVisible(true);
                _deleteButton.setText(Bundle.getMessage("DeleteSegmentButtonText"));  // NOI18N
                _deleteButtonPanel.setVisible(true);
                _graphButtonPanel.setVisible(true);
                editPressed();
                break;

            case "Station":     // NOI18N
                _deleteButton.setText(Bundle.getMessage("DeleteStationButtonText"));  // NOI18N
                _deleteButtonPanel.setVisible(true);
                editPressed();
                break;

            case "Schedules":     // NOI18N
                _addButton.setText(Bundle.getMessage("AddScheduleButtonText"));  // NOI18N
                _addButtonPanel.setVisible(true);
                makeDetailGrid("EmptyGrid");  // NOI18N
                break;

            case "Schedule":     // NOI18N
                _addButton.setText(Bundle.getMessage("AddTrainButtonText"));  // NOI18N
                _addButtonPanel.setVisible(true);
                _deleteButton.setText(Bundle.getMessage("DeleteScheduleButtonText"));  // NOI18N
                _deleteButtonPanel.setVisible(true);
                editPressed();
                break;

            case "Train":     // NOI18N
                _addButton.setText(Bundle.getMessage("AddStopButtonText"));  // NOI18N
                _addButtonPanel.setVisible(true);
                _deleteButton.setText(Bundle.getMessage("DeleteTrainButtonText"));  // NOI18N
                _deleteButtonPanel.setVisible(true);
                editPressed();
                break;

            case "Stop":     // NOI18N
                _deleteButton.setText(Bundle.getMessage("DeleteStopButtonText"));  // NOI18N
                _deleteButtonPanel.setVisible(true);
                editPressed();
                break;

            default:
                log.warn("Should not be here");  // NOI18N
        }
    }

    /**
     * Display reminder to save.
     */
    void showNodeEditMessage() {
        if (InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class) != null) {
            InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showInfoMessage(Bundle.getMessage("NodeEditTitle"), // NOI18N
                            Bundle.getMessage("NodeEditText"), // NOI18N
                            getClassName(),
                            "SkipTimeTableEditMessage"); // NOI18N
        }
    }

    /**
     * Set/clear dirty flag and save button
     * @param dirty True if changes have been made that are not saved.
     */
    public void setShowReminder(boolean dirty) {
        _isDirty = dirty;
        _saveButton.setEnabled(dirty);
    }

    /**
     * Enable/disable buttons based on edit state.
     * The edit state controls the ability to select tree nodes.
     *
     * @param active True to make edit active, false to make edit inactive
     */
    void setEditMode(boolean active) {
        _editActive = active;
        _cancelAction.setEnabled(active);
        _updateAction.setEnabled(active);
        _addButton.setEnabled(!active);
        _deleteButton.setEnabled(!active);
        if (_curNodeType != null && _curNodeType.equals("Stop")) {  // NOI18N
            setMoveButtons();
        }
    }

    /**
     * Timetable Tree Node Definition.
     */
    static class TimeTableTreeNode extends DefaultMutableTreeNode {

        private String ttText;
        private String ttType;
        private int ttId;
        private int ttRow;

        public TimeTableTreeNode(String nameText, String type, int sysId, int row) {
            this.ttText = nameText;
            this.ttType = type;
            this.ttId = sysId;
            this.ttRow = row;
        }

        public String getType() {
            return ttType;
        }

        public int getId() {
            return ttId;
        }

        public void setId(int newId) {
            ttId = newId;
        }

        public int getRow() {
            return ttRow;
        }

        public void setRow(int newRow) {
            ttRow = newRow;
        }

        public String getText() {
            return ttText;
        }

        public void setText(String newText) {
            ttText = newText;
        }

        @Override
        public String toString() {
            return ttText;
        }
    }

    protected String getClassName() {
        return TimeTableFrame.class.getName();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableFrame.class);
}
