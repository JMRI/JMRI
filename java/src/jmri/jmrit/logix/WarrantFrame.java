package jmri.jmrit.logix;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
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
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.SpeedStepMode;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.roster.RosterSpeedProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WarrantFame creates and edits Warrants
 * <br>
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author  Pete Cressman Copyright (C) 2009, 2010
 */
public class WarrantFrame extends WarrantRoute {

    int _rowHeight;
    private Warrant _warrant;
    private Warrant _saveWarrant;
    private ThrottleTableModel _commandModel;
    private JTable _commandTable;
    private JScrollPane _throttlePane;

    private ArrayList<ThrottleSetting> _throttleCommands = new ArrayList<>();
    private long _startTime;
    private float _speedFactor;
    private float _speed;
    private long _TTP = 0;
    private boolean _forward = true;
    LearnThrottleFrame _learnThrottle = null;       // need access for JUnit test
    static Color myGreen = new Color(0, 100, 0);

    JTextField _sysNameBox;
    JTextField _userNameBox;

    JTabbedPane _tabbedPane;
    JPanel _routePanel;
    JPanel _commandPanel;
    JRadioButton _isSCWarrant = new JRadioButton(Bundle.getMessage("SmallLayoutTrainAutomater"), false);
    JRadioButton _isWarrant = new JRadioButton(Bundle.getMessage("NormalWarrant"), true);
    JRadioButton _addSpeeds = new JRadioButton(Bundle.getMessage("AddTrackSpeeds"), false);
    JCheckBox    _runForward = new JCheckBox(Bundle.getMessage("Forward"));
    JFormattedTextField _speedFactorTextField = new JFormattedTextField();
    JFormattedTextField _TTPtextField = new JFormattedTextField();
    JCheckBox    _noRampBox = new JCheckBox();
    JCheckBox    _shareRouteBox = new JCheckBox();
    JCheckBox    _runETOnlyBox = new JCheckBox();
    JRadioButton _eStop = new JRadioButton(Bundle.getMessage("EStop"), false);
    JRadioButton _halt = new JRadioButton(Bundle.getMessage("Halt"), false);
    JRadioButton _resume = new JRadioButton(Bundle.getMessage("Resume"), false);
    JRadioButton _abort = new JRadioButton(Bundle.getMessage("Abort"), false);
    JRadioButton _invisible = new JRadioButton();
    JTextField   _statusBox = new JTextField(90);

    JTextField _searchStatus = new JTextField();

    /*
     * Constructor for opening an existing warrant for editing
     */
    protected WarrantFrame(Warrant w) {
        super();
        _saveWarrant = w;
        // temp unregistered version until editing is saved.
        _warrant = new Warrant(_saveWarrant.getSystemName(), _saveWarrant.getUserName());
        setup(_saveWarrant);
        WarrantTableAction.setWarrantFrame(this);
        init();
        if (routeIsValid() != null) {
            findRoute();
        }
    }

    /*
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
     * Set up params from an existing warrant. note that _warrant can be unregistered.
     * warrant may be registered if equal to _saveWarrant.
     */
    private void setup(Warrant warrant) {
        _origin.setOrder(warrant.getfirstOrder());
        _destination.setOrder(warrant.getLastOrder());
        _via.setOrder(warrant.getViaOrder());
        _avoid.setOrder(warrant.getAvoidOrder());
        List<BlockOrder> list = warrant.getBlockOrders();
        ArrayList<BlockOrder> orders = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            orders.add(new BlockOrder(list.get(i)));
        }
        setOrders(orders);

        List<ThrottleSetting> tList = warrant.getThrottleCommands();
        if (warrant instanceof SCWarrant) {
            _speedFactor = ((SCWarrant)warrant).getSpeedFactor();
            _TTP = ((SCWarrant)warrant).getTimeToPlatform();
            _forward = ((SCWarrant)warrant).getForward();
        }
        for (int i = 0; i < tList.size(); i++) {
            ThrottleSetting ts = new ThrottleSetting(tList.get(i));
            _throttleCommands.add(ts);
        }
       _shareRouteBox.setSelected(warrant.getShareRoute());
        _warrant.setShareRoute(warrant.getShareRoute());
        _noRampBox.setSelected(warrant.getNoRamp());
        _warrant.setNoRamp(warrant.getNoRamp());
        _runETOnlyBox.setSelected(warrant.getRunBlind());
        _warrant.setRunBlind(warrant.getRunBlind());
        setTrainName(warrant.getTrainName());
        _warrant.setTrainName(warrant.getTrainName());
        
