package jmri.jmrit.logix;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WarrantFame creates and edits Warrants
 * <P>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * This class is a window for creating and editing Warrants.
 * <p>
 * @author  Pete Cressman Copyright (C) 2009, 2010
 */
public class WarrantFrame extends WarrantRoute {

    static int ROW_HEIGHT;

    private Warrant _warrant;
    private Warrant _saveWarrant;
    private ThrottleTableModel _commandModel;
    private JTable _commandTable;
    private JScrollPane _throttlePane;

    private ArrayList<ThrottleSetting> _throttleCommands = new ArrayList<ThrottleSetting>();
    private long _startTime;
    private long _TTP = 0;
    private boolean _forward = true;
    LearnThrottleFrame _learnThrottle = null;       // need access for JUnit test
    static Color myGreen = new Color(0, 100, 0);

    JTextField _sysNameBox;
    JTextField _userNameBox;
    JButton _calculateButton;
    JButton _stopButton;
    JButton _startButton;

    JTabbedPane _tabbedPane;
    JPanel _routePanel;
    JPanel _commandPanel;
    JRadioButton _isSCWarrant = new JRadioButton(Bundle.getMessage("SignalControlled"), false);
    JRadioButton _isWarrant = new JRadioButton(Bundle.getMessage("NormalWarrant"), true);
    JCheckBox    _runForward = new JCheckBox(Bundle.getMessage("Forward"));
    JFormattedTextField _TTPtextField = new JFormattedTextField();
    JRadioButton _runProtect = new JRadioButton(Bundle.getMessage("RunProtected"), true);
    JRadioButton _runBlind = new JRadioButton(Bundle.getMessage("RunBlind"), false);
    JRadioButton _halt = new JRadioButton(Bundle.getMessage("Halt"), false);
    JRadioButton _resume = new JRadioButton(Bundle.getMessage("Resume"), false);
    JRadioButton _abort = new JRadioButton(Bundle.getMessage("Abort"), false);
    JRadioButton _invisible = new JRadioButton();
    JTextField _statusBox = new JTextField(90);

    JTextField _searchStatus = new JTextField();

    /**
     * Constructor for opening an existing warrant for editing
     */
    protected WarrantFrame(Warrant w) {
        super();
        _saveWarrant = w;
        // temp unregistered version until editing is saved.
        _warrant = new Warrant(_saveWarrant.getSystemName(), _saveWarrant.getUserName());
        setup(_saveWarrant);
        WarrantTableAction.newWarrantFrame(this);
        init();
        if (routeIsValid() != null) {
            findRoute();
        }
    }

    /**
     * Constructor for new warrant unregistered warrant (new, copy or concatenation)
     * Called by WarrantTableAction
     */
    protected WarrantFrame(Warrant warrant, boolean create) {
        super();
        // unregistered warrant
        _warrant = warrant;
        if (!create) {
            // this is a concatenation of warrants
            setup(_warrant);
        }
        init();
    }

    /**
     * Set up params from an existing warrant. note that _warrant is unregistered.
     * warrant may be registered.
     */
    private void setup(Warrant warrant) {
        _origin.setOrder(warrant.getfirstOrder());
        _destination.setOrder(warrant.getLastOrder());
        _via.setOrder(warrant.getViaOrder());
        _avoid.setOrder(warrant.getAvoidOrder());
        _TTP = 0;
        if (warrant instanceof SCWarrant) {
            _TTP = ((SCWarrant)warrant).getTimeToPlatform();
        }
        List<BlockOrder> list = warrant.getBlockOrders();
        ArrayList<BlockOrder> orders = new ArrayList<BlockOrder>(list.size());
        for (int i = 0; i < list.size(); i++) {
            orders.add(new BlockOrder(list.get(i)));
        }
        setOrders(orders);      // makes copy

        _forward = true;
        List<ThrottleSetting> tList = warrant.getThrottleCommands();
        for (int i = 0; i < tList.size(); i++) {
            ThrottleSetting ts = new ThrottleSetting(tList.get(i));
            if (ts.getCommand().toUpperCase().equals("FORWARD")) {
                _forward = ts.getValue().toUpperCase().equals("TRUE");
            }
            _throttleCommands.add(ts);
        }
        setTrainName(warrant.getTrainName());
        setTrainInfo(warrant.getTrainId());
        _warrant.setTrainName(warrant.getTrainName());
//        _warrant.setTrainId(warrant.getTrainId());
        _runBlind.setSelected(warrant.getRunBlind());
        _warrant.setRunBlind(warrant.getRunBlind());
    }

