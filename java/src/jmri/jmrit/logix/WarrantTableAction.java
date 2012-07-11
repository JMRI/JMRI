package jmri.jmrit.logix;

//import java.util.ArrayList;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

//import java.util.EventObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;


import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
/*
import javax.swing.TransferHandler;
import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
*/
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Path;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.jmrit.catalog.NamedIcon;

/**
 * A WarrantAction contains the operating permissions and directives needed for
 * a train to proceed from an Origin to a Destination.
 * WarrantTableAction provides the menu for panels to List, Edit and Create
 * Warrants.  It launched the appropiate frame for each action.
 * <P>
 * It contains an internal class, TableFrame, that lists existing Warrants
 * controls many of their functions.
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
 *
 * @author	Pete Cressman  Copyright (C) 2009, 2010
 */
public class WarrantTableAction extends AbstractAction {

	public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.logix.WarrantBundle");
    static int STRUT_SIZE = 10;
    static JMenu _warrantMenu;
    private static HashMap <String, WarrantFrame> _frameMap = new HashMap <String, WarrantFrame> ();
    private static TableFrame _tableFrame;
    private static JTextArea _textArea;
    private static boolean _hasErrors = false;
    private static JDialog _errorDialog;

    JTextField  _startWarrant = new JTextField(30);
    JTextField  _endWarrant = new JTextField(30);

    public WarrantTableAction(String menuOption) {
	    super(rb.getString(menuOption));
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (rb.getString("ShowWarrants").equals(command)){
            if (_tableFrame==null) {
                _tableFrame = new TableFrame();
                try {
                    _tableFrame.initComponents();
                } catch (Exception ex ) {/*bogus*/ }
            } else {
                _tableFrame.setVisible(true);
                _tableFrame.pack();
            }
        } else if (rb.getString("CreateWarrant").equals(command)){
            CreateWarrantFrame f = new CreateWarrantFrame();
            try {
                f.initComponents();
            } catch (Exception ex ) {/*bogus*/ }
            f.setVisible(true);
        }
        initPathPortalCheck();
        OBlockManager manager = InstanceManager.oBlockManagerInstance();
        String[] sysNames = manager.getSystemNameArray();
        for (int i = 0; i < sysNames.length; i++) {
            OBlock block = manager.getBySystemName(sysNames[i]);
            checkPathPortals(block);
        }
        showPathPortalErrors();
    }

