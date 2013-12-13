package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
/*
import javax.swing.TransferHandler;
import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
*/
import jmri.InstanceManager;
import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

import jmri.jmrit.picker.PickListModel;

/**
 * WarrantFame creates and edits Warrants
 * <P>
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
 * This class is a window for creating and editing Warrants.
 * <p>
 * @author	Pete Cressman  Copyright (C) 2009, 2010
 */
public class WarrantFrame extends WarrantRoute {

    static int ROW_HEIGHT;

    JMenu _warrantMenu;

    private Warrant             _warrant;
    private ThrottleTableModel  _commandModel;
    private JTable      _commandTable;
    private JScrollPane _throttlePane;
    private boolean     _create;    

    private ArrayList <ThrottleSetting> _throttleCommands = new ArrayList <ThrottleSetting>();
    private long 			_startTime;
    private LearnThrottleFrame _learnThrottle = null;
    private DccLocoAddress 	_locoAddress = null;

    JTextField  _sysNameBox;
    JTextField  _userNameBox;

    JTabbedPane _tabbedPane;
    JPanel      _routePanel;
    JPanel      _commandPanel;
    RosterEntry _train;
    JComboBox _rosterBox = new JComboBox();
    JTextField  _dccNumBox = new JTextField();
    JTextField  _trainNameBox = new JTextField();
    JTextField  _throttleFactorBox =  new JTextField();
    JRadioButton _runProtect = new JRadioButton(Bundle.getMessage("RunProtected"), true);
    JRadioButton _runBlind = new JRadioButton(Bundle.getMessage("RunBlind"), false);
    JRadioButton _halt = new JRadioButton(Bundle.getMessage("Halt"), false);
    JRadioButton _resume = new JRadioButton(Bundle.getMessage("Resume"), false);
    JRadioButton _abort = new JRadioButton(Bundle.getMessage("Abort"), false);
    JRadioButton _invisible = new JRadioButton();
    JTextField  _statusBox = new JTextField(30);

    JTextField  _searchDepth =  new JTextField();
    JTextField  _searchStatus =  new JTextField();
    private int _maxBlocks = 20;
    
    /**
    *  Constructor for existing warrant
    */
    public WarrantFrame(String warrantName) {
        super();
        _warrant = InstanceManager.getDefault(WarrantManager.class).getWarrant(warrantName);
        _create = false;
        setup();
        init();
        if (routeIsValid()!=null) { findRoute(_maxBlocks); }
    }
        
    public WarrantFrame(Warrant w) {
        super();
        _warrant = w;
        _create = false;
        setup();
        init();
        if (routeIsValid()!=null) { findRoute(_maxBlocks); }
    }
        
    /**
    *  Constructor for new warrant and GUI
    */
    public WarrantFrame(Warrant warrant, boolean create) {
        super();
        _warrant = warrant;
        if (!create) {
            // this is a concatenation of warrants
            setup();
            create = true;  // allows warrant to be registered
        } else {
            getRoster();    // also done in setup()
        }
        _create = create;
        init();
    }

    /**
    * Set up an existing warrant
    */
    private void setup() {
        // use local copies until input boxes are set
    	setOriginBoxes(_warrant.getfirstOrder());
    	setDestinationBoxes(_warrant.getLastOrder());
    	setViaBoxes(_warrant.getViaOrder());
    	setAvoidBoxes(_warrant.getAvoidOrder());
        setOrders(_warrant.getOrders());

        List <ThrottleSetting> tList = _warrant.getThrottleCommands();
        for (int i=0; i<tList.size(); i++) {
            ThrottleSetting ts = new ThrottleSetting(tList.get(i));
            _throttleCommands.add(ts);
        }
        getRoster();
        String id = _warrant.getTrainId();
        if (id==null || id.length()==0 || !setTrainInfo(id, false)) {
            jmri.DccLocoAddress address = _warrant.getDccAddress();
            if (address!=null) {
                _dccNumBox.setText(address.toString());
            }
        }
        _trainNameBox.setText(_warrant.getTrainName());
        _runBlind.setSelected(_warrant.getRunBlind());
    }