    private void init() {
        _commandModel = new ThrottleTableModel();

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));

        contentPane.add(makeTopPanel(), BorderLayout.NORTH);

        _tabbedPane = new JTabbedPane();
        _tabbedPane.addTab(Bundle.getMessage("MakeRoute"), makeFindRouteTabPanel());
        _tabbedPane.addTab(Bundle.getMessage("RecordPlay"), makeSetPowerTabPanel());
        contentPane.add(_tabbedPane, BorderLayout.CENTER);

        contentPane.add(makeEditableButtonPanel(), BorderLayout.SOUTH);
        if (getOrders().size() > 0) {
            _tabbedPane.setSelectedIndex(1);
        }
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                close();
            }
        });

        makeMenus();
        setTitle(_warrant.getDisplayName());
        setContentPane(contentPane);
        setLocation(0, 100);
        setVisible(true);
        pack();
    }

    private JPanel makeTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(Box.createHorizontalStrut(2 * STRUT_SIZE));
        panel.add(new JLabel(Bundle.getMessage("LabelSystemName")));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        _sysNameBox = new JTextField(_warrant.getSystemName());
        _sysNameBox.setBackground(Color.white);
        _sysNameBox.setEditable(false);
        panel.add(_sysNameBox);
        panel.add(Box.createHorizontalStrut(2 * STRUT_SIZE));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(new JLabel(Bundle.getMessage("LabelUserName")));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        _userNameBox = new JTextField(_warrant.getUserName());
        panel.add(_userNameBox);
        panel.add(Box.createHorizontalStrut(2 * STRUT_SIZE));
        topPanel.add(panel);
        topPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        return topPanel;
    }

    private JPanel makeFindRouteTabPanel() {
        JPanel tab1 = new JPanel();
        tab1.setLayout(new BoxLayout(tab1, BoxLayout.LINE_AXIS));
        tab1.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel topLeft = new JPanel();
        topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.PAGE_AXIS));

        topLeft.add(makeBlockPanels());

        topLeft.add(Box.createVerticalStrut(2 * STRUT_SIZE));
        tab1.add(topLeft);

        tab1.add(Box.createHorizontalStrut(STRUT_SIZE));
        JPanel topRight = new JPanel();
        topRight.setLayout(new BoxLayout(topRight, BoxLayout.LINE_AXIS));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(Box.createVerticalStrut(2 * STRUT_SIZE));
        _calculateButton = new JButton(Bundle.getMessage("Calculate"));
        _calculateButton.setMaximumSize(_calculateButton.getPreferredSize());
        _calculateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calculate();
            }
        });
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        JPanel pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(new JLabel(Bundle.getMessage("CalculateRoute")));
        p.add(pp);
        pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(_calculateButton);
        p.add(pp);
        panel.add(p);
        panel.add(Box.createVerticalStrut(2 * STRUT_SIZE));

        _stopButton = new JButton(Bundle.getMessage("Stop"));
        _stopButton.setMaximumSize(_stopButton.getPreferredSize());
        _stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopRouteFinder();
            }
        });

        int numBlocks = InstanceManager.getDefault(OBlockManager.class).getSystemNameList().size();
        if (numBlocks / 6 > getDepth()) {
            setDepth(numBlocks / 6);
        }
        panel.add(searchDepthPanel(true));

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        p.add(makeTextBoxPanel(true, _searchStatus, "SearchRoute", null));
        _searchStatus.setEditable(false);
        p.add(Box.createVerticalGlue());
        panel.add(p);

        _searchStatus.setBackground(Color.white);
        _searchStatus.setEditable(false);
        p = new JPanel();
        pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(_stopButton);
        p.add(pp, BorderLayout.SOUTH);
        panel.add(p);
        panel.add(Box.createRigidArea(new Dimension(10,
                topLeft.getPreferredSize().height - panel.getPreferredSize().height)));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(Box.createVerticalGlue());
        topRight.add(panel);
        topRight.add(Box.createHorizontalStrut(STRUT_SIZE));

        PickListModel pickListModel = PickListModel.oBlockPickModelInstance();
        topRight.add(new JScrollPane(pickListModel.makePickTable()));
        Dimension dim = topRight.getPreferredSize();
        topRight.setMinimumSize(dim);
        tab1.add(topRight);
        tab1.add(Box.createHorizontalStrut(STRUT_SIZE));
        return tab1;
    }

    private void calculate() {
        clearCommands();
        String msg = findRoute();
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private JPanel makeSetPowerTabPanel() {
        JPanel tab2 = new JPanel();
        tab2.setLayout(new BoxLayout(tab2, BoxLayout.PAGE_AXIS));
        tab2.add(makeTabMidPanel());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(makeBorderedTrainPanel());
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(makeRecordPanel());
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(makePlaybackPanel());
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        tab2.add(panel);

        panel = new JPanel();
        String status = getIdleMessage();

        panel.add(makeTextBoxPanel(false, _statusBox, "Status", null));
        _statusBox.setEditable(false);
        _statusBox.setMinimumSize(new Dimension(300, _statusBox.getPreferredSize().height));
        _statusBox.setMaximumSize(new Dimension(900, _statusBox.getPreferredSize().height));
        panel.add(_statusBox);
        setStatusText(status, Color.black);
        tab2.add(panel);

        return tab2;
    }
    private String  getIdleMessage() {
        switch (_warrant.getRunMode()) {
            case Warrant.MODE_NONE:
                if (getOrders().size() == 0) {
                    return Bundle.getMessage("BlankWarrant");
                } else if (getAddress() == null || getAddress().length() == 0) {
                    return Bundle.getMessage("NoLoco");
                }
                break;
            case Warrant.MODE_LEARN:
                return Bundle.getMessage("Learning",
                        _warrant.getCurrentBlockOrder().getBlock().getDisplayName());
            case Warrant.MODE_RUN:
            case Warrant.MODE_MANUAL:
                return _warrant.getRunningMessage();
        }        
        return Bundle.getMessage("Idle");
    }

    private JPanel makeBorderedTrainPanel() {
        JPanel trainPanel = makeTrainPanel();

        JPanel edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("SetPower"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(trainPanel);
        return edge;
    }

    private JPanel makeRecordPanel() {
        JPanel learnPanel = new JPanel();
        learnPanel.setLayout(new BoxLayout(learnPanel, BoxLayout.LINE_AXIS));
        learnPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        
        JPanel wTypePanel = new JPanel();
        wTypePanel.setLayout(new BoxLayout(wTypePanel, BoxLayout.PAGE_AXIS));
        wTypePanel.add(Box.createVerticalStrut(STRUT_SIZE));
        ButtonGroup group = new ButtonGroup();
        group.add(_isSCWarrant);
        group.add(_isWarrant);
        wTypePanel.add(_isSCWarrant);
        wTypePanel.add(_isWarrant);
        learnPanel.add(wTypePanel);
        learnPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        _isSCWarrant.setSelected(_saveWarrant instanceof SCWarrant);

        JPanel scParamPanel = new JPanel();
        scParamPanel.setLayout(new BoxLayout(scParamPanel, BoxLayout.PAGE_AXIS));
        scParamPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        scParamPanel.add(_runForward);
        _runForward.setSelected(_forward);
        JPanel panel = new JPanel();
        JLabel l = new JLabel(Bundle.getMessage(Bundle.getMessage("TTP")));
        _TTPtextField.setValue(Long.valueOf(_TTP));
        _TTPtextField.setColumns(10);
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        _TTPtextField.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(l);
        panel.add(_TTPtextField);
        panel.setToolTipText(Bundle.getMessage("TTPtoolTip"));
        scParamPanel.add(panel);
                
        JPanel startStopPanel = new JPanel();
        startStopPanel.setLayout(new BoxLayout(startStopPanel, BoxLayout.PAGE_AXIS));
        startStopPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        JButton startButton = new JButton(Bundle.getMessage("Start"));
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showCommands(true);
                runLearnModeTrain();
            }
        });
        JButton stopButton = new JButton(Bundle.getMessage("Stop"));
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopRunTrain();
            }
        });
        startButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        startStopPanel.add(startButton);
        startStopPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        startStopPanel.add(stopButton);
        startStopPanel.add(Box.createRigidArea(new Dimension(30 + stopButton.getPreferredSize().width, 10)));
        if (_isWarrant.isSelected()) {
            learnPanel.add(startStopPanel);
        } else {
            learnPanel.add(scParamPanel);
        }

        JPanel edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("LearnMode"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(learnPanel);
        
        _isSCWarrant.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                learnPanel.remove(startStopPanel);
                learnPanel.add(scParamPanel);
                learnPanel.revalidate();
                learnPanel.repaint();
            }
        });
        _isWarrant.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                learnPanel.remove(scParamPanel);
                learnPanel.add(startStopPanel);
                learnPanel.revalidate();
                learnPanel.repaint();
            }
        });
        return edge;
    }

    private JPanel makePlaybackPanel() {
        JPanel runPanel = new JPanel();
        runPanel.setLayout(new BoxLayout(runPanel, BoxLayout.LINE_AXIS));
        runPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        ButtonGroup group = new ButtonGroup();
        group.add(_runProtect);
        group.add(_runBlind);
        panel.add(_runProtect);
        panel.add(_runBlind);
        runPanel.add(panel);
        runPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        _runBlind.setSelected(_warrant.getRunBlind());

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        //panel.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel bPanel = new JPanel();
        bPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton runButton = new JButton(Bundle.getMessage("ARun"));
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runTrain();
            }
        });
        bPanel.add(runButton);
        panel.add(bPanel);
        runPanel.add(panel);
        runPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        group = new ButtonGroup();
        group.add(_halt);
        group.add(_resume);
        group.add(_abort);
        group.add(_invisible);
        panel.add(_halt);
        panel.add(_resume);
        panel.add(_abort);
        runPanel.add(panel);

        _halt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doControlCommand(Warrant.HALT);
            }
        });
        _resume.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doControlCommand(Warrant.RESUME);
            }
        });
        _abort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doControlCommand(Warrant.ABORT);
            }
        });
        runPanel.add(panel);
        JPanel edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("RunTrain"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(runPanel);
        return edge;
    }

    private JPanel makeTabMidPanel() {
        JPanel midPanel = new JPanel();
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.PAGE_AXIS));

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.LINE_AXIS));
        _routePanel = makeRouteTablePanel();
        tablePanel.add(_routePanel);
        tablePanel.add(Box.createHorizontalStrut(5));
        tablePanel.add(makeThrottleTablePanel());
        boolean show = (_throttleCommands.size() > 0);
        showCommands(show);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        ButtonGroup group = new ButtonGroup();
        JRadioButton showRoute = new JRadioButton(Bundle.getMessage("showRoute"), !show);
        JRadioButton showScript = new JRadioButton(Bundle.getMessage("showScript"), show);
        group.add(showRoute);
        group.add(showScript);
        buttonPanel.add(showRoute);
        buttonPanel.add(showScript);
        showRoute.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showCommands(false);
            }
        });
        showScript.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showCommands(true);
            }
        });
        midPanel.add(buttonPanel);
        midPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        midPanel.add(tablePanel);
        midPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        return midPanel;
    }

    private void showCommands(boolean setCmds) {
        _routePanel.setVisible(!setCmds);
        _commandPanel.setVisible(setCmds);
    }

    private JPanel makeThrottleTablePanel() {
        _commandTable = new JTable(_commandModel);
        _commandTable.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
        for (int i = 0; i < _commandModel.getColumnCount(); i++) {
            int width = _commandModel.getPreferredWidth(i);
            _commandTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        _throttlePane = new JScrollPane(_commandTable);
        ROW_HEIGHT = _commandTable.getRowHeight();
        Dimension dim = _commandTable.getPreferredSize();
        dim.height = ROW_HEIGHT * 8;
        _throttlePane.getViewport().setPreferredSize(dim);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        buttonPanel.add(Box.createVerticalStrut(3 * STRUT_SIZE));

        JButton insertButton = new JButton(Bundle.getMessage("buttonInsertRow"));
        insertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertRow();
            }
        });
        buttonPanel.add(insertButton);
        buttonPanel.add(Box.createVerticalStrut(2 * STRUT_SIZE));

        JButton deleteButton = new JButton(Bundle.getMessage("buttonDeleteRow"));
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteRow();
            }
        });
        buttonPanel.add(deleteButton);
        //buttonPanel.add(Box.createVerticalStrut(3*STRUT_SIZE));

        _commandPanel = new JPanel();
        _commandPanel.setLayout(new BoxLayout(_commandPanel, BoxLayout.PAGE_AXIS));
        JLabel title = new JLabel(Bundle.getMessage("CommandTableTitle"));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        JPanel p = new JPanel();
        p.add(_throttlePane);
        panel.add(p);
        buttonPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(buttonPanel);
        buttonPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        _commandPanel.add(title);
        _commandPanel.add(panel);
        _commandPanel.add(Box.createGlue());
        return _commandPanel;
    }

    private void insertRow() {
        int row = _commandTable.getSelectedRow();
        if (row < 0) {
            showWarning(Bundle.getMessage("selectRow"));
            return;
        }
        _throttleCommands.add(row+1, new ThrottleSetting(0, null, null, null));
        _commandModel.fireTableDataChanged();
        _commandTable.setRowSelectionInterval(row, row);
    }

    private void deleteRow() {
        int row = _commandTable.getSelectedRow();
        if (row < 0) {
            showWarning(Bundle.getMessage("selectRow"));
            return;
        }
        ThrottleSetting cmd = _throttleCommands.get(row);
        if (cmd != null) {
            String c = cmd.getCommand();
            if (c != null && c.trim().toUpperCase().equals("NOOP")) {
                showWarning(Bundle.getMessage("cannotDeleteNoop"));
                return;
            }
            long time = cmd.getTime();
            if ((row + 1) < _throttleCommands.size()) {
                time += _throttleCommands.get(row + 1).getTime();
                _throttleCommands.get(row + 1).setTime(time);
            }
        }
        _throttleCommands.remove(row);
        _commandModel.fireTableDataChanged();
    }

    /**
     * Save, Cancel, Delete buttons
     */
    private JPanel makeEditableButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalStrut(10 * STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (save()) {
                    close();                   
                }
            }
        });
        panel.add(saveButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        buttonPanel.add(panel);
        buttonPanel.add(Box.createHorizontalStrut(3 * STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JButton copyButton = new JButton(Bundle.getMessage("ButtonCopy"));
        copyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copy();
            }
        });
        panel.add(copyButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        buttonPanel.add(panel);
        buttonPanel.add(Box.createHorizontalStrut(3 * STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        panel.add(cancelButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        buttonPanel.add(panel);
        buttonPanel.add(Box.createHorizontalStrut(3 * STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InstanceManager.getDefault(WarrantManager.class).deregister(_warrant);
                _warrant.dispose();
                WarrantTableAction.updateWarrantMenu();
                close();
            }
        });
        panel.add(deleteButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        buttonPanel.add(panel);
        buttonPanel.add(Box.createHorizontalGlue());

        return buttonPanel;
    }

    private void doControlCommand(int cmd) {
        if (log.isDebugEnabled()) {
            log.debug("actionPerformed on doControlCommand  cmd= " + cmd);
        }
        int runMode = _warrant.getRunMode();
        if (runMode == Warrant.MODE_NONE) {
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("NotRunning", _warrant.getDisplayName()),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        } else if (runMode == Warrant.MODE_LEARN && cmd != Warrant.ABORT) {
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("LearnInvalidControl", _warrant.getDisplayName()),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        } else {
            _warrant.controlRunTrain(cmd);
        }
        _invisible.setSelected(true);
    }

    private void makeMenus() {
        setTitle(Bundle.getMessage("TitleWarrant", _warrant.getDisplayName()));
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        fileMenu.add(new jmri.configurexml.SaveMenu());
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.logix.CreateEditWarrant", true);
    }

    private void clearCommands() {
        _throttleCommands = new ArrayList<ThrottleSetting>();
        _commandModel.fireTableDataChanged();
        _searchStatus.setText("");
    }

    @Override
    public void selectedRoute(ArrayList<BlockOrder> orders) {
        _tabbedPane.setSelectedIndex(1);
    }

    /**
     * all non-null returns are fatal
     *
     * @return
     */
    private String checkTrainId() {
        String msg = null;
        if (_warrant.getRunMode() != Warrant.MODE_NONE) {
            msg = _warrant.getRunModeMessage();
            return msg;
        }
        List<BlockOrder> orders = getOrders();
        if (orders.size() == 0) {
            msg = Bundle.getMessage("NoRouteSet", _origin.getBlockName(), _destination.getBlockName());
            return msg;
        }
        msg = _warrant.setRoute(0, orders);     // calls allocateRoute
        if (msg != null) {
            return msg;
        }
        msg = checkLocoAddress();
        return msg;
    }

    private void runLearnModeTrain() {
        setStatusText(getIdleMessage(), Color.black);
        String msg = checkTrainId();
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg, Bundle.getMessage("WarningTitle"),
                    JOptionPane.WARNING_MESSAGE);
            setStatusText(msg, Color.red);
            return;
        }
        msg = _warrant.checkStartBlock(Warrant.MODE_LEARN);
        if (msg != null) {
            OBlock block = _warrant.getBlockAt(0);
            if (msg.equals(Bundle.getMessage("BlockDark", block.getDisplayName()))) {
                if (JOptionPane.showConfirmDialog(this, msg
                        + Bundle.getMessage("OkToRun", ""),
                        Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
                    return;
                }
                msg = null;
            } else {
                JOptionPane.showMessageDialog(this, msg, Bundle.getMessage("WarningTitle"),
                        JOptionPane.WARNING_MESSAGE);
                setStatusText(msg, Color.red);
                return;                
            }
        }
        
        msg = _warrant.checkRoute();
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("LearnError", msg),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            setStatusText(msg, Color.black);
            return;
        }
        
        if (_throttleCommands.size() > 0) {
            if (JOptionPane.showConfirmDialog(this, Bundle.getMessage("deleteCommand"),
                    Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
                return;
            }
            _throttleCommands = new ArrayList<ThrottleSetting>();
            _commandModel.fireTableDataChanged();
        }
        if (_learnThrottle == null) {
            _learnThrottle = new LearnThrottleFrame(this);
        } else {
            _learnThrottle.setVisible(true);
        }

        _warrant.setTrainName(getTrainName());
        _startTime = System.currentTimeMillis();
        _warrant.addPropertyChangeListener(this);
        
        msg = _warrant.setRunMode(Warrant.MODE_LEARN, getLocoAddress(), _learnThrottle,
                _throttleCommands, _runBlind.isSelected());
        if (msg != null) {
            stopRunTrain();
            JOptionPane.showMessageDialog(this, msg, Bundle.getMessage("WarningTitle"),
                    JOptionPane.WARNING_MESSAGE);
            setStatusText(msg, Color.red);
        }
    }

    private void runTrain() {
        String msg = checkTrainId();
        if (msg == null) {
            if (_throttleCommands == null || _throttleCommands.size() == 0) {
                msg = Bundle.getMessage("NoCommands", _warrant.getDisplayName());
            }
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg, Bundle.getMessage("WarningTitle"),
                    JOptionPane.WARNING_MESSAGE);
            setStatusText(msg, Color.black);
            return;
        }
               _warrant.setTrainName(getTrainName());
        if (!_warrant.hasRouteSet() && _runBlind.isSelected()) {
            msg = Bundle.getMessage("BlindRouteNotSet", _warrant.getDisplayName());
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            setStatusText(msg, Color.black);
            return;
        }
        _warrant.addPropertyChangeListener(this);
        msg = _warrant.setRunMode(Warrant.MODE_RUN, getLocoAddress(), null,
                _throttleCommands, _runBlind.isSelected());
        if (msg != null) {
            stopRunTrain();
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            setStatusText(msg, Color.red);
            return;
        }
        msg = _warrant.checkStartBlock(Warrant.MODE_RUN);
        if (msg != null) {
            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("OkToRun", msg), Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
                stopRunTrain();
                setStatusText(msg, Color.red);
            } else {
                setStatusText(_warrant.getRunningMessage(), myGreen);
            }
        }
    }

    protected void stopRunTrain() {
        List<BlockOrder> orders = getOrders();
        if (_learnThrottle != null) {
            // if last block is dark and previous block has not been exited, we must assume train
            // has entered the last block now that the user is terminating the recording.
            if (_learnThrottle.getSpeedSetting() > 0.0) {
                _learnThrottle.setSpeedSetting(-0.5F);
                _learnThrottle.setSpeedSetting(0.0F);
            }
            _learnThrottle.dispose();
            _learnThrottle = null;
        }
        if (_warrant != null) {
            clearWarrant();
            if (orders.size()>0) {
                BlockOrder bo = _warrant.getCurrentBlockOrder();
                if (bo!=null) {
                    OBlock lastBlock = orders.get(orders.size() - 1).getBlock();
                    OBlock currentBlock = bo.getBlock();
                    if (!lastBlock.equals(currentBlock)) {
                        if ((lastBlock.getState() & OBlock.DARK) != 0
                                && currentBlock.equals(orders.get(orders.size() - 2).getBlock())) {
                            setThrottleCommand("NoOp", Bundle.getMessage("Mark"), lastBlock.getDisplayName());
                            setStatusText(Bundle.getMessage("LearningStop"), myGreen);
                        } else {
                            JOptionPane.showMessageDialog(this, Bundle.getMessage("IncompleteScript", lastBlock),
                                    Bundle.getMessage("WarningTitle"),
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        setStatusText(Bundle.getMessage("LearningStop"), myGreen);                
                    }                                    
                }
            }
        }        
    }
    private void clearWarrant() {
        if (_warrant != null) {
            _warrant.deAllocate();
            _warrant.stopWarrant(false);
            _warrant.removePropertyChangeListener(this);
        }        
    }

    protected Warrant getWarrant() {
        return _warrant;
    }

    protected void setStatusText(String msg, Color c) {
        _statusBox.setForeground(c);
        _statusBox.setText(msg);
    }
    
    /**
     * Property names from Warrant: "runMode" - from setRunMode "controlChange"
     * - from controlRunTrain "blockChange" - from goingActive "allocate" - from
     * allocateRoute, deAllocate "setRoute" - from setRoute, goingActive
     * Property names from Engineer: "Command" - from run "SpeedRestriction" -
     * ThrottleRamp run Property names from RouteFinder: "RouteSearch" - from
     * run
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if (log.isDebugEnabled()) log.debug("propertyChange \""+property+
                                            "\" old= "+e.getOldValue()+" new= "+e.getNewValue()+
                                            " source= "+e.getSource().getClass().getName());
        if (property.equals("DnDrop")) {
            doAction(e.getSource());
        } else if (e.getSource() instanceof Warrant && _warrant.equals(e.getSource())) {
            switch (_warrant.getRunMode()) {
                case Warrant.MODE_NONE:
                    _warrant.removePropertyChangeListener(this);
                    int oldMode = ((Integer) e.getOldValue()).intValue();
                    int newMode = ((Integer) e.getNewValue()).intValue();
                    if (oldMode != Warrant.MODE_NONE) {
                        OBlock block = _warrant.getCurrentBlockOrder().getBlock();
                        int state = block.getState();
                        if ((state & OBlock.OCCUPIED) != 0 || (state & OBlock.DARK) != 0
                                 || _runBlind.isSelected()) {
                            setStatusText(
                                    Bundle.getMessage("warrantEnd",
                                            _warrant.getTrainName(),
                                            _warrant.getDisplayName(),
                                            block.getDisplayName()), myGreen);
                        } else {
                            setStatusText(
                                    Bundle.getMessage("warrantAbort",
                                            _warrant.getTrainName(),
                                            _warrant.getDisplayName()), myGreen);
                        }
                    }
                    break;
                case Warrant.MODE_LEARN:
                    if (property.equals("blockChange")) {
                        OBlock oldBlock = (OBlock) e.getOldValue();
                        OBlock newBlock = (OBlock) e.getNewValue();
                        if (newBlock == null) {
                            stopRunTrain();
                            setStatusText(
                                    Bundle.getMessage("ChangedRoute",
                                            _warrant.getDisplayName(),
                                            oldBlock.getDisplayName(),
                                            _warrant.getTrainName()), Color.red);
                        } else {
                            setThrottleCommand("NoOp", Bundle.getMessage("Mark"), ((OBlock) e.getNewValue()).getDisplayName());
                            setStatusText(
                                    Bundle.getMessage("TrackerBlockEnter",
                                            _warrant.getTrainName(),
                                            newBlock.getDisplayName()), myGreen);
                        }
                    } else if (property.equals("abortLearn")) {
                        stopRunTrain();
                        int oldIdx = ((Integer) e.getOldValue()).intValue();
                        int newIdx = ((Integer) e.getNewValue()).intValue();
                        if (oldIdx > newIdx) {
                            setStatusText(
                                    Bundle.getMessage("LearnAbortOccupied",
                                            _warrant.getBlockAt(oldIdx),
                                            _warrant.getDisplayName()), Color.red);                          
                        } else {
                            setStatusText(
                                    Bundle.getMessage("warrantAbort",
                                            _warrant.getTrainName(),
                                            _warrant.getDisplayName()), Color.red);                            
                        }
                    } else {
                        setStatusText(Bundle.getMessage("Learning",
                                _warrant.getCurrentBlockOrder().getBlock().getDisplayName()), Color.black);                        
                    }
                    break;
                case Warrant.MODE_RUN:
                case Warrant.MODE_MANUAL:
                    if (e.getPropertyName().equals("blockChange")) {
                        OBlock oldBlock = (OBlock) e.getOldValue();
                        OBlock newBlock = (OBlock) e.getNewValue();
                        if (newBlock == null) {
                            setStatusText(
                                    Bundle.getMessage("ChangedRoute",
                                            _warrant.getDisplayName(),
                                            oldBlock.getDisplayName(),
                                            _warrant.getTrainName()), Color.red);
                        } else {
                            setStatusText(
                                    Bundle.getMessage("TrackerBlockEnter",
                                            _warrant.getTrainName(),
                                            newBlock.getDisplayName()), myGreen);
                        }
                    } else if (e.getPropertyName().equals("blockRelease")) {
                        OBlock block = (OBlock) e.getNewValue();
                        long et = (System.currentTimeMillis() - block._entryTime) / 1000;
                        setStatusText(Bundle.getMessage("TrackerBlockLeave",
                                _warrant.getTrainName(), block.getDisplayName(), et / 60,
                                et % 60),  myGreen);
                    } else if (e.getPropertyName().equals("SpeedRestriction")) {
                        setStatusText(Bundle.getMessage("speedChange",
                                _warrant.getTrainName(), _warrant.getCurrentBlockOrder()
                                        .getBlock().getDisplayName(), e.getNewValue()), Color.black);
                    } else if (e.getPropertyName().equals("runMode")) {
                        oldMode = ((Integer) e.getOldValue()).intValue();
                        newMode = ((Integer) e.getNewValue()).intValue();
                        if (oldMode == Warrant.MODE_NONE) {
                            if (newMode != Warrant.MODE_NONE) {
                                setStatusText(Bundle.getMessage("warrantStart",
                                        _warrant.getTrainName(), _warrant.getDisplayName(),
                                        _warrant.getCurrentBlockOrder().getBlock()
                                                .getDisplayName(),
                                        Bundle.getMessage(Warrant.MODES[newMode])), myGreen);
                                if (_warrant.getState()==Warrant.HALT) {
                                    JOptionPane.showMessageDialog(this, _warrant.getRunningMessage(),
                                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);                                    
                                }                                
                            }
                        } else {
                            setStatusText(Bundle.getMessage("modeChange",
                                    _warrant.getTrainName(), _warrant.getDisplayName(),
                                    Bundle.getMessage(Warrant.MODES[oldMode]),
                                    Bundle.getMessage(Warrant.MODES[newMode])), Color.black);
                        }
                    } else if (e.getPropertyName().equals("controlChange")) {
                        int runState = ((Integer) e.getOldValue()).intValue();
                        int newCntrl = ((Integer) e.getNewValue()).intValue();
                        String stateStr = null;
                        if (runState < 0) {
                            stateStr = Bundle.getMessage(Warrant.MODES[-runState]);
                        } else {
                            stateStr = Bundle.getMessage(Warrant.RUN_STATE[runState],
                                    _warrant.getCurrentBlockOrder().getBlock()
                                            .getDisplayName());
                        }
                        setStatusText(Bundle.getMessage("controlChange",
                                _warrant.getTrainName(), stateStr,
                                Bundle.getMessage(Warrant.CNTRL_CMDS[newCntrl])),
                                Color.black);
                    } else if (e.getPropertyName().equals("throttleFail")) {
                        setStatusText(Bundle.getMessage("ThrottleFail",
                                _warrant.getTrainName(), e.getNewValue()), Color.red);
                    }
                    break;
            }
        }
        invalidate();
    }

    protected void setThrottleCommand(String cmd, String value) {
        String bName = Bundle.getMessage("NoBlock");
        BlockOrder bo = _warrant.getCurrentBlockOrder();
        if (bo != null) {
            OBlock block = bo.getBlock();
            if (block != null) {
                bName = block.getDisplayName();
            }
        }
        setThrottleCommand(cmd, value, bName);
    }

    private void setThrottleCommand(String cmd, String value, String bName) {
        long endTime = System.currentTimeMillis();
        long time = endTime - _startTime;
        _startTime = endTime;
        _throttleCommands.add(new ThrottleSetting(time, cmd, value, bName));
        _commandModel.fireTableDataChanged();

        scrollCommandTable(_commandModel.getRowCount());
    }

    private void scrollCommandTable(int row) {
        JScrollBar bar = _throttlePane.getVerticalScrollBar();
        bar.setValue(row * ROW_HEIGHT);
//        bar.setValue(bar.getMaximum());
    }    

    private void setForward(boolean forward) {
        String fwString;
        if (forward) {
            fwString = "true";
        } else {
            fwString = "false";
        }
        if (_throttleCommands.size() == 0) {
            _throttleCommands.add(new ThrottleSetting(0, "Forward", fwString, ""));
            log.debug("setForward adding to empty _throttleCommands");
            return;
        }
        for (int i=0; i<_throttleCommands.size(); i++) {
            ThrottleSetting ts = _throttleCommands.get(i);
            log.info("setForward examining _throttleCommands "+i+" command: "+ts.getCommand()+" value: "+ts.getValue());
            if (ts.getCommand().toUpperCase().equals("FORWARD")) {
                log.debug("setForward modifying _throttleCommands "+i);
                ts.setValue(fwString);
                return;
            }
        }
        log.debug("setForward inserting new command at beginning of list");
        _throttleCommands.add(0, new ThrottleSetting(0, "Forward", fwString, ""));
    }

    private boolean save() {
        if (_isSCWarrant.isSelected()) {
            setForward(_runForward.isSelected());
        }
        String msg = routeIsValid();
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("SaveError")+" - "+msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        msg = checkLocoAddress();
        if (msg==null) {
            if (_throttleCommands.size()==0) {
                msg = Bundle.getMessage("NoCommands", _warrant.getDisplayName());
            }            
        }
        if (msg!=null) {
            int result = JOptionPane.showConfirmDialog(this, msg+Bundle.getMessage("SaveQuestion"), Bundle.getMessage("QuestionTitle"), 
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result==JOptionPane.NO_OPTION) {
                return false;
            }       
        }

        if (_saveWarrant != null) {
            // _saveWarrant already registered, but might not be the correct class.
            InstanceManager.getDefault(WarrantManager.class).deregister(_saveWarrant);
        }
        _warrant = InstanceManager.getDefault(WarrantManager.class).createNewWarrant(_sysNameBox.getText(), _userNameBox.getText(), _isSCWarrant.isSelected(), (long)_TTPtextField.getValue());
        _warrant.setDccAddress(getTrainId());
        _warrant.setTrainName(getTrainName());
        _warrant.setRunBlind(_runBlind.isSelected());
        _warrant.setUserName(_userNameBox.getText());

        _warrant.setViaOrder(getViaBlockOrder());
        _warrant.setAvoidOrder(getAvoidBlockOrder());
        _warrant.setBlockOrders(getOrders());
        _warrant.setThrottleCommands(_throttleCommands);
        
        if (log.isDebugEnabled()) log.debug("warrant saved _train "+getTrainId()+", name= "+getTrainName());
        WarrantTableAction.updateWarrantMenu();
        WarrantTableFrame.getInstance().getModel().fireTableDataChanged();
        return true;
    }

    protected void setWarrant(Warrant w) {
        _warrant = w;
        _sysNameBox.setText(w.getSystemName());
        _userNameBox.setText(w.getUserName());
    }

    private void copy() {
        WarrantTableAction.CreateWarrantFrame f = new WarrantTableAction.CreateWarrantFrame();
        f.setVisible(true);
        try {
            f.initComponents();
            f.concatenate(_saveWarrant, null);
        } catch (Exception ex) {
            log.error("error making CreateWarrantFrame", ex);
        }
        dispose();
    }

    private void close() {
        clearTempWarrant();
        stopRunTrain();
        WarrantTableAction.closeWarrantFrame(this);
    }

    /**
     * *********************** Throttle Table *****************************
     */
    class ThrottleTableModel extends AbstractTableModel {

        public static final int ROW_NUM = 0;
        public static final int TIME_COLUMN = 1;
        public static final int COMMAND_COLUMN = 2;
        public static final int VALUE_COLUMN = 3;
        public static final int BLOCK_COLUMN = 4;
        public static final int NUMCOLS = 5;

        public ThrottleTableModel() {
            super();
        }

        public int getColumnCount() {
            return NUMCOLS;
        }

        public int getRowCount() {
            return _throttleCommands.size();
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case ROW_NUM:
                    return "#";
                case TIME_COLUMN:
                    return Bundle.getMessage("TimeCol");
                case COMMAND_COLUMN:
                    return Bundle.getMessage("CommandCol");
                case VALUE_COLUMN:
                    return Bundle.getMessage("ValueCol");
                case BLOCK_COLUMN:
                    return Bundle.getMessage("BlockCol");
            }
            return "";
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (row == ROW_NUM) {
                return false;
            }
            return true;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case ROW_NUM:
                    return new JTextField(2).getPreferredSize().width;
                case TIME_COLUMN:
                    return new JTextField(7).getPreferredSize().width;
                case COMMAND_COLUMN:
                    return new JTextField(9).getPreferredSize().width;
                case VALUE_COLUMN:
                    return new JTextField(8).getPreferredSize().width;
                case BLOCK_COLUMN:
                    return new JTextField(40).getPreferredSize().width;
            }
            return new JTextField(12).getPreferredSize().width;
        }

        public Object getValueAt(int row, int col) {
            // some error checking
            if (row >= _throttleCommands.size()) {
                log.debug("row is greater than throttle command size");
                return "";
            }
            ThrottleSetting ts = _throttleCommands.get(row);
            if (ts == null) {
                log.debug("Throttle setting is null!");
                return "";
            }
            switch (col) {
                case ROW_NUM:
                    return Integer.valueOf(row + 1);
                case TIME_COLUMN:
                    return ts.getTime();
                case COMMAND_COLUMN:
                    return ts.getCommand();
                case VALUE_COLUMN:
                    if ("SpeedStep".equalsIgnoreCase(ts.getCommand())) {
                        switch (Integer.parseInt(ts.getValue())) {
                            case DccThrottle.SpeedStepMode14:
                                return Integer.toString(14);
                            case DccThrottle.SpeedStepMode27:
                                return Integer.toString(27);
                            case DccThrottle.SpeedStepMode28:
                                return Integer.toString(28);
                        }
                        return Integer.toString(128);
                    } else if ("Mark".equalsIgnoreCase(ts.getValue())) {
                        return Bundle.getMessage("Mark");
                    }
                    return ts.getValue();
                case BLOCK_COLUMN:
                    return ts.getBlockName();
            }
            return "";
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            ThrottleSetting ts = _throttleCommands.get(row);
            String msg = null;
            switch (col) {
                case TIME_COLUMN:
                    long time = 0;
                    try {
                        time = Long.parseLong((String) value);
                        if (time < 0) {
                            msg = Bundle.getMessage("InvalidTime", (String) value);
                        } else {
                            ts.setTime(time);
                        }
                    } catch (NumberFormatException nfe) {
                        msg = Bundle.getMessage("InvalidTime", (String) value);
                    }
                    break;
                case COMMAND_COLUMN:
                    String cmd = ((String) value);
                    if (cmd == null || cmd.length() == 0) {
//                        msg = Bundle.getMessage("nullValue", Bundle.getMessage("CommandCol"));
                        break;
                    }
                    cmd = cmd.trim().toUpperCase();
                    if ("SPEED".equals(cmd)) {
                        ts.setCommand("Speed");
                    } else if ("SPEEDSTEP".equals(cmd)) {
                        ts.setCommand("SpeedStep");
                    } else if ("FORWARD".equals(cmd)) {
                        ts.setCommand("Forward");
                    } else if (cmd.startsWith("F")) {
                        try {
                            int cmdNum = Integer.parseInt(cmd.substring(1));
                            if (cmdNum < 0 || 28 < cmdNum) {
                                msg = Bundle.getMessage("badFunctionNum");
                            } else {
                                ts.setCommand(cmd);
                            }
                        } catch (Exception e) {
                            msg = Bundle.getMessage("badFunctionNum");
                        }
                    } else if (cmd.startsWith("LOCKF")) {
                        try {
                            int cmdNum = Integer.parseInt(cmd.substring(5));
                            if (cmdNum < 0 || 28 < cmdNum) {
                                msg = Bundle.getMessage("badLockFNum");
                            } else {
                                ts.setCommand(cmd);
                            }
                        } catch (Exception e) {
                            msg = Bundle.getMessage("badLockFNum");
                        }
                    } else if ("NOOP".equals(cmd)) {
                        msg = Bundle.getMessage("cannotEnterNoop", (String) value);
                    } else if (ts.getCommand() != null && ts.getCommand().equals("NoOp")) {
                        msg = Bundle.getMessage("cannotChangeNoop", (String) value);
                    } else if ("SENSOR".equals(cmd) || "SET SENSOR".equals(cmd) || "SET".equals(cmd)) {
                        ts.setCommand("Set Sensor");
                    } else if ("WAIT SENSOR".equals(cmd) || "WAIT".equals(cmd)) {
                        ts.setCommand("Wait Sensor");
                    } else if ("RUN WARRANT".equals(cmd)) {
                        ts.setCommand("Run Warrant");
                    } else if ("START TRACKER".equals(cmd)) {
                        ts.setCommand("Start Tracker");
                    } else {
                        msg = Bundle.getMessage("badCommand", (String) value);
                    }
                    break;
                case VALUE_COLUMN:
                    if (value == null || ((String) value).length() == 0) {
//                        msg = Bundle.getMessage("nullValue", Bundle.getMessage("ValueCol"));
                        break;
                    }
                    boolean resetBlockColumn = true;
                    if (ts==null || ts.getCommand()==null ) {
                        msg = Bundle.getMessage("nullValue", Bundle.getMessage("CommandCol"));
                        break;
                    }
                    cmd = ts.getCommand().toUpperCase();
                    if ("SPEED".equals(cmd)) {
                        try {
                            float speed = Float.parseFloat((String) value);
                            if (speed < 0.0f || 1.0f < speed) {
                                msg = Bundle.getMessage("throttlesetting", speed);
                            }
                        } catch (Exception e) {
                            msg = Bundle.getMessage("throttlesetting", value);
                        }
                        ts.setValue((String) value);
                    } else if ("SPEEDSTEP".equals(cmd)) {
                        int stepMode = DccThrottle.SpeedStepMode128;
                        try {
                            switch (Integer.parseInt((String) value)) {
                                case 14:
                                    stepMode = DccThrottle.SpeedStepMode14;
                                    break;
                                case 27:
                                    stepMode = DccThrottle.SpeedStepMode27;
                                    break;
                                case 28:
                                    stepMode = DccThrottle.SpeedStepMode28;
                                    break;
                                case 128:
                                    stepMode = DccThrottle.SpeedStepMode128;
                                    break;
                            }
                            msg = Bundle.getMessage("badStepMode");
                        } catch (Exception e) {
                            msg = Bundle.getMessage("invalidNumber");
                        }
                        ts.setValue(Integer.toString(stepMode));
                    } else if ("FORWARD".equalsIgnoreCase(cmd)) {
                        try {
                            Boolean.parseBoolean((String) value);
                        } catch (Exception e) {
                            msg = Bundle.getMessage("invalidBoolean");
                        }
                        ts.setValue((String) value);
                    } else if (cmd.startsWith("F")) {
                        try {
                            Boolean.parseBoolean((String) value);
                        } catch (Exception e) {
                            msg = Bundle.getMessage("invalidBoolean");
                        }
                        ts.setValue((String) value);
                    } else if (cmd.startsWith("LOCKF")) {
                        try {
                            Boolean.parseBoolean((String) value);
                        } catch (Exception e) {
                            msg = Bundle.getMessage("invalidBoolean");
                        }
                        ts.setValue((String) value);
                    } else if ("SET SENSOR".equals(cmd) || "WAIT SENSOR".equals(cmd)) {
                        String v = ((String) value).toUpperCase();
                        if ("ACTIVE".equals(v) || "INACTIVE".equals(v)) {
                            ts.setValue((String) value);
                        } else {
                            msg = Bundle.getMessage("badSensorCommand");
                        }
                        resetBlockColumn = false;
                    } else if ("RUN WARRANT".equals(cmd)) {
                        try {
                            Integer.parseInt((String) value);
                            ts.setValue((String) value);
                        } catch (NumberFormatException nfe) {
                            msg = Bundle.getMessage("badValue", value, cmd);
                        }
                        resetBlockColumn = false;
                    }
                    if (resetBlockColumn) {
                        ts.setBlockName(getPreviousBlockName(row));
                    }
                    break;
                case BLOCK_COLUMN:
                    cmd = ts.getCommand().toUpperCase();
                    if ("SET SENSOR".equals(cmd) || "WAIT SENSOR".equals(cmd)) {
                        try {
                            jmri.Sensor s = InstanceManager.sensorManagerInstance().getSensor((String) value);
                            if (s != null) {
                                ts.setBlockName((String) value);
                            } else {
                                msg = Bundle.getMessage("BadSensor", (String) value);
                            }
                        } catch (Exception ex) {
                            msg = Bundle.getMessage("BadSensor", (String) value) + ex;
                        }
                    } else if ("NOOP".equals(cmd)) {
                        msg = Bundle.getMessage("cannotChangeBlock", (String) value);
                    } else if ("RUN WARRANT".equals(cmd)) {
                        try {
                            Warrant w = InstanceManager.getDefault(WarrantManager.class).getWarrant((String) value);
                            if (w != null) {
                                ts.setBlockName((String) value);
                            } else {
                                msg = Bundle.getMessage("BadWarrant", (String) value);
                            }
                        } catch (Exception ex) {
                            msg = Bundle.getMessage("BadWarrant", value, cmd) + ex;
                        }
                    } else {
                        String name = getPreviousBlockName(row);
                        if (!name.equals(value)) {
                            msg = Bundle.getMessage("commandInBlock", name);
                            ts.setBlockName(name);
                        }
                    }
                    break;
            }
            if (msg != null) {
                showWarning(msg);
            } else {
                fireTableRowsUpdated(row, row);
            }
        }

        private String getPreviousBlockName(int row) {
            for (int i = row; i > 0; i--) {
                String name = _throttleCommands.get(i - 1).getBlockName();
                OBlock b = InstanceManager.getDefault(OBlockManager.class).getOBlock(name);
                if (b != null) {
                    return name;
                }
            }
            return "StartBlock";
        }

    }
    private final static Logger log = LoggerFactory.getLogger(WarrantFrame.class.getName());
}