    synchronized public static void updateWarrantMenu() {
        _warrantMenu.removeAll();
        _warrantMenu.add(new WarrantTableAction("ShowWarrants"));
        JMenu editWarrantMenu = new JMenu(rb.getString("EditWarrantMenu"));
        _warrantMenu.add(editWarrantMenu);
        ActionListener editWarrantAction = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                openWarrantFrame(e.getActionCommand());
            }
        };
        WarrantManager manager = InstanceManager.warrantManagerInstance();
        String[] sysNames = manager.getSystemNameArray();
         
        for (int i = 0; i < sysNames.length; i++) {
            Warrant warrant = manager.getBySystemName(sysNames[i]);
            JMenuItem mi = new JMenuItem(warrant.getDisplayName());
            mi.setActionCommand(warrant.getDisplayName());
            mi.addActionListener(editWarrantAction);
            editWarrantMenu.add(mi);                                                  
        }

        _warrantMenu.add(new jmri.jmrit.logix.WarrantTableAction("CreateWarrant"));
        if (log.isDebugEnabled()) log.debug("updateMenu to "+sysNames.length+" warrants.");
    }

    synchronized public static void closeWarrantFrame(String key) {
        _frameMap.remove(key);
    }

    synchronized public static void openWarrantFrame(String key) {
        WarrantFrame frame = _frameMap.get(key);
        if (frame==null) {
            frame = new WarrantFrame(key);
            _frameMap.put(key, frame);
        }
        if (log.isDebugEnabled()) log.debug("openWarrantFrame for "+key+", size= "+_frameMap.size());
        frame.setVisible(true);
        frame.toFront();
    }

    synchronized public WarrantFrame getOpenWarrantFrame(String key) {
        return _frameMap.get(key);
    }

    public static void initPathPortalCheck() {
        if (_errorDialog!=null) {
            _hasErrors = false;
            _textArea = null;
            _errorDialog.dispose();
        }        
    }
    /**
    *  Validation of paths within a block.
    *  Gathers messages in a text area that can be displayed after all
    * are written.
    */
    public static void checkPathPortals(OBlock b) {
        // warn user of incomplete blocks and portals
        if (_textArea==null) {
            _textArea = new javax.swing.JTextArea(10, 50);
            _textArea.setEditable(false);
            _textArea.setTabSize(4);
            _textArea.append(rb.getString("ErrWarnAreaMsg"));
            _textArea.append("\n\n");
        }
        List <Path> pathList = b.getPaths();
        if (pathList.size()==0) {
            _textArea.append(java.text.MessageFormat.format(
                                rb.getString("NoPaths"), b.getDisplayName()));
            _textArea.append("\n");
            _hasErrors = true;
            return;
        }
        List <Portal> pList = b.getPortals();
        // make list of names of all portals.  Then remove those we check, leaving the orphans
        ArrayList <String> portalList =new ArrayList <String>();
        for (int i=0; i<pList.size(); i++) {
            Portal portal = pList.get(i);
            if (portal.getFromPaths().size()==0) {
                _textArea.append(java.text.MessageFormat.format(
                                    rb.getString("BlockPortalNoPath"), portal.getName(),
                                     portal.getFromBlockName()));
                _textArea.append("\n");
                _hasErrors = true;
                return;
            }
            if (portal.getToPaths().size()==0) {
                _textArea.append(java.text.MessageFormat.format(
                                    rb.getString("BlockPortalNoPath"), portal.getName(),
                                     portal.getToBlockName()));
                _textArea.append("\n");
                _hasErrors = true;
                return;
            }
            portalList.add(portal.getName());
        }
        for (int i=0; i<pathList.size(); i++) {
            OPath path = (OPath)pathList.get(i);
            OBlock block = (OBlock)path.getBlock();
            if  (block==null || !block.equals(b)) {
                _textArea.append(java.text.MessageFormat.format(
                        rb.getString("PathWithBadBlock"), path.getName(), b.getDisplayName()));
                _textArea.append("\n");
                _hasErrors = true;
                return;
            }
            String msg = null;
            boolean hasPortal = false;
            Portal fromPortal = path.getFromPortal();
            if (fromPortal!=null) {
                if (!fromPortal.isValid()){
                    msg = fromPortal.getName();
                }
                hasPortal = true;
                portalList.remove(fromPortal.getName());
            }
            Portal toPortal = path.getToPortal();
            if (toPortal!=null) {
                 if (!toPortal.isValid()) {
                     msg = toPortal.getName();
                 }
                 hasPortal = true;
                 portalList.remove(toPortal.getName());
                 if (fromPortal!=null && fromPortal.equals(toPortal)) {
                     _textArea.append(java.text.MessageFormat.format(
                             rb.getString("PathWithDuplicatePortal"), path.getName(), b.getDisplayName()));
                     _textArea.append("\n");
                 }
            }
            if (msg != null ) {
                _textArea.append(java.text.MessageFormat.format(
                        rb.getString("PortalNeedsBlock"), msg));
                _textArea.append("\n");
                _hasErrors = true;
            } else if (!hasPortal) {
                _textArea.append(java.text.MessageFormat.format(
                        rb.getString("PathNeedsPortal"), path.getName(), b.getDisplayName()));
                _textArea.append("\n");
                _hasErrors = true;
            }
        }
        if (portalList.size() > 0) {
            _textArea.append(java.text.MessageFormat.format(
                    rb.getString("BlockPortalNoPath"), portalList.get(0), b.getDisplayName()));
            _textArea.append("\n");
            _hasErrors = true;
        }            
    }
    public static boolean showPathPortalErrors() {
        if (!_hasErrors) { return false; }
        if (_textArea==null) {
            log.error("_textArea is null!.");
            return true;
        }
        JScrollPane scrollPane = new JScrollPane(_textArea);
        _errorDialog = new JDialog();
        _errorDialog.setTitle(rb.getString("ErrorDialogTitle"));
        JButton ok = new JButton(rb.getString("ButtonOK"));
        class myListener extends java.awt.event.WindowAdapter implements ActionListener {
           /*  java.awt.Window _w;
             myListener(java.awt.Window w) {
                 _w = w;
             }  */
             public void actionPerformed(ActionEvent e) {
                 _hasErrors = false;
                 _textArea = null;
                 _errorDialog.dispose();
             }
             public void windowClosing(java.awt.event.WindowEvent e) {
                 _hasErrors = false;
                 _textArea = null;
                 _errorDialog.dispose();
             }
        }
        ok.addActionListener(new myListener());
        ok.setMaximumSize(ok.getPreferredSize());

        java.awt.Container contentPane = _errorDialog.getContentPane();  
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(Box.createVerticalStrut(5));
        contentPane.add(Box.createVerticalGlue());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(ok);
        contentPane.add(panel, BorderLayout.SOUTH);
        _errorDialog.addWindowListener( new myListener());
        _errorDialog.pack();
        _errorDialog.setVisible(true);
        return true;
    }

    /******************* CreateWarrant ***********************/

    static class CreateWarrantFrame extends JFrame {

        JTextField _sysNameBox;
        JTextField _userNameBox;

        Warrant _startW;
        Warrant _endW;

        public CreateWarrantFrame() {
            setTitle(WarrantTableAction.rb.getString("TitleCreateWarrant"));
        }

        public void initComponents() {
            JPanel contentPane = new JPanel();
            contentPane.setLayout(new BorderLayout(10,10));
            JLabel prompt = new JLabel(rb.getString("CreateWarrantPrompt"));
            contentPane.add(prompt, BorderLayout.NORTH);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            JPanel p = new JPanel();
            p.add(new JLabel(rb.getString("SystemName")));
            _sysNameBox = new JTextField(15);
            p.add(_sysNameBox);
            panel.add(p);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            p = new JPanel();
            p.add(new JLabel(rb.getString("UserName")));
            _userNameBox = new JTextField(15);
            p.add(_userNameBox);
            panel.add(p);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            contentPane.add(panel, BorderLayout.CENTER);

            panel = new JPanel();
            JButton doneButton = new JButton(rb.getString("ButtonDone"));
            doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    makeWarrant();
                }
            });
            doneButton.setPreferredSize(doneButton.getPreferredSize());
            panel.add(doneButton);
            contentPane.add(panel, BorderLayout.SOUTH);
            contentPane.add(Box.createVerticalStrut(STRUT_SIZE), BorderLayout.EAST);
            contentPane.add(Box.createVerticalStrut(STRUT_SIZE), BorderLayout.WEST);

            setContentPane(contentPane);
            setLocationRelativeTo(null);
            setVisible(true);
            pack();
        }

        void concatenate(Warrant startW, Warrant endW) {
            _startW = startW;
            _endW = endW;
        }

        void makeWarrant() {
            String sysName = _sysNameBox.getText().trim();
            String userName = _userNameBox.getText().trim();
            if (sysName==null || sysName.length()==0 || sysName.toUpperCase().equals("IW")) {
                dispose();
                return;
            }
            if (userName.trim().length()==0) {
                userName = null;
            }
            boolean failed = false;
            Warrant w = InstanceManager.warrantManagerInstance().getBySystemName(sysName);
            if (w != null) {
                failed = true;
            } else {
                w = InstanceManager.warrantManagerInstance().getByUserName(userName);
                if (w != null) {
                    failed = true;
                } else {
                    // register warrant if user saves this instance
                    w = new Warrant(sysName, userName);
                }
            }
            if (failed) {
                JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                rb.getString("WarrantExists"), userName, sysName), 
                                rb.getString("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            } else {
                if (_startW!=null && _endW!=null) {
                    List <BlockOrder> orders = _startW.getOrders();
                    int limit = orders.size()-1;
                    for (int i=0; i<limit; i++) {
                        w.addBlockOrder(new BlockOrder(orders.get(i)));
                    }
                    BlockOrder bo = new BlockOrder(orders.get(limit)); 
                    orders = _endW.getOrders();
                    bo.setExitName(orders.get(0).getExitName());
                    w.addBlockOrder(bo);
                    for (int i=1; i<orders.size(); i++) {
                        w.addBlockOrder(new BlockOrder(orders.get(i)));
                    }

                    List <ThrottleSetting> commands = _startW.getThrottleCommands();
                    for (int i=0; i<commands.size(); i++) {
                        w.addThrottleCommand(new ThrottleSetting(commands.get(i)));
                    }
                    commands = _endW.getThrottleCommands();
                    for (int i=0; i<commands.size(); i++) {
                        w.addThrottleCommand(new ThrottleSetting(commands.get(i)));
                    }
                    _frameMap.put(w.getDisplayName(), new WarrantFrame(w, false));
                } else {
                    _frameMap.put(w.getDisplayName(), new WarrantFrame(w, true));
                }
                dispose();
            }
        }

    }

    /**
    *  Note: _warrantMenu is static
    */
    synchronized public static JMenu makeWarrantMenu() {
        _warrantMenu = new JMenu(rb.getString("MenuWarrant"));
        if (jmri.InstanceManager.oBlockManagerInstance().getSystemNameList().size() > 1) {
            updateWarrantMenu();
        } else {
        	return null;
        }
        return _warrantMenu;
    }


    /********************** Show Warrants Table *************************/

    static final String halt = rb.getString("Halt");
    static final String resume = rb.getString("Resume");
    static final String abort = rb.getString("Abort");
    static final String[] controls = {halt, resume, abort};

    class TableFrame  extends jmri.util.JmriJFrame // implements ActionListener 
    {
        private WarrantTableModel     _model;


        public TableFrame() 
        {
            setTitle(rb.getString("WarrantTable"));
            _model = new WarrantTableModel();
            _model.init();
            JTable table;   // = new JTable(_model);
            try {   // following might fail due to a missing method on Mac Classic
                jmri.util.com.sun.TableSorter sorter = new jmri.util.com.sun.TableSorter(_model);
                table = jmri.util.JTableUtil.sortableDataModel(sorter);
                sorter.setTableHeader(table.getTableHeader());
            } catch (Throwable e) { // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
                log.error("WarrantTable: Unexpected error: "+e);
                table = new JTable(_model);
            }

            table.setDefaultRenderer(Boolean.class, new ButtonRenderer());
            table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
            table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
            JComboBox box = new JComboBox(controls);
            box.setFont(new Font(null, Font.PLAIN, 12));
            table.getColumnModel().getColumn(WarrantTableModel.CONTROL_COLUMN).setCellEditor(new DefaultCellEditor(box));
            table.getColumnModel().getColumn(WarrantTableModel.ROUTE_COLUMN).setCellEditor(new ComboBoxCellEditor(new JComboBox()));
            table.getColumnModel().getColumn(WarrantTableModel.ALLOCATE_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(WarrantTableModel.ALLOCATE_COLUMN).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(WarrantTableModel.DEALLOC_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(WarrantTableModel.DEALLOC_COLUMN).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(WarrantTableModel.SET_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(WarrantTableModel.SET_COLUMN).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(WarrantTableModel.AUTO_RUN_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(WarrantTableModel.AUTO_RUN_COLUMN).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(WarrantTableModel.MANUAL_RUN_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(WarrantTableModel.MANUAL_RUN_COLUMN).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(WarrantTableModel.EDIT_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(WarrantTableModel.EDIT_COLUMN).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(WarrantTableModel.DELETE_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(WarrantTableModel.DELETE_COLUMN).setCellRenderer(new ButtonRenderer());
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i=0; i<_model.getColumnCount(); i++) {
                int width = _model.getPreferredWidth(i);
                table.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
            table.setRowHeight(box.getPreferredSize().height);
            table.setDragEnabled(true);
            table.setTransferHandler(new jmri.util.DnDTableExportHandler());
            JScrollPane tablePane = new JScrollPane(table);
            Dimension dim = table.getPreferredSize();
            dim.height = table.getRowHeight()*12;
            tablePane.getViewport().setPreferredSize(dim);

            JPanel tablePanel = new JPanel();
            tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
            JLabel title = new JLabel(rb.getString("ShowWarrants"));
            tablePanel.add(title, BorderLayout.NORTH);
            tablePanel.add(tablePane, BorderLayout.CENTER);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(Box.createHorizontalStrut(2*STRUT_SIZE));
            //JPanel p = new JPanel();
            //p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            JPanel pp = new JPanel();
            pp.setLayout(new FlowLayout());
            pp.add(new JLabel("A:"));
            pp.add(_startWarrant);
            _startWarrant.setDragEnabled(true);
            _startWarrant.setTransferHandler(new jmri.util.DnDStringImportHandler());
            panel.add(pp);
            pp = new JPanel();
            pp.setLayout(new FlowLayout());
            pp.add(new JLabel("B:"));
            pp.add(_endWarrant);
            _endWarrant.setDragEnabled(true);
            _endWarrant.setTransferHandler(new jmri.util.DnDStringImportHandler());
            panel.add(pp);
            JButton concatButton = new JButton(rb.getString("Concatenate"));
            concatButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    concatenate();
                }
            });
            //panel.add(p);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            panel.add(concatButton);
            panel.add(Box.createHorizontalStrut(2*STRUT_SIZE));
            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
            bottomPanel.add(new JLabel(rb.getString("JoinPrompt")));
            bottomPanel.add(panel);
            tablePanel.add(bottomPanel, BorderLayout.SOUTH);

            addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        dispose();
                    }
                });			
            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
            fileMenu.add(new jmri.configurexml.SaveMenu());
            menuBar.add(fileMenu);
            setJMenuBar(menuBar);
            addHelpMenu("package.jmri.jmrit.logix.Warrant", true);

            setContentPane(tablePanel);
            setLocation(0,100);
            setVisible(true);
            pack();
        }
    }

    private void concatenate() {
        WarrantManager manager = InstanceManager.warrantManagerInstance();
        Warrant startW = manager.getWarrant(_startWarrant.getText().trim());
        Warrant endW = manager.getWarrant(_endWarrant.getText().trim());
        if (startW==null || endW==null) {
            JOptionPane.showMessageDialog(null, rb.getString("BadWarrantNames"),
                    WarrantTableAction.rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        BlockOrder last = startW.getLastOrder();
        BlockOrder next = endW.getfirstOrder();
        if (last==null || next==null) {
            JOptionPane.showMessageDialog(null, rb.getString("EmptyRoutes"),
                    WarrantTableAction.rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!last.getPathName().equals(next.getPathName())) {
            JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                    rb.getString("RoutesDontMatch"), startW.getDisplayName(), endW.getDisplayName()),
                    WarrantTableAction.rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        CreateWarrantFrame f = new CreateWarrantFrame();
        try {
            f.initComponents();
            f.concatenate(startW, endW);
        } catch (Exception ex ) { log.error("error making CreateWarrantFrame", ex);}
        f.setVisible(true);
    }

    static public class ComboBoxCellEditor extends DefaultCellEditor
    {
        ComboBoxCellEditor() {
            super(new JComboBox());
        }
        ComboBoxCellEditor(JComboBox comboBox) {
            super(comboBox);
            comboBox.setFont(new Font(null, Font.PLAIN, 12));
        }
        public Component getTableCellEditorComponent(JTable table, Object value, 
                                         boolean isSelected, int row, int column) 
        {
            WarrantTableModel model = (WarrantTableModel)table.getModel();
            Warrant warrant = model.getWarrantAt(row);
            JComboBox comboBox = (JComboBox)getComponent();
            comboBox.removeAllItems();
            List <BlockOrder> orders = warrant.getOrders();
            for (int i=0; i<orders.size(); i++) {
                comboBox.addItem(orders.get(i).getBlock().getDisplayName());
            }
            return comboBox; 
        }
    }
    
    /************************* WarrantTableModel Table ******************************/

    class WarrantTableModel extends AbstractTableModel implements PropertyChangeListener 
    {
        public static final int WARRANT_COLUMN = 0;
        public static final int ROUTE_COLUMN =1;
        public static final int TRAIN_ID_COLUMN = 2;
        public static final int ADDRESS_COLUMN = 3;
        public static final int ALLOCATE_COLUMN = 4;
        public static final int DEALLOC_COLUMN = 5;
        public static final int SET_COLUMN = 6;
        public static final int AUTO_RUN_COLUMN = 7;
        public static final int MANUAL_RUN_COLUMN = 8;
        public static final int CONTROL_COLUMN = 9;
        public static final int EDIT_COLUMN = 10;
        public static final int DELETE_COLUMN = 11;
        public static final int NUMCOLS = 12;

        WarrantManager _manager;
        protected ArrayList <Warrant>       _warList;

        public WarrantTableModel() {
            super();
            _manager = InstanceManager.warrantManagerInstance();
            _manager.addPropertyChangeListener(this);   // for adds and deletes
        }

        public void init() {
            if (_warList != null) {
                for (int i=0; i<_warList.size(); i++) {
                    _warList.get(i).removePropertyChangeListener(this);
                }
            }
            List <String> systemNameList = _manager.getSystemNameList();
            _warList = new ArrayList <Warrant> (systemNameList.size());

            Iterator <String> iter = systemNameList.iterator();
            while (iter.hasNext()) {
                _warList.add(_manager.getBySystemName(iter.next()));
            }
            // add name change listeners
            for (int i=0; i<_warList.size(); i++) {
                _warList.get(i).addPropertyChangeListener(this);
            }
            if (log.isDebugEnabled()) log.debug("_warList has "+_warList.size()+" warrants");
        }
        
        public Warrant getWarrantAt(int index) {
            return _warList.get(index);
        }

        public int getRowCount () {
            return _warList.size();
        }

        public int getColumnCount () {
            return NUMCOLS;
        }

        public String getColumnName(int col) {
            switch (col) {
                case WARRANT_COLUMN: return WarrantTableAction.rb.getString("Warrant");
                case ROUTE_COLUMN: return WarrantTableAction.rb.getString("Route");
                case TRAIN_ID_COLUMN: return WarrantTableAction.rb.getString("TrainId");
                case ADDRESS_COLUMN: return WarrantTableAction.rb.getString("DccAddress");
                case ALLOCATE_COLUMN: return WarrantTableAction.rb.getString("Allocate");
                case DEALLOC_COLUMN: return WarrantTableAction.rb.getString("Deallocate");
                case SET_COLUMN: return WarrantTableAction.rb.getString("SetRoute");
                case AUTO_RUN_COLUMN: return WarrantTableAction.rb.getString("ARun");
                case MANUAL_RUN_COLUMN: return WarrantTableAction.rb.getString("MRun");
                case CONTROL_COLUMN: return WarrantTableAction.rb.getString("Control");
            }
            return "";
        }


        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case WARRANT_COLUMN:
                    return false;
                case TRAIN_ID_COLUMN:
                case ADDRESS_COLUMN:
                case ROUTE_COLUMN:
                case ALLOCATE_COLUMN:
                case DEALLOC_COLUMN:
                case SET_COLUMN:
                case AUTO_RUN_COLUMN:
                case MANUAL_RUN_COLUMN:
                case CONTROL_COLUMN:
                case EDIT_COLUMN:
                case DELETE_COLUMN:
                    return true;
            }
            return false;
        }

        public Class<?> getColumnClass(int col) {
            switch (col) {
                case WARRANT_COLUMN:  return String.class;
                case ROUTE_COLUMN:    return String.class;  // JComboBox.class;
                case TRAIN_ID_COLUMN: return String.class;
                case ADDRESS_COLUMN:  return String.class;
                case ALLOCATE_COLUMN: return JButton.class;
                case DEALLOC_COLUMN:  return JButton.class;
                case SET_COLUMN:    return JButton.class;
                case AUTO_RUN_COLUMN: return JButton.class;
                case MANUAL_RUN_COLUMN: return JButton.class;
                case CONTROL_COLUMN:  return String.class; // JComboBox.class;
                case EDIT_COLUMN:     return JButton.class;
                case DELETE_COLUMN:   return JButton.class;
            }
            return String.class;
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case WARRANT_COLUMN:
                case ROUTE_COLUMN:
                case TRAIN_ID_COLUMN:
                    return new JTextField(13).getPreferredSize().width;
                case ADDRESS_COLUMN:
                    return new JTextField(5).getPreferredSize().width;
                case ALLOCATE_COLUMN:
                case DEALLOC_COLUMN:
                case SET_COLUMN:
                case AUTO_RUN_COLUMN:
                case MANUAL_RUN_COLUMN:
                    return new JButton("XX").getPreferredSize().width;
                case CONTROL_COLUMN:
                    return new JTextField(25).getPreferredSize().width;
                case EDIT_COLUMN:
                case DELETE_COLUMN:
                    return new JButton("DELETE").getPreferredSize().width;
            }
            return new JTextField(10).getPreferredSize().width;
        }

        public Object getValueAt(int row, int col) {
            //if (log.isDebugEnabled()) log.debug("getValueAt: row= "+row+", column= "+col);
            Warrant w = getWarrantAt(row);
            // some error checking
            if (w == null){
            	log.debug("Warrant is null!");
            	return "";
            }
            JRadioButton allocButton = new JRadioButton();
            JRadioButton deallocButton = new JRadioButton();
            ButtonGroup group = new ButtonGroup();
            group.add(allocButton);
            group.add(deallocButton);
            switch (col) {
                case WARRANT_COLUMN:
                    return w.getDisplayName();
                case ROUTE_COLUMN:
                    BlockOrder bo = w.getBlockOrderAt(0);
                    if (bo!=null) {
                        return java.text.MessageFormat.format(WarrantTableAction.rb.getString("Origin"),
                                                           bo.getBlock().getDisplayName());
                    }
                    break;
                case TRAIN_ID_COLUMN:
                    return w.getTrainId();
                case ADDRESS_COLUMN:
                    if (w.getDccAddress()!=null) {
                        return w.getDccAddress().toString();
                    }
                    break;
                case ALLOCATE_COLUMN:
                    if (w.isAllocated()) {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-occupied.gif", "occupied");
                    } else {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-empty.gif", "off");
                    }
                case DEALLOC_COLUMN:
                    if (w.isAllocated()) {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-empty.gif", "off");
                    } else {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-occupied.gif", "occupied");
                    }
                case SET_COLUMN:
                    if (w.hasRouteSet()) {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-green.gif", "off");
                    } else {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-empty.gif", "occupied");
                    }
                case AUTO_RUN_COLUMN:
                    if (w.getRunMode() == Warrant.MODE_RUN) {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "red");
                    } else {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-empty.gif", "off");
                    }
                case MANUAL_RUN_COLUMN:
                    if (w.getRunMode() == Warrant.MODE_MANUAL) {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "red");
                    } else {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-empty.gif", "off");
                    }
                case CONTROL_COLUMN:
                    String msg = w.getRunningMessage();
                    WarrantFrame frame = getOpenWarrantFrame(w.getDisplayName());
                    if (frame !=null) {
                        frame._statusBox.setText(msg);
                    }
                    return msg;
                case EDIT_COLUMN:
                    return WarrantTableAction.rb.getString("ButtonEdit");
                case DELETE_COLUMN:
                    return WarrantTableAction.rb.getString("ButtonDelete");
            }
            return "";
        }

        public void setValueAt(Object value, int row, int col) {
            if (log.isDebugEnabled()) log.debug("setValueAt: row= "+row+", column= "+col+", value= "+value.getClass().getName());
            Warrant w = getWarrantAt(row);
            String msg = null;
            switch (col) {
                case WARRANT_COLUMN:
                case ROUTE_COLUMN:
                    return;
                case TRAIN_ID_COLUMN:
                    w.setTrainId((String)value);
                    break;
                case ADDRESS_COLUMN:
                    String addr = (String)value;
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
                            w.setDccAddress( new DccLocoAddress(n, isLong));
                        } catch (NumberFormatException nfe) {
                            msg = java.text.MessageFormat.format(rb.getString("BadDccAddress"), addr);
                        }
                    }
                    break;
                case ALLOCATE_COLUMN:
                    msg = w.allocateRoute();
                    break;
                case DEALLOC_COLUMN:
                    if (w.getRunMode() == Warrant.MODE_NONE) {
                        w.deAllocate();
                    } else {
                        msg = java.text.MessageFormat.format(
                                WarrantTableAction.rb.getString("TrainRunning"), w.getDisplayName());
                    }
                    break;
                case SET_COLUMN:
                    msg = w.setRoute(0, null);
                    break;
                case AUTO_RUN_COLUMN:
                    if (w.getRunMode() == Warrant.MODE_NONE) {
                        DccLocoAddress address = w.getDccAddress();
                        if (address == null) {
                            msg = java.text.MessageFormat.format(
                                WarrantTableAction.rb.getString("NoAddress"), w.getDisplayName());
                            break;
                        }
                        if (w.getThrottleCommands().size() == 0) {
                            msg = java.text.MessageFormat.format(
                                    WarrantTableAction.rb.getString("NoCommands"), w.getDisplayName());
                            break;
                        }
                        if (w.getOrders().size() == 0) {
                            msg = WarrantTableAction.rb.getString("EmptyRoute");
                            break;
                        }
                        msg = w.setRoute(0, null);
                        if (log.isDebugEnabled()) log.debug("w.setRoute= "+msg);
                        if (msg!=null) {
                            BlockOrder bo = w.getfirstOrder();
                            OBlock block = bo.getBlock();
                            String msg2 = block.allocate(w);
                            if (msg2 == null) {
                                if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null,
                                            java.text.MessageFormat.format(WarrantTableAction.rb.getString("OkToRun"),
                                            msg), WarrantTableAction.rb.getString("WarningTitle"), 
                                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
                                    w.deAllocate();
                                    return;
                                }
                                block.setPath(bo.getPathName(), w);
                                msg = null;
                            } else {
                                if (log.isDebugEnabled()) log.debug("block.allocate(w)= "+msg2);
                                msg = java.text.MessageFormat.format(WarrantTableAction.rb.getString("OriginBlockNotSet"), 
                                        msg2);
                                break;
                            } 
                        }
                        msg = w.runAutoTrain(true);
                        if (log.isDebugEnabled()) log.debug("w.runAutoTrain= "+msg);
                    } else {
                        msg = java.text.MessageFormat.format(
                                WarrantTableAction.rb.getString("TrainRunning"), w.getDisplayName());
                    }
                    break;
                case MANUAL_RUN_COLUMN:
                    if (w.getRunMode() == Warrant.MODE_NONE) {
                        if (w.getOrders().size() == 0) {
                            msg = WarrantTableAction.rb.getString("EmptyRoute");
                            break;
                        }
                        msg = w.setRoute(0, null);
                        if (log.isDebugEnabled()) log.debug("w.setRoute= "+msg);
                        if (msg!=null) {
                            BlockOrder bo = w.getfirstOrder();
                            OBlock block = bo.getBlock();
                            String msg2 = block.allocate(w);
                            if (msg2 == null) {
                                if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null,
                                            java.text.MessageFormat.format(WarrantTableAction.rb.getString("OkToRun"),
                                            msg), WarrantTableAction.rb.getString("WarningTitle"), 
                                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
                                    w.deAllocate();
                                    return;
                                }
                                block.setPath(bo.getPathName(), w);
                                msg = null;
                            } else {
                                if (log.isDebugEnabled()) log.debug("block.allocate(w)= "+msg2);
                                msg = java.text.MessageFormat.format(WarrantTableAction.rb.getString("OriginBlockNotSet"), 
                                        msg2);
                                break;
                            } 
                        }
                        msg = w.runAutoTrain(false);
                        if (log.isDebugEnabled()) log.debug("w.runManualTrain= "+msg);
                    } else {
                        msg = java.text.MessageFormat.format(
                                WarrantTableAction.rb.getString("TrainRunning"), w.getDisplayName());
                    }
                    break;
                case CONTROL_COLUMN:
                    int mode = w.getRunMode();
                    if (mode==Warrant.MODE_RUN || mode==Warrant.MODE_MANUAL) {
                        String setting = (String)value;
                        int s = -1;
                        if (setting.equals(halt)) {
                            s = Warrant.HALT; 
                        } else if (setting.equals(resume)) {
                            s = Warrant.RESUME; 
                        } else if (setting.equals(abort)) {
                            s = Warrant.ABORT;
                        }
                        w.controlRunTrain(s);
                    } else {
                        msg = java.text.MessageFormat.format(
                                WarrantTableAction.rb.getString("NotRunning"), w.getDisplayName());
                    }
                    getValueAt(row,col);
                    break;
                case EDIT_COLUMN:
                    WarrantTableAction.openWarrantFrame(w.getDisplayName());
                    break;
                case DELETE_COLUMN:
                    if (w.getRunMode() == Warrant.MODE_NONE) {
                        _manager.deregister(w);
                        w.dispose();
                    } else {
                    }
                    break;
            }
            if (msg!=null) {
                JOptionPane.showMessageDialog(null, msg,
                        WarrantTableAction.rb.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            }
            fireTableRowsUpdated(row, row);
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a NamedBean added or deleted
                init();
                fireTableDataChanged();
            } else {
                // a value changed.  Find it, to avoid complete redraw
                NamedBean bean = (NamedBean)e.getSource();
                for (int i=0; i<_warList.size(); i++) {
                    if (bean.equals(_warList.get(i)))  {
                        fireTableRowsUpdated(i, i);
                    }
                }
            }
            if (log.isDebugEnabled()) log.debug("propertyChange of \""+e.getPropertyName()+
                                                "\" for "+e.getSource().toString());
        }

    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WarrantTableAction.class.getName());
}