        SpeedUtil spU = warrant.getSpeedUtil();
        setSpeedUtil(_warrant.getSpeedUtil());
        _speedUtil.setRosterId(spU.getRosterId());
        _speedUtil.setDccAddress(spU.getDccAddress());
        setTrainInfo(warrant.getTrainName());
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
        List<BlockOrder> orders = getOrders();
        if (orders != null && orders.size() > 1) {
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

        topLeft.add(makeBlockPanels(false));

        topLeft.add(Box.createVerticalStrut(2 * STRUT_SIZE));
        tab1.add(topLeft);

        tab1.add(Box.createHorizontalStrut(STRUT_SIZE));
        JPanel topRight = new JPanel();
        topRight.setLayout(new BoxLayout(topRight, BoxLayout.LINE_AXIS));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(Box.createVerticalStrut(2 * STRUT_SIZE));
        panel.add(calculatePanel(true));
        panel.add(Box.createVerticalStrut(2 * STRUT_SIZE));
        panel.add(searchDepthPanel(true));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        p.add(makeTextBoxPanel(true, _searchStatus, "SearchRoute", null));
        _searchStatus.setEditable(false);
        p.add(Box.createVerticalGlue());
        panel.add(p);

        _searchStatus.setBackground(Color.white);
        _searchStatus.setEditable(false);
        panel.add(Box.createRigidArea(new Dimension(10,
                topLeft.getPreferredSize().height - panel.getPreferredSize().height)));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(Box.createVerticalGlue());
        topRight.add(panel);
        topRight.add(Box.createHorizontalStrut(STRUT_SIZE));

        PickListModel<OBlock> pickListModel = PickListModel.oBlockPickModelInstance();
        topRight.add(new JScrollPane(pickListModel.makePickTable()));
        Dimension dim = topRight.getPreferredSize();
        topRight.setMinimumSize(dim);
        tab1.add(topRight);
        tab1.add(Box.createHorizontalStrut(STRUT_SIZE));
        return tab1;
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
        JPanel typePanel = makeTypePanel();
        JPanel edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("SelectType"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(typePanel);
        panel.add(edge);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        
        JPanel scParamPanel = makeSCParamPanel();
        edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("SetSCParameters"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(scParamPanel);
        panel.add(edge);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        
        JPanel learnPanel = makeRecordPanel();
        edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("LearnMode"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(learnPanel);
        panel.add(edge);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        
        JPanel paramsPanel = makeRunParmsPanel();
        edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("RunParameters"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(paramsPanel);
        panel.add(edge);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        
        JPanel runPanel = makePlaybackPanel();
        edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("RunTrain"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(runPanel);
        panel.add(edge);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        tab2.add(panel);
        
        _isSCWarrant.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPanelEnabled(scParamPanel,true);
                setPanelEnabled(learnPanel,false);
                setPanelEnabled(paramsPanel,false);
                setPanelEnabled(runPanel,false);
                _addSpeeds.setEnabled(false);
            }
        });
        if (_saveWarrant instanceof SCWarrant) {
            setPanelEnabled(scParamPanel,true);
            setPanelEnabled(learnPanel,false);
            setPanelEnabled(paramsPanel,false);
            setPanelEnabled(runPanel,false);
            _addSpeeds.setEnabled(false);
            _isSCWarrant.setSelected(true);
        }

        _isWarrant.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPanelEnabled(scParamPanel,false);
                setPanelEnabled(learnPanel,true);
                setPanelEnabled(paramsPanel,true);
                setPanelEnabled(runPanel,true);
                _addSpeeds.setEnabled(_throttleCommands.size() > 1);
            }
        });

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
                if (getOrders()==null || getOrders().size() == 0) {
                    return Bundle.getMessage("noBlockOrders");
                } else if (getAddress() == null || getAddress().length() == 0) {
                    return Bundle.getMessage("NoLoco");
                }
                break;
            case Warrant.MODE_LEARN:
                return Bundle.getMessage("Learning", _warrant.getCurrentBlockName());
            case Warrant.MODE_RUN:
            case Warrant.MODE_MANUAL:
                return _warrant.getRunningMessage();
            default:
                // fall through
                break;
        }
        return Bundle.getMessage("Idle");
    }

    private void setPanelEnabled(JPanel panel, Boolean isEnabled) {
        panel.setEnabled(isEnabled);

        Component[] components = panel.getComponents();

        for(int i = 0; i < components.length; i++) {
            if("javax.swing.JPanel".equals(components[i].getClass().getName()))  {
                setPanelEnabled((JPanel) components[i], isEnabled);
            }

            components[i].setEnabled(isEnabled);
        }
    }

    private JPanel makeBorderedTrainPanel() {
        JPanel trainPanel = makeTrainIdPanel(null);

        JPanel edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("SetPower"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(trainPanel);
        return edge;
    }

    private JPanel makeTypePanel() {
        JPanel typePanel = new JPanel();
        typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.LINE_AXIS));
        typePanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel wTypePanel = new JPanel();
        wTypePanel.setLayout(new BoxLayout(wTypePanel, BoxLayout.PAGE_AXIS));
        wTypePanel.add(Box.createVerticalStrut(STRUT_SIZE));
        ButtonGroup group = new ButtonGroup();
        group.add(_isSCWarrant);
        group.add(_isWarrant);
        _isSCWarrant.setToolTipText(Bundle.getMessage("SCW_Tooltip"));
        _isWarrant.setToolTipText(Bundle.getMessage("W_Tooltip"));
        wTypePanel.add(_isSCWarrant);
        wTypePanel.add(_isWarrant);
        wTypePanel.add(_addSpeeds);
        typePanel.add(wTypePanel);

        _addSpeeds.addActionListener((ActionEvent evt)-> {
            addSpeeds();
        });

        return typePanel;
    }
    
    private void addSpeeds() {
        setAddress();
        RosterSpeedProfile speedProfile =  _speedUtil.getSpeedProfile();         
        boolean isForward = true;
        for (ThrottleSetting ts :_throttleCommands) {
            if ("FORWARD".equalsIgnoreCase(ts.getCommand())) {
                isForward = Boolean.parseBoolean(ts.getValue());
            }
            if ("SPEED".equalsIgnoreCase(ts.getCommand())) {
                try {
                    ts.setSpeed(speedProfile.getSpeed(Float.parseFloat(ts.getValue()), isForward) / 1000);                    
                } catch (NumberFormatException nfe) {
                    log.error("Command failed! {} {}", ts.toString(), nfe.toString());
                }
            }
        }
        _commandModel.fireTableDataChanged();
        showCommands(true);
        _addSpeeds.setSelected(false);
    }

    private JPanel makeSCParamPanel() {
        JPanel scParamPanel = new JPanel();
        scParamPanel.setLayout(new BoxLayout(scParamPanel, BoxLayout.PAGE_AXIS));
        scParamPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        scParamPanel.add(_runForward);
        _runForward.setSelected(_forward);
        
        JPanel ttpPanel = new JPanel();
        ttpPanel.setLayout(new BoxLayout(ttpPanel, BoxLayout.LINE_AXIS));
        JLabel ttp_l = new JLabel(Bundle.getMessage("TTP"));
        _TTPtextField.setValue(Long.valueOf(_TTP));
        _TTPtextField.setColumns(6);
        ttp_l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        _TTPtextField.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        ttpPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        ttpPanel.add(ttp_l);
        ttpPanel.add(_TTPtextField);
        ttpPanel.setToolTipText(Bundle.getMessage("TTPtoolTip"));
        scParamPanel.add(ttpPanel);
        
        JPanel sfPanel = new JPanel();
        sfPanel.setLayout(new BoxLayout(sfPanel, BoxLayout.LINE_AXIS));
        JLabel sf_l = new JLabel(Bundle.getMessage("SF"));
        _speedFactorTextField.setValue(Long.valueOf((long)(100*_speedFactor)));
        _speedFactorTextField.setColumns(3);
        sf_l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        _speedFactorTextField.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        sfPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        sfPanel.add(sf_l);
        sfPanel.add(_speedFactorTextField);
        sfPanel.setToolTipText(Bundle.getMessage("sfToolTip"));
        scParamPanel.add(sfPanel);        

        if (_isWarrant.isSelected()) {
            setPanelEnabled(scParamPanel,false);
        }
        return scParamPanel;
    }

    private JPanel makeRecordPanel() {
        JPanel learnPanel = new JPanel();
        learnPanel.setLayout(new BoxLayout(learnPanel, BoxLayout.LINE_AXIS));
        learnPanel.add(Box.createHorizontalStrut(STRUT_SIZE));


        JPanel startStopPanel = new JPanel();
        startStopPanel.setLayout(new BoxLayout(startStopPanel, BoxLayout.PAGE_AXIS));
        startStopPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        JButton startButton = new JButton(Bundle.getMessage("Start"));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearTempWarrant();
                showCommands(true);
                runLearnModeTrain();
            }
        });
        JButton stopButton = new JButton(Bundle.getMessage("Stop"));
        stopButton.addActionListener(new ActionListener() {
            @Override
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
        learnPanel.add(startStopPanel);

        return learnPanel;
    }

    private JPanel makeRunParmsPanel() {
        JPanel paramsPanel = new JPanel();
        paramsPanel.setLayout(new BoxLayout(paramsPanel, BoxLayout.LINE_AXIS));
        paramsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(makeTextBoxPanel(_shareRouteBox, "ShareRoute", "ToolTipShareRoute"));
        panel.add(makeTextBoxPanel(_noRampBox, "NoRamping", "ToolTipNoRamping"));
        panel.add(makeTextBoxPanel(_runETOnlyBox, "RunETOnly", "ToolTipRunETOnly"));

        paramsPanel.add(panel);
        return paramsPanel;
    }

    private JPanel makePlaybackPanel() {
        JPanel runPanel = new JPanel();
        runPanel.setLayout(new BoxLayout(runPanel, BoxLayout.LINE_AXIS));
        runPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        //panel.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel bPanel = new JPanel();
        bPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton runButton = new JButton(Bundle.getMessage("ARun"));
        runButton.addActionListener(new ActionListener() {
            @Override
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
        ButtonGroup group = new ButtonGroup();
        group.add(_halt);
        group.add(_resume);
        group.add(_eStop);
        group.add(_abort);
        group.add(_invisible);
        panel.add(_halt);
        panel.add(_resume);
        panel.add(_eStop);
        panel.add(_abort);
        runPanel.add(panel);

        _halt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doControlCommand(Warrant.HALT);
            }
        });
        _resume.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doControlCommand(Warrant.RESUME);
            }
        });
        _eStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doControlCommand(Warrant.ESTOP);
            }
        });
        _abort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doControlCommand(Warrant.ABORT);
            }
        });
        runPanel.add(panel);
        return runPanel;
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
            @Override
            public void actionPerformed(ActionEvent e) {
                showCommands(false);
            }
        });
        showScript.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCommands(true);
            }
        });

        if (_saveWarrant instanceof SCWarrant) {
            showRoute.setSelected(true);
            showCommands(false);
            setPanelEnabled(buttonPanel,false);
        }
        _isSCWarrant.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRoute.setSelected(true);
                showCommands(false);
                setPanelEnabled(buttonPanel,false);
            }
        });
        _isWarrant.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPanelEnabled(buttonPanel,true);
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
        _rowHeight = _commandTable.getRowHeight();
        Dimension dim = _commandTable.getPreferredSize();
        dim.height = _rowHeight * 10;
        _throttlePane.getViewport().setPreferredSize(dim);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        buttonPanel.add(Box.createVerticalStrut(3 * STRUT_SIZE));

        JButton insertButton = new JButton(Bundle.getMessage("buttonInsertRow"));
        insertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertRow();
            }
        });
        buttonPanel.add(insertButton);
        buttonPanel.add(Box.createVerticalStrut(2 * STRUT_SIZE));

        JButton deleteButton = new JButton(Bundle.getMessage("buttonDeleteRow"));
        deleteButton.addActionListener(new ActionListener() {
            @Override
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
            @Override
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
            @Override
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
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        panel.add(cancelButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        buttonPanel.add(panel);
        buttonPanel.add(Box.createHorizontalStrut(3 * STRUT_SIZE));

        buttonPanel.add(Box.createHorizontalGlue());
        return buttonPanel;
    }

    private void doControlCommand(int cmd) {
        if (log.isDebugEnabled()) {
            log.debug("actionPerformed on doControlCommand  cmd= {}", cmd);
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
        _throttleCommands = new ArrayList<>();
        _commandModel.fireTableDataChanged();
        _searchStatus.setText("");
    }

    @Override
    public void selectedRoute(ArrayList<BlockOrder> orders) {
        clearCommands();
        _tabbedPane.setSelectedIndex(1);
    }

    /**
     * all non-null returns are fatal
     *
     */
    private String checkTrainId() {
        setAddress();       // sets SpeedUtil address in 'this' (WarrantRoute)
        _warrant.getSpeedUtil().setDccAddress(getAddress());    // sets SpeedUtil address in _warrant
        String msg = null;
        if (_warrant.getRunMode() != Warrant.MODE_NONE) {
            msg = _warrant.getRunModeMessage();
            return msg;
        }
        List<BlockOrder> orders = getOrders();
        if (orders==null || orders.size() < 2) {
            msg = Bundle.getMessage("NoRouteSet", _origin.getBlockName(), _destination.getBlockName());
            return msg;
        }
        msg = _warrant.setRoute(false, orders);     // calls allocateRoute
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

        msg = _warrant.checkRoute();
        if (msg == null) {
            msg = _warrant.checkforTrackers();
        }
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
            _throttleCommands = new ArrayList<>();
            _commandModel.fireTableDataChanged();
        }

        msg = _warrant.checkStartBlock();
        if (msg != null) {
            if (msg.equals("warnStart")) {
                msg = Bundle.getMessage("warnStart", getTrainName(), _warrant.getCurrentBlockName());
                JOptionPane.showMessageDialog(this, msg,
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                setStatusText(msg, Color.red);
                return;
            } else if (msg.equals("BlockDark")) {
                msg = Bundle.getMessage("BlockDark", _warrant.getCurrentBlockName(), getTrainName());
                if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this,
                        Bundle.getMessage("OkToRun", msg), Bundle.getMessage("QuestionTitle"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
                    stopRunTrain();
                    setStatusText(msg, Color.red);
                    return;
                }
            }
            setStatusText(msg, Color.black);
        }
        if (_learnThrottle == null) {
            _learnThrottle = new LearnThrottleFrame(this);
        } else {
            _learnThrottle.setVisible(true);
        }

        _warrant.setTrainName(getTrainName());
        _startTime = System.currentTimeMillis();
        _speed = 0.0f;
        
//        _warrant.getSpeedUtil().getValidSpeedProfile(this);
        _warrant.addPropertyChangeListener(this);

        msg = _warrant.setRunMode(Warrant.MODE_LEARN, _speedUtil.getDccAddress(), _learnThrottle,
                _throttleCommands, _runETOnlyBox.isSelected());
        if (msg != null) {
            stopRunTrain();
            JOptionPane.showMessageDialog(this, msg, Bundle.getMessage("WarningTitle"),
                    JOptionPane.WARNING_MESSAGE);
            setStatusText(msg, Color.red);
        }
    }

    private void runTrain() {
        _warrant.setTrainName(getTrainName());
        _warrant.setShareRoute(_shareRouteBox.isSelected());
        _warrant.setNoRamp(_noRampBox.isSelected());
        String msg = checkTrainId();
        if (msg == null) {
            if (_throttleCommands.size() <= getOrders().size()) {
                msg = Bundle.getMessage("NoCommands", _warrant.getDisplayName());
            }
            if (msg == null) {
                if (!_warrant.hasRouteSet() && _runETOnlyBox.isSelected()) {
                    msg = Bundle.getMessage("BlindRouteNotSet", _warrant.getDisplayName());
                }
                if (msg == null) {
                    WarrantTableModel model = WarrantTableFrame.getDefault().getModel(); 
                    msg = model.checkAddressInUse(_warrant);
                    if (msg == null) {
                        if ( model.getRow(_warrant) != -1) {
                            // _warrant matches an existing warrant, but as objects they are unequal
                            msg = Bundle.getMessage("DuplicateSectionName", _warrant.getTrainName(), _warrant.getDisplayName());
                        } else {
                            model.addNXWarrant(_warrant);
                        }
                    }
                }
            }
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg, Bundle.getMessage("WarningTitle"),
                    JOptionPane.WARNING_MESSAGE);
            clearWarrant();
            setStatusText(msg, Color.black);
            return;
        }
/*        if (_warrant.commandsHaveTrackSpeeds()) {
            _warrant.getSpeedUtil().getValidSpeedProfile(this);            
        } else {
            setStatusText(Bundle.getMessage("NoTrackSpeeds", _warrant.getDisplayName()), Color.red);
        }*/
        _warrant.addPropertyChangeListener(this);
        
        msg = _warrant.setRunMode(Warrant.MODE_RUN, _speedUtil.getDccAddress(), null,
                _throttleCommands, _runETOnlyBox.isSelected());
        if (msg == null) {
            msg = _warrant.checkforTrackers();
        }
        if (msg != null) {
            clearWarrant();
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            setStatusText(msg, Color.red);
            return;
        } else
        
        msg = _warrant.checkStartBlock();
        if (msg != null) {
            if (msg.equals("warnStart")) {
                msg = Bundle.getMessage("warnStart", _warrant.getTrainName(), _warrant.getCurrentBlockName());
            } else if (msg.equals("BlockDark")) {
                msg = Bundle.getMessage("BlockDark", _warrant.getCurrentBlockName(), _warrant.getTrainName());
            }
            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("OkToRun", msg), Bundle.getMessage("QuestionTitle"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
                clearWarrant();
                setStatusText(msg, Color.red);
                return;
            } else {
                setStatusText(_warrant.getRunningMessage(), myGreen);
            }
        }
        Warrant w = new Warrant(_warrant.getSystemName(), _warrant.getUserName());
        _warrant = w;
    }

    protected void stopRunTrain() {
        if (_saveWarrant != null && _saveWarrant.getRunMode() != Warrant.MODE_NONE) {
          return;
        }
        if (_learnThrottle != null) {
            _learnThrottle.dispose();
            _learnThrottle = null;
        }

        if (_warrant.getRunMode() == Warrant.MODE_LEARN) {
            List<BlockOrder> orders = getOrders();
            if (orders!=null && orders.size()>1) {
                BlockOrder bo = _warrant.getCurrentBlockOrder();
                if (bo!=null) {
                    OBlock lastBlock = orders.get(orders.size() - 1).getBlock();
                    OBlock currentBlock = bo.getBlock();
                    if (!lastBlock.equals(currentBlock)) {
                        if ((lastBlock.getState() & OBlock.UNDETECTED) != 0
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
        clearWarrant();
    }
    private void clearWarrant() {
        if (_warrant != null) {
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
        if (log.isDebugEnabled())
            log.debug("propertyChange \"{}\" old= {} new= {} source= {}",
                    property, e.getOldValue(), e.getNewValue(), e.getSource().getClass().getName());
        if (property.equals("DnDrop")) {
            doAction(e.getSource());
        } else if (e.getSource() instanceof Warrant && _warrant.equals(e.getSource())) {
            String msg = null;
            Color color = myGreen;
            switch (_warrant.getRunMode()) {
                case Warrant.MODE_NONE:
                    _warrant.removePropertyChangeListener(this);
                    if (e.getPropertyName().equals("runMode")) {
                        int newMode = ((Integer) e.getNewValue()).intValue();
                        if (newMode==Warrant.MODE_ABORT) {
                            msg =Bundle.getMessage("warrantAbort",
                                    _warrant.getTrainName(),
                                    _warrant.getDisplayName());
                        } else {
                            int oldMode = ((Integer) e.getOldValue()).intValue();
                            if (oldMode != Warrant.MODE_NONE) {
                                OBlock curBlock = _warrant.getCurrentBlockOrder().getBlock();
                                OBlock lastBlock = _warrant.getLastOrder().getBlock();
                                if (lastBlock.equals(curBlock)) {
                                    msg = Bundle.getMessage("warrantComplete",
                                            _warrant.getTrainName(), _warrant.getDisplayName(),
                                            lastBlock.getDisplayName());
                                    color = Color.green;
                                } else {
                                    msg = Bundle.getMessage("warrantEnd",
                                            _warrant.getTrainName(), _warrant.getDisplayName(),
                                            lastBlock.getDisplayName());
                                    color = Color.red;
                                }
                            }
                        }
                    }
                    break;
                case Warrant.MODE_LEARN:
                    if (property.equals("blockChange")) {
                        OBlock oldBlock = (OBlock) e.getOldValue();
                        OBlock newBlock = (OBlock) e.getNewValue();
                        if (newBlock == null) {
                            stopRunTrain();
                            msg =Bundle.getMessage("ChangedRoute",
                                            _warrant.getDisplayName(),
                                            oldBlock.getDisplayName(),
                                            _warrant.getTrainName());
                            color = Color.red;
                        } else {
                            setThrottleCommand("NoOp", Bundle.getMessage("Mark"), ((OBlock) e.getNewValue()).getDisplayName());
                            msg = Bundle.getMessage("TrackerBlockEnter",
                                            _warrant.getTrainName(),
                                            newBlock.getDisplayName());
                        }
                    } else if (property.equals("abortLearn")) {
                        stopRunTrain();
                        int oldIdx = ((Integer) e.getOldValue()).intValue();
                        int newIdx = ((Integer) e.getNewValue()).intValue();
                        if (oldIdx > newIdx) {
                            msg = Bundle.getMessage("LearnAbortOccupied",
                                            _warrant.getBlockAt(oldIdx),
                                            _warrant.getDisplayName());
                            color = Color.red;
                        } else {
                            msg = Bundle.getMessage("warrantAbort",
                                            _warrant.getTrainName(),
                                            _warrant.getDisplayName());
                            color = Color.red;
                        }
                    } else {
                        msg = Bundle.getMessage("Learning", _warrant.getCurrentBlockName());
                        color = Color.black;
                    }
                    break;
                case Warrant.MODE_RUN:
                case Warrant.MODE_MANUAL:
                    if (e.getPropertyName().equals("blockChange")) {
                        OBlock oldBlock = (OBlock) e.getOldValue();
                        OBlock newBlock = (OBlock) e.getNewValue();
                        if (newBlock == null) {
                            msg = Bundle.getMessage("ChangedRoute",
                                            _warrant.getDisplayName(),
                                            oldBlock.getDisplayName(),
                                            _warrant.getTrainName());
                            color = Color.red;
                        } else {
                            msg = Bundle.getMessage("TrackerBlockEnter",
                                            _warrant.getTrainName(),
                                            newBlock.getDisplayName());
                        }
                    } else if (e.getPropertyName().equals("blockRelease")) {
                        return;
                    } else if (e.getPropertyName().equals("ReadyToRun")) {
                        msg = _warrant.getRunningMessage();
                    } else if (e.getPropertyName().equals("SpeedChange")) {
                        msg = _warrant.getRunningMessage();
                        color = Color.black;
                    } else if (e.getPropertyName().equals("SpeedRestriction")) {
                        msg = Bundle.getMessage("speedChange", _warrant.getTrainName(),
                                _warrant.getCurrentBlockName(), e.getNewValue());
                        color = Color.black;
                    } else if (e.getPropertyName().equals("runMode")) {
                        int oldMode = ((Integer) e.getOldValue()).intValue();
                        int newMode = ((Integer) e.getNewValue()).intValue();
                        if (oldMode == Warrant.MODE_NONE) {
                            if (newMode != Warrant.MODE_NONE) {
                                msg = Bundle.getMessage("warrantStart",
                                        _warrant.getTrainName(), _warrant.getDisplayName(),
                                        _warrant.getCurrentBlockName(),
                                        Bundle.getMessage(Warrant.MODES[newMode]));
                                if (_warrant.getState()==Warrant.HALT) {
                                    JOptionPane.showMessageDialog(this, _warrant.getRunningMessage(),
                                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                                }
                            }
                        }
                    } else if (e.getPropertyName().equals("controlChange")) {
                        int runState = ((Integer) e.getOldValue()).intValue();
                        int newCntrl = ((Integer) e.getNewValue()).intValue();
                        String stateStr = null;
                        if (runState < 0) {
                            stateStr = Bundle.getMessage(Warrant.MODES[-runState]);
                        } else {
                            stateStr = Bundle.getMessage(Warrant.RUN_STATE[runState],
                                    _warrant.getCurrentBlockName());
                        }
                        msg = Bundle.getMessage("controlChange",
                                _warrant.getTrainName(), stateStr,
                                Bundle.getMessage(Warrant.CNTRL_CMDS[newCntrl]));
                                color = Color.black;
                    } else if (e.getPropertyName().equals("throttleFail")) {
                        msg = Bundle.getMessage("ThrottleFail",
                                _warrant.getTrainName(), e.getNewValue());
                        color = Color.red;
                    } else {
                        return;
                    }
                    break;
                default:
            }
            setStatusText(msg, color);
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
        if  (cmd.equals("Forward")) {
            _speedUtil.setIsForward(Boolean.parseBoolean(value));
        }
        setThrottleCommand(cmd, value, bName);
    }
    
    protected void setSpeedCommand(float speed) {
        if (_warrant.getSpeedUtil().profileHasSpeedInfo()) {
            _speed = _warrant.getSpeedUtil().getTrackSpeed(speed);  // mm/ms            
        } else {
            _speed = 0.0f;
        }
        setThrottleCommand("speed", Float.toString(speed));
    }

    private void setThrottleCommand(String cmd, String value, String bName) {
        long endTime = System.currentTimeMillis();
        long time = endTime - _startTime;
        _startTime = endTime;
        ThrottleSetting ts = new ThrottleSetting(time, cmd, value, bName, _speed);
        if (log.isDebugEnabled()) {
            log.debug("setThrottleCommand= {}", ts.toString());
        }
        _throttleCommands.add(ts);
        _commandModel.fireTableDataChanged();

        scrollCommandTable(_commandModel.getRowCount());
    }

    private void scrollCommandTable(int row) {
        JScrollBar bar = _throttlePane.getVerticalScrollBar();
        bar.setValue(row * _rowHeight);
//        bar.setValue(bar.getMaximum());
    }

    private boolean save() {
        if (_saveWarrant != null && _saveWarrant.getRunMode() != Warrant.MODE_NONE) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("CannotEdit", _warrant.getDisplayName()),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String msg = routeIsValid();
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("SaveError")+" - "+msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        msg = checkLocoAddress();
        if (msg==null && !_isSCWarrant.isSelected()) {
            List<BlockOrder> orders = getOrders();  // valid orders
            if (_throttleCommands.size() <= orders.size() + 1) {
                msg = Bundle.getMessage("NoCommands", _warrant.getDisplayName());
            } else {
                for (int i=0; i<_throttleCommands.size(); i++) {
                    ThrottleSetting ts = _throttleCommands.get(i);
                    if (ts.getValue()==null || ts.getCommand()==null || ts.getNamedBeanHandle()==null) {
                        JOptionPane.showMessageDialog(this, Bundle.getMessage("BadThrottleSetting", i+1, ts.toString()),
                                Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                }
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
            _warrant = _saveWarrant;
            if ((_saveWarrant instanceof SCWarrant && !_isSCWarrant.isSelected()) ||
                    (!(_saveWarrant instanceof SCWarrant) && _isSCWarrant.isSelected())) {
                // _saveWarrant already registered, but is not the correct class.
                InstanceManager.getDefault(WarrantManager.class).deregister(_saveWarrant);
                _warrant = InstanceManager.getDefault(WarrantManager.class).createNewWarrant(
                        _sysNameBox.getText(), _userNameBox.getText(), _isSCWarrant.isSelected(), (long)_TTPtextField.getValue());
            }
        } else {
            _warrant = InstanceManager.getDefault(WarrantManager.class).createNewWarrant(
                    _sysNameBox.getText(), _userNameBox.getText(), _isSCWarrant.isSelected(), (long)_TTPtextField.getValue());
        }

        if (_isSCWarrant.isSelected()) {
            ((SCWarrant)_warrant).setForward(_runForward.isSelected());
            ((SCWarrant)_warrant).setTimeToPlatform((long)_TTPtextField.getValue());
            long sf = (long)_speedFactorTextField.getValue();
            float sf_float = sf;
            ((SCWarrant)_warrant).setSpeedFactor(sf_float / 100);
        }
        _warrant.setTrainName(getTrainName());
        _warrant.setRunBlind(_runETOnlyBox.isSelected());
        _warrant.setShareRoute(_shareRouteBox.isSelected());
        _warrant.setNoRamp(_noRampBox.isSelected());
        _warrant.setUserName(_userNameBox.getText());

        _warrant.setViaOrder(getViaBlockOrder());
        _warrant.setAvoidOrder(getAvoidBlockOrder());
        _warrant.setBlockOrders(getOrders());
        _warrant.setThrottleCommands(_throttleCommands);
        _speedUtil.setOrders(getOrders());
        _warrant.setSpeedUtil(_speedUtil);  // transfer SpeedUtil to warrant

        if (log.isDebugEnabled()) log.debug("warrant {} saved _train {} name= {}",
                _warrant.getDisplayName(), _speedUtil.getRosterId(), getTrainName());
        WarrantTableAction.updateWarrantMenu();
        WarrantTableFrame.getDefault().getModel().fireTableDataChanged();
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
            log.error("error making CreateWarrantFrame {}", ex);
        }
        WarrantTableAction.closeWarrantFrame(this);
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
        public static final int SPEED_COLUMN = 5;
        public static final int NUMCOLS = 6;
        java.text.DecimalFormat threeDigit = new java.text.DecimalFormat("0.000");
        NumberFormat formatter = NumberFormat.getNumberInstance(); 

        public ThrottleTableModel() {
            super();
        }

        @Override
        public int getColumnCount() {
            return NUMCOLS;
        }

        @Override
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
                case SPEED_COLUMN:
                    return Bundle.getMessage("trackSpeed");
                default:
                    // fall through
                    break;
            }
            return "";
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == ROW_NUM || col == SPEED_COLUMN) {
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
                case SPEED_COLUMN:
                    return new JTextField(10).getPreferredSize().width;
                default:
                    // fall through
                    break;
            }
            return new JTextField(12).getPreferredSize().width;
        }

        // TODO Internationalize command names
        @Override
        public Object getValueAt(int row, int col) {
            // some error checking
            if (row >= _throttleCommands.size()) {
                if (log.isDebugEnabled()) log.debug("row {} is greater than throttle command size {}",
                        row, _throttleCommands.size());
                return "";
            }
            ThrottleSetting ts = _throttleCommands.get(row);
            if (ts == null) {
                if (log.isDebugEnabled()) log.debug("Throttle setting is null!");
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
                    if ("Mark".equalsIgnoreCase(ts.getValue())) {
                        return Bundle.getMessage("Mark");
                    } else {
                        if (ts.getValue() != null ) {
                            String cmd = ts.getCommand().toUpperCase();
                            if ("SPEED".equals(cmd)) {
                                try {
                                    float speed = Float.parseFloat(ts.getValue());                                
                                    return formatter.format(speed);                               
                                } catch (NumberFormatException npe) {
                                    log.error("Null value in ThrottleSetting: "+ ts.toString());
                                }
                            }
                        }
                    }
                    return ts.getValue();
                case BLOCK_COLUMN:
                    return ts.getBeanDisplayName();
                case SPEED_COLUMN:
                    return threeDigit.format(ts.getSpeed() * 1000);
                default:
                    // fall through
                    break;
            }
            return "";
        }

        // TODO Internationalize command names
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
                        } catch (NumberFormatException e) {
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
                        } catch (NumberFormatException nfe) {
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
                            float speed = formatter.parse((String) value).floatValue();
                            if (0.0f <= speed && speed <= 1.0f) {
                                ts.setValue(Float.toString(speed));
                                break;
                            }
                            msg = Bundle.getMessage("badValue", value, ts.getCommand());
                        } catch (java.text.ParseException pe) {
                            msg = Bundle.getMessage("invalidNumber");
                        } catch (NumberFormatException nfe) {
                            msg = Bundle.getMessage("invalidNumber");
                        }
                        ts.setValue(null);
                    } else if ("SPEEDSTEP".equals(cmd)) {
                        try {
                            // Get speed step mode, just to check that it's valid.
                            SpeedStepMode.getByName((String)value);
                            ts.setValue((String)value);
                        } catch (IllegalArgumentException nfe) {
                            msg = Bundle.getMessage("badStepMode");
                            ts.setValue(null);
                        }
                    } else if ("FORWARD".equalsIgnoreCase(cmd)) {
                            if (((String)value).length() < 4) {
                                msg = Bundle.getMessage("invalidBoolean");
                            } else {
                                if (Boolean.parseBoolean((String) value)) {
                                    ts.setValue("true");
                                } else {
                                    ts.setValue("false");
                                }
                            }
                    } else if (cmd.startsWith("F")) {
                            if (((String)value).length() < 4) {
                                msg = Bundle.getMessage("invalidBoolean");
                            } else {
                                if (Boolean.parseBoolean((String) value)) {
                                    ts.setValue("true");
                                } else {
                                    ts.setValue("false");
                                }
                            }
                    } else if (cmd.startsWith("LOCKF")) {
                        if (((String)value).length() < 4) {
                            msg = Bundle.getMessage("invalidBoolean");
                        } else {
                            if (Boolean.parseBoolean((String) value)) {
                                ts.setValue("true");
                            } else {
                                ts.setValue("false");
                            }
                        }
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
                    } else {
                        ts.setValue(null);
                    }
                    if (resetBlockColumn) {
                        ts.setNamedBeanHandle(getPreviousBlockHandle(row));
                    }
                    break;
                case BLOCK_COLUMN:
                    if (ts==null || ts.getCommand()==null ) {
                        msg = Bundle.getMessage("nullValue", Bundle.getMessage("CommandCol"));
                        break;
                    }
                    cmd = ts.getCommand().toUpperCase();
                    if ("SET SENSOR".equals(cmd) || "WAIT SENSOR".equals(cmd)) {
                        try {
                            jmri.Sensor s = InstanceManager.sensorManagerInstance().getSensor((String) value);
                            if (s != null) {
                                ts.setNamedBean(cmd, (String) value);
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
                                ts.setNamedBean(cmd, (String) value);
                            } else {
                                msg = Bundle.getMessage("BadWarrant", (String) value);
                            }
                        } catch (Exception ex) {
                            msg = Bundle.getMessage("BadWarrant", value, cmd) + ex;
                        }
                    } else {
                        NamedBeanHandle<?> bh = getPreviousBlockHandle(row);
                        if (bh != null) {
                            String name = bh.getBean().getDisplayName();
                            if (!name.equals(value)) {
                                msg = Bundle.getMessage("commandInBlock", name);
                                ts.setNamedBeanHandle(bh);
                            }
                        }
                    }
                    break;
                case SPEED_COLUMN:
                    break;
                default:
            }
            if (msg != null) {
                showWarning(msg);
            } else {
                fireTableRowsUpdated(row, row);
            }
        }

        private NamedBeanHandle <? extends NamedBean> getPreviousBlockHandle(int row) {
            for (int i = row; i > 0; i--) {
                NamedBeanHandle <? extends NamedBean> bh = _throttleCommands.get(i - 1).getNamedBeanHandle();
                if (bh != null && (bh.getBean() instanceof OBlock)) {
                    return bh;
                }
            }
            return null;
        }

    }
    private final static Logger log = LoggerFactory.getLogger(WarrantFrame.class);
}