    protected void init() {
    	super.init();
        doSize(_searchDepth, 30, 10);
        doSize(_searchStatus, 50, 30);

        _commandModel = new ThrottleTableModel();

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5,5));

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
                public void windowClosing(java.awt.event.WindowEvent e) {
                    dispose();
                }
            });

        makeMenus();
        setTitle(_warrant.getDisplayName());
        setContentPane(contentPane);
        setLocation(0,100);
        setVisible(true);
        pack();
    }

    private JPanel makeTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(2*STRUT_SIZE));
        panel.add(new JLabel(Bundle.getMessage("SystemName")));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        _sysNameBox =  new JTextField(_warrant.getSystemName());
        _sysNameBox.setBackground(Color.white);        
        _sysNameBox.setEditable(false);
        panel.add(_sysNameBox);
        panel.add(Box.createHorizontalStrut(2*STRUT_SIZE));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(new JLabel(Bundle.getMessage("UserName")));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        _userNameBox =  new JTextField(_warrant.getUserName());
        panel.add(_userNameBox);
        panel.add(Box.createHorizontalStrut(2*STRUT_SIZE));
        topPanel.add(panel);
        topPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        return topPanel;
    }

    private JPanel makeFindRouteTabPanel() {
        JPanel tab1 = new JPanel();
        tab1.setLayout(new BoxLayout(tab1, BoxLayout.X_AXIS));

        JPanel topLeft = new JPanel();
        topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));
        
        topLeft.add(makeBlockPanels());
        
        topLeft.add(Box.createVerticalStrut(STRUT_SIZE));
        tab1.add(topLeft);

        tab1.add(Box.createHorizontalStrut(STRUT_SIZE));
        JPanel topRight = new JPanel();
        topRight.setLayout(new BoxLayout(topRight, BoxLayout.X_AXIS));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        JButton button = new JButton(Bundle.getMessage("Calculate"));
        button.setMaximumSize(button.getPreferredSize());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	calculate();
            }
       });
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JPanel pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(new JLabel(Bundle.getMessage("CalculateRoute")));
        p.add(pp);
        pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(button);
        p.add(pp);
        panel.add(p);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));

        button = new JButton(Bundle.getMessage("Stop"));
        button.setMaximumSize(button.getPreferredSize());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	stopRouteFinder(); 
            }
        });

        int numBlocks = InstanceManager.getDefault(OBlockManager.class).getSystemNameList().size();
        if (numBlocks/6 > _maxBlocks) {
            _maxBlocks = numBlocks/6;
        }
        _searchDepth.setText(Integer.toString(_maxBlocks));
        _searchDepth.setMaximumSize(new Dimension(20, _searchDepth.getPreferredSize().height));
        _searchDepth.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        _searchDepth.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        p = new JPanel();
        p.setLayout(new BorderLayout());
        pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(new JLabel(Bundle.getMessage("SearchDepth")));
        p.add(pp, BorderLayout.NORTH);
        p.add(_searchDepth, BorderLayout.CENTER);
        panel.add(p);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));

        _searchStatus.setBackground(Color.white);        
        _searchStatus.setEditable(false);
        _searchStatus.setMaximumSize(_searchStatus.getPreferredSize());
        p = new JPanel();
        p.setLayout(new BorderLayout());
        pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(new JLabel(Bundle.getMessage("SearchRoute")));
        p.add(pp, BorderLayout.NORTH);
        p.add(_searchStatus, BorderLayout.CENTER);
        _searchStatus.setMaximumSize(new Dimension(20, _searchDepth.getPreferredSize().height));
        _searchStatus.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        _searchStatus.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(button);
        p.add(pp, BorderLayout.SOUTH);
        panel.add(p);
        panel.add(Box.createRigidArea(new Dimension(10,
                      topLeft.getPreferredSize().height-panel.getPreferredSize().height)));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        topRight.add(panel);
        topRight.add(Box.createHorizontalStrut(STRUT_SIZE));

        PickListModel pickListModel = PickListModel.oBlockPickModelInstance();
        topRight.add(new JScrollPane(pickListModel.makePickTable()));
        Dimension dim = topRight.getPreferredSize();
        topRight.setMinimumSize(dim);
        tab1.add(topRight);

        JPanel x = new JPanel();
        x.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth  = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        x.add(tab1, c);
        return x;
    }

    private void calculate() {
        clearWarrant();
        int depth = _maxBlocks;
        try {
            depth = Integer.parseInt(_searchDepth.getText());
        } catch (NumberFormatException nfe) {
            depth = _maxBlocks;
        }
        String msg = findRoute(depth);
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }    	
    }
    private JPanel makeSetPowerTabPanel() {
        JPanel tab2 = new JPanel();
        tab2.setLayout(new BoxLayout(tab2, BoxLayout.Y_AXIS));
        tab2.add(makeTabMidPanel());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(makeTrainPanel());
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(makeRecordPanel());
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(makePlaybackPanel());
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        tab2.add(panel);

        panel = new JPanel();
        String status = "";
        switch (_warrant.getRunMode()) {
            case Warrant.MODE_NONE:
                if (getOrders().size()==0) {
                    status = Bundle.getMessage("BlankWarrant");
                } else if (_dccNumBox.getText()==null || _dccNumBox.getText().length()==0){
                    status = Bundle.getMessage("NoLoco");
                } else if (_throttleCommands.size() == 0) {
                    status = Bundle.getMessage("NoCommands",_warrant.getDisplayName());
                } else {
                    status = Bundle.getMessage("Idle");
                }
                break;
            case Warrant.MODE_LEARN:
                status = Bundle.getMessage("Learning",
                                _warrant.getCurrentBlockOrder().getBlock().getDisplayName());
                break;
            case Warrant.MODE_RUN:
            case Warrant.MODE_MANUAL: 
                status = _warrant.getRunningMessage();
                break;
        }

        panel.add(makeTextBoxPanel(false, _statusBox, "Status", false));
        _statusBox.setMinimumSize(new Dimension(300, _statusBox.getPreferredSize().height));
        _statusBox.setMaximumSize(new Dimension(900, _statusBox.getPreferredSize().height));
        _statusBox.setText(status);
        panel.add(_statusBox);
        tab2.add(panel);

        return tab2;
    }

    private void getRoster() {
        List<RosterEntry> list = Roster.instance().matchingList(null, null, null, null, null, null, null);
        _rosterBox.setRenderer(new jmri.jmrit.roster.swing.RosterEntryListCellRenderer());
        _rosterBox.addItem(" ");
        for (int i = 0; i < list.size(); i++) {
            RosterEntry r = list.get(i);
            _rosterBox.addItem(r.titleString());
        }
        _rosterBox.addItem(Bundle.getMessage("noSuchAddress"));
        //_rosterBox = Roster.instance().fullRosterComboBox();
        _rosterBox.setMaximumSize(_rosterBox.getPreferredSize());
        _rosterBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setTrainInfo((String)_rosterBox.getSelectedItem(), false);
                }
        });
    }

    private JPanel makeTrainPanel() {
        JPanel trainPanel = new JPanel();
        trainPanel.setLayout(new BoxLayout(trainPanel, BoxLayout.X_AXIS));
        trainPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(makeTextBoxPanel(false, _trainNameBox, "TrainName", true));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(makeBoxPanel(false, _rosterBox, "Roster"));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(makeTextBoxPanel(false, _dccNumBox, "DccAddress", true));
        trainPanel.add(panel);
        trainPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        
        _dccNumBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTrainInfo(_dccNumBox.getText(), true);
            }
        });
        JPanel x = new JPanel();
        x.setLayout(new BoxLayout(x, BoxLayout.Y_AXIS));
        x.add(trainPanel);
