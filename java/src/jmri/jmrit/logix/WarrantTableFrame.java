package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTable;

import jmri.InstanceManager;

import jmri.util.com.sun.TableSorter;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * The WarrantTableFrame lists the existing Warrants and has controls to set their routes,
 * train IDs launch them and control their running (halt, resume, abort. etc.
 * 
 *  The WarrantTableFrame also can initiate NX (eNtry/eXit) warrants
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

class WarrantTableFrame  extends jmri.util.JmriJFrame implements MouseListener 
{
	static final String halt = Bundle.getMessage("Halt");
	static final String resume = Bundle.getMessage("Resume");
	static final String abort = Bundle.getMessage("Abort");
	static final String retry = Bundle.getMessage("Retry");
	static final String[] controls = {halt, resume, retry, abort};

	// Session persistent defaults for NX warrants
	static boolean _defaultEStop = false;
	static boolean _defaultHaltStart = false;
	static String _defaultSearchdepth = "15";
	static String _defaultSpeed = "0.5";
	static String _defaultIntervalTime = "4.0";
    public static int _maxHistorySize = 30;

	private JTextField  _startWarrant = new JTextField(30);
	private JTextField  _endWarrant = new JTextField(30);
    private NXFrame _nxFrame;
    private JTextField  _status = new JTextField(90);
    private ArrayList<String> _statusHistory = new ArrayList<String>();
    private JScrollPane _tablePane;
    private int _rowHeight;
    
    private WarrantTableModel     _model;    
    private static WarrantTableFrame _instance;
    
    static WarrantTableFrame getInstance() {
    	if (_instance==null) {
    		_instance = new WarrantTableFrame();
    	}
    	return _instance;
    }

    WarrantTableModel getModel() {
    	return _model;
    }

    private WarrantTableFrame() 
    {
        setTitle(Bundle.getMessage("WarrantTable"));
        _model = new WarrantTableModel(this);
        _model.init();
        //Casts at getTableCellEditorComponent() now fails with 3.0 ??            
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
        _rowHeight = box.getPreferredSize().height;
        table.setRowHeight(_rowHeight);
        table.setDragEnabled(true);
        table.setTransferHandler(new jmri.util.DnDTableExportHandler());
        _tablePane = new JScrollPane(table);
        Dimension dim = table.getPreferredSize();
        dim.height = _rowHeight*12;
        _tablePane.getViewport().setPreferredSize(dim);

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(Bundle.getMessage("ShowWarrants"));
        tablePanel.add(title, BorderLayout.NORTH);
        tablePanel.add(_tablePane, BorderLayout.CENTER);
        
        
        JPanel p = new JPanel();
        p.add(new JLabel("status"));
        _status.addMouseListener(this);
		_status.setBackground(Color.white);
//    	_background = _status.getBackground();
    	_status.setFont(jmri.util.FontUtil.deriveFont(_status.getFont(),java.awt.Font.BOLD));
        setStatusText(BLANK.substring(0,90), null, false);
        p.add(_status);
        _status.setEditable(false);
        JPanel ps = new JPanel();
        ps.setLayout(new BoxLayout(ps, BoxLayout.Y_AXIS));
        ps.add(Box.createVerticalStrut(WarrantTableAction.STRUT_SIZE));
        ps.add(p);
        tablePanel.add(ps, BorderLayout.CENTER);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(WarrantTableAction.STRUT_SIZE));
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
        panel.add(Box.createHorizontalStrut(WarrantTableAction.STRUT_SIZE));
        panel.add(concatButton);
        panel.add(Box.createHorizontalStrut(WarrantTableAction.STRUT_SIZE));

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
        pp.add(Box.createHorizontalStrut(WarrantTableAction.STRUT_SIZE));
        pp.add(panel);
        pp.add(Box.createHorizontalStrut(WarrantTableAction.STRUT_SIZE));
        pp.add(concatPanel);
        pp.add(Box.createHorizontalStrut(WarrantTableAction.STRUT_SIZE));
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
        setLocation(50,0);
        setVisible(true);
        pack();
    }
    protected void scrollTable() {
        JScrollBar bar = _tablePane.getVerticalScrollBar();
        bar.setValue(bar.getMaximum());
    }

    
    protected boolean mouseClickedOnBlock(OBlock block) {
    	if (_nxFrame!=null) {
    		_nxFrame.mouseClickedOnBlock(block);
    		return true;
    	}
    	return false;
    }
       
    private void nxAction() {
    	_nxFrame = NXFrame.getInstance();
    }
    
    protected void closeNXFrame() {
    	_nxFrame = null;
    }
    
    private void concatenate() {
        WarrantManager manager = InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class);
        Warrant startW = manager.getWarrant(_startWarrant.getText().trim());
        Warrant endW = manager.getWarrant(_endWarrant.getText().trim());
        if (startW==null || endW==null) {
        	showWarning("BadWarrantNames");
            return;
        }
        BlockOrder last = startW.getLastOrder();
        BlockOrder next = endW.getfirstOrder();
        if (last==null || next==null) {
        	showWarning("EmptyRoutes");
            return;
        }
        if (!last.getPathName().equals(next.getPathName())) {
        	showWarning("RoutesDontMatch");
            return;
        }
        WarrantTableAction.CreateWarrantFrame f = new WarrantTableAction.CreateWarrantFrame();
        try {
            f.initComponents();
            f.concatenate(startW, endW);
        } catch (Exception ex ) { log.error("error making CreateWarrantFrame", ex);}
        f.setVisible(true);
    }
    
    public void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, Bundle.getMessage(msg),
                Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);    	
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
            List <BlockOrder> orders = warrant.getBlockOrders();
            for (int i=0; i<orders.size(); i++) {
            	BlockOrder order = orders.get(i);
                comboBox.addItem(order.getBlock().getDisplayName()+": - "+order.getPath().getName());
            }
            return comboBox; 
        }
    }
    
    String runTrain(Warrant w) {
    	String msg =  w.setRoute(0, null);
        if (msg==null) {
            msg = w.checkForContinuation();
        }
        if (msg==null) {
            msg = w.setRunMode(Warrant.MODE_RUN, null, null, null, false);
        }
        if (msg!=null) {
        	setStatusText(msg, Color.red, false);
        	return msg;
        }
    	msg = w.checkRoute();	// notify about occupation ahead
    	if (msg!=null) {
           	setStatusText(msg, WarrantTableModel.myGold, false);                        		
    	}
    	msg = w.checkStartBlock();	// notify first block occupied by this train
    	if (msg!=null) {
           	setStatusText(msg, WarrantTableModel.myGold, false);                        		
    	}
    	return null;
    }

    public void mouseClicked(MouseEvent event) {
        javax.swing.JPopupMenu  popup = new javax.swing.JPopupMenu();
    	for (int i=_statusHistory.size()-1; i>=0; i--) {
    		popup.add(_statusHistory.get(i));
    	}
    	popup.show(_status, 0, 0);
    }

    public void mousePressed(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    
    void setStatusText(String msg, Color c, boolean save) {
/*    	if (WarrantTableModel.myGold.equals(c)) {
    		_status.setBackground(Color.lightGray);
    	} else if (Color.red.equals(c)) {
        	_status.setBackground(Color.white);   		
    	} else {
        	_status.setBackground(Color.white);    		
    	}*/
    	_status.setForeground(c);
    	_status.setText(msg);
    	if (save && msg!=null && msg.length()>0) {
    		_statusHistory.add(msg);
    		while (_statusHistory.size()>_maxHistorySize) {
    			_statusHistory.remove(0);
    		}
    	}
    	
    }
    static String BLANK = "                                                                                                 ";
    
    
    static Logger log = LoggerFactory.getLogger(WarrantTableFrame.class.getName());
}
