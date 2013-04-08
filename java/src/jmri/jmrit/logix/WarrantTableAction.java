package jmri.jmrit.logix;

//import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

//import java.util.EventObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
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
/*
import javax.swing.TransferHandler;
import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
*/
import jmri.BeanSetting;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Path;
import jmri.util.com.sun.TableSorter;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

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

    static int STRUT_SIZE = 10;
    static JMenu _warrantMenu;
    private static WarrantTableAction _instance;
    private static HashMap <String, WarrantFrame> _frameMap = new HashMap <String, WarrantFrame> ();
    private static TableFrame _tableFrame;
    private static JTextArea _textArea;
    private static boolean _hasErrors = false;
    private static JDialog _errorDialog;
    private static NXFrame _nxFrame;
    JTextField  _status = new JTextField(90);
    Color _background;

    public WarrantTableAction(String menuOption) {
	    super(Bundle.getMessage(menuOption));
    }
    
    static WarrantTableAction getInstance() {
    	if (_instance==null) {
    		_instance = new WarrantTableAction("ShowWarrants");
    	}
    	return _instance;
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (Bundle.getMessage("ShowWarrants").equals(command)){
            if (_tableFrame==null) {
                _tableFrame = new TableFrame();
                try {
                    _tableFrame.initComponents();
                } catch (Exception ex ) {/*bogus*/ }
            } else {
                _tableFrame.setVisible(true);
                _tableFrame.pack();
            }
        } else if (Bundle.getMessage("CreateWarrant").equals(command)){
            CreateWarrantFrame f = new CreateWarrantFrame();
            try {
                f.initComponents();
            } catch (Exception ex ) {/*bogus*/ }
            f.setVisible(true);
        }
        initPathPortalCheck();
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        String[] sysNames = manager.getSystemNameArray();
        for (int i = 0; i < sysNames.length; i++) {
            OBlock block = manager.getBySystemName(sysNames[i]);
            checkPathPortals(block);
        }
        showPathPortalErrors();
    }
    
    /**
    *  Note: _warrantMenu is static
    */
    synchronized public static JMenu makeWarrantMenu() {
        _warrantMenu = new JMenu(Bundle.getMessage("MenuWarrant"));
        if (jmri.InstanceManager.getDefault(OBlockManager.class).getSystemNameList().size() > 1) {
            updateWarrantMenu();
        } else {
        	return null;
        }
        return _warrantMenu;
    }

    synchronized public static void updateWarrantMenu() {
        _warrantMenu.removeAll();
        _warrantMenu.add(getInstance());
        JMenu editWarrantMenu = new JMenu(Bundle.getMessage("EditWarrantMenu"));
        _warrantMenu.add(editWarrantMenu);
        ActionListener editWarrantAction = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                openWarrantFrame(e.getActionCommand());
            }
        };
        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
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

    synchronized public static void openWarrantFrame(Warrant w) {
    	String key = w.getDisplayName();
        WarrantFrame frame = _frameMap.get(key);
        if (frame==null) {
            frame = new WarrantFrame(w);
            _frameMap.put(key, frame);
        }
        if (log.isDebugEnabled()) log.debug("openWarrantFrame for "+key+", size= "+_frameMap.size());
        frame.setVisible(true);
        frame.toFront();
    }

    synchronized public WarrantFrame getOpenWarrantFrame(String key) {
        return _frameMap.get(key);
    }
    
    synchronized static public void mouseClickedOnBlock(OBlock block) {
    	if (_nxFrame!=null) {
    		_nxFrame.mouseClickedOnBlock(block);
    	}
    }
    
    /******************** Error checking ************************/

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
    	if (log.isDebugEnabled()) log.debug("checkPathPortals for "+b.getDisplayName());
        // warn user of incomplete blocks and portals
        if (_textArea==null) {
            _textArea = new javax.swing.JTextArea(10, 50);
            _textArea.setEditable(false);
            _textArea.setTabSize(4);
            _textArea.append(Bundle.getMessage("ErrWarnAreaMsg"));
            _textArea.append("\n\n");
        }
        List <Path> pathList = b.getPaths();
        if (pathList.size()==0) {
            _textArea.append(Bundle.getMessage("NoPaths", b.getDisplayName()));
            _textArea.append("\n");
            _hasErrors = true;
            return;
        }
        List <Portal> portalList = b.getPortals();
        // make list of names of all portals.  Then remove those we check, leaving the orphans
        ArrayList <String> portalNameList =new ArrayList <String>();
        for (int i=0; i<portalList.size(); i++) {
            Portal portal = portalList.get(i);
            if (portal.getFromPaths().size()==0) {
                _textArea.append(Bundle.getMessage("BlockPortalNoPath", portal.getName(),
                                     portal.getFromBlockName()));
                _textArea.append("\n");
                _hasErrors = true;
                return;
            }
            if (portal.getToPaths().size()==0) {
                _textArea.append(Bundle.getMessage("BlockPortalNoPath", portal.getName(),
                                     portal.getToBlockName()));
                _textArea.append("\n");
                _hasErrors = true;
                return;
            }
            portalNameList.add(portal.getName());
        }
        Iterator <Path> iter = pathList.iterator();
        while (iter.hasNext()) {
            OPath path = (OPath)iter.next();
            OBlock block = (OBlock)path.getBlock();
            if  (block==null || !block.equals(b)) {
                _textArea.append(Bundle.getMessage("PathWithBadBlock", path.getName(), b.getDisplayName()));
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
                portalNameList.remove(fromPortal.getName());
            }
            Portal toPortal = path.getToPortal();
            if (toPortal!=null) {
                 if (!toPortal.isValid()) {
                     msg = toPortal.getName();
                 }
                 hasPortal = true;
                 portalNameList.remove(toPortal.getName());
                 if (fromPortal!=null && fromPortal.equals(toPortal)) {
                     _textArea.append(Bundle.getMessage("PathWithDuplicatePortal",
                    		 path.getName(), b.getDisplayName()));
                     _textArea.append("\n");
                 }
            }
            if (msg != null ) {
                _textArea.append(Bundle.getMessage("PortalNeedsBlock", msg));
                _textArea.append("\n");
                _hasErrors = true;
            } else if (!hasPortal) {
                _textArea.append(Bundle.getMessage("PathNeedsPortal", 
                		path.getName(), b.getDisplayName()));
                _textArea.append("\n");
                _hasErrors = true;
            }
            // check that the path's portals have the path in their lists
            boolean validPath;
            if (toPortal!=null) {
            	if (fromPortal!=null) {
            		validPath = toPortal.isValidPath(path) && fromPortal.isValidPath(path);
            	} else {
            		validPath = toPortal.isValidPath(path);
            	}
            }else {
            	if (fromPortal!=null) {
            		validPath = fromPortal.isValidPath(path);
            	} else {
            		validPath = false;
            	}            	
            }
            if (!validPath) {            	
                _textArea.append(Bundle.getMessage("PathNotConnectedToPortal", 
                		path.getName(), b.getDisplayName()));
                _textArea.append("\n");
                _hasErrors =true;
            }
        }
        for (int i=0; i<portalNameList.size(); i++) {
            _textArea.append(Bundle.getMessage("BlockPortalNoPath", 
            		portalNameList.get(i), b.getDisplayName()));
            _textArea.append("\n");
            _hasErrors = true;
        }
        // check whether any turnouts are shared between two blocks;
        checkSharedTurnouts(b);
    }
    
    public static boolean checkSharedTurnouts(OBlock block) {
    	boolean hasShared = false;
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        String[] sysNames = manager.getSystemNameArray();
        List <Path> pathList = block.getPaths();
        Iterator <Path> iter = pathList.iterator();
        while (iter.hasNext()) {
            OPath path = (OPath)iter.next();
            for (int i=0; i < sysNames.length; i++) {
            	if (block.getSystemName().equals(sysNames[i])) {
            		continue;
            	}
                OBlock b = manager.getBySystemName(sysNames[i]);
                Iterator <Path> it = b.getPaths().iterator();
                while (it.hasNext()) {
                    boolean shared = sharedTO(path, (OPath)it.next());
                	if (shared) {
                		hasShared =true;
                        break;
                	}
                }
            }
        }
        return hasShared;
    }
    private static boolean sharedTO(OPath myPath, OPath path) {
        List<BeanSetting> myTOs = myPath.getSettings();
    	Iterator <BeanSetting> iter = myTOs.iterator();
        List<BeanSetting> tos = path.getSettings();
        boolean ret = false;
    	while (iter.hasNext()) {
    		BeanSetting mySet = iter.next();
    		NamedBean myTO = mySet.getBean();
			int myState = mySet.getSetting();
    		Iterator <BeanSetting> it = tos.iterator();
    		while (it.hasNext()) {
    			BeanSetting set = it.next();
    			NamedBean to = set.getBean();
    			if(myTO.equals(to)) {
    				// turnouts are equal.  check if settings are compatible.
    				OBlock myBlock = (OBlock)myPath.getBlock();
    				int state = set.getSetting();
    				OBlock block = (OBlock)path.getBlock();
//    				String note = "WARNING: ";
    				if (myState!=state) {
                       ret = myBlock.addSharedTurnout(myPath, block, path);
/*                       _textArea.append(note+Bundle.getMessage("sharedTurnout", myPath.getName(), myBlock.getDisplayName(), 
                       		 myTO.getDisplayName(), (myState==jmri.Turnout.CLOSED ? "Closed":"Thrown"),
                       		 path.getName(), block.getDisplayName(), to.getDisplayName(), 
                       		 (state==jmri.Turnout.CLOSED ? "Closed":"Thrown")));
                      _textArea.append("\n");
    				} else {
    					note = "Note: "; */
    				}  					
    			}
    		}
    	}
    	return ret;
    }
    
    public static boolean showPathPortalErrors() {
        if (!_hasErrors) { return false; }
        if (_textArea==null) {
            log.error("_textArea is null!.");
            return true;
        }
        JScrollPane scrollPane = new JScrollPane(_textArea);
        _errorDialog = new JDialog();
        _errorDialog.setTitle(Bundle.getMessage("ErrorDialogTitle"));
        JButton ok = new JButton(Bundle.getMessage("ButtonOK"));
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
            setTitle(Bundle.getMessage("TitleCreateWarrant"));
        }

        public void initComponents() {
            JPanel contentPane = new JPanel();
            contentPane.setLayout(new BorderLayout(10,10));
            JLabel prompt = new JLabel(Bundle.getMessage("CreateWarrantPrompt"));
            contentPane.add(prompt, BorderLayout.NORTH);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("SystemName")));
            _sysNameBox = new JTextField(15);
            p.add(_sysNameBox);
            panel.add(p);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("UserName")));
            _userNameBox = new JTextField(15);
            p.add(_userNameBox);
            panel.add(p);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            contentPane.add(panel, BorderLayout.CENTER);

            panel = new JPanel();
            JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
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
            WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
            Warrant w = manager.getBySystemName(sysName);
            if (w != null) {
                failed = true;
            } else {
                w = manager.getByUserName(userName);
                if (w != null) {
                    failed = true;
                } else {
                    // register warrant if user saves this instance
                    w = new Warrant(sysName, userName);
                }
            }
            if (failed) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("WarrantExists", 
                			userName, sysName), Bundle.getMessage("WarningTitle"),
                			JOptionPane.ERROR_MESSAGE);
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

    /********************** Show Warrants Table *************************/

    static final String halt = Bundle.getMessage("Halt");
    static final String resume = Bundle.getMessage("Resume");
    static final String abort = Bundle.getMessage("Abort");
    static final String retry = Bundle.getMessage("Retry");
    static final String[] controls = {halt, resume, retry, abort};

    class TableFrame  extends jmri.util.JmriJFrame 
    {
        JTextField  _startWarrant = new JTextField(30);
        JTextField  _endWarrant = new JTextField(30);

        private WarrantTableModel     _model;
        
        WarrantTableModel getModel() {
        	return _model;
        }

        public TableFrame() 
        {
            setTitle(Bundle.getMessage("WarrantTable"));
            _model = new WarrantTableModel();
            _model.init();
//	Casts at getTableCellEditorComponent() now fails with 3.0 ??            
            JTable table;   // = new JTable(_model);
            ComboBoxCellEditor comboEd;
            try {   // following might fail due to a missing method on Mac Classic
            	TableSorter sorter = new jmri.util.com.sun.TableSorter(_model);
                table = jmri.util.JTableUtil.sortableDataModel(sorter);
                sorter.setTableHeader(table.getTableHeader());
                comboEd = new ComboBoxCellEditor(new JComboBox(), sorter);
                // set model last so later casts will work
                ((jmri.util.com.sun.TableSorter)table.getModel()).setTableModel(_model);
            } catch (Throwable e) { // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
                log.error("WarrantTable: Unexpected error: "+e);
                table = new JTable(_model);
                comboEd = new ComboBoxCellEditor(new JComboBox());
            }
            
            table.setDefaultRenderer(Boolean.class, new ButtonRenderer());
            table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
            table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
            JComboBox box = new JComboBox(controls);
            box.setFont(new Font(null, Font.PLAIN, 12));
            table.getColumnModel().getColumn(WarrantTableModel.CONTROL_COLUMN).setCellEditor(new DefaultCellEditor(box));
            table.getColumnModel().getColumn(WarrantTableModel.ROUTE_COLUMN).setCellEditor(comboEd);
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
            //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
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
            JLabel title = new JLabel(Bundle.getMessage("ShowWarrants"));
            tablePanel.add(title, BorderLayout.NORTH);
            tablePanel.add(tablePane, BorderLayout.CENTER);
            
            JPanel panel = new JPanel();
            JPanel p = new JPanel();
            p.add(new JLabel("status"));
        	_background = _status.getBackground();
        	_status.setFont(jmri.util.FontUtil.deriveFont(_status.getFont(),java.awt.Font.BOLD));
            p.add(_status);
            _status.setEditable(false);
            tablePanel.add(p, BorderLayout.CENTER);
            
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
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
            JButton concatButton = new JButton(Bundle.getMessage("Concatenate"));
            concatButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    concatenate();
                }
            });
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            panel.add(concatButton);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));

            JPanel concatPanel = new JPanel();
            concatPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                    Bundle.getMessage("JoinPrompt"),
                    javax.swing.border.TitledBorder.CENTER,
                    javax.swing.border.TitledBorder.TOP));
            concatPanel.add(panel);

            panel = new JPanel();
            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                    Bundle.getMessage("AutoWarrant"),
                    javax.swing.border.TitledBorder.CENTER,
                    javax.swing.border.TitledBorder.TOP));
            JButton nxButton = new JButton(Bundle.getMessage("CreateNXWarrant"));
            nxButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	nxAction();
                }
            });
            panel.add(nxButton);
            
            pp = new JPanel();
            pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
            pp.add(Box.createHorizontalStrut(STRUT_SIZE));
            pp.add(panel);
            pp.add(Box.createHorizontalStrut(STRUT_SIZE));
            pp.add(concatPanel);
            pp.add(Box.createHorizontalStrut(STRUT_SIZE));
            tablePanel.add(pp, BorderLayout.SOUTH);

            addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        dispose();
                    }
                });			
            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
            fileMenu.add(new jmri.configurexml.SaveMenu());
            menuBar.add(fileMenu);
            setJMenuBar(menuBar);
            addHelpMenu("package.jmri.jmrit.logix.Warrant", true);

            setContentPane(tablePanel);
            setLocation(0,100);
            setVisible(true);
            pack();
        }
        
        private void nxAction() {
        	_nxFrame = new NXFrame(this);
        }
        
        private void concatenate() {
            WarrantManager manager = InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class);
            Warrant startW = manager.getWarrant(_startWarrant.getText().trim());
            Warrant endW = manager.getWarrant(_endWarrant.getText().trim());
            if (startW==null || endW==null) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("BadWarrantNames"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            BlockOrder last = startW.getLastOrder();
            BlockOrder next = endW.getfirstOrder();
            if (last==null || next==null) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("EmptyRoutes"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!last.getPathName().equals(next.getPathName())) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("RoutesDontMatch", 
                		startW.getDisplayName(), endW.getDisplayName()),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            CreateWarrantFrame f = new CreateWarrantFrame();
            try {
                f.initComponents();
                f.concatenate(startW, endW);
            } catch (Exception ex ) { log.error("error making CreateWarrantFrame", ex);}
            f.setVisible(true);
        }
    }
    
    class NXFrame extends WarrantRoute {
    	TableFrame 	_parent;
        JTextField  _dccNumBox = new JTextField();
        JTextField  _speedBox = new JTextField();
        JCheckBox	_forward = new JCheckBox(Bundle.getMessage("forward", true));
        JTextField _searchDepth = new JTextField();
        int _clickCount;

        NXFrame(TableFrame parent) {
    		super();
    		_parent = parent;
    		_clickCount = 0;
    		setTitle(Bundle.getMessage("AutoWarrant"));
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout(10,10));
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//            doSize(_dccNumBox, 50, 20);
//            doSize(_speedBox, 50, 20);
//            doSize(_searchDepth, 50, 20);
            
            panel.add(makeBlockPanels());
            panel.add(Box.createVerticalStrut(STRUT_SIZE));
            panel.add(WarrantFrame.makeTextBoxPanel(false, _dccNumBox, "DccAddress", true));
            panel.add(Box.createVerticalStrut(STRUT_SIZE));
            JPanel pp = new JPanel();
            pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
            pp.add(WarrantFrame.makeTextBoxPanel(false, _speedBox, "Speed", true));
            _forward.setSelected(true);
            pp.add(_forward);
            panel.add(pp);
            panel.add(Box.createVerticalStrut(STRUT_SIZE));
            panel.add(WarrantFrame.makeTextBoxPanel(false, _searchDepth, "SearchDepth", true));
            panel.add(Box.createVerticalStrut(STRUT_SIZE));
            _speedBox.setText("0.5");
            _searchDepth.setText("10");
            JPanel p = new JPanel();
            JButton button = new JButton(Bundle.getMessage("ButtonRunNX"));
            button.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                        	makeAndRunWarrant();
                        }
                    });
            p.add(button);
            button = new JButton(Bundle.getMessage("ButtonCancel"));
            button.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                        	dispose();
                        	_nxFrame = null;
                        }
                    });
            p.add(button);
            panel.add(p);
            mainPanel.add(panel);
            getContentPane().add(mainPanel);
            addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    dispose();
                    _nxFrame = null;
                }
            });
            setLocation(parent.getLocation().x+200, parent.getLocation().y+100);
            setAlwaysOnTop(true);
            pack();
            setVisible(true);      		
    	}
        
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            String property = e.getPropertyName();
//            if (log.isDebugEnabled()) log.debug("propertyChange \""+property+
//                                                "\" old= "+e.getOldValue()+" new= "+e.getNewValue()+
//                                                " source= "+e.getSource().getClass().getName());
            if (property.equals("DnDrop")) {
            	doAction(e.getSource());
            }
        }
 
        /**
         * Callback from RouteFinder.findRoute()
         */
        public void selectedRoute(ArrayList<BlockOrder> orders) {
        	String msg =null;
        	String s = (""+Math.random()).substring(2);
        	Warrant warrant = new Warrant("IW"+s, "NX"+s);
            if (_dccNumBox.getText()==null || _dccNumBox.getText().length()==0){
                msg = Bundle.getMessage("NoLoco");
            } else {
                String addr = _dccNumBox.getText();
                if (addr!= null && addr.length() != 0) {
                	addr = addr.toUpperCase().trim();
                    try {
                        boolean isLong = false;
                        int dccNum = 0;
                    	if (addr.endsWith("(L)")) {
                    		isLong = true;
                        	dccNum = Integer.parseInt(addr.substring(0, addr.length()-3));
                    	} else if (!addr.endsWith("(S)")) {
                    		// no suffix specified
                    		dccNum = Integer.parseInt(addr);
                    		Character ch = addr.charAt(0);
                            isLong = (ch=='0' || addr.length()>3);  // leading zero means long
                    	} else {
                    		// short specified
                        	dccNum = Integer.parseInt(addr.substring(0, addr.length()-3));
                    	}
                    	warrant.setDccAddress( new DccLocoAddress(dccNum, isLong));
                    	warrant.setTrainName(addr);
                    } catch (NumberFormatException nfe) {
                        msg = Bundle.getMessage("BadDccAddress", addr);
                    }
                }
            }
            warrant.addBlockOrders(getOrders());
            if (msg==null) {
            	msg = makeCommands(warrant);           	
            }
            if (msg==null) {
            	msg = runTrain(warrant);           	
            }
            if (msg!=null) {
                JOptionPane.showMessageDialog(this, msg,
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                warrant = null;
            } else {
            	_parent.getModel().addNXWarrant(warrant);
            	_parent.getModel().fireTableDataChanged();
            	dispose();
            	_nxFrame = null;           	
            }
        }
        
        private String makeCommands(Warrant w) {
        	List<BlockOrder> orders = getOrders();
        	String blockName = orders.get(0).getBlock().getDisplayName();
        	w.addThrottleCommand(new ThrottleSetting(0, "F0", "true", blockName));
        	w.addThrottleCommand(new ThrottleSetting(0, "F2", "true", blockName));
        	w.addThrottleCommand(new ThrottleSetting(1750, "F2", "false", blockName));
        	w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
        	w.addThrottleCommand(new ThrottleSetting(1750, "F2", "false", blockName));
        	w.addThrottleCommand(new ThrottleSetting(0, "Forward", (_forward.isSelected()?"true":"false"), blockName));        		
            String speed = _speedBox.getText();
            try {
            	float f = Float.parseFloat(speed);
            	if (f>1.0 || f<0) {
                    return Bundle.getMessage("badSpeed");            	            		
            	}
            } catch (NumberFormatException nfe) {
                return Bundle.getMessage("badSpeed");            	
            }
        	w.addThrottleCommand(new ThrottleSetting(500, "Speed", speed, blockName));        		
        	for (int i=1; i<orders.size(); i++) {
        		w.addThrottleCommand(new ThrottleSetting(5000, "NoOp", "Enter Block", 
        				orders.get(i).getBlock().getDisplayName()));        		
        	}
        	blockName = orders.get(orders.size()-1).getBlock().getDisplayName();
        	w.addThrottleCommand(new ThrottleSetting(0, "Speed", "0.0", blockName));        		
        	w.addThrottleCommand(new ThrottleSetting(500, "F2", "true", blockName));
        	w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));
        	w.addThrottleCommand(new ThrottleSetting(500, "F0", "false", blockName));
        	return null;
        }
	 
        void mouseClickedOnBlock(OBlock block) {
        	_clickCount++;
        	switch (_clickCount) {
        		case 1:
            		_originBlockBox.setText(block.getDisplayName());
            		setOriginBlock();
            		break;
        		case 2:
            		_destBlockBox.setText(block.getDisplayName());
                    setDestinationBlock();
                    break;
        		case 3 :
        			_viaBlockBox.setText(block.getDisplayName());
                    setViaBlock();
                    break;
        		case 4:
            		_avoidBlockBox.setText(block.getDisplayName());
                    setAvoidBlock();
                    break;
    			default:
    				_clickCount= 0;       				
        	}
        }
    	boolean makeAndRunWarrant() {
            int depth = 10;
            try {
                depth = Integer.parseInt(_searchDepth.getText());
            } catch (NumberFormatException nfe) {
            }
            String msg = findRoute(depth);
            if (msg!=null) {
                JOptionPane.showMessageDialog(this, msg,
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return true;
    	}
    }
    
    String runTrain(Warrant w) {
    	String msg = null;
        msg = w.allocateRoute(null);
        if (msg!=null) {
        	setStatusText(msg, Color.red);
        }
        String msg2 = w.setRoute(0, null);
        if (msg==null && msg2!=null) {
        	setStatusText(msg2, Color.red);
        }
        if (msg==null) {
        	msg = w.checkRoute();
        	if (msg!=null) {
               	setStatusText(msg, Color.yellow);                        		
        	}
        }
        if (msg==null) {
        	msg = w.checkStartBlock();
        	if (msg!=null) {
               	setStatusText(msg, Color.yellow);                        		
        	}
        }
        msg = w.checkForContinuation();
        if (msg==null) {
            msg = w.setRunMode(Warrant.MODE_RUN, null, null, null, false);
            msg = null;		// will be the same message that user OK'd to ignore
        }
    	return msg;
    }
    
    /************************* Table ****************************************/

    static public class ComboBoxCellEditor extends DefaultCellEditor
    {
        TableSorter _sorter;
        
        ComboBoxCellEditor(JComboBox comboBox) {
            super(comboBox);
            comboBox.setFont(new Font(null, Font.PLAIN, 12));
        }
        ComboBoxCellEditor(JComboBox comboBox, TableSorter sorter) {
            super(comboBox);
            comboBox.setFont(new Font(null, Font.PLAIN, 12));
            _sorter = sorter;
        }
        public Component getTableCellEditorComponent(JTable table, Object value, 
                                         boolean isSelected, int row, int column) 
        {
        	jmri.util.com.sun.TableSorter m = ((jmri.util.com.sun.TableSorter)table.getModel());       	
            WarrantTableModel model = (WarrantTableModel)m.getTableModel();

            // If table has been sorted, table row no longer is the same as array index
            if (_sorter!=null) {
            	row = _sorter.modelIndex(row);            	
            }
            Warrant warrant = model.getWarrantAt(row);
            JComboBox comboBox = (JComboBox)getComponent();
            comboBox.removeAllItems();
            List <BlockOrder> orders = warrant.getOrders();
            for (int i=0; i<orders.size(); i++) {
            	BlockOrder order = orders.get(i);
                comboBox.addItem(order.getBlock().getDisplayName()+": - "+order.getPath().getName());
            }
            return comboBox; 
        }
    }
    
    void setStatusText(String msg, Color c) {
    	if (Color.red.equals(c)) {
        	_status.setBackground(Color.white);   		
    	} else {
        	_status.setBackground(Color.gray);    		
    	}
    	_status.setForeground(c);
    	_status.setText(msg);
    }
    
    /************************* WarrantTableModel Table ******************************/

    class WarrantTableModel extends AbstractTableModel implements PropertyChangeListener 
    {
        public static final int WARRANT_COLUMN = 0;
        public static final int ROUTE_COLUMN =1;
        public static final int TRAIN_NAME_COLUMN = 2;
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
        private ArrayList <Warrant>       _warList;
        private ArrayList <Warrant>       _warNX;	// temporary warrants appended to table

        public WarrantTableModel() {
            super();
            _manager = InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class);
            _manager.addPropertyChangeListener(this);   // for adds and deletes
            _warNX = new ArrayList<Warrant>();
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
        
        public void addNXWarrant(Warrant w) {
        	_warList.add(w);
        	_warNX.add(w);
        	w.addPropertyChangeListener(this);
        }
        public void removeNXWarrant(Warrant w) {
        	w.removePropertyChangeListener(this);
        	_warList.remove(w);       	
        	_warNX.remove(w);
        }
        
        public Warrant getWarrantAt(int index) {
        	if (index>=_warList.size()) {
        		return null;
        	}
    		return  _warList.get(index);
        }

        public int getRowCount () {
            return _warList.size();
        }

        public int getColumnCount () {
            return NUMCOLS;
        }

        public String getColumnName(int col) {
            switch (col) {
                case WARRANT_COLUMN: return Bundle.getMessage("Warrant");
                case ROUTE_COLUMN: return Bundle.getMessage("Route");
                case TRAIN_NAME_COLUMN: return Bundle.getMessage("TrainName");
                case ADDRESS_COLUMN: return Bundle.getMessage("DccAddress");
                case ALLOCATE_COLUMN: return Bundle.getMessage("Allocate");
                case DEALLOC_COLUMN: return Bundle.getMessage("Deallocate");
                case SET_COLUMN: return Bundle.getMessage("SetRoute");
                case AUTO_RUN_COLUMN: return Bundle.getMessage("ARun");
                case MANUAL_RUN_COLUMN: return Bundle.getMessage("MRun");
                case CONTROL_COLUMN: return Bundle.getMessage("Control");
            }
            return "";
        }


        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case WARRANT_COLUMN:
                    return false;
                case TRAIN_NAME_COLUMN:
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
                case TRAIN_NAME_COLUMN: return String.class;
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
                case TRAIN_NAME_COLUMN:
                    return new JTextField(13).getPreferredSize().width;
                case ROUTE_COLUMN:
                    return new JTextField(25).getPreferredSize().width;
                case ADDRESS_COLUMN:
                    return new JTextField(5).getPreferredSize().width;
                case ALLOCATE_COLUMN:
                case DEALLOC_COLUMN:
                case SET_COLUMN:
                case AUTO_RUN_COLUMN:
                case MANUAL_RUN_COLUMN:
                    return new JButton("XX").getPreferredSize().width;
                case CONTROL_COLUMN:
                    return new JTextField(40).getPreferredSize().width;
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
                        return Bundle.getMessage("Origin", bo.getBlock().getDisplayName());
                    }
                    break;
                case TRAIN_NAME_COLUMN:
                    return w.getTrainName();
                case ADDRESS_COLUMN:
                    if (w.getDccAddress()!=null) {
                        return w.getDccAddress().toString();
                    }
                    break;
                case ALLOCATE_COLUMN:
                    if (w.isTotalAllocated()) {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-green.gif", "occupied");
                    } else if(w.isAllocated()) {
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
                    if (w.hasRouteSet() && w.isTotalAllocated()) {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-green.gif", "off");
                    } else if(w.hasRouteSet() && w.isAllocated()) {
                        return new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-occupied.gif", "occupied");
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
                    return Bundle.getMessage("ButtonEdit");
                case DELETE_COLUMN:
                    return Bundle.getMessage("ButtonDelete");
            }
            return "";
        }

        public void setValueAt(Object value, int row, int col) {
            if (log.isDebugEnabled()) log.debug("setValueAt: row= "+row+", column= "+col+", value= "+value.getClass().getName());
            Warrant w = getWarrantAt(row);
            String msg = null;
        	_status.setBackground(_background);
        	_status.setText(null);
        	switch (col) {
                case WARRANT_COLUMN:
                case ROUTE_COLUMN:
                    return;
                case TRAIN_NAME_COLUMN:
                    w.setTrainName((String)value);
                    break;
                case ADDRESS_COLUMN:
                    String addr = (String)value;
                    if (addr!= null && addr.length() != 0) {
                    	addr = addr.toUpperCase().trim();
                        try {
                            boolean isLong = false;
                            int dccNum = 0;
                        	if (addr.endsWith("(L)")) {
                        		isLong = true;
                        	} else if (!addr.endsWith("(S)")) {
                        		// no suffix specified
                        		dccNum = Integer.parseInt(addr);
                        		Character ch = addr.charAt(0);
                                isLong = (ch=='0' || addr.length()>3);  // leading zero means long
                                w.setDccAddress( new DccLocoAddress(dccNum, isLong));
                                break;
                        	}
                        	// user has specified short or long
                        	dccNum = Integer.parseInt(addr.substring(0, addr.length()-3));
                            w.setDccAddress( new DccLocoAddress(dccNum, isLong));
                        } catch (NumberFormatException nfe) {
                            msg = Bundle.getMessage("BadDccAddress", addr);
                        }
                    }
                    break;
                case ALLOCATE_COLUMN:
                    msg = w.allocateRoute(null);
                    if (msg == null) {
                    	setStatusText(Bundle.getMessage("completeAllocate", w.getDisplayName()),Color.green);
                    } else {
                    	setStatusText(msg, Color.red);
                    	msg = null;
                    }
                    break;
                case DEALLOC_COLUMN:
                    if (w.getRunMode() == Warrant.MODE_NONE) {
                        w.deAllocate();
                    } else {
                    	setStatusText(Bundle.getMessage("TrainRunning", w.getDisplayName()), Color.yellow);
                    }
                    break;
                case SET_COLUMN:
                    msg = w.setRoute(0, null);
                    if (msg == null) {
                    	setStatusText(Bundle.getMessage("pathsSet", w.getDisplayName()), Color.green);
                    } else {
                    	setStatusText(msg, Color.yellow);
                    	msg = null;
                    }
                   break;
                case AUTO_RUN_COLUMN:
                    if (w.getRunMode() == Warrant.MODE_NONE) {
                        DccLocoAddress address = w.getDccAddress();
                        if (address == null) {
                            msg = Bundle.getMessage("NoAddress", w.getDisplayName());
                            break;
                        }
                        if (w.getThrottleCommands().size() == 0) {
                            msg = Bundle.getMessage("NoCommands", w.getDisplayName());
                            break;
                        }
                        if (w.getOrders().size() == 0) {
                            msg = Bundle.getMessage("EmptyRoute");
                            break;
                        }
                    	msg = runTrain(w);
                    } else {
                    	setStatusText(Bundle.getMessage("TrainRunning", w.getDisplayName()), Color.red);
                    }
                    break;
                case MANUAL_RUN_COLUMN:
                    if (w.getRunMode() == Warrant.MODE_NONE) {
                        if (w.getOrders().size() == 0) {
                            msg = Bundle.getMessage("EmptyRoute");
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
                                            Bundle.getMessage("OkToRun", msg), 
                                            Bundle.getMessage("WarningTitle"), 
                                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
                                    w.deAllocate();
                                    return;
                                }
                                msg = block.setPath(bo.getPathName(), w);
                            } else {
                                if (log.isDebugEnabled()) log.debug("block.allocate(w)= "+msg2);
                                msg = Bundle.getMessage("OriginBlockNotSet", msg2);
                                break;
                            } 
                        }
                        msg = w.setRunMode(Warrant.MODE_MANUAL, null, null, null, false);
                        if (msg!=null) {
                        	w.deAllocate();
                        }
                        if (log.isDebugEnabled()) log.debug("w.runManualTrain= "+msg);
                    } else {
                        msg = Bundle.getMessage("TrainRunning", w.getDisplayName());
                    }
                    break;
                case CONTROL_COLUMN:
                	// Message is set when propertyChangeEvent (below) is received from a warrant
                	// change.  fireTableRows then causes getValueAt() which calls getRunningMessage()
                    int mode = w.getRunMode();
                    if (mode==Warrant.MODE_LEARN) {Bundle.getMessage("Learning", 
                    				w.getCurrentBlockOrder().getBlock().getDisplayName());                 	
                    } else {
                        String setting = (String)value;
                        if (mode==Warrant.MODE_RUN || mode==Warrant.MODE_MANUAL) {
                        	int s = -1;
                            if (setting.equals(halt)) {
                                s = Warrant.HALT; 
                            } else if (setting.equals(resume)) {
                                s = Warrant.RESUME; 
                            } else if (setting.equals(retry)) {
                                s = Warrant.RETRY;
                            } else if (setting.equals(abort)) {
                                s = Warrant.ABORT;
                            }
                            w.controlRunTrain(s);
                        } else if (setting.equals(abort)) {
                        	w.deAllocate();                       	
                        } else if (mode==Warrant.MODE_NONE) {
                        	Bundle.getMessage("Idle");
                            msg = Bundle.getMessage("NotRunning", w.getDisplayName());
                        } else {
                        	getValueAt(row,col);
                        }
                    }
                    break;
                case EDIT_COLUMN:
                    WarrantTableAction.openWarrantFrame(w);
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
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                setStatusText(msg, Color.red);
            }
            fireTableRowsUpdated(row, row);
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
        	String property = e.getPropertyName();
            if (property.equals("length")) {
                // a NamedBean added or deleted
                init();
                fireTableDataChanged();
            } else if (e.getSource() instanceof NamedBean){
                // a value changed.  Find it, to avoid complete redraw
                NamedBean bean = (NamedBean)e.getSource();
                for (int i=0; i<_warList.size(); i++) {
                    if (bean.equals(_warList.get(i)))  {
                    	
                        if ( _warNX.contains(bean) && (
                        	(property.equals("runMode") && e.getNewValue()==Integer.valueOf(Warrant.MODE_NONE))
                        	|| (property.equals("controlChange") &&e.getNewValue()==Integer.valueOf(Warrant.ABORT))
                        	)) {
                        	removeNXWarrant((Warrant)bean);                       	
                            try {
                                Thread.sleep(50);
                                fireTableRowsDeleted(i, i);
                                Thread.sleep(50);                        	
                            } catch (InterruptedException ie) {                       	
                            }
                        } else {
                            fireTableRowsUpdated(i, i);
                        }
                        break;
                    }
                }
                if (e.getPropertyName().equals("blockChange")) {
                    setStatusText("", Color.red);               	
                }
            }
            if (log.isDebugEnabled()) log.debug("propertyChange of \""+e.getPropertyName()+
                                                "\" for "+e.getSource().getClass().getName());
        }

    }

    static Logger log = LoggerFactory.getLogger(WarrantTableAction.class.getName());
}