//        x.add(Box.createRigidArea(new Dimension(600, 2)));

        JPanel edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                                                        Bundle.getMessage("SetPower"),
                                                        javax.swing.border.TitledBorder.CENTER,
                                                        javax.swing.border.TitledBorder.TOP));
        edge.add(x);
        return edge;
    }

    private JPanel makeRecordPanel() {
        JPanel learnPanel = new JPanel();
        learnPanel.setLayout(new BoxLayout(learnPanel, BoxLayout.Y_AXIS));

        JButton startButton = new JButton(Bundle.getMessage("Start"));
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
        learnPanel.add(startButton);
        learnPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        learnPanel.add(stopButton);
        learnPanel.add(Box.createRigidArea(new Dimension(30+stopButton.getPreferredSize().width,10)));

        JPanel edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                                                        Bundle.getMessage("LearnMode"),
                                                        javax.swing.border.TitledBorder.CENTER,
                                                        javax.swing.border.TitledBorder.TOP));
        edge.add(learnPanel);
        return edge;
    }

    private JPanel makePlaybackPanel() {
        JPanel runPanel = new JPanel();
        runPanel.setLayout(new BoxLayout(runPanel, BoxLayout.X_AXIS));
        runPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel panel = new JPanel(); 
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        ButtonGroup group = new ButtonGroup();
        group.add(_runProtect);
        group.add(_runBlind);
        panel.add(_runProtect);
        panel.add(_runBlind);
        runPanel.add(panel);
        runPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        _throttleFactorBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Transient variable, just verify it is a float.
                try {
                    Float.parseFloat(_throttleFactorBox.getText());
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("MustBeFloat"),
                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                    _throttleFactorBox.setText("1.0");
                }
            }
        });
        _throttleFactorBox.setToolTipText(Bundle.getMessage("TooltipThrottleFactor"));
        _runBlind.setSelected(_warrant.getRunBlind());

        panel = new JPanel(); 
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        //panel.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel bPanel = new JPanel();
        bPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton runButton = new JButton(Bundle.getMessage("ARun"));
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runTrain(Warrant.MODE_RUN);
            }
        });
        bPanel.add(runButton);
        panel.add(bPanel);
        //panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(makeTextBoxPanel(true, _throttleFactorBox, "ThrottleFactor", true));
        _throttleFactorBox.setMaximumSize(new Dimension(100, _throttleFactorBox.getPreferredSize().height));
        _throttleFactorBox.setMinimumSize(new Dimension(30, _throttleFactorBox.getPreferredSize().height));
        _throttleFactorBox.setText("1.0");
        runPanel.add(panel);
        runPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
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
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.X_AXIS));
        _routePanel = makeRouteTablePanel(); 
        tablePanel.add(_routePanel);
        tablePanel.add(Box.createHorizontalStrut(5));
        tablePanel.add(makeThrottleTablePanel());
        boolean show = (_throttleCommands.size() > 0); 
        _routePanel.setVisible(!show);
        _commandPanel.setVisible(show);
        midPanel.add(tablePanel);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        ButtonGroup group = new ButtonGroup();
        JRadioButton showRoute = new JRadioButton(Bundle.getMessage("showRoute"), !show);
        JRadioButton showScript = new JRadioButton(Bundle.getMessage("showScript"), show);
        group.add(showRoute);
        group.add(showScript);
        buttonPanel.add(showRoute);
        buttonPanel.add(showScript);
        showRoute.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _routePanel.setVisible(true);
                _commandPanel.setVisible(false);
                }
            });
        showScript.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _routePanel.setVisible(false);
                _commandPanel.setVisible(true);
                }
            });
        midPanel.add(buttonPanel);
        midPanel.add(Box.createVerticalStrut(STRUT_SIZE));


        return midPanel;
    }

    private JPanel makeThrottleTablePanel() {
        _commandTable = new JTable(_commandModel);
        _commandTable.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
//        _commandTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i=0; i<_commandModel.getColumnCount(); i++) {
            int width = _commandModel.getPreferredWidth(i);
            _commandTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        } 
        _throttlePane = new JScrollPane(_commandTable);
        ROW_HEIGHT = _commandTable.getRowHeight();
        Dimension dim = _commandTable.getPreferredSize();
        dim.height = ROW_HEIGHT*8;
        _throttlePane.setPreferredSize(dim);
        _throttlePane.getViewport().setPreferredSize(dim);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalStrut(3*STRUT_SIZE));

        JButton insertButton =  new JButton(Bundle.getMessage("buttonInsertRow"));
        insertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertRow();
            }
        });
        buttonPanel.add(insertButton);
        buttonPanel.add(Box.createVerticalStrut(2*STRUT_SIZE));

        JButton deleteButton =  new JButton(Bundle.getMessage("buttonDeleteRow"));
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteRow();
            }
        });
        buttonPanel.add(deleteButton);
        //buttonPanel.add(Box.createVerticalStrut(3*STRUT_SIZE));

        _commandPanel = new JPanel();
        _commandPanel.setLayout(new BoxLayout(_commandPanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(Bundle.getMessage("CommandTableTitle"));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
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
        if (row<0) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("selectRow"),
            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        _throttleCommands.add(row, new ThrottleSetting(0, null, null, null));
        _commandModel.fireTableDataChanged();
    }

    private void deleteRow() {
        int row = _commandTable.getSelectedRow();
        if (row<0) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("selectRow"),
            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        ThrottleSetting cmd = _throttleCommands.get(row);
        if (cmd!=null) {
        	String c = cmd.getCommand();
            if (c!=null && c.trim().toUpperCase().equals("NOOP")) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("cannotDeleteNoop"),
                Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            long time = cmd.getTime();
            if ((row+1) < _throttleCommands.size()) {
                time += _throttleCommands.get(row+1).getTime(); 
                _throttleCommands.get(row+1).setTime(time);
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
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalStrut(10*STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        panel.add(saveButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        buttonPanel.add(panel);
        buttonPanel.add(Box.createHorizontalStrut(3*STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JButton copyButton = new JButton(Bundle.getMessage("ButtonCopy"));
        copyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copy();
            }
        });
        panel.add(copyButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        buttonPanel.add(panel);
        buttonPanel.add(Box.createHorizontalStrut(3*STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        panel.add(cancelButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        buttonPanel.add(panel);
        buttonPanel.add(Box.createHorizontalStrut(3*STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InstanceManager.getDefault(WarrantManager.class).deregister(_warrant);
                _warrant.dispose();
                WarrantTableAction.updateWarrantMenu();
                dispose();
            }
        });
        panel.add(deleteButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        buttonPanel.add(panel);
        buttonPanel.add(Box.createHorizontalGlue());

        return buttonPanel;
    }

    static protected JPanel makeTextBoxPanel(boolean vertical, JTextField textField, String label, boolean editable) {
        JPanel panel = makeBoxPanel(vertical, textField, label);
        textField.setEditable(editable);
        textField.setBackground(Color.white);        
        return panel;
    }

    static protected JPanel makeBoxPanel(boolean vertical, JComponent textField, String label) {
        JPanel panel = new JPanel();
        JLabel l = new JLabel(Bundle.getMessage(label));
        if (vertical) {
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            l.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            textField.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            panel.add(Box.createVerticalStrut(STRUT_SIZE));
        } else {
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            textField.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        }
        panel.add(l);
        if (!vertical) {
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        }
        textField.setMaximumSize(new Dimension(300, textField.getPreferredSize().height));
        textField.setMinimumSize(new Dimension(200, textField.getPreferredSize().height));
        panel.add(textField);
        if (vertical) {
            panel.add(Box.createVerticalStrut(STRUT_SIZE));        	
        } else {
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));        	
        }
        return panel;
    }

    private void doControlCommand(int cmd) {
        if (log.isDebugEnabled()) log.debug("actionPerformed on doControlCommand  cmd= "+cmd);
        if (_warrant.getRunMode() != Warrant.MODE_RUN) {
            JOptionPane.showMessageDialog(this, 
                    Bundle.getMessage("NotRunning", _warrant.getDisplayName()),
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
        addHelpMenu("package.jmri.jmrit.logix.WarrantTable", true);
    }

    private boolean setTrainInfo(String name, boolean isAddress) {
        if (log.isDebugEnabled()) log.debug("setTrainInfo for: "+name+" isAddress= "+isAddress);
        if (isAddress)  {
            _dccNumBox.setText(name);
        }
        if (name != null && name.length()>0) {
            _train = null;
            if (isAddress)  {
                int index = name.indexOf('(');
                if (index >= 0) {
                    name = name.substring(0, index);
                }
                List<RosterEntry> l = Roster.instance().matchingList(null, null, name, null, null, null, null );
                if (l.size() > 0) {
                    _train = l.get(0);
                }
            } else {
                _train = Roster.instance().entryFromTitle(name);
            }
            if (_train !=null) {
                _trainNameBox.setText(_train.getRoadNumber());
                _dccNumBox.setText(_train.getDccLocoAddress().toString());
                _rosterBox.setSelectedItem(_train.getId());
            } else {
                _rosterBox.setSelectedItem(Bundle.getMessage("noSuchAddress"));
                return false;
            }
        }
        String n = _trainNameBox.getText();
        if (n==null ||n.length()==0 || _train==null) {
        	_trainNameBox.setText(_dccNumBox.getText());
        }
       if (_tabbedPane!=null) {
            _tabbedPane.invalidate();
        }
        return true;
    }

    private void clearWarrant() {
        _throttleCommands = new ArrayList <ThrottleSetting>();
        _commandModel.fireTableDataChanged();
        clearRoute();
        _searchStatus.setText("");
    }
    
	public void selectedRoute(ArrayList <BlockOrder> orders) {
        _tabbedPane.setSelectedIndex(1);		
	}

    protected RosterEntry getTrain() {
        return _train;
    }

    /******************* Learn or Run a train *******************/
    /**
     * all non-null returns are fatal
     * @return
     */
    private String checkTrainId() {
    	String msg = null;
        if (_warrant.getRunMode()!=Warrant.MODE_NONE) {
        	msg = Bundle.getMessage("TrainRunning", _trainNameBox.getText());
            return msg;
        }
        if (getOrders().size()==0) {
            msg = Bundle.getMessage("NoRouteSet", _originBlockBox.getText(), _destBlockBox.getText());
            return msg;
        } 
    	if (_train!=null) {
            _locoAddress = _train.getDccLocoAddress();
            if (_locoAddress==null) {
                _locoAddress = getLocoAddress();
            }

        } else {
            _locoAddress = getLocoAddress();
        }
        if (_locoAddress==null) {
            msg = Bundle.getMessage("NoRosterEntry");
        }
        _statusBox.setText(msg);
    	return msg;
    }
    /**
     * non-null returns not necessarily fatal, but may require used decision or action
     */
    private String setupRun() {
    	String msg = null;
    	List<BlockOrder> orders = getOrders();
    	msg = _warrant.allocateRoute(orders);    		
        if (msg==null) {
            msg = _warrant.setRoute(0, orders);
        }
        if (msg==null) {
        	msg = _warrant.checkStartBlock();
        }
    	return msg;
    }
    
    private void runLearnModeTrain() {
    	String msg = checkTrainId();
    	if (msg==null) {
            msg = setupRun();
            if (msg != null) {
            	OBlock block = _warrant.getBlockAt(0);
            	if (msg.equals(Bundle.getMessage("BlockDark", block.getDisplayName()))) {
                    if (JOptionPane.showConfirmDialog(this, msg+
                    		Bundle.getMessage("OkToRun",""),
                            Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION, 
                                JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
                             return;
                         }
                    msg = null;
            	}
            }
    	}
    	if (msg==null) {
            if (_throttleCommands.size() > 0) {
                if (JOptionPane.showConfirmDialog(this, Bundle.getMessage("deleteCommand"),
                   Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION, 
                       JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
                    return;
                }
                _throttleCommands = new ArrayList <ThrottleSetting>();
                _commandModel.fireTableDataChanged();
            }
            if (_learnThrottle==null) {
                _learnThrottle = new LearnThrottleFrame(this);
            } else {
                _learnThrottle.setVisible(true);
            }    		
            msg = _warrant.setThrottleFactor(_throttleFactorBox.getText());
    	}
        if (msg==null) {
        	msg = _warrant.checkRoute();
        }
        if (msg==null) {
            String trainName = _trainNameBox.getText();
            if (trainName!=null && trainName.length()>0) {
                _warrant.setTrainName(trainName);
            }
            _startTime = System.currentTimeMillis();
            _warrant.addPropertyChangeListener(this);
            msg = _warrant.setRunMode(Warrant.MODE_LEARN, _locoAddress, _learnThrottle, 
                                          _throttleCommands, _runBlind.isSelected());
        }
        if (msg!=null) {
            stopRunTrain();
           JOptionPane.showMessageDialog(this, msg, Bundle.getMessage("WarningTitle"), 
                                          JOptionPane.WARNING_MESSAGE);
           _statusBox.setText(msg);
        }
    }

    private void runTrain(int mode) {
    	String msg = checkTrainId();
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg, Bundle.getMessage("WarningTitle"), 
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (_throttleCommands==null || _throttleCommands.size()==0)  {
        	msg = Bundle.getMessage("NoCommands",_warrant.getDisplayName());
            JOptionPane.showMessageDialog(this, msg, 
                          Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            _warrant.deAllocate();
           return;
        }
    	msg = setupRun();
    	if (msg!=null) {
            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("OkToRun", msg), Bundle.getMessage("WarningTitle"), 
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
                _warrant.deAllocate();
                _statusBox.setText(msg);
                return;
            }
    	}
        String trainName = _trainNameBox.getText();
        if (trainName!=null && trainName.length()>0) {
            _warrant.setTrainName(trainName);
        }
        _warrant.addPropertyChangeListener(this);
        msg = _warrant.setRunMode(Warrant.MODE_RUN, _locoAddress, null, 
                                      _throttleCommands, _runBlind.isSelected());
        if (msg!=null) {
            stopRunTrain();
            JOptionPane.showMessageDialog(this, msg,
                                Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            _statusBox.setText(msg);
            return;
        }
    }

    protected void stopRunTrain() {
    	_warrant.deAllocate();
        _warrant.setRunMode(Warrant.MODE_NONE, null, null, null, false);
        _warrant.removePropertyChangeListener(this);
        if (_learnThrottle!=null) {
        	if (_learnThrottle.getSpeedSetting()>0.0) {
                _learnThrottle.setSpeedSetting(-0.5F);
                _learnThrottle.setSpeedSetting(0.0F);        		
        	}
            _learnThrottle.dispose();
            _learnThrottle = null;
        }
    }
    
    /**
    * Property names from Warrant:
    *   "runMode" - from setRunMode
    *   "controlChange" - from controlRunTrain
    *   "blockChange" - from goingActive
    *   "allocate" - from allocateRoute, deAllocate
    *   "setRoute" - from setRoute, goingActive
    * Property names from Engineer:
    *   "Command" - from run
    *   "SpeedRestriction" - ThrottleRamp run 
    * Property names from RouteFinder:
    *   "RouteSearch" - from run
    */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        String property = e.getPropertyName();
//        if (log.isDebugEnabled()) log.debug("propertyChange \""+property+
//                                            "\" old= "+e.getOldValue()+" new= "+e.getNewValue()+
//                                            " source= "+e.getSource().getClass().getName());
        if (property.equals("DnDrop")) {
        	doAction(e.getSource());
        }
        else if (_warrant!=null) {
            String item = "Error";
            switch (_warrant.getRunMode()) {
                case Warrant.MODE_NONE:
                    _warrant.removePropertyChangeListener(this);
                    item = Bundle.getMessage("Idle");
                    break;
                case Warrant.MODE_LEARN:
                    if (property.equals("blockChange")) {
                        setThrottleCommand("NoOp", Bundle.getMessage("Mark"));
                    } else if (property.equals("blockSkip")) {
                        setThrottleCommand("NoOp", Bundle.getMessage("Skip"));
                    } else if (property.equals("abortLearn")) {
                        stopRunTrain();
                    }
                    item = Bundle.getMessage("Learning",
                                _warrant.getCurrentBlockOrder().getBlock().getDisplayName());
                    break;
                case Warrant.MODE_RUN:
                case Warrant.MODE_MANUAL:
                    item = _warrant.getRunningMessage();
                    scrollCommandTable(_warrant.getCurrentCommandIndex());
                    break;
            }
            _statusBox.setText(item);
        }
        invalidate();
    }

    protected void setThrottleCommand(String cmd, String value) {
        long endTime = System.currentTimeMillis();
        long time = endTime - _startTime;
        _startTime = endTime;
        BlockOrder bo = _warrant.getCurrentBlockOrder();
        String bName; 
        if (bo==null) {
            bName = Bundle.getMessage("NoBlock");
        } else {
            bName = _warrant.getCurrentBlockOrder().getBlock().getDisplayName();
        }
        _throttleCommands.add(new ThrottleSetting(time, cmd, value, bName));
        _commandModel.fireTableDataChanged();

        scrollCommandTable(_commandModel.getRowCount());
    }

    private void scrollCommandTable(int row) {
        JScrollBar bar = _throttlePane.getVerticalScrollBar();
        bar.setValue(row*ROW_HEIGHT);
//        bar.setValue(bar.getMaximum());
    }

    private DccLocoAddress getLocoAddress() {
        String addr = _dccNumBox.getText();
        String msg = null;
        if (addr!= null && addr.length() != 0) {
            boolean isLong = false;
            int dccNum = 0;
        	addr = addr.toUpperCase().trim();
    		Character ch = addr.charAt(addr.length()-1);
    		try {
        		if (!Character.isDigit(ch)) {
        			if (ch!='S' && ch!='L' && ch!=')') {
        				msg = Bundle.getMessage("BadDccAddress", addr);
        			}
        			if (ch==')') {
                    	dccNum = Integer.parseInt(addr.substring(0, addr.length()-3));
                    	ch = addr.charAt(addr.length()-2);
                    	isLong = (ch=='L');
        			} else {
                    	dccNum = Integer.parseInt(addr.substring(0, addr.length()-1));        				
                    	isLong = (ch=='L');
        			}
        		} else {
            		dccNum = Integer.parseInt(addr);
            		ch = addr.charAt(0);
                    isLong = (ch=='0' || dccNum>255);  // leading zero means long
                    addr = addr + (isLong?"L":"S");
        		}
        		if (msg==null) {
                    return new DccLocoAddress(dccNum, isLong);
        		}
            } catch (NumberFormatException nfe) {
                msg = Bundle.getMessage("BadDccAddress", addr);
            }
        } else {
        	msg = Bundle.getMessage("NoAddress", _warrant.getDisplayName());
        }
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);        	
        }
        return null;
    }

    private void save() {
    	String msg = routeIsValid();
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("SaveError")+" - "+msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        _warrant.clearAll();
        _warrant.setViaOrder(getViaBlockOrder());
        _warrant.setAvoidOrder(getAvoidBlockOrder());
        _warrant.addBlockOrders(getOrders());
        for (int i=0; i<_throttleCommands.size(); i++) {
            _warrant.addThrottleCommand(new ThrottleSetting(_throttleCommands.get(i)));
        }
        String name = _trainNameBox.getText();
        if (name==null ||name.length()==0 || _train==null) {
        	name = _dccNumBox.getText();
        }
        _warrant.setTrainName(name);
        if (_train != null){
            _warrant.setTrainId(_train.getId());
            _warrant.setDccAddress(_train.getDccLocoAddress());
        } else {
            _warrant.setDccAddress(getLocoAddress());
        }
        _warrant.setRunBlind(_runBlind.isSelected());
        _warrant.setUserName(_userNameBox.getText());

        if (log.isDebugEnabled()) log.debug("warrant saved _train "+(_train != null)+", name= "+_trainNameBox.getText());

        if (_create) {
            InstanceManager.getDefault(WarrantManager.class).register(_warrant);
            WarrantTableAction.updateWarrantMenu(); 
        }
    }

    protected void setWarrant(Warrant w) {
    	_warrant = w;
        _sysNameBox.setText(w.getSystemName());
        _userNameBox.setText(w.getUserName());
    }
    private void copy() {
        String sysName = _warrant.getSystemName();
        String userName = _warrant.getUserName();
        if (userName!=null && userName.length()>0) {
        	sysName = sysName+"("+userName+")";
        }
        WarrantTableAction.CreateWarrantFrame f = new WarrantTableAction.CreateWarrantFrame();
        f.setVisible(true);
        try {
            f.initComponents();
            f.concatenate(_warrant, null);
        } catch (Exception ex ) { log.error("error making CreateWarrantFrame", ex);}
    }

    public void dispose() {
    	if (_warrant!=null) {
            WarrantTableAction.closeWarrantFrame(_warrant.getDisplayName());    		
    	}
        super.dispose();
    }

    /************************* Throttle Table ******************************/

    class ThrottleTableModel extends AbstractTableModel {
        public static final int ROW_NUM = 0;
        public static final int TIME_COLUMN = 1;
        public static final int COMMAND_COLUMN =2;
        public static final int VALUE_COLUMN =3;
        public static final int BLOCK_COLUMN = 4;
        public static final int NUMCOLS = 5;

        public ThrottleTableModel() {
            super();
        }

        public int getColumnCount () {
            return NUMCOLS;
        }

        public int getRowCount() {
            return _throttleCommands.size();
        }

        public String getColumnName(int col) {
            switch (col) {
                case ROW_NUM: return "#";
                case TIME_COLUMN: return Bundle.getMessage("TimeCol");
                case COMMAND_COLUMN: return Bundle.getMessage("CommandCol");
                case VALUE_COLUMN: return Bundle.getMessage("ValueCol");
                case BLOCK_COLUMN: return Bundle.getMessage("BlockCol");
            }
            return "";
        }


        public boolean isCellEditable(int row, int col) {
            if (row==ROW_NUM) { return false; }
            return true;
        }

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
        	if (row >= _throttleCommands.size()){
        		log.debug("row is greater than throttle command size");
        		return "";
        	}
            ThrottleSetting ts = _throttleCommands.get(row);
            if (ts == null){
            	log.debug("Throttle setting is null!");
            	return "";
            }
            switch (col) {
                case ROW_NUM:
                    return Integer.valueOf(row+1);
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

        public void setValueAt(Object value, int row, int col) {
            ThrottleSetting ts = _throttleCommands.get(row);
            String msg = null;
            switch (col) {
                case TIME_COLUMN:
                    long time = 0;
                    try { 
                        time = Long.parseLong((String)value);
                        if (time < 0) {
                            msg = Bundle.getMessage("InvalidTime", (String)value); 
                        }
                        ts.setTime(time);
                    } catch (NumberFormatException nfe) {
                        msg = Bundle.getMessage("InvalidTime", (String)value); 
                    }
                    break;
                case COMMAND_COLUMN:
                    String cmd = ((String)value);
                    if (cmd == null || cmd.length()==0){
                        msg = Bundle.getMessage("nullValue", Bundle.getMessage("CommandCol")); 
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
                        msg = Bundle.getMessage("cannotEnterNoop", (String)value); 
                    } else if (ts.getCommand()!=null && ts.getCommand().equals("NoOp")) {
                        msg = Bundle.getMessage("cannotChangeNoop", (String)value); 
                    } else if ("SENSOR".equals(cmd) || "SET SENSOR".equals(cmd) || "SET".equals(cmd)) {
                        ts.setCommand("Set Sensor");
                    } else if ("WAIT SENSOR".equals(cmd) || "WAIT".equals(cmd)) {
                        ts.setCommand("Wait Sensor");
                    } else if ("RUN WARRANT".equals(cmd)) {
                        ts.setCommand("Run Warrant");                    	
                    } else {
                        msg = Bundle.getMessage("badCommand", (String)value); 
                    }
                    break;
                case VALUE_COLUMN:
                    if (value == null || ((String)value).length()==0){
                        msg = Bundle.getMessage("nullValue", Bundle.getMessage("ValueCol")); 
                        break;
                    }
                    cmd = ts.getCommand().toUpperCase();
                     if ("SPEED".equals(cmd)) {
                        try {
                            float speed = Float.parseFloat((String)value);
                            if (speed < 0.0f || 1.0f < speed) {
                                msg = Bundle.getMessage("badSpeed");
                            }
                        } catch (Exception e) {
                            msg = Bundle.getMessage("badSpeed");
                        }
                        ts.setValue((String)value);
                    } else if ("SPEEDSTEP".equals(cmd)) {
                        int stepMode = DccThrottle.SpeedStepMode128;
                        try {
                            switch (Integer.parseInt((String)value)) {
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
                            Boolean.parseBoolean((String)value);
                        } catch (Exception e) {
                            msg = Bundle.getMessage("invalidBoolean");
                        }
                        ts.setValue((String)value);
                    } else if (cmd.startsWith("F")) {
                        try {
                            Boolean.parseBoolean((String)value);
                        } catch (Exception e) {
                            msg = Bundle.getMessage("invalidBoolean");
                        }
                        ts.setValue((String)value);
                    } else if (cmd.startsWith("LOCKF")) {
                        try {
                            Boolean.parseBoolean((String)value);
                        } catch (Exception e) {
                            msg = Bundle.getMessage("invalidBoolean");
                        }
                        ts.setValue((String)value);
                    } else if ("SET SENSOR".equals(cmd) || "WAIT SENSOR".equals(cmd)) {
                        String v = ((String)value).toUpperCase();
                        if ("ACTIVE".equals(v) || "INACTIVE".equals(v)) {
                            ts.setValue((String)value);
                        } else {
                            msg = Bundle.getMessage("badSensorCommand");
                        }
                    } else if ("RUN WARRANT".equals(cmd)) {
                        try {
                            Integer.parseInt((String)value);
                            ts.setValue((String)value);
                        } catch (NumberFormatException nfe) {
                        	msg  = Bundle.getMessage("badValue", value, cmd);
                        }
                    }
                     ts.setBlockName(getPreviousBlockName(row));
                    break;
                case BLOCK_COLUMN:
                    cmd = ts.getCommand().toUpperCase();
                    if ("SET SENSOR".equals(cmd) || "WAIT SENSOR".equals(cmd)) {
                        try {
                            jmri.Sensor s = InstanceManager.sensorManagerInstance().getSensor((String)value);
                            if (s != null) {
                                ts.setBlockName((String)value);
                            } else {
                                msg = Bundle.getMessage("BadSensor", (String)value); 
                            }
                        } catch (Exception ex) {
                            msg = Bundle.getMessage("BadSensor", (String)value) + ex; 
                        }
                    } else if ("NOOP".equals(cmd)) {
                        msg = Bundle.getMessage("cannotChangeBlock", (String)value); 
                    } else if ("RUN WARRANT".equals(cmd)) {
                        try {
                            Warrant w = InstanceManager.getDefault(WarrantManager.class).getWarrant((String)value);
                            if (w != null) {
                                ts.setBlockName((String)value);
                            } else {
                                msg = Bundle.getMessage("BadWarrant", (String)value); 
                            }
                        } catch (Exception ex) {
                        	msg  = Bundle.getMessage("BadWarrant", value, cmd)+ex;
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
                JOptionPane.showMessageDialog(null, msg,
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            } else {
                fireTableRowsUpdated(row, row);
            }
        }
        
        private String getPreviousBlockName(int row) {
        	for (int i=row; i>0; i--) {
        		String name = _throttleCommands.get(i-1).getBlockName();
                OBlock b = InstanceManager.getDefault(OBlockManager.class).getOBlock(name);
                if (b!=null) {
                	return name;
                }
        	}       	
        	return "StartBlock";
        }

    }
    static Logger log = LoggerFactory.getLogger(WarrantFrame.class.getName());
}

