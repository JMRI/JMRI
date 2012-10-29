package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
/*
import javax.swing.TransferHandler;
import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
*/
import jmri.InstanceManager;
import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.Path;
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
public class WarrantFrame extends jmri.util.JmriJFrame implements ActionListener, PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.logix.WarrantBundle");
    static int STRUT_SIZE = 10;
    static int ROW_HEIGHT;

    JMenu _warrantMenu;

    private Warrant             _warrant;
    private RouteTableModel     _routeModel;
    private ThrottleTableModel  _commandModel;
    private JTable      _commandTable;
    private JScrollPane _throttlePane;
    private boolean     _create;    

    private ArrayList <BlockOrder> _orders = new ArrayList <BlockOrder>();
    private ArrayList <ThrottleSetting> _throttleCommands = new ArrayList <ThrottleSetting>();
    private long 			_startTime;
    private LearnThrottleFrame _learnThrottle = null;
    private DccLocoAddress 	_locoAddress = null;

    BlockOrder  _originBlockOrder;
    BlockOrder  _destBlockOrder;
    BlockOrder  _viaBlockOrder;
    BlockOrder  _avoidBlockOrder;

    JTextField  _sysNameBox;
    JTextField  _userNameBox;
    JTextField  _originBlockBox = new JTextField();
    JTextField  _destBlockBox = new JTextField();
    JTextField  _viaBlockBox =  new JTextField();
    JTextField  _avoidBlockBox =  new JTextField();
    JComboBox   _originPathBox = new JComboBox();
    JComboBox   _destPathBox = new JComboBox();
    JComboBox   _viaPathBox = new JComboBox();
    JComboBox   _avoidPathBox = new JComboBox();
    JComboBox   _originPortalBox = new JComboBox();     // exit
    JComboBox   _destPortalBox = new JComboBox();       // entrance
    int _thisActionEventId;     // id for the listener of the above items

    JTabbedPane _tabbedPane;
    JPanel      _routePanel;
    JPanel      _commandPanel;
    RosterEntry _train;
    JComboBox   _rosterBox = new JComboBox();
    JTextField  _dccNumBox = new JTextField();
    JTextField  _trainNameBox = new JTextField();
    JTextField  _throttleFactorBox =  new JTextField();
    JRadioButton _runProtect = new JRadioButton(rb.getString("RunProtected"), true);
    JRadioButton _runBlind = new JRadioButton(rb.getString("RunBlind"), false);
    JRadioButton _halt = new JRadioButton(rb.getString("Halt"), false);
    JRadioButton _resume = new JRadioButton(rb.getString("Resume"), false);
    JRadioButton _abort = new JRadioButton(rb.getString("Abort"), false);
    JRadioButton _invisible = new JRadioButton();
    JTextField  _statusBox = new JTextField(30);

    JTextField  _searchDepth =  new JTextField();
    JTextField  _searchStatus =  new JTextField();
    RouteFinder _routeFinder;
    private int _maxBlocks = 20;
    JFrame      _debugFrame;
    JDialog     _pickRouteDialog;
    
    /**
    *  Constructor for existing warrant
    */
    public WarrantFrame(String warrantName) {
        super(false, false);
        _warrant = InstanceManager.warrantManagerInstance().getWarrant(warrantName);
        _create = false;
        setup();
        init();
        if (!routeIsValid()) { findRoute(); }
    }
        
    /**
    *  Constructor for new warrant and GUI
    */
    public WarrantFrame(Warrant warrant, boolean create) {
        super(false, false);
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
        _originBlockOrder = _warrant.getfirstOrder();
        if (_originBlockOrder!=null) {
            OBlock block = _originBlockOrder.getBlock();
            String pathName = _originBlockOrder.getPathName();
            String portalName = _originBlockOrder.getExitName();
            _originBlockBox.setText(block.getDisplayName());
            setPathBox(_originPathBox, _originPortalBox, block);
            _originPathBox.setSelectedItem(pathName);
            setPortalBox(_originPathBox, _originPortalBox, _originBlockOrder);
            _originPortalBox.setSelectedItem(portalName);

            _destBlockOrder = _warrant.getLastOrder();
            block = _destBlockOrder.getBlock();
            pathName = _destBlockOrder.getPathName();
            portalName = _destBlockOrder.getExitName();
            _destBlockBox.setText(block.getDisplayName());
            setPathBox(_destPathBox, _destPortalBox, block);
            _destPathBox.setSelectedItem(pathName);
            setPortalBox(_destPathBox, _destPortalBox, _destBlockOrder);
            _destPortalBox.setSelectedItem(portalName);

            _originBlockOrder = _warrant.getfirstOrder();
            _destBlockOrder = _warrant.getLastOrder();
        }        
        _viaBlockOrder = _warrant.getViaOrder();
        if (_viaBlockOrder!=null) {
            OBlock block = _viaBlockOrder.getBlock();
            String pathName = _viaBlockOrder.getPathName();
            _viaBlockBox.setText(block.getDisplayName());
            setPathBox(_viaPathBox, null, block);
            _viaPathBox.setSelectedItem(pathName);
        }

        _avoidBlockOrder = _warrant.getAvoidOrder();
        if (_avoidBlockOrder!=null) {
            OBlock block = _avoidBlockOrder.getBlock();
            String pathName = _avoidBlockOrder.getPathName();
            _avoidBlockBox.setText(block.getDisplayName());
            setPathBox(_avoidPathBox, null, block);
            _avoidPathBox.setSelectedItem(pathName);
        }

        List <BlockOrder> oList = _warrant.getOrders();
        for (int i=0; i<oList.size(); i++) {
            BlockOrder bo = new BlockOrder(oList.get(i));
            _orders.add(bo);
        }
        List <ThrottleSetting> tList = _warrant.getThrottleCommands();
        for (int i=0; i<tList.size(); i++) {
            ThrottleSetting ts = new ThrottleSetting(tList.get(i));
            _throttleCommands.add(ts);
        }
        getRoster();
        if (!setTrainInfo(_warrant.getTrainId(), false)) {
            jmri.DccLocoAddress address = _warrant.getDccAddress();
            if (address!=null) {
                _dccNumBox.setText(address.toString());
            }
        }
        _trainNameBox.setText(_warrant.getTrainName());
        _runBlind.setSelected(_warrant.getRunBlind());
    }

    private void init() {

        doSize(_originBlockBox, 500, 200);
        doSize(_destBlockBox, 500, 200);
        doSize(_viaBlockBox, 500, 200);
        doSize(_avoidBlockBox, 500, 200);
        doSize(_originPathBox, 500, 200);
        doSize(_destPathBox, 500, 200);
        doSize(_viaPathBox, 500, 200);
        doSize(_avoidPathBox, 500, 200);
        doSize(_originPortalBox, 500, 200);
        doSize(_destPortalBox, 500, 200);
        doSize(_searchDepth, 30, 10);
        doSize(_searchStatus, 50, 30);

        _routeModel = new RouteTableModel();
        _commandModel = new ThrottleTableModel();

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5,5));

        contentPane.add(makeTopPanel(), BorderLayout.NORTH);

        _tabbedPane = new JTabbedPane();
        _tabbedPane.addTab(rb.getString("MakeRoute"), makeFindRouteTabPanel());
        _tabbedPane.addTab(rb.getString("RecordPlay"), makeSetPowerTabPanel());
        contentPane.add(_tabbedPane, BorderLayout.CENTER);
        
        contentPane.add(makeEditableButtonPanel(), BorderLayout.SOUTH);
        if (_orders.size() > 0) {
            _tabbedPane.setSelectedIndex(1);
        } 
        addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    if (_debugFrame!=null) {
                        _debugFrame.dispose();
                    }
                    if (_pickRouteDialog!=null) {
                        _pickRouteDialog.dispose();
                    }
                    dispose();
                }
            });

        makeMenus();
        setContentPane(contentPane);
        setLocation(0,100);
        setVisible(true);
        pack();
    }

    private void doSize(JComponent comp, int max, int min) {
        Dimension dim = comp.getPreferredSize();
        dim.width = max;
        comp.setMaximumSize(dim);
        dim.width = min;
        comp.setMinimumSize(dim);
    }

    private JPanel makeTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(2*STRUT_SIZE));
        panel.add(new JLabel(rb.getString("SystemName")));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        _sysNameBox =  new JTextField(_warrant.getSystemName());
        _sysNameBox.setBackground(Color.white);        
        _sysNameBox.setEditable(false);
        panel.add(_sysNameBox);
        panel.add(Box.createHorizontalStrut(2*STRUT_SIZE));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(new JLabel(rb.getString("UserName")));
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

        JPanel oPanel = makeEndPoint("OriginBlock", makeBlockBox(_originBlockBox, "OriginToolTip"), 
                                     makeLabelCombo("PathName", _originPathBox, "OriginToolTip"), 
                                     makeLabelCombo("ExitPortalName", _originPortalBox, "OriginToolTip"),
                                     "OriginToolTip");
        topLeft.add(oPanel);
        topLeft.add(Box.createVerticalStrut(STRUT_SIZE));

        oPanel = makeEndPoint("DestBlock", makeBlockBox(_destBlockBox, "DestToolTip"), 
                              makeLabelCombo("EntryPortalName", _destPortalBox, "DestToolTip"),
                              makeLabelCombo("PathName", _destPathBox, "DestToolTip"),
                              "DestToolTip");
        topLeft.add(oPanel);
        topLeft.add(Box.createVerticalStrut(STRUT_SIZE));

        oPanel = makeEndPoint("ViaBlock", makeBlockBox(_viaBlockBox, "ViaToolTip"), 
                              makeLabelCombo("PathName", _viaPathBox, "ViaToolTip"),
                              null, "ViaToolTip");
        topLeft.add(oPanel);
        topLeft.add(Box.createVerticalStrut(STRUT_SIZE));

        oPanel = makeEndPoint("AvoidBlock", makeBlockBox(_avoidBlockBox, "AvoidToolTip"), 
                              makeLabelCombo("PathName", _avoidPathBox, "AvoidToolTip"),
                              null, "AvoidToolTip");
        topLeft.add(oPanel);
        topLeft.add(Box.createVerticalStrut(STRUT_SIZE));
        tab1.add(topLeft);

        tab1.add(Box.createHorizontalStrut(STRUT_SIZE));
        JPanel topRight = new JPanel();
        topRight.setLayout(new BoxLayout(topRight, BoxLayout.X_AXIS));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        JButton button = new JButton(rb.getString("Calculate"));
        button.setMaximumSize(button.getPreferredSize());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                findRoute();
            }
       });
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JPanel pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(new JLabel(rb.getString("CalculateRoute")));
        p.add(pp);
        pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(button);
        p.add(pp);
        panel.add(p);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));

        button = new JButton(rb.getString("Stop"));
        button.setMaximumSize(button.getPreferredSize());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_routeFinder!=null) {
                    _routeFinder.quit();
                }
            }
        });

        int numBlocks = InstanceManager.oBlockManagerInstance().getSystemNameList().size();
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
        pp.add(new JLabel(rb.getString("SearchDepth")));
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
        pp.add(new JLabel(rb.getString("SearchRoute")));
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
                if (_orders.size()==0) {
                    status = WarrantTableAction.rb.getString("BlankWarrant");
                } else if (_dccNumBox.getText()==null || _dccNumBox.getText().length()==0){
                    status = WarrantTableAction.rb.getString("NoLoco");
                } else if (_throttleCommands.size() == 0) {
                    status = java.text.MessageFormat.format(rb.getString("NoCommands"),_warrant.getDisplayName());
                } else {
                    status = WarrantTableAction.rb.getString("Idle");
                }
                break;
            case Warrant.MODE_LEARN:
                status = java.text.MessageFormat.format(WarrantTableAction.rb.getString("Learning"),
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
        _rosterBox.addItem(rb.getString("noSuchAddress"));
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
                                                        rb.getString("SetPower"),
                                                        javax.swing.border.TitledBorder.CENTER,
                                                        javax.swing.border.TitledBorder.TOP));
        edge.add(x);
        return edge;
    }

    private JPanel makeRecordPanel() {
        JPanel learnPanel = new JPanel();
        learnPanel.setLayout(new BoxLayout(learnPanel, BoxLayout.Y_AXIS));

        JButton startButton = new JButton(rb.getString("Start"));
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runLearnModeTrain();
            }
        });
        JButton stopButton = new JButton(rb.getString("Stop"));
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
                                                        rb.getString("LearnMode"),
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
                    JOptionPane.showMessageDialog(null, rb.getString("MustBeFloat"),
                            rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                    _throttleFactorBox.setText("1.0");
                }
            }
        });
        _throttleFactorBox.setToolTipText(rb.getString("TooltipThrottleFactor"));
        _runBlind.setSelected(_warrant.getRunBlind());

        panel = new JPanel(); 
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        //panel.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel bPanel = new JPanel();
        bPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton runButton = new JButton(rb.getString("ARun"));
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
                                                        rb.getString("RunTrain"),
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
        tablePanel.add(makeRouteTablePanel());
        tablePanel.add(Box.createHorizontalStrut(5));
        tablePanel.add(makeThrottleTablePanel());
        boolean show = (_throttleCommands.size() > 0); 
        _routePanel.setVisible(!show);
        _commandPanel.setVisible(show);
        midPanel.add(tablePanel);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        ButtonGroup group = new ButtonGroup();
        JRadioButton showRoute = new JRadioButton(rb.getString("showRoute"), !show);
        JRadioButton showScript = new JRadioButton(rb.getString("showScript"), show);
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

    private JPanel makeRouteTablePanel() {
        JTable routeTable = new JTable(_routeModel);
        routeTable.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
        //routeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i=0; i<_routeModel.getColumnCount(); i++) {
            int width = _routeModel.getPreferredWidth(i);
            routeTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        JScrollPane tablePane = new JScrollPane(routeTable);
        Dimension dim = routeTable.getPreferredSize();
        dim.height = routeTable.getRowHeight()*8;
        tablePane.getViewport().setPreferredSize(dim);

        _routePanel = new JPanel();
        _routePanel.setLayout(new BoxLayout(_routePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(rb.getString("RouteTableTitle"));
        _routePanel.add(title, BorderLayout.NORTH);
        _routePanel.add(tablePane);
        return _routePanel;
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

        JButton insertButton =  new JButton(rb.getString("buttonInsertRow"));
        insertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertRow();
            }
        });
        buttonPanel.add(insertButton);
        buttonPanel.add(Box.createVerticalStrut(2*STRUT_SIZE));

        JButton deleteButton =  new JButton(rb.getString("buttonDeleteRow"));
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteRow();
            }
        });
        buttonPanel.add(deleteButton);
        //buttonPanel.add(Box.createVerticalStrut(3*STRUT_SIZE));

        _commandPanel = new JPanel();
        _commandPanel.setLayout(new BoxLayout(_commandPanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(rb.getString("CommandTableTitle"));
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
            JOptionPane.showMessageDialog(null, rb.getString("selectRow"),
            rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        _throttleCommands.add(row, new ThrottleSetting(0, null, null, null));
        _commandModel.fireTableDataChanged();
    }

    private void deleteRow() {
        int row = _commandTable.getSelectedRow();
        if (row<0) {
            JOptionPane.showMessageDialog(null, rb.getString("selectRow"),
            rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        ThrottleSetting cmd = _throttleCommands.get(row);
        if (cmd!=null) {
        	String c = cmd.getCommand();
            if (c!=null && c.trim().toUpperCase().equals("NOOP")) {
                JOptionPane.showMessageDialog(null, rb.getString("cannotDeleteNoop"),
                rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
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
        JButton saveButton = new JButton(rb.getString("ButtonSave"));
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
        JButton copyButton = new JButton(rb.getString("ButtonCopy"));
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
        JButton cancelButton = new JButton(rb.getString("ButtonCancel"));
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
        JButton deleteButton = new JButton(rb.getString("ButtonDelete"));
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InstanceManager.warrantManagerInstance().deregister(_warrant);
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

    private JPanel makeTextBoxPanel(boolean vertical, JTextField textField, String label, boolean editable) {
        JPanel panel = makeBoxPanel(vertical, textField, label);
        textField.setEditable(editable);
        textField.setBackground(Color.white);        
        return panel;
    }

    private JPanel makeBoxPanel(boolean vertical, JComponent textField, String label) {
        JPanel panel = new JPanel();
        JLabel l = new JLabel(rb.getString(label));
        if (vertical) {
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            l.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            textField.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        } else {
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            textField.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        }
        panel.add(l);
        if (!vertical) {
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        }
        textField.setMaximumSize(new Dimension(300, textField.getPreferredSize().height));
        textField.setMinimumSize(new Dimension(200, textField.getPreferredSize().height));
        panel.add(textField);
        return panel;
    }

    private void doControlCommand(int cmd) {
        if (log.isDebugEnabled()) log.debug("actionPerformed on doControlCommand  cmd= "+cmd);
        if (_warrant.getRunMode() != Warrant.MODE_RUN) {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                    rb.getString("NotRunning"), _warrant.getDisplayName()),
                    rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        } else {
            _warrant.controlRunTrain(cmd);
        }
        _invisible.setSelected(true);
    }

    private JPanel makeEndPoint(String title, JPanel p0, JPanel p1, JPanel p2, String tooltip) {
        JPanel oPanel = new JPanel();
        oPanel.setLayout(new BoxLayout(oPanel, BoxLayout.Y_AXIS));
        oPanel.add(new JLabel(rb.getString(title)));
        JPanel hPanel = new JPanel();
        hPanel.setLayout(new BoxLayout(hPanel, BoxLayout.X_AXIS));
        hPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        hPanel.add(p0);
        hPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        JPanel pPanel = new JPanel();
        pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.X_AXIS));
        pPanel.add(p1);
        pPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        if (p2!=null) { 
            pPanel.add(p2); 
            pPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        }
        hPanel.add(pPanel);
        oPanel.add(hPanel);
        pPanel.setToolTipText(rb.getString(tooltip));
        hPanel.setToolTipText(rb.getString(tooltip));
        oPanel.setToolTipText(rb.getString(tooltip));
        oPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        oPanel.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK));
        return oPanel;
    }
    
    private JPanel makeBlockBox(JTextField blockBox, String tooltip) {
        blockBox.setDragEnabled(true);
        blockBox.setTransferHandler(new jmri.util.DnDStringImportHandler());
        blockBox.setColumns(15);
        blockBox.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        //blockBox.setMaximumSize(new Dimension(100, blockBox.getPreferredSize().height));
        //blockBox.setDropMode(DropMode.USE_SELECTION);
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        JPanel pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(new JLabel(rb.getString("BlockName")));
        p.setToolTipText(rb.getString(tooltip));
        blockBox.setToolTipText(rb.getString(tooltip));
        p.add(pp, BorderLayout.NORTH);
        p.add(blockBox, BorderLayout.CENTER);
        blockBox.addActionListener(this);
        blockBox.addPropertyChangeListener(this);
        return p;
    }

    private JPanel makeLabelCombo(String title, JComboBox box, String tooltip) {

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        JPanel pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(new JLabel(rb.getString(title)));
        p.setToolTipText(rb.getString(tooltip));
        box.setToolTipText(rb.getString(tooltip));
        p.add(pp, BorderLayout.NORTH);
        p.add(box, BorderLayout.CENTER);
        box.addActionListener(this);
//        box.addPropertyChangeListener(this);
        box.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        //box.setMaximumSize(new Dimension(100, box.getPreferredSize().height));
        return p;
    }

    private void makeMenus() {
        setTitle(java.text.MessageFormat.format(
                                rb.getString("TitleWarrant"), _warrant.getDisplayName()));
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
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
                _trainNameBox.setText("");
                if (!isAddress) {
                    _dccNumBox.setText("");
                    _rosterBox.setSelectedItem(rb.getString("noSuchAddress"));
                } else {
                    _rosterBox.setSelectedItem(" ");
                }
                return false;
            }
        }
        if (_tabbedPane!=null) {
            _tabbedPane.invalidate();
        }
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        _thisActionEventId = e.getID();
        if (log.isDebugEnabled()) log.debug("actionPerformed: source "+((Component)obj).getName()+
                     " id= "+e.getID()+", ActionCommand= "+e.getActionCommand());
        doAction(obj);
    }
    
    void doAction(Object obj) {
        if (obj instanceof JTextField)
        {
            JTextField box = (JTextField)obj;
            //String text = box.getText();
            if (box == _originBlockBox) {
                setOriginBlock();
            } else if (box == _destBlockBox) {
                setDestinationBlock();
            } else if (box == _viaBlockBox) {
                setViaBlock();
            } else if (box == _avoidBlockBox) {
                setAvoidBlock();
            }
        } else {
            JComboBox box = (JComboBox)obj;
            if (box == _originPathBox) {
                setPortalBox(_originPathBox, _originPortalBox, _originBlockOrder);
            } else if (box == _originPortalBox) {
                _originBlockOrder.setExitName((String)_originPortalBox.getSelectedItem());
            } else if (box == _destPathBox) {
                setPortalBox(_destPathBox, _destPortalBox, _destBlockOrder);
            } else if (box == _destPortalBox) {
                _destBlockOrder.setEntryName((String)_destPortalBox.getSelectedItem());
            } else if (box == _viaPathBox) {
                String pathName = (String)_viaPathBox.getSelectedItem();
                _viaBlockOrder.setPathName(pathName);
            } else if (box == _avoidPathBox) {
                String pathName = (String)_avoidPathBox.getSelectedItem();
                _avoidBlockOrder.setPathName(pathName);
            }
            clearWarrant();
        }
    }

    private OBlock getEndPointBlock(JTextField textBox) {
        String text = textBox.getText();
        int idx = text.indexOf(java.awt.event.KeyEvent.VK_TAB);
        if (idx > 0){
            if (idx+1 < text.length()) {
                text = text.substring(idx+1);
            } else {
                text = text.substring(0, idx);
            }
        }
        textBox.setText(text);
        OBlock block = InstanceManager.oBlockManagerInstance().getOBlock(text);
        if (block == null && text.length()>0) {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                    rb.getString("BlockNotFound"), text),
                    rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
        return block;
    }

    private boolean setPathBox(JComboBox pathBox, JComboBox portalBox, OBlock block) {
    	if(log.isDebugEnabled()) log.debug("setPathBox: block= "+block.getDisplayName()); 
        pathBox.removeAllItems();
        if (portalBox!=null) {
            portalBox.removeAllItems();
        }
        List <Path> list = block.getPaths();
        if (list.size()==0) {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                    rb.getString("NoPaths"), block.getDisplayName()),
                    rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            //pack();
            return false;
        }
        for (int i=0; i<list.size(); i++) {
             pathBox.addItem(((OPath)list.get(i)).getName());
        }
        if (log.isDebugEnabled()) log.debug("setPathBox: Block "+
                     block.getDisplayName()+" has "+list.size()+" paths.");
        return true;
    }

    private void setPortalBox(JComboBox pathBox, JComboBox portalBox, BlockOrder order) {
    	if(log.isDebugEnabled()) log.debug("setPortalBox: block= "+order.getBlock().getDisplayName());
        portalBox.removeAllItems();
        String pathName = (String)pathBox.getSelectedItem();
        order.setPathName(pathName);
        OPath path = order.getPath();
        if (path != null) {
            Portal portal = path.getFromPortal();
            if (portal!=null) {
                String name = portal.getName();
                if (name!=null) { portalBox.addItem(name); }
            }
            portal = path.getToPortal();
            if (portal!=null) {
                String name = portal.getName();
                if (name!=null) { portalBox.addItem(name); }
            }
            if (log.isDebugEnabled()) log.debug("setPortalBox: Path "+path.getName()+
                         " set in block "+order.getBlock().getDisplayName());
        } else {
            if (log.isDebugEnabled()) log.debug("setPortalBox: Path set to null in block"
                         +order.getBlock().getDisplayName());
        }
    }

    private boolean setOriginBlock() {
        OBlock block = getEndPointBlock(_originBlockBox);
        boolean result = true;
        if (block == null) {
            result = false;
        } else {
            if (_originBlockOrder!= null && block==_originBlockOrder.getBlock() &&
                    pathIsValid(block, _originBlockOrder.getPathName())) {
                return true; 
            } else {
                if (pathsAreValid(block)) {
                    _originBlockOrder = new BlockOrder(block);
                    if (!setPathBox(_originPathBox, _originPortalBox, block)) {
                        result = false;
                        _originBlockBox.setText("");
                    }
                } else {
                    _originBlockBox.setText("");
                    result = false;
                }
            }
        }
        if (!result) {
            _originPathBox.removeAllItems();
            _originPortalBox.removeAllItems();
        }
        return result; 
    }

    private boolean setDestinationBlock() {
        OBlock block = getEndPointBlock(_destBlockBox);
        boolean result = true;
        if (block == null) {
            result = false;
        } else {
            if (_destBlockOrder!= null && block==_destBlockOrder.getBlock() &&
                    pathIsValid(block, _destBlockOrder.getPathName())) {
                return true; 
            } else {
                if (pathsAreValid(block)) {
                    _destBlockOrder = new BlockOrder(block);
                    if (!setPathBox(_destPathBox, _destPortalBox, block)) {
                        result = false;
                        _destBlockBox.setText("");
                    }
                } else {
                    _destBlockBox.setText("");
                    result = false;
                }
            }
        }
        if (!result) {
            _originPathBox.removeAllItems();
            _originPortalBox.removeAllItems();
        }
        return result; 
    }

    private boolean setViaBlock() {
        OBlock block = getEndPointBlock(_viaBlockBox);
        if (block == null) {
            _viaPathBox.removeAllItems();
            _viaBlockOrder = null;
            return true;
        } else {
            if (_viaBlockOrder!=null && block==_viaBlockOrder.getBlock() &&
                    pathIsValid(block, _viaBlockOrder.getPathName())) {
                return true;
            } else {
                if (pathsAreValid(block)) {
                    _viaBlockOrder = new BlockOrder(block);
                    if (!setPathBox(_viaPathBox, null, block)) {
                        _viaPathBox.removeAllItems();
                        _viaBlockBox.setText("");
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private boolean setAvoidBlock() {
        OBlock block = getEndPointBlock(_avoidBlockBox);
        if (block == null) {
            _avoidPathBox.removeAllItems();
            _avoidBlockOrder = null;
            return true;
        } else {
            if (_avoidBlockOrder!=null && block==_avoidBlockOrder.getBlock() &&
                    pathIsValid(block, _avoidBlockOrder.getPathName())) {
                return true;
            } else {
                if (pathsAreValid(block)) {
                    _avoidBlockOrder = new BlockOrder(block);
                    if (!setPathBox(_avoidPathBox, null, block)) {
                        _avoidPathBox.removeAllItems();
                        _avoidBlockBox.setText("");
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private void clearWarrant() {
        _orders = new ArrayList <BlockOrder>();
        _routeModel.fireTableDataChanged();
        _throttleCommands = new ArrayList <ThrottleSetting>();
        _commandModel.fireTableDataChanged();
        if (_debugFrame!=null) {
            _debugFrame.dispose();
            _debugFrame = null;
        }
        if (_pickRouteDialog!=null) {
            _pickRouteDialog.dispose();
            _pickRouteDialog = null;
        }
        _searchStatus.setText("");
    }
    
    /**
    * Gather parameters to search for a route
    */
    private void findRoute() {
        // read and verify origin and destination blocks/paths/portals
        String msg = null;
        String pathName = null;
        boolean ok = setOriginBlock();
        if (ok) {
            pathName = _originBlockOrder.getPathName();
            ok = (pathName!=null);
            if (ok) {
                if (_originBlockOrder.getExitName() == null) {
                    msg = java.text.MessageFormat.format(
                        rb.getString("SetExitPortal"), rb.getString("OriginBlock"));
                    ok = false;
                } else {
                    ok = pathIsValid(_originBlockOrder.getBlock(), pathName);
                }
            } else {
                msg = java.text.MessageFormat.format(
                    rb.getString("SetPath"), rb.getString("OriginBlock"));
            }
        } else {
            msg = java.text.MessageFormat.format(
                rb.getString("SetEndPoint"), rb.getString("OriginBlock"));
        }
        if (ok) {
            ok = setDestinationBlock();
            if (ok) {
                pathName = _destBlockOrder.getPathName();
                ok = (pathName!=null);
                if (ok) {
                    if (_destBlockOrder.getEntryName() == null) {
                        msg = java.text.MessageFormat.format(
                            rb.getString("SetEntryPortal"), rb.getString("DestBlock"));
                        ok = false;
                    } else {
                        ok = pathIsValid(_destBlockOrder.getBlock(), pathName);
                    }
                } else {
                    msg = java.text.MessageFormat.format(
                        rb.getString("SetPath"), rb.getString("DestBlock"));
                }
            } else {
                msg = java.text.MessageFormat.format(
                    rb.getString("SetEndPoint"), rb.getString("DestBlock"));
            }
        }
        if (ok) {
            ok = setViaBlock();
            if (ok) {
                if (_viaBlockOrder!=null && _viaBlockOrder.getPathName()==null) {
                    msg = java.text.MessageFormat.format(
                        rb.getString("SetPath"), rb.getString("ViaBlock"));
                    ok = false;
                }
            } else {
                msg = java.text.MessageFormat.format(
                    rb.getString("SetEndPoint"), rb.getString("ViaBlock"));
            }
        }
        if (ok) {
            ok = setAvoidBlock();
            if (ok) {
                if (_avoidBlockOrder!=null && _avoidBlockOrder.getPathName()==null) {
                    msg = java.text.MessageFormat.format(
                        rb.getString("SetPath"), rb.getString("AvoidBlock"));
                    ok = false;
                }
            } else {
                msg = java.text.MessageFormat.format(
                    rb.getString("SetEndPoint"), rb.getString("AvoidBlock"));
            }
        }
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg,
                    rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ok) { return; }
        calculate();
    }

    /*************** Finding the route ***********************/
    //class Finsher implements Runnable {
    //}

    private void calculate()
    {
        clearWarrant();
        if (_originBlockOrder!=null) {
            int depth = _maxBlocks;
            try {
                depth = Integer.parseInt(_searchDepth.getText());
            } catch (NumberFormatException nfe) {
                depth = _maxBlocks;
            }
            _routeFinder = new RouteFinder(this, _originBlockOrder, _destBlockOrder,
                                            _viaBlockOrder, _avoidBlockOrder, depth);
            new Thread(_routeFinder).start();
            //javax.swing.SwingUtilities.invokeLater(_routeFinder);
        }

    }

    /**
    *  Callback from RouteFinder - no routes found
    */
    protected void debugRoute(DefaultTreeModel tree) {
        if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this, java.text.MessageFormat.format(
                            rb.getString("NoRoute"),  
                            new Object[] {_originBlockOrder.getBlock().getDisplayName(), 
                                                        _originBlockOrder.getPathName(), 
                                                        _originBlockOrder.getExitName(),
                                                        _destBlockOrder.getBlock().getDisplayName(),
                                                        _destBlockOrder.getPathName() }),
                            rb.getString("WarningTitle"), JOptionPane.YES_NO_OPTION, 
                                                    JOptionPane.WARNING_MESSAGE)) {
            return; 
        }
        if (_debugFrame!=null) {
            _debugFrame.dispose();
        }
        _debugFrame = new JFrame(rb.getString("DebugRoute"));
        javax.swing.JTree dTree = new javax.swing.JTree(tree);
        dTree.setShowsRootHandles(true);
        dTree.setScrollsOnExpand(true);
        dTree.setExpandsSelectedPaths(true);
        JScrollPane treePane = new JScrollPane(dTree);
        treePane.getViewport().setPreferredSize(new Dimension(900, 300));
        _debugFrame.getContentPane().add(treePane);
        _debugFrame.setVisible(true);
        _debugFrame.pack();
    }

    /**
    *  Callback from RouteFinder - several routes found
    */
    protected void pickRoute(List <DefaultMutableTreeNode> destNodes, DefaultTreeModel tree) {
        if (destNodes.size()==1) {
            showRoute(destNodes.get(0), tree);
            _tabbedPane.setSelectedIndex(1);
            return;
        }
        _pickRouteDialog = new JDialog(this, rb.getString("DialogTitle"), false);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(5,5));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(java.text.MessageFormat.format(
                    rb.getString("NumberRoutes1"), Integer.valueOf(destNodes.size()))));
        panel.add(new JLabel(rb.getString("NumberRoutes2")));

        mainPanel.add(panel, BorderLayout.NORTH);
        ButtonGroup buttons = new ButtonGroup();

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (int i=0; i<destNodes.size(); i++) {
            JRadioButton button = new JRadioButton(java.text.MessageFormat.format(
                    rb.getString("RouteSize"), Integer.valueOf(i+1), 
                    Integer.valueOf(destNodes.get(i).getLevel())) );
            button.setActionCommand(""+i);
            buttons.add(button);
            panel.add(button);
        }
        JScrollPane scrollPane = new JScrollPane(panel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        JButton ok = new JButton(rb.getString("ButtonSelect"));
        ok.addActionListener(new ActionListener() {
                ButtonGroup buttons;
                JDialog dialog;
                List <DefaultMutableTreeNode> destNodes;
                DefaultTreeModel tree;
                public void actionPerformed(ActionEvent e) {
                    if (buttons.getSelection()!=null) {
                        int i = Integer.parseInt(buttons.getSelection().getActionCommand());
                        showRoute(destNodes.get(i), tree);
                        _tabbedPane.setSelectedIndex(1);
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, rb.getString("SelectRoute"),
                                            rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                    }
                }
                ActionListener init(ButtonGroup bg, JDialog d, List <DefaultMutableTreeNode> dn,
                                    DefaultTreeModel t) {
                    buttons = bg;
                    dialog = d;
                    destNodes = dn;
                    tree = t;
                    return this;
                }
            }.init(buttons, _pickRouteDialog, destNodes, tree));
        ok.setMaximumSize(ok.getPreferredSize());
        JButton show = new JButton(rb.getString("ButtonReview"));
        show.addActionListener(new ActionListener() {
                ButtonGroup buttons;
                List <DefaultMutableTreeNode> destNodes;
                DefaultTreeModel tree;
                public void actionPerformed(ActionEvent e) {
                    if (buttons.getSelection()!=null) {
                        int i = Integer.parseInt(buttons.getSelection().getActionCommand());
                        showRoute(destNodes.get(i), tree);
                    } else {
                        JOptionPane.showMessageDialog(null, rb.getString("SelectRoute"),
                                            rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                    }
                }
                ActionListener init(ButtonGroup bg, List <DefaultMutableTreeNode> dn,
                                    DefaultTreeModel t) {
                    buttons = bg;
                    destNodes = dn;
                    tree = t;
                    return this;
                }
            }.init(buttons, destNodes, tree));
        show.setMaximumSize(show.getPreferredSize());
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(show);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(ok);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        mainPanel.add(panel, BorderLayout.SOUTH);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(makeRouteTablePanel());
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(mainPanel);
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));

        _pickRouteDialog.getContentPane().add(panel);
        _pickRouteDialog.setLocation(getLocation().x+50, getLocation().y+150);
        _pickRouteDialog.pack();
        _pickRouteDialog.setVisible(true);
    }

    /**
    *  Callback from RouteFinder - exactly one route found
    */
    protected void showRoute(DefaultMutableTreeNode destNode, DefaultTreeModel tree) {
        TreeNode[] nodes = tree.getPathToRoot(destNode);
        _orders.clear();
        for (int i=0; i<nodes.length; i++) {
            _orders.add((BlockOrder)((DefaultMutableTreeNode)nodes[i]).getUserObject());
        }
        _routeModel.fireTableDataChanged();
        if (log.isDebugEnabled()) log.debug("showRoute: Route has "+_orders.size()+" orders.");
    }

    protected RosterEntry getTrain() {
        return _train;
    }


    private int getIndexOfBlock(OBlock block) {
        for (int i=0; i<_orders.size(); i++){
            if (_orders.get(i).getBlock().equals(block)) {
                return i;
            }
        }
        return -1;
    }

    /******************* Learn or Run a train *******************/
    /**
     * all non-null returns are fatal
     * @return
     */
    private String checkTrainId() {
    	String msg = null;
        if (_warrant.getRunMode()!=Warrant.MODE_NONE) {
        	msg = java.text.MessageFormat.format(rb.getString("TrainRunning"), _trainNameBox.getText());
            return msg;
        }
        if (_orders.size()==0) {
            msg = java.text.MessageFormat.format(rb.getString("NoRouteSet"),
                        _originBlockBox.getText(), _destBlockBox.getText());
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
            msg = rb.getString("NoRosterEntry");
        }
        _statusBox.setText(msg);
    	return msg;
    }
    /**
     * non-null returns not necessarily fatal, but may require used decision or action
     */
    private String setupRun() {
    	String msg = null;
    	msg = _warrant.allocateRoute(_orders);    		
        if (msg==null) {
            msg = _warrant.setRoute(0, _orders);
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
    	}
    	if (msg==null) {
            if (_throttleCommands.size() > 0) {
                if (JOptionPane.showConfirmDialog(this, rb.getString("deleteCommand"),
                   rb.getString("QuestionTitle"), JOptionPane.YES_NO_OPTION, 
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
            _statusBox.setText(msg);
           JOptionPane.showMessageDialog(this, msg, rb.getString("WarningTitle"), 
                                          JOptionPane.WARNING_MESSAGE);
        }
    }

    private void runTrain(int mode) {
    	String msg = checkTrainId();
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg, rb.getString("WarningTitle"), 
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (_throttleCommands==null || _throttleCommands.size()==0)  {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                    rb.getString("NoCommands"),_warrant.getDisplayName()), 
                          rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            _warrant.deAllocate();
            _statusBox.setText(msg);
           return;
        }
    	msg = setupRun();
    	if (msg!=null) {
            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this,
                    java.text.MessageFormat.format(WarrantTableAction.rb.getString("OkToRun"),
                    msg), WarrantTableAction.rb.getString("WarningTitle"), 
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
            _statusBox.setText(msg);
            stopRunTrain();
            JOptionPane.showMessageDialog(this, msg,
                                rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
    }

    protected void stopRunTrain() {
    	_warrant.deAllocate();
        String msg = _warrant.setRunMode(Warrant.MODE_NONE, null, null, null, false);
        _warrant.removePropertyChangeListener(this);
        if (_learnThrottle!=null) {
            _learnThrottle.setSpeedSetting(-0.5F);
            _learnThrottle.setSpeedSetting(0.0F);
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
        if (log.isDebugEnabled()) log.debug("propertyChange \""+property+
                                            "\" old= "+e.getOldValue()+" new= "+e.getNewValue()+
                                            " source= "+e.getSource().getClass().getName());
        if (property.equals("RouteSearch"))  {
            _searchStatus.setText(java.text.MessageFormat.format(rb.getString("FinderStatus"),
                           new Object[] {e.getOldValue(), e.getNewValue()}));
        } else if (property.equals("DnDrop"))  {
        	doAction(e.getSource());
        } else {
            String item = "Error";
            switch (_warrant.getRunMode()) {
                case Warrant.MODE_NONE:
                    _warrant.removePropertyChangeListener(this);
                    item = rb.getString("Idle");
                    break;
                case Warrant.MODE_LEARN:
                    if (property.equals("blockChange")) {
                        setThrottleCommand("NoOp", rb.getString("Mark"));
                    } else if (property.equals("blockSkip")) {
                        setThrottleCommand("NoOp", rb.getString("Skip"));
                    } else if (property.equals("abortLearn")) {
                        stopRunTrain();
                    }
                    item = java.text.MessageFormat.format(rb.getString("Learning"),
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
            bName = rb.getString("NoBlock");
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
        if (addr!= null && addr.length() != 0) {
            try {
                char ch = Character.toUpperCase(addr.charAt(addr.length()-1));
                boolean isLong = true;
                int n = 0;
                if (Character.isDigit(ch)){
                    n = Integer.parseInt(addr);
                } else {
                    isLong = (ch == 'L');
                    n = Integer.parseInt(addr.substring(0, addr.length()-1));
                }
                return new DccLocoAddress(n, isLong);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                    rb.getString("BadDccAddress"),addr),
                                    rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            }
        }
        return null;
    }

    private void save() {
        if (!routeIsValid()) {
            JOptionPane.showMessageDialog(this, rb.getString("SaveError"),
                    rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        _warrant.clearAll();
        _warrant.setViaOrder(_viaBlockOrder);
        _warrant.setAvoidOrder(_avoidBlockOrder);
        for (int i=0; i<_orders.size(); i++) {
            _warrant.addBlockOrder(new BlockOrder(_orders.get(i)));
        }
        for (int i=0; i<_throttleCommands.size(); i++) {
            _warrant.addThrottleCommand(new ThrottleSetting(_throttleCommands.get(i)));
        }
        _warrant.setTrainName(_trainNameBox.getText());
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
            InstanceManager.warrantManagerInstance().register(_warrant);
            WarrantTableAction.updateWarrantMenu(); 
        }
        //dispose();
    }

    private void copy() {
        if (JOptionPane.showConfirmDialog(this, java.text.MessageFormat.format(
            rb.getString("makeCopy"), _warrant.getDisplayName()),
                rb.getString("QuestionTitle"), JOptionPane.OK_CANCEL_OPTION, 
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
            String sysName = _warrant.getSystemName();
            save();
            _warrant = null;
            _create = true;
            int n = 0;
            while (_warrant==null) {
                n++;
                _warrant = InstanceManager.warrantManagerInstance().createNewWarrant(sysName+n, sysName+n);
            }
            _userNameBox.setText("");
            _sysNameBox.setText(sysName+n);
        }
    }

    public void dispose() {
        if (_debugFrame!=null) {
            _debugFrame.dispose();
        }
        WarrantTableAction.closeWarrantFrame(_warrant.getDisplayName());
        super.dispose();
    }

    private boolean routeIsValid() {
        if (_orders.size() == 0) {
            return false;
        }
        BlockOrder blockOrder = _orders.get(0);
        if (!pathIsValid(blockOrder.getBlock(), blockOrder.getPathName())) {
            return false;
        }
        for (int i=1; i<_orders.size(); i++){
            BlockOrder nextBlockOrder = _orders.get(i);
            if (!pathIsValid(nextBlockOrder.getBlock(), nextBlockOrder.getPathName())) {
                return false;
            }
            if (!blockOrder.getExitName().equals(nextBlockOrder.getEntryName())) {
                if (log.isDebugEnabled()) log.debug("route inValid at blockOrder: "+i+" exitName= "+
                                blockOrder.getExitName()+" nextEntry= "+nextBlockOrder.getEntryName());
                return false;
            }
            blockOrder = nextBlockOrder;
        }
        return true;
    }

    private boolean pathsAreValid(OBlock block) {
        List <Path> list = block.getPaths();
        if (list.size()==0) {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                    rb.getString("NoPaths"), block.getDisplayName()),
                    rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        for (int i=0; i<list.size(); i++) {
            OPath path = (OPath)list.get(i);
            if (path.getFromPortal()==null && path.getToPortal()==null) {
                JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                        rb.getString("PathNeedsPortal"), path.getName(), block.getDisplayName()),
                        rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private boolean pathIsValid(OBlock block, String pathName) {
        List <Path> list = block.getPaths();
        if (list.size()==0) {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                    rb.getString("NoPaths"), block.getDisplayName()),
                    rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (pathName!=null) {
            for (int i=0; i<list.size(); i++) {
                OPath path = (OPath)list.get(i);
                //if (log.isDebugEnabled()) log.debug("pathIsValid: pathName= "+pathName+", i= "+i+", path is "+path.getName());  
                if (pathName.equals(path.getName()) ){
                    if (path.getFromPortal()==null && path.getToPortal()==null) {
                        JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                rb.getString("PathNeedsPortal"), pathName, block.getDisplayName()),
                                rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                    return true;
                }
            }
        }
        JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                rb.getString("PathInvalid"), pathName, block.getDisplayName()),
                rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        return false;
    }

    /************************* Route Table ******************************/
    class RouteTableModel extends AbstractTableModel {
        public static final int BLOCK_COLUMN = 0;
        public static final int ENTER_PORTAL_COL =1;
        public static final int PATH_COLUMN = 2;
        public static final int DEST_PORTAL_COL = 3;
        public static final int NUMCOLS = 4;

        public RouteTableModel() {
            super();
        }

        public int getColumnCount () {
            return NUMCOLS;
        }

        public int getRowCount() {
            return _orders.size();
        }

        public String getColumnName(int col) {
            switch (col) {
                case BLOCK_COLUMN: return rb.getString("BlockCol");
                case ENTER_PORTAL_COL: return rb.getString("EnterPortalCol");
                case PATH_COLUMN: return rb.getString("PathCol");
                case DEST_PORTAL_COL: return rb.getString("DestPortalCol");
            }
            return "";
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        public int getPreferredWidth(int col) {
            return new JTextField(20).getPreferredSize().width;
        }

        public Object getValueAt(int row, int col) {
        	// some error checking
        	if (row >= _orders.size()){
        		log.debug("row is greater than _orders");
        		return "";
        	}
            BlockOrder bo = _orders.get(row);
          	// some error checking
        	if (bo == null){
        		log.debug("BlockOrder is null");
        		return "";
        	}
            switch (col) {
                case BLOCK_COLUMN: 
                    return bo.getBlock().getDisplayName();
                case ENTER_PORTAL_COL: 
                    return bo.getEntryName();
                case PATH_COLUMN:
                    return bo.getPathName();
                case DEST_PORTAL_COL:
                    if (row==_orders.size()-1) { return ""; }
                    return bo.getExitName();
            }
            return "";
        }

        public void setValueAt(Object value, int row, int col) {
            BlockOrder bo = _orders.get(row);
            OBlock block = null;
            switch (col) {
                case BLOCK_COLUMN:
                    block = InstanceManager.oBlockManagerInstance().getOBlock((String)value);
                    if (block != null) { bo.setBlock(block); }
                    break;
                case ENTER_PORTAL_COL: 
                    bo.setEntryName((String)value);
                    break;
                case PATH_COLUMN:
                    bo.setPathName((String)value);
                    break;
                case DEST_PORTAL_COL: 
                    bo.setExitName((String)value);
                    break;
            }
            fireTableRowsUpdated(row, row);
        }
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
                case TIME_COLUMN: return rb.getString("TimeCol");
                case COMMAND_COLUMN: return rb.getString("CommandCol");
                case VALUE_COLUMN: return rb.getString("ValueCol");
                case BLOCK_COLUMN: return rb.getString("BlockCol");
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
                        return rb.getString("Mark");
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
                            msg = java.text.MessageFormat.format(
                                    rb.getString("InvalidTime"), (String)value); 
                        }
                        ts.setTime(time);
                    } catch (NumberFormatException nfe) {
                        msg = java.text.MessageFormat.format(
                                rb.getString("InvalidTime"), (String)value); 
                    }
                    break;
                case COMMAND_COLUMN:
                    String cmd = ((String)value);
                    if (cmd == null || cmd.length()==0){
                        msg = java.text.MessageFormat.format(
                                rb.getString("nullValue"), rb.getString("CommandCol")); 
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
                                msg = rb.getString("badFunctionNum");
                            } else {
                                ts.setCommand(cmd);
                            }
                        } catch (Exception e) {
                            msg = rb.getString("badFunctionNum");
                        }
                    } else if (cmd.startsWith("LOCKF")) {
                        try {
                            int cmdNum = Integer.parseInt(cmd.substring(5));
                            if (cmdNum < 0 || 28 < cmdNum) {
                                msg = rb.getString("badLockFNum");
                            } else {
                                ts.setCommand(cmd);
                            }
                        } catch (Exception e) {
                            msg = rb.getString("badLockFNum");
                        }
                    } else if ("NOOP".equals(cmd)) {
                        msg = java.text.MessageFormat.format(
                                rb.getString("cannotEnterNoop"), (String)value); 
                    } else if (ts.getCommand()!=null && ts.getCommand().equals("NoOp")) {
                        msg = java.text.MessageFormat.format(
                                rb.getString("cannotChangeNoop"), (String)value); 
                    } else if ("SENSOR".equals(cmd) || "SET SENSOR".equals(cmd) || "SET".equals(cmd)) {
                        ts.setCommand("Set Sensor");
                    } else if ("WAIT SENSOR".equals(cmd) || "WAIT".equals(cmd)) {
                        ts.setCommand("Wait Sensor");
                    } else if ("RUN WARRANT".equals(cmd)) {
                        ts.setCommand("Run Warrant");                    	
                    } else {
                        msg = java.text.MessageFormat.format(
                                rb.getString("badCommand"), (String)value); 
                    }
                    break;
                case VALUE_COLUMN:
                    if (value == null || ((String)value).length()==0){
                        msg = java.text.MessageFormat.format(
                                rb.getString("nullValue"), rb.getString("ValueCol")); 
                        break;
                    }
                    cmd = ts.getCommand().toUpperCase();
                     if ("SPEED".equals(cmd)) {
                        try {
                            float speed = Float.parseFloat((String)value);
                            if (speed < 0.0f || 1.0f < speed) {
                                msg = rb.getString("badSpeed");
                            }
                        } catch (Exception e) {
                            msg = rb.getString("badSpeed");
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
                            msg = rb.getString("badStepMode");
                        } catch (Exception e) {
                            msg = rb.getString("invalidNumber");
                        }
                        ts.setValue(Integer.toString(stepMode));
                    } else if ("FORWARD".equalsIgnoreCase(cmd)) {
                        try {
                            Boolean.parseBoolean((String)value);
                        } catch (Exception e) {
                            msg = rb.getString("invalidBoolean");
                        }
                        ts.setValue((String)value);
                    } else if (cmd.startsWith("F")) {
                        try {
                            Boolean.parseBoolean((String)value);
                        } catch (Exception e) {
                            msg = rb.getString("invalidBoolean");
                        }
                        ts.setValue((String)value);
                    } else if (cmd.startsWith("LOCKF")) {
                        try {
                            Boolean.parseBoolean((String)value);
                        } catch (Exception e) {
                            msg = rb.getString("invalidBoolean");
                        }
                        ts.setValue((String)value);
                    } else if ("SET SENSOR".equals(cmd) || "WAIT SENSOR".equals(cmd)) {
                        String v = ((String)value).toUpperCase();
                        if ("ACTIVE".equals(v) || "INACTIVE".equals(v)) {
                            ts.setValue((String)value);
                        } else {
                            msg = rb.getString("badSensorCommand");
                        }
                    } else if ("RUN WARRANT".equals(cmd)) {
                        try {
                            int num = Integer.parseInt((String)value);
                            ts.setValue((String)value);
                        } catch (NumberFormatException nfe) {
                        	msg  = java.text.MessageFormat.format(
                                    rb.getString("badValue"), value, cmd);
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
                                msg = java.text.MessageFormat.format(
                                        rb.getString("BadSensor"), (String)value); 
                            }
                        } catch (Exception ex) {
                            msg = java.text.MessageFormat.format(
                                    rb.getString("BadSensor"), (String)value) + ex; 
                        }
                    } else if ("NOOP".equals(cmd)) {
                        msg = java.text.MessageFormat.format(
                                rb.getString("cannotChangeBlock"), (String)value); 
                    } else if ("RUN WARRANT".equals(cmd)) {
                        try {
                            Warrant w = InstanceManager.warrantManagerInstance().getWarrant((String)value);
                            if (w != null) {
                                ts.setBlockName((String)value);
                            } else {
                                msg = java.text.MessageFormat.format(
                                        rb.getString("BadWarrant"), (String)value); 
                            }
                        } catch (Exception ex) {
                        	msg  = java.text.MessageFormat.format(
                                    rb.getString("BadWarrant"), value, cmd)+ex;
                        }
                    } else {
                    	String name = getPreviousBlockName(row);
                    	if (!name.equals(value)) {
                            msg = java.text.MessageFormat.format(
                                    rb.getString("commandInBlock"), name);                              		
                            ts.setBlockName(name);
                    	}
                    }
                    break;
            }
            if (msg != null) {
                JOptionPane.showMessageDialog(null, msg,
                        rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            } else {
                fireTableRowsUpdated(row, row);
            }
        }
        
        private String getPreviousBlockName(int row) {
        	for (int i=row; i>0; i--) {
        		String name = _throttleCommands.get(i-1).getBlockName();
                OBlock b = InstanceManager.oBlockManagerInstance().getOBlock(name);
                if (b!=null) {
                	return name;
                }
        	}       	
        	return "StartBlock";
        }

    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WarrantFrame.class.getName());
}

