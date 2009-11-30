
package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.io.IOException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
//import javax.swing.DropMode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.AbstractTableModel;

import javax.swing.TransferHandler;
import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import jmri.InstanceManager;
import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.Path;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

import jmri.jmrit.display.PickListModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * An WarrantAction contains the operating permissions and directives needed for
 * a train to proceed from an Origin to a Destination
 * <P>
 * 
 *
 * @author	Pete Cressman  Copyright (C) 2009
 */
public class WarrantFrame extends jmri.util.JmriJFrame implements ActionListener, PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.logix.WarrantBundle");
    static int STRUT_SIZE = 10;
    static int ROW_HEIGHT;

    JMenu _warrantMenu;

    private Warrant             _warrant;
    private RouteTableModel     _routeModel;
    private ThrottleTableModel  _commandModel;
    private JScrollPane _throttlePane;
    private boolean     _create;    

    private ArrayList <BlockOrder> _orders = new ArrayList <BlockOrder>();
    private ArrayList <ThrottleSetting> _throttleCommands = new ArrayList <ThrottleSetting>();
    private long _startTime;
    private LearnThrottleFrame _learnThrottle = null;

    BlockOrder  _originBlockOrder;
    BlockOrder  _destBlockOrder;
    BlockOrder  _viaBlockOrder;

    JTextField  _userNameBox;
    JTextField  _originBlockBox = new JTextField(30);
    JTextField  _destBlockBox = new JTextField(30);
    JTextField  _viaBlockBox =  new JTextField(30);
    JComboBox   _originPathBox = new JComboBox();
    JComboBox   _destPathBox = new JComboBox();
    JComboBox   _originPortalBox = new JComboBox();
    JComboBox   _viaPathBox = new JComboBox();
    int _thisActionEventId;     // id for the listener of the above items

    RosterEntry _train;
    JComboBox   _rosterBox;
    JTextField  _trainIdBox = new JTextField(30);
    JTextField  _dccNumBox = new JTextField(30);
    JTextField  _rrNameBox = new JTextField(30);
    JTextField  _rrNumBox = new JTextField(30);
    JRadioButton _runProtect = new JRadioButton(rb.getString("RunProtected"), true);
    JRadioButton _runBlind = new JRadioButton(rb.getString("RunBlind"), false);
    JComboBox   _controlBox;
    JTextField  _statusBox = new JTextField(30);

    /**
    *  Constructor for existing warrant
    */
    public WarrantFrame(String warrantName) {
        _warrant = InstanceManager.warrantManagerInstance().provideWarrant(warrantName);
        _create = false;
        setup();
        init();
        if (!routeIsValid()) { calculate(false); }
    }
        
    /**
    *  Constructor for new warrant and GUI
    */
    public WarrantFrame(Warrant warrant, boolean create) {
        _warrant = warrant;
        _create = create;
        init();
    }

    private void init() {
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5,5));

        JPanel topPanel1 = new JPanel();
        topPanel1.setLayout(new BoxLayout(topPanel1, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(2*STRUT_SIZE));
        panel.add(new JLabel(rb.getString("SystemName")));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        JTextField sysNameBox =  new JTextField(_warrant.getSystemName());
        sysNameBox.setBackground(Color.white);        
        sysNameBox.setEditable(false);
        panel.add(sysNameBox);
        panel.add(Box.createHorizontalStrut(2*STRUT_SIZE));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(new JLabel(rb.getString("UserName")));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        _userNameBox =  new JTextField(_warrant.getUserName());
        _userNameBox.addActionListener(new ActionListener() {
            JTextField sysNameBox;
            public void actionPerformed(ActionEvent e) {
                userNameChange(sysNameBox);
            }
            ActionListener init(JTextField box) {
                sysNameBox = box;
                return this;
            }
        }.init(sysNameBox));                
        panel.add(_userNameBox);
        panel.add(Box.createHorizontalStrut(2*STRUT_SIZE));
        topPanel1.add(panel);
        topPanel1.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel topPanel2 = makeEditableTopPanel();
        topPanel1.add(topPanel2);
        contentPane.add(topPanel1, BorderLayout.NORTH);

        _routeModel = new RouteTableModel();
        JTable routeTable = new JTable(_routeModel);
        routeTable.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
        routeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i=0; i<_routeModel.getColumnCount(); i++) {
            int width = _routeModel.getPreferredWidth(i);
            routeTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        JScrollPane tablePane = new JScrollPane(routeTable);
        Dimension dim = routeTable.getPreferredSize();
        dim.height = routeTable.getRowHeight()*8;
        tablePane.getViewport().setPreferredSize(dim);

        JPanel routePanel = new JPanel();
        routePanel.setLayout(new BoxLayout(routePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(rb.getString("RouteTableTitle"));
        routePanel.add(title, BorderLayout.NORTH);
        routePanel.add(tablePane);

        _commandModel = new ThrottleTableModel();
        JTable commandTable = new JTable(_commandModel);
        commandTable.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
        commandTable.getColumnModel().getColumn(ThrottleTableModel.DELETE_COLUMN).setCellEditor(
                                                new ButtonEditor(new JButton()));
        commandTable.getColumnModel().getColumn(ThrottleTableModel.DELETE_COLUMN).setCellRenderer(
                                                new ButtonRenderer());
        commandTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i=0; i<_commandModel.getColumnCount(); i++) {
            int width = _commandModel.getPreferredWidth(i);
            commandTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        _throttlePane = new JScrollPane(commandTable);
        ROW_HEIGHT = commandTable.getRowHeight();
        dim = commandTable.getPreferredSize();
        dim.height = ROW_HEIGHT*8;
        _throttlePane.getViewport().setPreferredSize(dim);

        JPanel cmdPanel = new JPanel();
        cmdPanel.setLayout(new BoxLayout(cmdPanel, BoxLayout.Y_AXIS));
        title = new JLabel(rb.getString("CommandTableTitle"));
        cmdPanel.add(title, BorderLayout.NORTH);
        cmdPanel.add(_throttlePane);

        JPanel midPanel = new JPanel();
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.X_AXIS));
        midPanel.add(routePanel);
        midPanel.add(Box.createHorizontalStrut(5));
        midPanel.add(cmdPanel);
        contentPane.add(midPanel, BorderLayout.CENTER);

        JPanel buttonPanel = makeEditableButtonPanel();

        JPanel trainPanel = new JPanel();
        trainPanel.setLayout(new BoxLayout(trainPanel, BoxLayout.X_AXIS));
        trainPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(makeTextBoxPanel(false, _trainIdBox, "TrainId", true));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(makeTextBoxPanel(false, _dccNumBox, "DccAddress", true));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        trainPanel.add(panel);
        trainPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        _trainIdBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!setTrainInfo(_trainIdBox.getText(), false)) {
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                        rb.getString("noSuchTrain"), _trainIdBox.getText()),
                            rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                } else {
                    _rosterBox.setSelectedItem(_trainIdBox.getText());
                }
            }
        });
        _dccNumBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!setTrainInfo(_dccNumBox.getText(), true)) {
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                        rb.getString("noSuchAddress"), _dccNumBox.getText()),
                            rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        panel = new JPanel(); 
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(makeTextBoxPanel(false, _rrNameBox, "RoadName", false));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(makeTextBoxPanel(false, _rrNumBox, "RoadNumber", false));
        trainPanel.add(panel);
        trainPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JButton startButton = new JButton(rb.getString("Start"));
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runTrain(Warrant.MODE_LEARN);
            }
        });
        JButton stopButton = new JButton(rb.getString("Stop"));
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StopRunTrain();
                if (!runProtectedOK()) {
                    _runBlind.setSelected(true);
                }
            }
        });
        panel.add(new JLabel(rb.getString("LearnMode")));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(startButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(stopButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        JPanel edge = new JPanel();
        edge.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK));
        edge.add(panel);
        trainPanel.add(edge);
        trainPanel.add(Box.createHorizontalStrut(2*STRUT_SIZE));

        panel = new JPanel(); 
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        ButtonGroup group = new ButtonGroup();
        panel.add(_runProtect);
        group.add(_runProtect);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(_runBlind);
        group.add(_runBlind);
        trainPanel.add(panel);
        trainPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        _runProtect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (log.isDebugEnabled()) log.debug("_runProtect.isSelected()="+_runProtect.isSelected()+
                                                    " runProtectedOK()= "+runProtectedOK());

                if (_runProtect.isSelected() && !runProtectedOK()) {
                    JOptionPane.showMessageDialog(null, rb.getString("MustRunBlind"),
                            rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                    _runBlind.setSelected(true);
                }
            }
        });
        _runBlind.setSelected(_warrant.getRunBlind());

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JButton runButton = new JButton(rb.getString("Run"));
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runTrain(Warrant.MODE_RUN);
            }
        });
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
                status = java.text.MessageFormat.format(WarrantTableAction.rb.getString("Issued"),
                                           _warrant.getCurrentBlockOrder().getBlock().getDisplayName());
                break;
        }
        _statusBox.setText(status);
        _controlBox = new JComboBox(new String[] {"", rb.getString("Halt"), 
                                            rb.getString("Resume"), rb.getString("Abort")} );
        _controlBox.setFont(new Font(null, Font.PLAIN, 12));
        _controlBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int idx = _controlBox.getSelectedIndex();
                if (log.isDebugEnabled()) log.debug("actionPerformed on _controlBox \""+e.getActionCommand()+
                                                    "\" idx= "+idx);
                if (_warrant.getRunMode() != Warrant.MODE_RUN) {
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                            rb.getString("NotRunning"), _warrant.getDisplayName()),
                            rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                } else {
                    _warrant.controlRunTrain(idx);
                }
                _controlBox.setSelectedIndex(0);
            }
        });
        panel.add(new JLabel(rb.getString("RunTrain")));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(runButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(_controlBox);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        edge = new JPanel();
        edge.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK));
        edge.add(panel);
        trainPanel.add(edge);

        trainPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(trainPanel);
        bottomPanel.add(buttonPanel);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);
 
        addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    dispose();
                }
            });			
        makeMenus();
        setContentPane(contentPane);
        setLocation(0,100);
        setVisible(true);
        pack();
    }

    private void userNameChange(JTextField sysNameBox) {
        String text = _userNameBox.getText();
        if (text != null && text.length()>0) {
            setTitle(java.text.MessageFormat.format(rb.getString("TitleWarrant"), text));
        }
        if (text != null && !text.equals(_warrant.getUserName())) {
            if (JOptionPane.showConfirmDialog(this, java.text.MessageFormat.format(
                rb.getString("makeCopy"), _warrant.getUserName(), text),
                    rb.getString("QuestionTitle"), JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                save();
                String sysName = _warrant.getSystemName();
                _warrant = null;
                _create = true;
                int n = 0;
                while (_warrant==null) {
                    n++;
                    _warrant = InstanceManager.warrantManagerInstance().createNewWarrant(sysName+n, text);
                }
                sysNameBox.setText(sysName+n);
            }
        }
    }

    private JPanel makeEditableTopPanel() {
        JPanel topPanel2 = new JPanel();
        topPanel2.setLayout(new BoxLayout(topPanel2, BoxLayout.X_AXIS));

        JPanel topLeft = new JPanel();
        topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));

        JPanel oPanel = makeEndPoint("OriginBlock", makeBlockBox(_originBlockBox), 
                                     makePathBox(_originPathBox), 
                                     makePortalBox("ExitPortalName", _originPortalBox));
        topLeft.add(oPanel);
        topLeft.add(Box.createVerticalStrut(STRUT_SIZE));

        oPanel = makeEndPoint("DestBlock", makeBlockBox(_destBlockBox), 
                              makePathBox(_destPathBox), null);
        topLeft.add(oPanel);
        topLeft.add(Box.createVerticalStrut(STRUT_SIZE));

        oPanel = makeEndPoint("ViaBlock", makeBlockBox(_viaBlockBox), 
                              makePathBox(_viaPathBox), null);
        topLeft.add(oPanel);
        topLeft.add(Box.createVerticalStrut(STRUT_SIZE));
        topPanel2.add(topLeft);

        topPanel2.add(Box.createHorizontalStrut(STRUT_SIZE));
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
        p.add(new JLabel(rb.getString("CalculateRoute")));
        p.add(button);
        JPanel pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(p);
        panel.add(pp);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        button = new JButton(rb.getString("Debug"));
        button.setMaximumSize(button.getPreferredSize());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                debugRoute();
            }
        });
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel(rb.getString("DebugRoute")));
        p.add(button);
        pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(p);
        panel.add(pp);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        _rosterBox = Roster.instance().fullRosterComboBox();
        _rosterBox.setMaximumSize(_rosterBox.getPreferredSize());
        _rosterBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setTrainInfo((String)_rosterBox.getSelectedItem(), false);
                }
        });
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel(rb.getString("Roster")));
        p.add(_rosterBox);
        pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(p);
        panel.add(pp);
        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        topRight.add(panel);
        topRight.add(Box.createHorizontalStrut(STRUT_SIZE));

        JScrollPane pane = makePickList();
        topRight.add(pane);
        topPanel2.add(topRight);

        return topPanel2;
    }

    /**
    * Save, Cancel, Delete buttons
    */
    private JPanel makeEditableButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

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
        buttonPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

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
        buttonPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

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
        buttonPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        buttonPanel.add(makeTextBoxPanel(false, _statusBox, "Status", false));
        _statusBox.setMinimumSize(new Dimension(300, _statusBox.getPreferredSize().height));
        return buttonPanel;
    }

    private JPanel makeTextBoxPanel(boolean vertical, JTextField textField, String label, boolean editable) {
        JPanel panel = new JPanel();
        if (vertical) {
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        } else {
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        }
        panel.add(new JLabel(rb.getString(label)));
        textField.setMaximumSize(new Dimension(300, textField.getPreferredSize().height));
        textField.setMinimumSize(new Dimension(100, textField.getPreferredSize().height));
        textField.setEditable(editable);
        if (editable) {
            textField.setBackground(Color.white);        
        } else {
            JLabel l = new JLabel();
            textField.setBackground(l.getBackground());        
        }
        panel.add(textField);
        return panel;
    }

    private boolean runProtectedOK() {
        if (_throttleCommands.size() > 0) {
            return !_throttleCommands.get(0).getBlockName().equals(
                _throttleCommands.get(_throttleCommands.size()-1).getBlockName());
        }
        return true;
    }

    private JPanel makeEndPoint(String title, JPanel p0, JPanel p1, JPanel p2) {
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
        oPanel.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK));
        return oPanel;
    }

    private JPanel makeBlockBox(JTextField blockBox) {
        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
        boxPanel.add(new JLabel(rb.getString("BlockName")));
        blockBox.addActionListener(this);
        blockBox.setDragEnabled(true);
        blockBox.setTransferHandler(new DnDImportHandler());
        blockBox.setColumns(15);
        //blockBox.setDropMode(DropMode.USE_SELECTION);
        blockBox.setSize(blockBox.getPreferredSize());
        boxPanel.add(blockBox);
        return boxPanel;
    }

    private JPanel makePathBox(JComboBox pathBox) {
        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
        boxPanel.add(new JLabel(rb.getString("PathName"), SwingConstants.LEFT));
        pathBox.setSize(_originBlockBox.getPreferredSize());
        pathBox.addActionListener(this);
        boxPanel.add(pathBox);
        return boxPanel;
    }

    private JPanel makePortalBox(String title, JComboBox portalBox) {
        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
        boxPanel.add(new JLabel(rb.getString(title), SwingConstants.LEFT));
        portalBox.setSize(_originBlockBox.getPreferredSize());
        portalBox.addActionListener(this);
        boxPanel.add(portalBox);
        return boxPanel;
    }

    public JScrollPane makePickList() {
        PickListModel pickListModel = PickListModel.oBlockPickModelInstance();
        pickListModel.init();
        JTable table = new JTable(pickListModel);

        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setPreferredScrollableViewportSize(new java.awt.Dimension(250,table.getRowHeight()*7));
        table.setDragEnabled(true);
        table.setTransferHandler(new DnDExportHandler());
        TableColumnModel columnModel = table.getColumnModel();

        TableColumn sNameColumnT = columnModel.getColumn(PickListModel.SNAME_COLUMN);
        sNameColumnT.setResizable(true);
        sNameColumnT.setMinWidth(50);
        sNameColumnT.setMaxWidth(200);

        TableColumn uNameColumnT = columnModel.getColumn(PickListModel.UNAME_COLUMN);
        uNameColumnT.setResizable(true);
        uNameColumnT.setMinWidth(100);
        uNameColumnT.setMaxWidth(300);

        return new JScrollPane(table);
    }


    class DnDImportHandler extends TransferHandler{
        int _type;

        DnDImportHandler() {
        }

        /////////////////////import
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            if (log.isDebugEnabled()) log.debug("DnDImportHandler.canImport ");

            for (int k=0; k<transferFlavors.length; k++){
                if (transferFlavors[k].equals(DataFlavor.stringFlavor)) {
                    return true;
                }
            }
            return false;
        }

        public boolean importData(JComponent comp, Transferable tr) {
            if (log.isDebugEnabled()) log.debug("DnDImportHandler.importData ");
            DataFlavor[] flavors = new DataFlavor[] {DataFlavor.stringFlavor};

            if (!canImport(comp, flavors)) {
                return false;
            }

            try {
                if (tr.isDataFlavorSupported(DataFlavor.stringFlavor) ) {
                    String data = (String)tr.getTransferData(DataFlavor.stringFlavor);
                    JTextField field = (JTextField)comp;
                    field.setText(data);
                    actionPerformed(new ActionEvent(field, _thisActionEventId, data));
                    return true;
                }
            } catch (UnsupportedFlavorException ufe) {
                log.warn("DnDImportHandler.importData: "+ufe.getMessage());
            } catch (IOException ioe) {
                log.warn("DnDImportHandler.importData: "+ioe.getMessage());
            }
            return false;
        }
        /* OB4
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            Transferable t = support.getTransferable();
            String data = null;
            try {
                data = (String)tr.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException ufe) {
                log.warn("DnDImportHandler.importData: "+ufe.getMessage());
            } catch (IOException ioe) {
                log.warn("DnDImportHandler.importData: "+ioe.getMessage());
            }
            JTextField field = (JTextField)support.getComponent();
            field.setText(data);
            actionPerformed(new ActionEvent(field, _thisActionEventId, data));
            return true;
        }
        */
    }

    class DnDExportHandler extends TransferHandler{
        int _type;

        DnDExportHandler() {
        }

        public int getSourceActions(JComponent c) {
            return COPY;
        }

        public Transferable createTransferable(JComponent c) {
            JTable table = (JTable)c;
            int col = table.getSelectedColumn();
            int row = table.getSelectedRow();
            if (col<0 || row<0) {
                return null;
            }
            if (log.isDebugEnabled()) log.debug("TransferHandler.createTransferable: from ("
                                                +row+", "+col+") for \""
                                                +table.getModel().getValueAt(row, col)+"\"");
            return new StringSelection((String)table.getModel().getValueAt(row, col));
        }

        public void exportDone(JComponent c, Transferable t, int action) {
            if (log.isDebugEnabled()) log.debug("TransferHandler.exportDone ");
        }
    }

    private void makeMenus() {
        setTitle(java.text.MessageFormat.format(
                                rb.getString("TitleWarrant"), _warrant.getDisplayName()));
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        fileMenu.add(new jmri.configurexml.SaveMenu());
        //fileMenu.add(new jmri.configurexml.SaveMenu());
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.logix.WarrantTable", true);
    }

    private boolean setTrainInfo(String name, boolean isAddress) {
        if (log.isDebugEnabled()) log.debug("setTrainInfo for: "+name+" isAddress= "+isAddress);
        if (isAddress)  {
            _dccNumBox.setText(name);
        } else {
            _trainIdBox.setText(name);
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
                    _dccNumBox.setText(_train.getDccLocoAddress().toString());
                }
            } else {
                _train = Roster.instance().entryFromTitle(name);
            }
            if (_train !=null) {
                _rrNameBox.setText(_train.getRoadName());
                _rrNumBox.setText(_train.getRoadNumber());
                _dccNumBox.setText(_train.getDccLocoAddress().toString());
                _trainIdBox.setText(_train.getId());
            } else {
                _rrNameBox.setText("");
                _rrNumBox.setText("");
                if (!isAddress) {
                    _dccNumBox.setText("");
                } else {
                    _trainIdBox.setText("");
                }
                return false;
            }
        }
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        _thisActionEventId = e.getID();
        if (log.isDebugEnabled()) log.debug("actionPerformed: source "+((Component)obj).getName()+
                     " id= "+e.getID()+", ActionCommand= "+e.getActionCommand());
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
            }
        } else {
            JComboBox box = (JComboBox)obj;
            if (box == _originPathBox) {
                setPortalBox(_originPathBox, _originPortalBox, _originBlockOrder);
            } else if (box == _originPortalBox) {
                _originBlockOrder.setExitName((String)_originPortalBox.getSelectedItem());
            } else if (box == _destPathBox) {
                String pathName = (String)_destPathBox.getSelectedItem();
                _destBlockOrder.setPathName(pathName);
            } else if (box == _viaPathBox) {
                String pathName = (String)_viaPathBox.getSelectedItem();
                _viaBlockOrder.setPathName(pathName);
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
        OBlock block = InstanceManager.oBlockManagerInstance().provideOBlock(text);
        if (block == null && text.length()>0) {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                    rb.getString("BlockNotFound"), text),
                    rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
        return block;
    }

    private boolean setPathBox(JComboBox pathBox, JComboBox portalBox, OBlock block) {
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
        portalBox.removeAllItems();
        String pathName = (String)pathBox.getSelectedItem();
        order.setPathName(pathName);
        OPath path = order.getPath();
        if (path != null) {
            String name = path.getFromPortalName();
            if (name!=null) { portalBox.addItem(name); }
            name = path.getToPortalName();
            if (name!=null) { portalBox.addItem(name); }
            if (log.isDebugEnabled()) log.debug("setPortalBox: Path "+path.getName()+
                         " set in block "+order.getBlock().getDisplayName());
        } else {
            if (log.isDebugEnabled()) log.debug("setPortalBox: Path set to null in block"
                         +order.getBlock().getDisplayName());
        }
    }

    private boolean setOriginBlock() {
        OBlock block = getEndPointBlock(_originBlockBox);
        if (block == null) {
            _originPathBox.removeAllItems();
            _originPortalBox.removeAllItems();
            return false;
        } else {
            if (_originBlockOrder!= null && block==_originBlockOrder.getBlock()) {
                return true; 
            }
            _originBlockOrder = new BlockOrder(block);
            if (!setPathBox(_originPathBox, _originPortalBox, block)) {
                _originPathBox.removeAllItems();
                _originPortalBox.removeAllItems();
                _originBlockBox.setText("");
                return false;
            }
        }
        return true; 
    }

    private boolean setDestinationBlock() {
        OBlock block = getEndPointBlock(_destBlockBox);
        if (block == null) {
            _destPathBox.removeAllItems();
            return false;
        } else {
            if (_destBlockOrder!=null && block==_destBlockOrder.getBlock()) {
                return true;
            }
            _destBlockOrder = new BlockOrder(block);
            if (!setPathBox(_destPathBox, null, block)) {
                _destPathBox.removeAllItems();
                _destBlockBox.setText("");
                return false;
            }
        }
        return true;
    }

    private boolean setViaBlock() {
        OBlock block = getEndPointBlock(_viaBlockBox);
        if (block == null) {
            _viaPathBox.removeAllItems();
            _viaBlockOrder = null;
            return true;
        } else {
            if (_viaBlockOrder!=null && block==_viaBlockOrder.getBlock()) {
                return true;
            }
            _viaBlockOrder = new BlockOrder(block);
            if (!setPathBox(_viaPathBox, null, block)) {
                _viaPathBox.removeAllItems();
                _viaBlockBox.setText("");
                return false;
            }
        }
        return false;
    }

    /********************* Start non-route editable ********************/
    /*
    private void makeWarrantTopPanel(JPanel topPanel2) {
    }
    */
    private void clearWarrant() {
        _orders = new ArrayList <BlockOrder>();
        _routeModel.fireTableDataChanged();
        _throttleCommands = new ArrayList <ThrottleSetting>();
        _commandModel.fireTableDataChanged();
    }
    
    private void findRoute() {
        // read and verify origin and destination blocks/paths/portals
        String msg = null;
        boolean ok = setOriginBlock();
        if (ok) {
            ok = (_originBlockOrder.getPathName()!=null);
            if (ok) {
                if (_originBlockOrder.getExitName() == null) {
                    msg = java.text.MessageFormat.format(
                        rb.getString("SetExitPortal"), rb.getString("OriginBlock"));
                    ok = false;
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
                if (_destBlockOrder.getPathName()==null) {
                    msg = java.text.MessageFormat.format(
                        rb.getString("SetPath"), rb.getString("DestBlock"));
                    ok = false;
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
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg,
                    rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        calculate(false);
    }

    private void setup() {
        // use local copies until input boxes are set
        _originBlockOrder = _warrant.getfirstOrder();
        if (_originBlockOrder!=null) {
            OBlock block = _originBlockOrder.getBlock();
            String pathName = _originBlockOrder.getPathName();
            String exitName = _originBlockOrder.getExitName();
            _originBlockBox.setText(block.getDisplayName());
            setPathBox(_originPathBox, _originPortalBox, block);
            _originPathBox.setSelectedItem(pathName);
            setPortalBox(_originPathBox, _originPortalBox, _originBlockOrder);
            _originPortalBox.setSelectedItem(exitName);

            _destBlockOrder = _warrant.getLastOrder();
            block = _destBlockOrder.getBlock();
            pathName = _destBlockOrder.getPathName();
            _destBlockBox.setText(block.getDisplayName());
            setPathBox(_destPathBox, null, block);
            _originBlockOrder = _warrant.getfirstOrder();
            _destBlockOrder = _warrant.getLastOrder();
            _destPathBox.setSelectedItem(pathName);
        }        
        _viaBlockOrder = _warrant.getViaOrder();
        if (_viaBlockOrder!=null) {
            OBlock block = _viaBlockOrder.getBlock();
            String pathName = _viaBlockOrder.getPathName();
            _viaBlockBox.setText(block.getDisplayName());
            setPathBox(_viaPathBox, null, block);
            _viaPathBox.setSelectedItem(pathName);
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
        _trainIdBox.setText(_warrant.getTrainId());
        if (!setTrainInfo(_warrant.getTrainId(), false)) {
            jmri.DccLocoAddress address = _warrant.getDccAddress();
            if (address!=null) {
                _dccNumBox.setText(address.toString());
            }
        }
        _runBlind.setSelected(_warrant.getRunBlind());
    }

    JFrame _debugFrame;
    private void debugRoute() {
        DefaultTreeModel tree = calculate(true);
        if (_debugFrame!=null) {
            _debugFrame.dispose();
        }
        _debugFrame = new JFrame(rb.getString("DebugRoute"));
        javax.swing.JTree dTree = new javax.swing.JTree(tree);
        dTree.setShowsRootHandles(true);
        dTree.setScrollsOnExpand(true);
        dTree.setExpandsSelectedPaths(true);
        JScrollPane treePane = new JScrollPane(dTree);
        treePane.getViewport().setPreferredSize(new Dimension(500, 300));
        _debugFrame.getContentPane().add(treePane);
        _debugFrame.setVisible(true);
        _debugFrame.pack();
    }

    private void pickRoute(List <DefaultMutableTreeNode> destNodes, DefaultTreeModel tree) {
        if (log.isDebugEnabled()) {
            log.debug("pickRoute: has "+destNodes.size()+" routes.");
        }
        JDialog dialog = new JDialog(this, rb.getString("DialogTitle"), false);
        java.awt.Container contentPane = dialog.getContentPane();  
        contentPane.setLayout(new BorderLayout(5,5));
        contentPane.add(new JLabel(java.text.MessageFormat.format(
                    rb.getString("NumberRoutes"), new Integer(destNodes.size()))), BorderLayout.NORTH);
        ButtonGroup buttons = new ButtonGroup();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (int i=0; i<destNodes.size(); i++) {
            JRadioButton button = new JRadioButton(java.text.MessageFormat.format(
                    rb.getString("RouteSize"), new Integer(i+1), 
                    new Integer(destNodes.get(i).getLevel())) );
            button.setActionCommand(""+i);
            buttons.add(button);
            panel.add(button);
        }
        contentPane.add(panel, BorderLayout.CENTER);
        JButton ok = new JButton(rb.getString("ButtonSelect"));
        ok.addActionListener(new ActionListener() {
                ButtonGroup buttons;
                JDialog dialog;
                List <DefaultMutableTreeNode> destNodes;
                DefaultTreeModel tree;
                public void actionPerformed(ActionEvent e) {
                    int i = Integer.parseInt(buttons.getSelection().getActionCommand());
                    showRoute(destNodes.get(i), tree);
                    dialog.dispose();
                }
                ActionListener init(ButtonGroup bg, JDialog d, List <DefaultMutableTreeNode> dn,
                                    DefaultTreeModel t) {
                    buttons = bg;
                    dialog = d;
                    destNodes = dn;
                    tree = t;
                    return this;
                }
            }.init(buttons, dialog, destNodes, tree));
        ok.setMaximumSize(ok.getPreferredSize());
        JButton show = new JButton(rb.getString("ButtonReview"));
        show.addActionListener(new ActionListener() {
                ButtonGroup buttons;
                List <DefaultMutableTreeNode> destNodes;
                DefaultTreeModel tree;
                public void actionPerformed(ActionEvent e) {
                    int i = Integer.parseInt(buttons.getSelection().getActionCommand());
                    showRoute(destNodes.get(i), tree);
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
        contentPane.add(panel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.pack();
        dialog.setVisible(true);
    }

    private DefaultTreeModel calculate(boolean debug)
    {
        clearWarrant();
        if (_originBlockOrder==null) { return null; }
        // build tree of paths until a branch leaf is the destination 
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(_originBlockOrder);
        DefaultTreeModel tree = new DefaultTreeModel(root);
        _needViaPath = (_viaBlockOrder!=null);
        _viaLevel = 0;
        ArrayList <DefaultMutableTreeNode> destNodes = new ArrayList <DefaultMutableTreeNode>();
        findDestination(root, tree, _viaLevel, destNodes);

        if (log.isDebugEnabled()) {
            log.debug("calculate:\n_originBlockOrder= "+_originBlockOrder.toString()+
                      "\n_destBlockOrder = "+_destBlockOrder.toString()+
                      "\n_viaBlockOrder = "+(_viaBlockOrder!=null ? _viaBlockOrder.toString() : null));
        }
        if (destNodes.size()==0) {
            if (!debug) {
                JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                    rb.getString("NoRoute"),  new Object[] {_originBlockOrder.getBlock().getDisplayName(), 
                                                            _originBlockOrder.getPathName(), 
                                                            _originBlockOrder.getExitName(),
                                                            _destBlockOrder.getBlock().getDisplayName(),
                                                            _destBlockOrder.getPathName() }),
                        rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            }
            return tree; 
        } else if (destNodes.size()==1) {
            showRoute(destNodes.get(0), tree);
        } else {
            pickRoute(destNodes, tree);
        }
        return tree; 
    }

    private void showRoute(DefaultMutableTreeNode destNode, DefaultTreeModel tree) {
        TreeNode[] nodes = tree.getPathToRoot(destNode);
        _orders.clear();
        for (int i=0; i<nodes.length; i++) {
            _orders.add((BlockOrder)((DefaultMutableTreeNode)nodes[i]).getUserObject());
        }
        _routeModel.fireTableDataChanged();
        if (log.isDebugEnabled()) log.debug("showRoute: Route has "+_orders.size()+" orders.");
    }

    boolean _needViaPath;
    int _viaLevel;

    @SuppressWarnings("unchecked")
    private void findDestination(DefaultMutableTreeNode parent, DefaultTreeModel tree, 
                                 int depth, List <DefaultMutableTreeNode> destNodes) {
        depth++;
        BlockOrder pOrder = (BlockOrder)parent.getUserObject();
        OBlock pBlock = pOrder.getBlock();
        String pName = pOrder.getExitName();    // is entryName of next block
        Portal exitPortal = pBlock.getPortalByName(pName);
        if (exitPortal != null) {
            List <OPath> paths = exitPortal.getPathsFromOpposingBlock(pBlock);
            if (log.isDebugEnabled()) log.debug("findDestination node: block "+pBlock.getSystemName()
                                            +" path "+pOrder.getPathName()+" exits through portal "+pName
                                                +" with "+paths.size()+" paths into next block.");
            if (paths.size()==0) {
                log.error("Portal \""+pName+"\" does not any exit paths into the next block! (\""+
                          exitPortal.getOpposingBlock(pBlock)+"\").");
            }
            for (int i=0; i<paths.size(); i++) {
                OPath path = paths.get(i);
                String exitName = path.getOppositePortalName(pName);
                BlockOrder nOrder = new BlockOrder((OBlock)path.getBlock(), path.getName(), pName, exitName);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(nOrder);
                tree.insertNodeInto(node, parent, parent.getChildCount());
                //nodes.add(node);

                if (_needViaPath)
                {
                    if (_viaBlockOrder.getBlock() == nOrder.getBlock() && 
                    _viaBlockOrder.getPathName().equals(path.getName()) ) {
                        _needViaPath = false;
                        _viaLevel = depth;
                        if (log.isDebugEnabled()) log.debug("Via reached: "+" viaLevel= "+_viaLevel+
                                                            " Order= "+nOrder.toString());
                    }
                }
                if (!_needViaPath) {
                    if (_destBlockOrder.getBlock() == nOrder.getBlock() && 
                    _destBlockOrder.getPathName().equals(path.getName()) ) {
                        destNodes.add(node);
                        break;
                    }
                }
            }
            if (log.isDebugEnabled()) log.debug("needVia= "+_needViaPath+" _viaLevel= "+_viaLevel+" depth= "+depth+
                                                " node:\n"+((BlockOrder)parent.getUserObject()).toString()+
                                                " inserted "+parent.getChildCount()+" children.");
            Enumeration <DefaultMutableTreeNode> en = parent.children();
            while (en.hasMoreElements()) {
                DefaultMutableTreeNode n = en.nextElement();
                findDestination(n, tree, depth, destNodes);
            }
        }
        if (log.isDebugEnabled()) log.debug("Dead branch: needVia= "+_needViaPath+", _viaLevel= "
                                            +_viaLevel+", depth= "+depth);
        if (depth < _viaLevel) {
            _needViaPath = (_viaBlockOrder!=null);
        }
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

    private void runTrain(int mode) {
        String msg = null;
        DccLocoAddress locoAddress = null;

        if (_orders.size()==0) {
            msg = java.text.MessageFormat.format(rb.getString("NoRouteSet"),
                        _originBlockBox.getText(), _destBlockBox.getText());
        } else if (_train!=null) {
            locoAddress = _train.getDccLocoAddress();
            if (locoAddress==null) {
                locoAddress = getLocoAddress();
                if (locoAddress==null) {
                    msg = rb.getString("NoRosterEntry");
                } else {
                    if (JOptionPane.showConfirmDialog(this, java.text.MessageFormat.format(
                            rb.getString("UseAddress"), _dccNumBox.getText()), rb.getString("QuestionTitle"), 
                             JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) 
                                    == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
            }
        } else {
            locoAddress = getLocoAddress();
            if (locoAddress==null) {
                msg = rb.getString("NoRosterEntry");
            }
        }
        if (msg==null) {
            msg = _warrant.setRoute(0, _orders);
            if (msg!=null) {
                if (_warrant.getBlockAt(0).allocate(_warrant)!=null) {
                    msg = java.text.MessageFormat.format(WarrantTableAction.rb.getString("OriginBlockNotSet"), 
                            _warrant.getBlockAt(0).getDisplayName());
                } else if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
                            java.text.MessageFormat.format(WarrantTableAction.rb.getString("OkToRun"),
                            msg), WarrantTableAction.rb.getString("WarningTitle"), 
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
                    msg = null;
                }
            }
        }
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg, rb.getString("WarningTitle"), 
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (mode==Warrant.MODE_LEARN) {
            if (_throttleCommands.size() > 0) {
                if (JOptionPane.showConfirmDialog(this, rb.getString("deleteCommand"),
                   rb.getString("QuestionTitle"), JOptionPane.YES_NO_OPTION, 
                       JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
                    return;
                }
                _throttleCommands = new ArrayList <ThrottleSetting>();
            }
            if (_learnThrottle==null) {
                _learnThrottle = new LearnThrottleFrame(this);
            } else {
                _learnThrottle.setVisible(true);
            }
        } else if (mode==Warrant.MODE_RUN) { 
            if (_throttleCommands==null || _throttleCommands.size()==0)  {
                JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                        rb.getString("NoCommands"),_warrant.getDisplayName()), 
                              rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            return;
        }

        _startTime = System.currentTimeMillis();
        _warrant.addPropertyChangeListener(this);
        msg = _warrant.setRunMode(mode, locoAddress, _learnThrottle, 
                                      _throttleCommands, _runBlind.isSelected());
        if (msg!=null) 
        {
            JOptionPane.showMessageDialog(this, msg,
                                rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            // learnThrottle will be disposed by _warrant.setRunMode(Warrant.MODE_NONE, null, null, null);
            StopRunTrain();
            return;
        }
    }

    protected void StopRunTrain() {
        _warrant.setRunMode(Warrant.MODE_NONE, null, null, null, false);
        _warrant.removePropertyChangeListener(this);
        if (_learnThrottle!=null) {
            _learnThrottle.dispose();
            _learnThrottle = null;
        }
    }
    
    /**
    * Property names from Warrant:
    *   "save" - from clearall
    *   "runMode" - from setRunMode
    *   "controlChange" - from controlRunTrain
    *   "blockChange" - from goingActive
    *   "Command" - from Engineer run
    *   
    */
	@SuppressWarnings("fallthrough")
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("propertyChange of \""+e.getPropertyName());
        String item = "";
        switch (_warrant.getRunMode()) {
            case Warrant.MODE_NONE:
                _warrant.removePropertyChangeListener(this);
                item = rb.getString("Idle");
                break;
            case Warrant.MODE_LEARN:
                if (e.getPropertyName().equals("blockChange")) {
                    setThrottleCommand("NoOp", rb.getString("Mark"));
                }
            // fall through
            case Warrant.MODE_RUN:
                String key;
                if (_warrant.isWaiting()) {
                    key = "Waiting";
                } else { 
                    key = "Issued";
                }
                BlockOrder bo = _warrant.getCurrentBlockOrder();
                int idx = _warrant.getCurrentCommandIndex();
                if (bo!=null) {
                    item = java.text.MessageFormat.format(rb.getString(key),
                                bo.getBlock().getDisplayName(), idx);
                    scrollCommandTable(idx);
                } else {
                    log.error("Current BlockOrder is null!");
                }
                break;
        }
        _statusBox.setText(item);
    }

    protected void setThrottleCommand(String cmd, String value) {
        long endTime = System.currentTimeMillis();
        Long time = new Long(endTime - _startTime);
        if (!cmd.equals("NoOp")) {
            _startTime = endTime;
        }
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

        int setRow = row - 4;
        if (setRow < 0) {
            setRow = 0;
        }
        _throttlePane.getVerticalScrollBar().setValue(setRow*ROW_HEIGHT);
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
        for (int i=0; i<_orders.size(); i++) {
            _warrant.addBlockOrder(new BlockOrder(_orders.get(i)));
        }
        for (int i=0; i<_throttleCommands.size(); i++) {
            _warrant.addThrottleCommand(new ThrottleSetting(_throttleCommands.get(i)));
        }
        if (_train != null){
            _warrant.setTrainId(_train.getId());
            _warrant.setDccAddress(_train.getDccLocoAddress());
        } else {
            _warrant.setDccAddress(getLocoAddress());
        }
        _warrant.setRunBlind(_runBlind.isSelected());
        if (log.isDebugEnabled()) log.debug("warrant saved _train "+(_train != null));

        if (_create) {
            InstanceManager.warrantManagerInstance().register(_warrant);
            WarrantTableAction.updateWarrantMenu(); 
        }
        //dispose();
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
        for (int i=1; i<_orders.size(); i++){
            BlockOrder nextBlockOrder = _orders.get(i);

            if (!blockOrder.getExitName().equals(nextBlockOrder.getEntryName())) {
                if (log.isDebugEnabled()) log.debug("route inValid at blockOrder: "+i+" exitName= "+
                                blockOrder.getExitName()+" nextEntry= "+nextBlockOrder.getEntryName());
                return false;
            }
            blockOrder = nextBlockOrder;
        }
        return true;
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
            return new JTextField(14).getPreferredSize().width;
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
                    block = InstanceManager.oBlockManagerInstance().provideOBlock((String)value);
                    if (block != null) { bo.setBlock(block); }
                    break;
                case ENTER_PORTAL_COL: 
                    bo.setEntryName((String)value);
                    break;
                case PATH_COLUMN:
                    block =bo.getBlock();
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
        public static final int DELETE_COLUMN = 5;
        public static final int NUMCOLS = 6;

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
            if (col == DELETE_COLUMN) {
                return JButton.class;
            }
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
                    return new JTextField(11).getPreferredSize().width;
                case DELETE_COLUMN:
                    return new JButton("DELETE").getPreferredSize().width;
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
                    return row+1;
                case TIME_COLUMN:
                    return ts.getTime().toString();
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
                case DELETE_COLUMN:
                    return rb.getString("ButtonDelete");
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
                        ts.setTime(new Long(time));
                    } catch (NumberFormatException nfe)  {
                        String str = ((String)value).toUpperCase();
                        if ( str.equals("SYNCH") || str.equals("SYNC")) {
                            if (_runBlind.isSelected()) {
                                msg = java.text.MessageFormat.format(rb.getString("CannotSynchBlind"),
                                                                           _warrant.getDisplayName());
                            } else {
                                ts.setTime("Synch");
                            }
                        } else {
                            msg = java.text.MessageFormat.format(
                                    rb.getString("InvalidTime"), (String)value); 
                        }
                    }
                    break;
                case COMMAND_COLUMN:
                    String cmd = null;
                    if ((String)value == null){
                        msg = rb.getString("badCommand");
                        break;
                    } else {
                        cmd = ((String)value).trim().toUpperCase();
                    }
                    if ("SPEED".equals(cmd) || "SPEEDSTEP".equals(cmd) || "FORWARD".equals(cmd)) {
                        ts.setCommand((String)value);
                    } else if (cmd.startsWith("F")) {
                        try {
                            int cmdNum = Integer.parseInt(cmd.substring(1));
                            if (cmdNum < 0 || 28 < cmdNum) {
                                msg = rb.getString("badFunctionNum");
                            } else {
                                ts.setCommand((String)value);
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
                                ts.setCommand((String)value);
                            }
                        } catch (Exception e) {
                            msg = rb.getString("badLockFNum");
                        }
                    } else {
                        msg = rb.getString("badCommand");
                    }
                    break;
                case VALUE_COLUMN:
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
                    } else {
                        msg = rb.getString("badCommand");
                    }
                    if (msg!=null) {
                        JOptionPane.showMessageDialog(null, msg,
                        rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                    }
                    break;
                case BLOCK_COLUMN:
                    OBlock block = InstanceManager.oBlockManagerInstance().provideOBlock((String)value);
                    if (block != null && getIndexOfBlock(block) >= 0) {
                        ts.setBlockName((String)value);
                    } else {
                        msg = java.text.MessageFormat.format(
                                rb.getString("BlockNotFound"), (String)value); 
                    }
                    break;
                case DELETE_COLUMN: 
                    _throttleCommands.remove(row);
                    fireTableDataChanged();
                    return;
            }
            if (msg != null) {
                JOptionPane.showMessageDialog(null, msg,
                        rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            } else {
                fireTableRowsUpdated(row, row);
            }
        }

    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WarrantFrame.class.getName());
}

