
package jmri.jmrit.beantable;


import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;

import java.text.DecimalFormat;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
//import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.TransferHandler;

import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.StringSelection;

import java.awt.dnd.*;
import java.io.IOException;

import jmri.BeanSetting;
import jmri.Block;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Path;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;

import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.util.com.sun.TransferActionListener;
import jmri.jmrit.display.PickListModel;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantTableAction;

/**
 * GUI to define OBlocks, OPaths and Portals 
 *
 *<P>
 * 
 *
 *<P>
 *
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
 * @author	Pete Cressman (C) 2009
 */

public class OBlockTableAction extends AbstractAction {

    private ArrayList <Portal> _portalList = new ArrayList <Portal>();

    static int ROW_HEIGHT;
    static int STRUT_SIZE = 10;
	static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.LogixTableBundle");

    static String noneText = AbstractTableAction.rb.getString("BlockNone");
    static String gradualText = AbstractTableAction.rb.getString("BlockGradual");
    static String tightText = AbstractTableAction.rb.getString("BlockTight");
    static String severeText = AbstractTableAction.rb.getString("BlockSevere");
    static String[] curveOptions = {noneText, gradualText, tightText, severeText};

    static final String unknown = AbstractTableAction.rbean.getString("BeanStateUnknown");
    static final String inconsistent = AbstractTableAction.rbean.getString("BeanStateInconsistent");
    static final String closed = InstanceManager.turnoutManagerInstance().getClosedText();
    static final String thrown = InstanceManager.turnoutManagerInstance().getThrownText();
    static final String[] turnoutStates = {closed, thrown, unknown, inconsistent};
    
    OBlockTableModel  _oBlockModel;
    JScrollPane _blockTablePane;
    PortalTableModel _portalModel;
    JScrollPane _portalTablePane;
    BlockPortalTableModel _blockPortalXRefModel;

    static final int BLOCK_TABLE = 1;
    static final int PORTAL_TABLE = 2;
    static final int BLOCK_PATH_TABLE = 3;
    static final int XREF_TABLE = 4;
    static final int TURNOUT_TABLE = 5;

    public OBlockTableAction() {
        this("OBlock Table");
    }
    public OBlockTableAction(String actionName) {
	    super(actionName);
    }

    public void actionPerformed(ActionEvent e) {
        OBlockTableFrame f = new OBlockTableFrame();
        f.initComponents();
    }

    class OBlockTableFrame extends jmri.util.JmriJFrame implements InternalFrameListener {

        JDesktopPane _desktop;
        JInternalFrame _blockTableFrame;
        JCheckBox _inchBox = new JCheckBox(AbstractTableAction.rb.getString("LengthInches"));
        JCheckBox _centimeterBox = new JCheckBox(AbstractTableAction.rb.getString("LengthCentimeters"));

        JInternalFrame _portalTableFrame;
        JInternalFrame _blockPortalXRefFrame;

        JMenu _openMenu;
        HashMap <String, JInternalFrame> _blockPathMap = new HashMap <String, JInternalFrame>();
        HashMap <String, JInternalFrame> _PathTurnoutMap = new HashMap <String, JInternalFrame>();

        public OBlockTableFrame() {
            this("OBlock Table");
        }
        public OBlockTableFrame(String actionName) {
            super(actionName);
        }

        public void initComponents() {
            setTitle(rbx.getString("TitleOBlocks"));
            JMenuBar menuBar = new JMenuBar();
            ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");
            JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
            fileMenu.add(new jmri.configurexml.SaveMenu());
            menuBar.add(fileMenu);

            JMenu editMenu = new JMenu(rb.getString("MenuEdit"));
            editMenu.setMnemonic(KeyEvent.VK_E);
            TransferActionListener actionListener = new TransferActionListener();

            JMenuItem menuItem = new JMenuItem(rb.getString("MenuItemCut"));
            menuItem.setActionCommand((String)TransferHandler.getCutAction().getValue(Action.NAME));
            menuItem.addActionListener(actionListener);
            menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
            menuItem.setMnemonic(KeyEvent.VK_T);
            editMenu.add(menuItem);
        
            menuItem = new JMenuItem(rb.getString("MenuItemCopy"));
            menuItem.setActionCommand((String)TransferHandler.getCopyAction().getValue(Action.NAME));
            menuItem.addActionListener(actionListener);
            menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
            menuItem.setMnemonic(KeyEvent.VK_C);
            editMenu.add(menuItem);
        
            menuItem = new JMenuItem(rb.getString("MenuItemPaste"));
            menuItem.setActionCommand((String)TransferHandler.getPasteAction().getValue(Action.NAME));
            menuItem.addActionListener(actionListener);
            menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
            menuItem.setMnemonic(KeyEvent.VK_P);
            editMenu.add(menuItem);
            menuBar.add(editMenu);

            _openMenu = new JMenu(rbx.getString("OpenMenu"));
            updateOpenMenu();
            menuBar.add(_openMenu);

            setJMenuBar(menuBar);
            addHelpMenu("package.jmri.jmrit.logix.OBlockTable", true);

            _desktop = new JDesktopPane();
            _desktop.putClientProperty("JDesktopPane.dragMode", "outline");
            _desktop.setPreferredSize(new Dimension(1100, 550));
            setContentPane(_desktop);
            _blockTableFrame = makeBlockFrame();
            _blockTableFrame.setVisible(true);
            _desktop.add(_blockTableFrame);

            _portalTableFrame = makePortalFrame();
            _portalTableFrame.setVisible(true);
            _desktop.add(_portalTableFrame);

            _blockPortalXRefFrame = makeBlockPortalFrame();
            _blockPortalXRefFrame.setVisible(false);
            _desktop.add(_blockPortalXRefFrame);

            setLocation(10,30);
            setVisible(true);
            pack();
            errorCheck();
        }

        /**
        * Add the cut/copy/paste actions to the action map.
        */
        private void setActionMappings(JTable table) {
            ActionMap map = table.getActionMap();
            map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
            map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
            map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
        }

        public void windowClosing(java.awt.event.WindowEvent e) {
            errorCheck();
            setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
            if (log.isDebugEnabled()) log.debug("windowClosing: "+toString());
        }

        private void errorCheck() {
            WarrantTableAction.initPathPortalCheck();
            OBlockManager manager = InstanceManager.oBlockManagerInstance();
            String[] sysNames = manager.getSystemNameArray();
            for (int i = 0; i < sysNames.length; i++) {
                WarrantTableAction.checkPathPortals(manager.getBySystemName(sysNames[i]));
            }
            WarrantTableAction.showPathPortalErrors();
        }

        void updateOpenMenu() {
            _openMenu.removeAll();
            JMenuItem openBlock = new JMenuItem(rbx.getString("OpenBlockMenu"));
            _openMenu.add(openBlock);
            openBlock.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        _blockTableFrame.setVisible(true);
                        try {
                            _blockTableFrame.setIcon(false);
                        } catch (PropertyVetoException pve) {
                            log.warn("Block Table Frame vetoed setIcon "+pve.toString());
                        }
                        _blockTableFrame.moveToFront();
                    }
                });
            JMenuItem openPortal = new JMenuItem(rbx.getString("OpenPortalMenu"));
            _openMenu.add(openPortal);
            openPortal.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        _portalTableFrame.setVisible(true);
                        try {
                            _portalTableFrame.setIcon(false);
                        } catch (PropertyVetoException pve) {
                            log.warn("Portal Table Frame vetoed setIcon "+pve.toString());
                        }
                        _portalTableFrame.moveToFront();
                    }
                });
            JMenuItem openXRef = new JMenuItem(rbx.getString("OpenXRefMenu"));
            _openMenu.add(openXRef);
            openXRef.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        _blockPortalXRefFrame.setVisible(true);
                        try {
                            _blockPortalXRefFrame.setIcon(false);
                        } catch (PropertyVetoException pve) {
                            log.warn("XRef Table Frame vetoed setIcon "+pve.toString());
                        }
                        _blockPortalXRefFrame.moveToFront();
                    }
                });

            JMenu openBlockPath = new JMenu(rbx.getString("OpenBlockPathMenu")); 
            ActionListener openFrameAction = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    String sysName = e.getActionCommand();
                    openBlockPathFrame(sysName);
                }
            };
            OBlockManager manager = InstanceManager.oBlockManagerInstance();
            String[] sysNames = manager.getSystemNameArray();
            for (int i = 0; i < sysNames.length; i++) {
                OBlock block = manager.getBySystemName(sysNames[i]);
                JMenuItem mi = new JMenuItem(java.text.MessageFormat.format(
                        rbx.getString("OpenPathMenu"), block.getDisplayName()));
                mi.setActionCommand(sysNames[i]);
                mi.addActionListener(openFrameAction);
                openBlockPath.add(mi);                                                  
            }
            _openMenu.add(openBlockPath);

            JMenu openTurnoutPath = new JMenu(rbx.getString("OpenBlockPathTurnoutMenu")); 
            sysNames = manager.getSystemNameArray();
            for (int i = 0; i < sysNames.length; i++) {
                OBlock block = manager.getBySystemName(sysNames[i]);
                JMenu openTurnoutMenu = new JMenu(java.text.MessageFormat.format(
                        rbx.getString("OpenTurnoutMenu"), block.getDisplayName()));
                openTurnoutPath.add(openTurnoutMenu);
                openFrameAction = new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        String pathTurnoutName = e.getActionCommand();
                        openPathTurnoutFrame(pathTurnoutName);
                    }
                };
                Iterator <Path> iter = block.getPaths().iterator();
                while (iter.hasNext()) {
                    OPath path = (OPath)iter.next();
                    JMenuItem mi = new JMenuItem(java.text.MessageFormat.format(
                                                 rbx.getString("OpenPathTurnoutMenu"), path.getName()));
                    mi.setActionCommand(makePathTurnoutName(sysNames[i], path.getName()));
                    mi.addActionListener(openFrameAction);
                    openTurnoutMenu.add(mi);                                                  
                }
            }
            _openMenu.add(openTurnoutPath);
        }

        /***********************  BlockFrame ******************************/
        JInternalFrame makeBlockFrame() {
            JInternalFrame frame = new JInternalFrame(rbx.getString("TitleBlockTable"), true, false, false, true);
            _oBlockModel = new OBlockTableModel(this);
            _oBlockModel.init();
            JTable blockTable = new DnDJTable(_oBlockModel);
            blockTable.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
            blockTable.getColumnModel().getColumn(OBlockTableModel.EDIT_COL).setCellEditor(new ButtonEditor(new JButton()));
            blockTable.getColumnModel().getColumn(OBlockTableModel.EDIT_COL).setCellRenderer(new ButtonRenderer());
            blockTable.getColumnModel().getColumn(OBlockTableModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
            blockTable.getColumnModel().getColumn(OBlockTableModel.DELETE_COL).setCellRenderer(new ButtonRenderer());
            JComboBox box = new JComboBox(curveOptions);
            blockTable.getColumnModel().getColumn(OBlockTableModel.CURVECOL).setCellEditor(new DefaultCellEditor(box));
            //blockTable.getColumnModel().getColumn(OBockTableModel.CURVECOL).setCellRenderer(new DefaultCellEditor(box));
            blockTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i=0; i<_oBlockModel.getColumnCount(); i++) {
                int width = _oBlockModel.getPreferredWidth(i);
                blockTable.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
            blockTable.sizeColumnsToFit(-1);
            blockTable.setDragEnabled(true);
            //blockTable.setDropMode(DropMode.USE_SELECTION);
            blockTable.setTransferHandler(new DnDHandler(BLOCK_TABLE));
            setActionMappings(blockTable);
            ROW_HEIGHT = blockTable.getRowHeight();
            int tableWidth = blockTable.getPreferredSize().width;
			blockTable.setPreferredScrollableViewportSize( new java.awt.Dimension(tableWidth, ROW_HEIGHT*10));
//			blockTable.setPreferredScrollableViewportSize( new java.awt.Dimension(875, ROW_HEIGHT*10));
            _blockTablePane = new JScrollPane(blockTable);

            JPanel contentPane = new JPanel();
            contentPane.setLayout(new BorderLayout(5,5));
            JLabel prompt = new JLabel(rbx.getString("AddBlockPrompt"));
            contentPane.add(prompt, BorderLayout.NORTH);
            contentPane.add(_blockTablePane, BorderLayout.CENTER);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(Box.createVerticalStrut(STRUT_SIZE));
            panel.add(_inchBox);
            _inchBox.setToolTipText(AbstractTableAction.rb.getString("InchBoxToolTip"));
            _inchBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        inchBoxChanged();
                    }
                });
            panel.add(Box.createVerticalStrut(STRUT_SIZE));
            _centimeterBox.setSelected(true);
            panel.add(_centimeterBox);
            _centimeterBox.setToolTipText(AbstractTableAction.rb.getString("CentimeterBoxToolTip"));
            _centimeterBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        centimeterBoxChanged();
                    }
                });
            panel.add(Box.createVerticalStrut(STRUT_SIZE));
            contentPane.add(panel, BorderLayout.SOUTH);
            frame.setContentPane(contentPane);
            frame.pack();
            return frame;
        }
        
        void inchBoxChanged() {
            _centimeterBox.setSelected(!_inchBox.isSelected());
            _oBlockModel.setInches(_inchBox.isSelected());
        }
        void centimeterBoxChanged() {
            _inchBox.setSelected(!_centimeterBox.isSelected());
            _oBlockModel.setInches(_inchBox.isSelected());
        }

        /***********************  PortalFrame ******************************/
        JInternalFrame makePortalFrame() {
            JInternalFrame frame = new JInternalFrame(rbx.getString("TitlePortalTable"), true, false, false, true);
            _portalModel = new PortalTableModel();
            _portalModel.init();
            JTable portalTable = new DnDJTable(_portalModel);
            portalTable.getColumnModel().getColumn(_portalModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
            portalTable.getColumnModel().getColumn(_portalModel.DELETE_COL).setCellRenderer(new ButtonRenderer());
            portalTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i=0; i<_portalModel.getColumnCount(); i++) {
                int width = _portalModel.getPreferredWidth(i);
                portalTable.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
            portalTable.sizeColumnsToFit(-1);
            portalTable.setDragEnabled(true);
            //portalTable.setDropMode(DropMode.USE_SELECTION);
            portalTable.setTransferHandler(new DnDHandler(PORTAL_TABLE));
            setActionMappings(portalTable);
            int tableWidth = portalTable.getPreferredSize().width;
			portalTable.setPreferredScrollableViewportSize( new java.awt.Dimension(tableWidth, ROW_HEIGHT*10));
//			portalTable.setPreferredScrollableViewportSize( new java.awt.Dimension(713, ROW_HEIGHT*10));
            _portalTablePane = new JScrollPane(portalTable);

            JPanel contentPane = new JPanel();
            contentPane.setLayout(new BorderLayout(5,5));
            JLabel prompt = new JLabel(rbx.getString("AddPortalPrompt"));
            contentPane.add(prompt, BorderLayout.NORTH);
            contentPane.add(_portalTablePane, BorderLayout.CENTER);

            frame.setContentPane(contentPane);
            frame.setLocation(150,250);
            frame.pack();
            return frame;
        }

        /***********************  BlockPortalFrame ******************************/
        JInternalFrame makeBlockPortalFrame() {
            JInternalFrame frame = new JInternalFrame(rbx.getString("TitleBlockPortalXRef"), true, false, false, true);
            _blockPortalXRefModel = new BlockPortalTableModel();
            JTable blockPortalTable = new DnDJTable(_blockPortalXRefModel);
            blockPortalTable.setDefaultRenderer(String.class, new jmri.jmrit.symbolicprog.ValueRenderer());
            blockPortalTable.setDefaultEditor(String.class, new jmri.jmrit.symbolicprog.ValueEditor());
            blockPortalTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i=0; i<_blockPortalXRefModel.getColumnCount(); i++) {
                int width = _blockPortalXRefModel.getPreferredWidth(i);
                blockPortalTable.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
            blockPortalTable.sizeColumnsToFit(-1);
            blockPortalTable.setDragEnabled(true);
            blockPortalTable.setTransferHandler(new DnDHandler(XREF_TABLE));
            setActionMappings(blockPortalTable);
            int tableWidth = blockPortalTable.getPreferredSize().width;
			blockPortalTable.setPreferredScrollableViewportSize( new java.awt.Dimension(tableWidth, ROW_HEIGHT*25));
//			blockPortalTable.setPreferredScrollableViewportSize( new java.awt.Dimension(275, ROW_HEIGHT*25));
            JScrollPane tablePane = new JScrollPane(blockPortalTable);

            JPanel contentPane = new JPanel();
            contentPane.setLayout(new BorderLayout(5,5));
            contentPane.add(tablePane, BorderLayout.CENTER);

            frame.addInternalFrameListener(this);
            frame.setContentPane(contentPane);
            frame.setLocation(700,30);
            frame.pack();
            return frame;
        }

        /***********************  BlockPathFrame ******************************/
        class BlockPathFrame extends JInternalFrame {
            BlockPathTableModel blockPathModel;

            public BlockPathFrame(String title, boolean resizable, boolean closable, 
                           boolean maximizable, boolean iconifiable) {
                super(title, resizable, closable, maximizable, iconifiable);
            }

            public void init (OBlock block, OBlockTableFrame parent) {
                blockPathModel = new BlockPathTableModel(block, parent);
            }

            public BlockPathTableModel getModel() {
                return blockPathModel;
            }
        }        
        /***********************  BlockPathFrame ******************************/
        BlockPathFrame makeBlockPathFrame(OBlock block) {
            String title = java.text.MessageFormat.format(
                            rbx.getString("TitleBlockPathTable"), block.getDisplayName());
            BlockPathFrame frame = new BlockPathFrame(title, true, true, false, true);
            if (log.isDebugEnabled()) log.debug("makeBlockPathFrame for Block "+block.getDisplayName());
            frame.setName(block.getSystemName());
            frame.init (block, this);
            BlockPathTableModel blockPathModel = frame.getModel();
            blockPathModel.init();
            JTable blockPathTable = new DnDJTable(blockPathModel);
            //blockPathTable.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
            blockPathTable.getColumnModel().getColumn(blockPathModel.EDIT_COL).setCellEditor(new ButtonEditor(new JButton()));
            blockPathTable.getColumnModel().getColumn(blockPathModel.EDIT_COL).setCellRenderer(new ButtonRenderer());
            blockPathTable.getColumnModel().getColumn(blockPathModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
            blockPathTable.getColumnModel().getColumn(blockPathModel.DELETE_COL).setCellRenderer(new ButtonRenderer());
            blockPathTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            blockPathTable.setDragEnabled(true);
            //blockPathTable.setDropMode(DropMode.USE_SELECTION);
            blockPathTable.setTransferHandler(new DnDHandler(BLOCK_PATH_TABLE));
            setActionMappings(blockPathTable);
            for (int i=0; i<blockPathModel.getColumnCount(); i++) {
                int width = blockPathModel.getPreferredWidth(i);
                blockPathTable.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
            blockPathTable.sizeColumnsToFit(-1);
            int tableWidth = blockPathTable.getPreferredSize().width;
			blockPathTable.setPreferredScrollableViewportSize( new java.awt.Dimension(tableWidth, ROW_HEIGHT*10));
//			blockPathTable.setPreferredScrollableViewportSize( new java.awt.Dimension(766, ROW_HEIGHT*10));
            JScrollPane tablePane = new JScrollPane(blockPathTable);

            JPanel contentPane = new JPanel();
            contentPane.setLayout(new BorderLayout(5,5));
            JLabel prompt = new JLabel(rbx.getString("AddPathPrompt"));
            contentPane.add(prompt, BorderLayout.NORTH);
            contentPane.add(tablePane, BorderLayout.CENTER);

            frame.addInternalFrameListener(this);
            frame.setContentPane(contentPane);
            frame.setLocation(50,30);
            frame.pack();
            return frame;
        }
        
            /***********************  PathTurnoutFrame ******************************/
        JInternalFrame makePathTurnoutFrame(OBlock block, String pathName) {
            String title = java.text.MessageFormat.format(
                            rbx.getString("TitlePathTurnoutTable"), block.getDisplayName(), pathName);
            JInternalFrame frame = new JInternalFrame(title, true, true, false, true);
            if (log.isDebugEnabled()) log.debug("makePathTurnoutFrame for Block "+block.getDisplayName()+" and Path "+pathName);
            frame.setName(makePathTurnoutName(block.getSystemName(), pathName));
            OPath path = block.getPathByName(pathName);
            if (path==null) { return null; }
            PathTurnoutTableModel PathTurnoutModel = new PathTurnoutTableModel(path);
            PathTurnoutModel.init();
            JTable PathTurnoutTable = new DnDJTable(PathTurnoutModel);
            JComboBox box = new JComboBox(turnoutStates);
            //PathTurnoutTable.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
            PathTurnoutTable.getColumnModel().getColumn(PathTurnoutModel.SETTINGCOLUMN).setCellEditor(new DefaultCellEditor(box));
            PathTurnoutTable.getColumnModel().getColumn(PathTurnoutModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
            PathTurnoutTable.getColumnModel().getColumn(PathTurnoutModel.DELETE_COL).setCellRenderer(new ButtonRenderer());
            PathTurnoutTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i=0; i<PathTurnoutModel.getColumnCount(); i++) {
                int width = PathTurnoutModel.getPreferredWidth(i);
                PathTurnoutTable.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
            PathTurnoutTable.sizeColumnsToFit(-1);
            PathTurnoutTable.setDragEnabled(true);
            //PathTurnoutTable.setDropMode(DropMode.USE_SELECTION);
            PathTurnoutTable.setTransferHandler(new DnDHandler(TURNOUT_TABLE));
            setActionMappings(PathTurnoutTable);
            int tableWidth = PathTurnoutTable.getPreferredSize().width;
            PathTurnoutTable.setPreferredScrollableViewportSize( new java.awt.Dimension(tableWidth, ROW_HEIGHT*5));
//            PathTurnoutTable.setPreferredScrollableViewportSize( new java.awt.Dimension(397, ROW_HEIGHT*5));
            JScrollPane tablePane = new JScrollPane(PathTurnoutTable);

            JPanel contentPane = new JPanel();
            contentPane.setLayout(new BorderLayout(5,5));
            JLabel prompt = new JLabel(rbx.getString("AddTurnoutPrompt"));
            contentPane.add(prompt, BorderLayout.NORTH);
            contentPane.add(tablePane, BorderLayout.CENTER);

            frame.addInternalFrameListener(this);
            frame.setContentPane(contentPane);
            frame.setLocation(10,270);
            frame.pack();
            return frame;
        }

        void openBlockPathFrame(String sysName) {
            JInternalFrame frame = _blockPathMap.get(sysName);
            if (frame==null) {
                OBlock block = InstanceManager.oBlockManagerInstance().getBySystemName(sysName);
                if (block==null) { return; }
                frame = makeBlockPathFrame(block);
                _blockPathMap.put(sysName, frame);
                frame.setVisible(true);
                _desktop.add(frame);
                frame.moveToFront();
            } else {
                frame.setVisible(true);
                try {
                    frame.setIcon(false);
                } catch (PropertyVetoException pve) {
                    log.warn("BlockPath Table Frame for \""+sysName+"\" vetoed setIcon "+pve.toString());
                }
                frame.moveToFront();
            }
        }

        String makePathTurnoutName(String blockSysName, String pathName) {
            return "%"+pathName+"&"+blockSysName;
        }

        void openPathTurnoutFrame(String pathTurnoutName) {        
            JInternalFrame frame = _PathTurnoutMap.get(pathTurnoutName);
            log.debug("openPathTurnoutFrame for "+pathTurnoutName);
            if (frame==null) {
                int index = pathTurnoutName.indexOf('&');
                String pathName = pathTurnoutName.substring(1, index);
                String sysName = pathTurnoutName.substring(index+1);
                OBlock block = InstanceManager.oBlockManagerInstance().getBySystemName(sysName);
                if (block==null) { return; }
                frame = makePathTurnoutFrame(block, pathName);
                if (frame==null) { return; }
                _PathTurnoutMap.put(pathTurnoutName, frame);
                frame.setVisible(true);
                _desktop.add(frame);
                frame.moveToFront();
            } else {
                frame.setVisible(true);
                try {
                    frame.setIcon(false);
                } catch (PropertyVetoException pve) {
                    log.warn("PathTurnout Table Frame for \""+pathTurnoutName+
                             "\" vetoed setIcon "+pve.toString());
                }
                frame.moveToFront();
            }
        }

        /*********************** InternalFrameListener implementatiom ******************/
        
        public void internalFrameClosing(InternalFrameEvent e) {
            //JInternalFrame frame = (JInternalFrame)e.getSource();
            //log.debug("Internal frame closing: "+frame.getTitle());
        }

        public void internalFrameClosed(InternalFrameEvent e) {
            JInternalFrame frame = (JInternalFrame)e.getSource();
            String name = frame.getName();
            if (log.isDebugEnabled()) log.debug("Internal frame closed: "+
                      frame.getTitle()+", name= "+name+" size ("+
                      frame.getSize().getWidth()+", "+frame.getSize().getHeight()+")");
            if (name!=null && name.startsWith("OB")){
                _blockPathMap.remove(name);
                WarrantTableAction.initPathPortalCheck();
                WarrantTableAction.checkPathPortals(((BlockPathFrame)frame).getModel().getBlock());
                ((BlockPathFrame)frame).getModel().removeListener();
                WarrantTableAction.showPathPortalErrors();
            } else {
                _PathTurnoutMap.remove(name);
            }
        }

        public void internalFrameOpened(InternalFrameEvent e) {
          /*  JInternalFrame frame = (JInternalFrame)e.getSource();
            if (log.isDebugEnabled()) log.debug("Internal frame Opened: "+
                      frame.getTitle()+", name= "+frame.getName()+" size ("+
                      frame.getSize().getWidth()+", "+frame.getSize().getHeight()+")"); */
        }

        public void internalFrameIconified(InternalFrameEvent e) {
            JInternalFrame frame = (JInternalFrame)e.getSource();
            String name = frame.getName();
            if (log.isDebugEnabled()) log.debug("Internal frame Iconified: "+
                      frame.getTitle()+", name= "+name+" size ("+
                      frame.getSize().getWidth()+", "+frame.getSize().getHeight()+")");
            if (name!=null && name.startsWith("OB")){
                WarrantTableAction.initPathPortalCheck();
                WarrantTableAction.checkPathPortals(((BlockPathFrame)frame).getModel().getBlock());
                WarrantTableAction.showPathPortalErrors();
            }
        }

        public void internalFrameDeiconified(InternalFrameEvent e) {
            //JInternalFrame frame = (JInternalFrame)e.getSource();
            //log.debug("Internal frame deiconified: "+frame.getTitle());
        }

        public void internalFrameActivated(InternalFrameEvent e) {
            //JInternalFrame frame = (JInternalFrame)e.getSource();
            //log.debug("Internal frame activated: "+frame.getTitle());
        }

        public void internalFrameDeactivated(InternalFrameEvent e) {
            //JInternalFrame frame = (JInternalFrame)e.getSource();
            //log.debug("Internal frame deactivated: "+frame.getTitle());
        }
    }

    /****************************** Table Models **********************************/
    /**
     * Duplicates the JTable model for BlockTableAction and adds a column
     * for the occupancy sensor.  Configured for use within an internal frame.
     */
    class OBlockTableModel extends PickListModel {

        static public final int SYSNAMECOL  = 0;
        static public final int USERNAMECOL = 1;
        static public final int COMMENTCOL = 2;
        static public final int SENSORCOL = 3;
        static public final int LENGTHCOL = 4;
        static public final int CURVECOL = 5;
        static public final int EDIT_COL = 6;
        static public final int DELETE_COL = 7;
        static public final int NUMCOLS = 8;

        DecimalFormat twoDigit = new DecimalFormat("0.00");

        boolean _inches;
        OBlockManager manager;
        private String[] tempRow= new String[NUMCOLS];
        OBlockTableFrame _parent;

        OBlockTableModel(OBlockTableFrame parent) {
            super();
            _parent = parent;
            manager = InstanceManager.oBlockManagerInstance();
            initTempRow();
        }

        void initTempRow() {
            for (int i=0; i<LENGTHCOL; i++) {
                tempRow[i] = null;
            }
            tempRow[LENGTHCOL] = twoDigit.format(0.0);
            tempRow[CURVECOL] = noneText;
        }

        public Manager getManager() {
            return manager;
        }
        public NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        // Method name not appropriate (initial use was for Icon Editors)
        public NamedBean addBean(String name) {
            return manager.getOBlock(name);
        }

        public int getColumnCount () {
            return NUMCOLS;
        }
        public int getRowCount () {
            return super.getRowCount() + 1;
        }

        public void setInches(boolean inches) {
            _inches = inches;
            fireTableDataChanged();  // update view
        }

        String _saveBlockName;
        public Object getValueAt(int row, int col) 
        {
            if (super.getRowCount() == row) {
                if (_saveBlockName!=null && _blockTablePane!=null) {
                    //String sysName = tempRow[SYSNAMECOL];
                    if (!_saveBlockName.startsWith("OB")) {
                        _saveBlockName = "OB"+_saveBlockName;
                    }
                    OBlock b = manager.provideOBlock(_saveBlockName.toUpperCase());
                    if (b!=null) {
                        int idx =  getIndexOf(b);
                        _blockTablePane.getVerticalScrollBar().setValue(idx*ROW_HEIGHT);
                        _saveBlockName = null;
                    }
                }
                return tempRow[col];
            }
            Block b = (Block)getBeanAt(row);
            if (b == null) {
                //log.debug("requested getValueAt(\""+row+"\"), Block doesn't exist");
                return "(no Block)";
            }
            switch (col) {
                case COMMENTCOL:
                    return b.getComment();
                case SENSORCOL:
                    Sensor s = b.getSensor();
                    if (s==null) {
                         return "";
                    }
                    String uName = s.getUserName();
                    String name;
                    if (uName != null) {
                        name = uName +" ("+s.getSystemName()+")";
                    } else {
                        name = s.getSystemName();
                    }
                    return name;
                case CURVECOL:
                    String c = "";
                    if (b.getCurvature()==Block.NONE) c = noneText;
                    else if (b.getCurvature()==Block.GRADUAL) c = gradualText;
                    else if (b.getCurvature()==Block.TIGHT) c = tightText;
                    else if (b.getCurvature()==Block.SEVERE) c = severeText;
                    return c;
                case LENGTHCOL:
                    double len = 0.0;
                    if (_inches)
                        len = b.getLengthIn();
                    else 
                        len = b.getLengthCm();
                    return (twoDigit.format(len));
                case EDIT_COL:
                    return rbx.getString("ButtonEditPath");
                case DELETE_COL:
                    return AbstractTableAction.rb.getString("ButtonDelete");
            }
            return super.getValueAt(row, col);
        }    		

        public void setValueAt(Object value, int row, int col) {
            if (log.isDebugEnabled()) log.debug("setValueAt: row= "+row+", col= "+col+", value= "+(String)value);
            if (super.getRowCount() == row) 
            {
                if (col==SYSNAMECOL) {
                    tempRow[SYSNAMECOL] = (String)value;
                    _saveBlockName = tempRow[SYSNAMECOL];
                    OBlock block = manager.createNewOBlock((String)value, tempRow[USERNAMECOL]);
                    if (block==null) {
                        block = manager.provideOBlock(tempRow[USERNAMECOL]);
                        String name = "blank";     // zero length string error
                        if (block!=null) {
                            name = block.getDisplayName();
                        }
                        JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                            rbx.getString("CreateDuplBlockErr"), name),
                            AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                    }
                    if (block!=null) {
                        if (tempRow[SENSORCOL] != null) {
                            Sensor sensor = null;
                            try {
                                sensor = InstanceManager.sensorManagerInstance().provideSensor(tempRow[SENSORCOL]);
                                if (sensor!=null) {
                                    block.setSensor(sensor);
                                }
                            } catch (Exception ex) {
                                log.error("No Sensor named \""+(String)value+"\" found. threw exception: "+ ex);
                            }
                            if (sensor==null) {
                                JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                                    rbx.getString("NoSuchSensorErr"), tempRow[SENSORCOL]),
                                    AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                            }
                        }
                        block.setComment(tempRow[COMMENTCOL]);
                        float len = Float.valueOf(tempRow[LENGTHCOL]).floatValue();
                        if (_inches) 
                            block.setLength(len*25.4f);
                        else
                            block.setLength(len*10.0f);
                        if (tempRow[CURVECOL].equals(noneText)) block.setCurvature(Block.NONE);
                        else if (tempRow[CURVECOL].equals(gradualText)) block.setCurvature(Block.GRADUAL);
                        else if (tempRow[CURVECOL].equals(tightText)) block.setCurvature(Block.TIGHT);
                        else if (tempRow[CURVECOL].equals(severeText)) block.setCurvature(Block.SEVERE);
                    }  
                    //fireTableRowsUpdated(row,row);
                    initTempRow();
                    fireTableDataChanged();
                } else {
                    tempRow[col] = (String)value;
                }
                return;
            }
            OBlock block = (OBlock)super.getBeanAt(row);
            switch (col) {
                case USERNAMECOL:
                    OBlock b = manager.provideOBlock((String)value);
                    if (b != null) {
                        JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                            rbx.getString("CreateDuplBlockErr"), block.getDisplayName()),
                            AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    block.setUserName((String)value);
                    fireTableRowsUpdated(row,row);
                    return;
                case COMMENTCOL:
                    block.setComment((String)value);
                    return;
                case SENSORCOL:
                    try {
                        if (((String)value).trim().length()==0) {
                            block.setSensor(null);
                            block.setState(OBlock.DARK);
                        } else {
                            Sensor s = InstanceManager.sensorManagerInstance().provideSensor((String)value);
                            if (s!=null) {
                                block.setSensor(s);
                                fireTableRowsUpdated(row,row);
                            }
                        }
                        return;
                    } catch (Exception ex) {
                        log.error("provideSensor("+(String)value+") threw exception: "+ ex);
                    }
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                            rbx.getString("NoSuchSensorErr"), (String)value),
                            AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                    return;
                case LENGTHCOL:
                    float len = Float.valueOf((String)value).floatValue();
                    if (_inches) 
                        block.setLength(len*25.4f);
                    else
                        block.setLength(len*10.0f);
                    fireTableRowsUpdated(row,row);
                    return;
                case CURVECOL:
                    String cName = (String)value;
                    if (cName.equals(noneText)) block.setCurvature(Block.NONE);
                    else if (cName.equals(gradualText)) block.setCurvature(Block.GRADUAL);
                    else if (cName.equals(tightText)) block.setCurvature(Block.TIGHT);
                    else if (cName.equals(severeText)) block.setCurvature(Block.SEVERE);
                    fireTableRowsUpdated(row,row);
                    return;
                case EDIT_COL:
                    _parent.openBlockPathFrame(block.getSystemName());
                    return;
                case DELETE_COL:
                    deleteBean(block);
                    block = null;
                    return;
            }
            super.setValueAt(value, row, col);					
        }

        public String getColumnName(int col) {
            switch (col) {
                case COMMENTCOL: return AbstractTableAction.rb.getString("Comment");
                case SENSORCOL: return AbstractTableAction.rbean.getString("BeanNameSensor");
                case CURVECOL: return AbstractTableAction.rb.getString("BlockCurveColName");
                case LENGTHCOL: return AbstractTableAction.rb.getString("BlockLengthColName");
                case EDIT_COL: return "";
                case DELETE_COL: return "";
            }
            return super.getColumnName(col);
        }

        boolean noWarnDelete = false;

        void deleteBean(OBlock bean) {
            int count = bean.getNumPropertyChangeListeners()-2; // one is this table, other is manager
            if (log.isDebugEnabled()) {
                log.debug("Delete with "+count+" remaining listenner");
                //java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(bean);
                PropertyChangeListener[] listener=((jmri.implementation.AbstractNamedBean)bean).getPropertyChangeListeners();
                for (int i=0; i<listener.length; i++) {
                    log.debug(i+") "+listener[i].getClass().getName());
                }
            }
            if (!noWarnDelete) {
                String msg;
                if (count>0) { // warn of listeners attached before delete
                    msg = java.text.MessageFormat.format(
                            AbstractTableAction.rb.getString("DeletePrompt")+"\n"
                            +AbstractTableAction.rb.getString("ReminderInUse"),
                            new Object[]{bean.getSystemName(),""+count});
                } else {
                    msg = java.text.MessageFormat.format(
                            AbstractTableAction.rb.getString("DeletePrompt"),
                            new Object[]{bean.getSystemName()});
                }

                // verify deletion
                int val = JOptionPane.showOptionDialog(null, 
                        msg, AbstractTableAction.rb.getString("WarningTitle"), 
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        new Object[]{AbstractTableAction.rb.getString("ButtonYes"),
                                     AbstractTableAction.rb.getString("ButtonYesPlus"),
                                     AbstractTableAction.rb.getString("ButtonNo")},
                        AbstractTableAction.rb.getString("ButtonNo"));
                if (val == 2) return;  // return without deleting
                if (val == 1) { // suppress future warnings
                    noWarnDelete = true;
                }
            }
            // finally OK, do the actual delete
            _portalModel.deleteBlock(bean);
            List <Path> list = bean.getPaths();
            for (int i=0; i<list.size(); i++) {
                bean.removePath(list.get(i));
            }
            getManager().deregister(bean);
            bean.dispose();
        }

        public Class<?> getColumnClass(int col) {
            if (col == CURVECOL) {
                return JComboBox.class;
            } else if (col==DELETE_COL || col==EDIT_COL) {
                return JButton.class;
            }
            return String.class;
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case SYSNAMECOL: return new JTextField(13).getPreferredSize().width;
                case USERNAMECOL: return new JTextField(13).getPreferredSize().width;
                case COMMENTCOL: return new JTextField(8).getPreferredSize().width;
                case SENSORCOL: return new JTextField(13).getPreferredSize().width;
                case CURVECOL: return new JTextField(8).getPreferredSize().width;
                case LENGTHCOL: return new JTextField(7).getPreferredSize().width;
                case EDIT_COL: return new JButton("DELETE").getPreferredSize().width;
                case DELETE_COL: return new JButton("DELETE").getPreferredSize().width;
            }
            return 5;
        }

        public boolean isCellEditable(int row, int col) {
            if (super.getRowCount() == row) return true;
            if (col==SYSNAMECOL) return false;
            else return true;
        }

        public void propertyChange(PropertyChangeEvent e) {
            super.propertyChange(e);
            String property = e.getPropertyName();
            _portalModel.propertyChange(e);
            _blockPortalXRefModel.propertyChange(e);

            if (property.equals("length") || property.equals("UserName")
                                || property.equals("portalCount")) {
                _parent.updateOpenMenu();
            }
        }
    }
    /************ End OBlockTableModel *********************************/

    /************************************** Start PortalTableModel ***************/
    class PortalTableModel extends AbstractTableModel 
    {
        public static final int FROM_SIGNAL_COL = 0;
        public static final int FROM_BLOCK_COLUMN = 1;
        public static final int NAME_COLUMN = 2;
        public static final int TO_BLOCK_COLUMN = 3;
        public static final int TO_SIGNAL_COL = 4;
        static public final int DELETE_COL = 5;
        public static final int NUMCOLS = 6;

        private String[] tempRow= new String[NUMCOLS];

        public PortalTableModel() {
            super();
        }

        public void init() {
            makeList();
            initTempRow();
        }

        void initTempRow() {
            for (int i=0; i<NUMCOLS; i++) {
                tempRow[i] = null;
            }
        }

        @SuppressWarnings("unchecked")
        private void makeList() {
             ArrayList <Portal> tempList = new ArrayList <Portal>();
             // save portals that do not have all their blocks yet
             String msg = null;
             for (int i=0; i<_portalList.size(); i++) {
                 Portal portal = _portalList.get(i);
                 if (portal.getToBlock()==null && portal.getFromBlock()==null) {
                     tempList.add(portal);
                 }
             }
            // find portals with Blocks assigned
            Iterator bIter = _oBlockModel.getBeanList().iterator();
            while (bIter.hasNext()) {
                OBlock block = (OBlock)bIter.next();
                List <Portal> list = block.getPortals();
                for (int i=0; i<list.size(); i++) {
                    Portal portal = list.get(i);
                    String pName = portal.getName();
                    if (portal.getToBlock()==null || portal.getFromBlock()==null) { 
                        // double load of config file will have the first creation of a Portal
                        // with no blocks by the second file (it's just how things are loaded with
                        // forward and backward references to each other.  These objects cannot
                        // be created with complete specifications on their instantiation.
                        msg = java.text.MessageFormat.format(rbx.getString("PortalNeedsBlock"), pName);
                        //msg = java.text.MessageFormat.format(
                        //               rbx.getString("SuppressWarning"), msg);
                    }
                    boolean skip = false;
                    for (int j=0; j<tempList.size(); j++) {
                        if (pName.equals(tempList.get(j).getName())) {
                            skip = true;
                            break;
                        }
                    }
                    if (skip)  { continue;  }
                    // not in list, for the sort, insert at correct position
                    boolean add = true;
                    for (int j=0; j<tempList.size(); j++) {
                        if (pName.compareTo(tempList.get(j).getName()) < 0) {
                            tempList.add(j, portal);
                            add = false;
                            break;
                        }
                    }
                    if (add) {
                        tempList.add(portal);
                    }
                }
            }
            _portalList = tempList;
            if (msg != null) {
                JOptionPane.showMessageDialog(null, msg,
                        AbstractTableAction.rb.getString("WarningTitle"),
                        JOptionPane.WARNING_MESSAGE);
            }
            if (log.isDebugEnabled()) log.debug("makeList exit: _portalList has "
                                                +_portalList.size()+" rows.");
        }

        public int getColumnCount () {
            return NUMCOLS;
        }

        public int getRowCount() {
            return _portalList.size() + 1;
        }

        public String getColumnName(int col) {
            switch (col) {
                case FROM_SIGNAL_COL: return rbx.getString("FromSignalName");
                case FROM_BLOCK_COLUMN: return rbx.getString("FromBlockName");
                case NAME_COLUMN: return rbx.getString("PortalName");
                case TO_BLOCK_COLUMN: return rbx.getString("ToBlockName");
                case TO_SIGNAL_COL: return rbx.getString("ToSignalName");
            }
            return "";
        }

        String _savePortalName;
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (_portalList.size() == rowIndex) {
                if (_savePortalName!=null && _portalTablePane!=null) {
                    int idx = getPortalIndex(_savePortalName);
                    if (idx > -1) {
                        if (log.isDebugEnabled()) log.debug("Portal Scroll of "+_savePortalName+", to "+idx*ROW_HEIGHT); 
                        _portalTablePane.getVerticalScrollBar().setValue(idx*ROW_HEIGHT);
                        _savePortalName = null;
                    }
                }
                return tempRow[columnIndex];
            }
            switch(columnIndex) {
                case FROM_SIGNAL_COL:
                    return _portalList.get(rowIndex).getFromSignalName();
                case FROM_BLOCK_COLUMN:
                    return _portalList.get(rowIndex).getFromBlockName();
                case NAME_COLUMN:
                    return _portalList.get(rowIndex).getName();
                case TO_BLOCK_COLUMN:
                    return _portalList.get(rowIndex).getToBlockName();
                case TO_SIGNAL_COL:
                    return _portalList.get(rowIndex).getToSignalName();
                case DELETE_COL:
                    return AbstractTableAction.rb.getString("ButtonDelete");
            }
            return "";
        }

        public void setValueAt(Object value, int row, int col) {
            if (_portalList.size() == row) {

                if (log.isDebugEnabled()) log.debug("setValueAt: col= "+col+", value= "+(String)value);
                if (col==NAME_COLUMN) {
                    String name = (String)value;
                    if (getPortalByName(name)==null) {
                        _savePortalName = name;
                        // Note: Portal ctor will add this Portal to each of its 'from' & 'to' Block.
                        SignalHead fromSignal = InstanceManager.signalHeadManagerInstance()
                                                    .getSignalHead(tempRow[FROM_SIGNAL_COL]);
                        OBlock fromBlock = InstanceManager.oBlockManagerInstance()
                                                    .provideOBlock(tempRow[FROM_BLOCK_COLUMN]);
                        OBlock toBlock = InstanceManager.oBlockManagerInstance()
                                                    .provideOBlock(tempRow[TO_BLOCK_COLUMN]);
                        SignalHead toSignal = InstanceManager.signalHeadManagerInstance()
                                                    .getSignalHead(tempRow[TO_SIGNAL_COL]);
                        if (fromBlock != null && 
                                fromBlock.equals(toBlock)) {
                            JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                                rbx.getString("SametoFromBlock"), value, fromBlock.getDisplayName()),
                                    AbstractTableAction.rb.getString("WarningTitle"),
                                    JOptionPane.WARNING_MESSAGE);
                            tempRow[FROM_SIGNAL_COL] = null;
                        } else if (name != null && name.length()>0) {
                            Portal portal = new Portal(fromSignal, fromBlock, name, toBlock, toSignal);
                            _portalList.add(portal);
                            makeList();
                            initTempRow();
                            fireTableDataChanged();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                            rbx.getString("DuplPortalName"), (String)value),
                                AbstractTableAction.rb.getString("WarningTitle"),
                                JOptionPane.WARNING_MESSAGE);
                        //tempRow[col] = name;
                    }
                }
                else { tempRow[col] = (String)value; }
                return;
            }

            Portal portal =_portalList.get(row);
            String msg = null;

            switch(col) {
                case FROM_SIGNAL_COL:
                    SignalHead signal = InstanceManager.signalHeadManagerInstance().getSignalHead((String)value);
                    if (signal==null) {
                        msg = java.text.MessageFormat.format(
                            rbx.getString("NoSuchSignal"), (String)value);
                        break;
                    }
                    portal.setFromSignal(signal);
                    fireTableRowsUpdated(row,row);
                    break;
                case FROM_BLOCK_COLUMN:
                    OBlock block = InstanceManager.oBlockManagerInstance().provideOBlock((String)value);
                    if (block==null) {
                        msg = java.text.MessageFormat.format(
                            rbx.getString("NoSuchBlock"), (String)value);
                        break;
                    }
                    if (block.equals(portal.getToBlock())){
                        msg = java.text.MessageFormat.format(
                                rbx.getString("SametoFromBlock"), value, block.getDisplayName());
                        break;
                    }
                    if ( !portal.setFromBlock(block, false)) {
                        int response = JOptionPane.showConfirmDialog(null, java.text.MessageFormat.format(
                            rbx.getString("BlockPathsConflict"), value, portal.getFromBlockName()),
                            AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.YES_NO_OPTION, 
                            JOptionPane.WARNING_MESSAGE);
                        if (response==JOptionPane.NO_OPTION) {
                            break;
                        }

                    }
                    portal.setFromBlock(block, true);
                    fireTableRowsUpdated(row,row);
                    break;
                case NAME_COLUMN:
                    if (getPortalByName((String)value)!=null) {
                        msg = java.text.MessageFormat.format(
                            rbx.getString("DuplPortalName"), (String)value);
                        break;
                    }
                    if ( listContains((String)value) ) {
                        msg = java.text.MessageFormat.format(
                            rbx.getString("PortalNameConflict"), (String)value);
                    } else {
                        portal.setName((String)value);
                        fireTableRowsUpdated(row,row);
                    }
                    break;
                case TO_BLOCK_COLUMN:
                    block = InstanceManager.oBlockManagerInstance().provideOBlock((String)value);
                    if (block==null) {
                        msg = java.text.MessageFormat.format(
                            rbx.getString("NoSuchBlock"), (String)value);
                        break;
                    }
                    if (block.equals(portal.getFromBlock())){
                        msg = java.text.MessageFormat.format(
                                rbx.getString("SametoFromBlock"), value, block.getDisplayName());
                        break;
                    }
                    if ( !portal.setToBlock(block, false)) {
                        int response = JOptionPane.showConfirmDialog(null, java.text.MessageFormat.format(
                            rbx.getString("BlockPathsConflict"), value, portal.getToBlockName()),
                            AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.YES_NO_OPTION, 
                            JOptionPane.WARNING_MESSAGE);
                        if (response==JOptionPane.NO_OPTION) {
                            break;
                        }

                    }
                    portal.setToBlock(block, true);
                    fireTableRowsUpdated(row,row);
                    break;
                case TO_SIGNAL_COL:
                    signal = InstanceManager.signalHeadManagerInstance().getSignalHead((String)value);
                    if (signal==null) {
                        msg = java.text.MessageFormat.format(
                            rbx.getString("NoSuchSignal"), (String)value);
                        break;
                    }
                    portal.setToSignal(signal);
                    fireTableRowsUpdated(row,row);
                    break;
                case DELETE_COL:
                    if (deletePortal(portal)) {
                        fireTableDataChanged();
                    }
            }
            if (msg != null) {
                JOptionPane.showMessageDialog(null, msg,
                        AbstractTableAction.rb.getString("WarningTitle"),
                        JOptionPane.WARNING_MESSAGE);
            }
        }

        private boolean listContains(String name) {
            for (int i=0; i<_portalList.size(); i++)  {
                if (_portalList.get(i).equals(name)) { return true; }
            }
            return false;
        }

        private boolean deletePortal(Portal portal) {
            if (JOptionPane.showConfirmDialog(null, 
                            java.text.MessageFormat.format(rbx.getString("DeletePortalConfirm"),
                            portal.getName()), AbstractTableAction.rb.getString("WarningTitle"),
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                        ==  JOptionPane.YES_OPTION) {
                //_portalList.remove(portal);
                String name = portal.getName();
                for (int i = 0; i < _portalList.size(); i++) {
                    if (name.equals(_portalList.get(i).getName())) {
                        _portalList.remove(i);
                        i--;
                    }
                }
                OBlockManager manager = InstanceManager.oBlockManagerInstance();
                String[] sysNames = manager.getSystemNameArray();
                for (int i = 0; i < sysNames.length; i++) {
                    manager.getBySystemName(sysNames[i]).removePortal(portal);
                }
                portal.dispose();
                return true;
            }
            return false;
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public Class<?> getColumnClass(int col) {
            if (col == DELETE_COL) {
                return JButton.class;
            }
            return String.class;
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case FROM_SIGNAL_COL:
                case TO_SIGNAL_COL: return new JTextField(11).getPreferredSize().width;
                case FROM_BLOCK_COLUMN:
                case TO_BLOCK_COLUMN: return new JTextField(13).getPreferredSize().width;
                case NAME_COLUMN: return new JTextField(14).getPreferredSize().width;
                case DELETE_COL: return new JButton("DELETE").getPreferredSize().width;
            }
            return 5;
        }

        private Portal getPortalByName(String name) {
            for (int i=0; i<_portalList.size(); i++) {
                if (_portalList.get(i).getName().equals(name) ) {
                    return _portalList.get(i);
                }
            }
            return null;
        }

        private int getPortalIndex(String name) {
            for (int i=0; i<_portalList.size(); i++) {
                if (_portalList.get(i).getName().equals(name) ) {
                    return i;
                }
            }
            return -1;
        }

        private void deleteBlock(OBlock block) {
            if (log.isDebugEnabled()) log.debug("deleteBlock: "+
                                   (block!=null ? block.getDisplayName() : null)+" and its portals.");
            if (block==null) {
                return;
            }
            List <Portal> list = block.getPortals();
            for (int i=0; i<list.size(); i++) {
                Portal portal = list.get(i);
                OBlock opBlock = portal.getOpposingBlock(block);
                // remove portal and stub paths through portal in opposing block
                opBlock.removePortal(portal);
            }
            if (log.isDebugEnabled()) log.debug("deleteBlock: _portalList has "+
                                                _portalList.size()+" rows.");
            fireTableDataChanged();
        }
/*
        private void removePortalFromPaths(Portal portal, List <OPath> list) {
            String name = portal.getName();
            for (int j=0; j<list.size(); j++) {
                OPath path = list.get(j);
                if (name.equals(path.getFromPortalName())) {
                    path.setFromPortalName(null);
                    OBlock block = (OBlock)path.getBlock();
                    block.removePortal(portal);
                }
                if (name.equals(path.getToPortalName())) {
                    OBlock block = (OBlock)path.getBlock();
                    block.removePortal(portal);
                    path.setToPortalName(null);
                }
            }
        }
*/
        public void propertyChange(PropertyChangeEvent e) {
            String property = e.getPropertyName();
            if (property.equals("length") || property.equals("portalCount")
                                || property.equals("UserName")) {
                makeList();
                fireTableDataChanged();
            }
        }
    }
    /************ End PortalTableModel *************/

    /**************************** Start BlockPortalTableModel ***************/
    class BlockPortalTableModel extends AbstractTableModel implements PropertyChangeListener {
        public static final int BLOCK_NAME_COLUMN = 0;
        public static final int PORTAL_NAME_COLUMN = 1;
        public static final int NUMCOLS = 2;

        public BlockPortalTableModel() {
            super();
        }

        public int getColumnCount () {
            return NUMCOLS;
        }

        public int getRowCount() {
            int count = 0;
            List <NamedBean> list = _oBlockModel.getBeanList();
            for (int i=0; i<list.size(); i++) {
                count += ((OBlock)list.get(i)).getPortals().size();
            }
            return count;
        }

        public String getColumnName(int col) {
            switch (col) {
                case BLOCK_NAME_COLUMN: return rbx.getString("BlockName");
                case PORTAL_NAME_COLUMN: return rbx.getString("PortalName");
            }
            return "";
        }

        public Object getValueAt(int row, int col) {
            List <NamedBean> list = _oBlockModel.getBeanList();
            if (list.size() > 0) {
                int count = 0;
                int idx = 0;
                OBlock block = null;
                while (count <= row)  {
                    count += ((OBlock)list.get(idx++)).getPortals().size();
                }
                block = (OBlock)list.get(--idx);
                idx = row - (count - block.getPortals().size());
                if (col==BLOCK_NAME_COLUMN) {
                    if (idx==0) {
                        return block.getDisplayName();
                    }
                    return "";
                }
                return block.getPortals().get(idx).getName();
            }
            return null;
        }


        public void setValueAt(Object value, int row, int col) {
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        public int getPreferredWidth(int col) {
            return new JTextField(15).getPreferredSize().width;
        }

        public void propertyChange(PropertyChangeEvent e) {
            String property = e.getPropertyName();
            if (property.equals("length")|| property.equals("UserName")) {
                fireTableDataChanged();
            }
        }

    }
    /************ End BlockPortalTableModel *************/

    /********************************************** Start BlockPathTableModel ***************/
    class BlockPathTableModel extends AbstractTableModel implements PropertyChangeListener {
        public static final int FROM_PORTAL_COLUMN = 0;
        public static final int NAME_COLUMN = 1;
        public static final int TO_PORTAL_COLUMN = 2;
        static public final int EDIT_COL = 3;
        static public final int DELETE_COL = 4;
        public static final int NUMCOLS = 5;

        private String[] tempRow= new String[NUMCOLS];

        OBlockTableFrame _parent;
        private OBlock _block;

        public BlockPathTableModel() {
            super();
        }

        public BlockPathTableModel(OBlock block, OBlockTableFrame parent) {
            super();
            _block = block;
            _parent = parent;
        }

        public void init() {
            initTempRow();
            _block.addPropertyChangeListener(this);
        }

        public void removeListener() {
            if (_block==null) return;
            try {
                _block.removePropertyChangeListener(this);
            } catch (NullPointerException npe) { // OK when block is removed
            }
        }

        protected OBlock getBlock() {
            return _block;
        }

         void initTempRow() {
            for (int i=0; i<NUMCOLS; i++) {
                tempRow[i] = null;
            }
        }

        public int getColumnCount () {
            return NUMCOLS;
        }

        public int getRowCount() {
            return _block.getPaths().size() + 1;
        }

        public String getColumnName(int col) {
            switch (col) {
                case FROM_PORTAL_COLUMN: return rbx.getString("FromPortal");
                case NAME_COLUMN: return rbx.getString("PathName");
                case TO_PORTAL_COLUMN: return rbx.getString("ToPortal");
            }
            return "";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (_block.getPaths().size() == rowIndex) {
                return tempRow[columnIndex];
            }
            OPath path = (OPath)_block.getPaths().get(rowIndex);
            switch(columnIndex) {
                case FROM_PORTAL_COLUMN:
                    return path.getFromPortalName();
                case NAME_COLUMN:
                    return path.getName();
                case TO_PORTAL_COLUMN:
                    return path.getToPortalName();
                case EDIT_COL:
                    return rbx.getString("ButtonEditTO");
                case DELETE_COL:
                    return AbstractTableAction.rb.getString("ButtonDelete");
            }
            return "";
        }

        public void setValueAt(Object value, int row, int col) {
            String msg = null;
            if (_block.getPaths().size() == row) {
                if (col==NAME_COLUMN) {
                    if (_block.getPathByName((String)value)!=null) {
                        msg = java.text.MessageFormat.format(
                                rbx.getString("DuplPathName"), (String)value);
                        tempRow[col] = (String)value;
                    } else {
                        Portal fromPortal = _block.getPortalByName(tempRow[FROM_PORTAL_COLUMN]);
                        String fromName = null;
                        if (fromPortal!=null) { fromName = fromPortal.getName(); }

                        Portal toPortal = _block.getPortalByName(tempRow[TO_PORTAL_COLUMN]);
                        String toName = null;
                        if (toPortal!=null) { toName = toPortal.getName(); }

                        OPath path = new OPath((String)value, _block, fromName, 0, toName, 0);
                        _block.addPath(path);
                        initTempRow();
                        _parent.updateOpenMenu();
                    }
                    fireTableDataChanged();
                }
                else {
                    tempRow[col] = (String)value;
                }
                if (msg != null) {
                    JOptionPane.showMessageDialog(null, msg,
                            AbstractTableAction.rb.getString("WarningTitle"),
                            JOptionPane.WARNING_MESSAGE);
                }
                return;
            }

            OPath path =(OPath)_block.getPaths().get(row);

            switch(col) {
                case FROM_PORTAL_COLUMN:
                    Portal portal = _block.getPortalByName((String)value);
                    if (portal == null && !_portalList.contains(portal)) {
                        int response = JOptionPane.showConfirmDialog(null, java.text.MessageFormat.format(
                            rbx.getString("BlockPortalConflict"), value, _block.getDisplayName()),
                            AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.YES_NO_OPTION, 
                            JOptionPane.WARNING_MESSAGE);
                        if (response==JOptionPane.NO_OPTION) {
                            break;
                        }
                        portal = new Portal(_block, (String)value, null);
                    }
                    path.setFromPortalName((String)value);
                    fireTableRowsUpdated(row,row);
                    break;
                case NAME_COLUMN:
                    if (_block.getPathByName((String)value)!=null) {
                        msg = java.text.MessageFormat.format(
                                rbx.getString("DuplPathName"), (String)value); 
                    } else {
                        path.setName((String)value);
                        fireTableRowsUpdated(row,row);
                    }
                    break;
                case TO_PORTAL_COLUMN:
                    portal = _block.getPortalByName((String)value);
                    if (portal == null && !_portalList.contains(portal)) {
                        int response = JOptionPane.showConfirmDialog(null, java.text.MessageFormat.format(
                            rbx.getString("BlockPortalConflict"), value, _block.getDisplayName()),
                            AbstractTableAction.rb.getString("WarningTitle"), JOptionPane.YES_NO_OPTION, 
                            JOptionPane.WARNING_MESSAGE);
                        if (response==JOptionPane.NO_OPTION) {
                            break;
                        }
                        portal = new Portal(null, (String)value, _block);
                    }
                    path.setToPortalName((String)value);
                    fireTableRowsUpdated(row,row);
                    break;
                case EDIT_COL:
                    _parent.openPathTurnoutFrame(_parent.makePathTurnoutName(
                                                    _block.getSystemName(), path.getName()));
                    break;
                case DELETE_COL:
                    if (deletePath(path)) { fireTableDataChanged(); }

                    
            }
            if (msg != null) {
                JOptionPane.showMessageDialog(null, msg,
                        AbstractTableAction.rb.getString("WarningTitle"),
                        JOptionPane.WARNING_MESSAGE);
            }
        }

        boolean deletePath(OPath path) {
            if (JOptionPane.showConfirmDialog(null, 
                        java.text.MessageFormat.format(rbx.getString("DeletePathConfirm"),
                        path.getName()), AbstractTableAction.rb.getString("WarningTitle"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                    ==  JOptionPane.YES_OPTION) {
                _block.removePath(path);
                return true;
            }
            return false;
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public Class<?> getColumnClass(int col) {
            if (col==DELETE_COL || col==EDIT_COL) {
                return JButton.class;
            }
            return String.class;
        }

        @SuppressWarnings("fallthrough")
        public int getPreferredWidth(int col) {
            switch (col) {
                case FROM_PORTAL_COLUMN:
                case NAME_COLUMN: 
                case TO_PORTAL_COLUMN:
                    return new JTextField(14).getPreferredSize().width;
                case EDIT_COL:
                    return new JButton("TURNOUT").getPreferredSize().width;
                case DELETE_COL: 
                    return new JButton("DELETE").getPreferredSize().width;
            }
            return 5;
        }

        public OPath getPathByName(String name) {
            for (int i=0; i<_block.getPaths().size(); i++) {
                OPath path = (OPath)_block.getPaths().get(i);
                if (name.equals(path.getName()) ) {
                    return path;
                }
            }
            return null;
        }

        public void propertyChange(PropertyChangeEvent e) {
            if (_block.equals(e.getSource())) {
                String property = e.getPropertyName();
                if (log.isDebugEnabled()) log.debug("propertyChange \""+property+"\".  source= "+e.getSource());
                if (property.equals("portalCount") || property.equals("pathCount")) {
                    fireTableDataChanged();
                }
           }
        }
    }
    /************ End BlockPathTableModel *************************/

    /*************************************** Start PathTurnoutTableModel ***************/
    class PathTurnoutTableModel extends AbstractTableModel {
        static public final int TURNOUT_NAME_COL = 0;
        public static final int SETTINGCOLUMN = 1;
        static public final int DELETE_COL = 2;
        public static final int NUMCOLS = 3;

        private String[] tempRow= new String[NUMCOLS];
        private OPath _path;

        public PathTurnoutTableModel() {
            super();
        }

        public PathTurnoutTableModel(OPath path) {
            super();
            _path = path;
        }

        public void init() {
            initTempRow();
        }

        void initTempRow() {
            for (int i=0; i<NUMCOLS; i++) {
                tempRow[i] = null;
            }
        }

        public int getColumnCount () {
            return NUMCOLS;
        }

        public int getRowCount() {
            return _path.getSettings().size() + 1;
        }

        public String getColumnName(int col) {
            switch (col) {
                case TURNOUT_NAME_COL: return rbx.getString("LabelItemName");
                case SETTINGCOLUMN: return rbx.getString("ColumnSetting");
            }
            return "";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (_path.getSettings().size() == rowIndex) {
                return tempRow[columnIndex];
            }
            // some error checking
            if (rowIndex >= _path.getSettings().size()){
            	log.debug("row greater than bean list size");
            	return "Error bean list";
            }
            BeanSetting bs = _path.getSettings().get(rowIndex);
            // some error checking
            if (bs == null){
            	log.debug("bean is null");
            	return "Error no bean";
            }
            switch(columnIndex) {
                case TURNOUT_NAME_COL:
                    return bs.getBean().getDisplayName();
                case SETTINGCOLUMN:
                    switch (bs.getSetting()) {
                        case Turnout.CLOSED:
                            return closed;
                        case Turnout.THROWN:
                            return thrown;
                        case Turnout.UNKNOWN:
                            return unknown;
                        case Turnout.INCONSISTENT:
                            return inconsistent;
                    }
                    return  unknown;
                case DELETE_COL:
                    return AbstractTableAction.rb.getString("ButtonDelete");
            }
            return "";
        }

        public void setValueAt(Object value, int row, int col) {
            if (_path.getSettings().size() == row) {
                switch(col) {
                    case TURNOUT_NAME_COL:
                        String name = (String)value;
                        Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout(name);
                        if (t != null) {
                            int s = Turnout.UNKNOWN;
                            if (tempRow[SETTINGCOLUMN] == null) {
                                s = Turnout.UNKNOWN;
                            } else if (tempRow[SETTINGCOLUMN].equals(closed)) {
                                s = Turnout.CLOSED;
                            } else if (tempRow[SETTINGCOLUMN].equals(thrown)) {
                                s = Turnout.THROWN; 
                            } else if (tempRow[SETTINGCOLUMN].equals(unknown)) {
                                s = Turnout.UNKNOWN;
                            } else if (tempRow[SETTINGCOLUMN].equals(inconsistent)) {
                                s = Turnout.INCONSISTENT; 
                            }
                            BeanSetting bs = new BeanSetting(t,s);
                            _path.addSetting(bs);
                            fireTableRowsUpdated(row,row);
                        } else {
                            JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                                    rbx.getString("NoSuchTurnout"), name),
                                    AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        break;           
                    case SETTINGCOLUMN:
                        tempRow[col] = (String)value;
                        break;           
                }
                return;
            }

            BeanSetting bs = _path.getSettings().get(row);

            switch(col) {
                case TURNOUT_NAME_COL:
                    Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout((String)value);
                    if (t!=null) {
                        _path.removeSetting(bs);
                        _path.addSetting(new BeanSetting(t, bs.getSetting()));
                    } else {
                        JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                                rbx.getString("NoSuchTurnout"), (String)value),
                                AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    fireTableRowsUpdated(row,row);
                    break;
                case SETTINGCOLUMN:
                    String setting = (String)value;
                    int s = Turnout.UNKNOWN;
                    if (setting.equals(closed)) {
                        s = Turnout.CLOSED; 
                    } else if (setting.equals(thrown)) {
                        s = Turnout.THROWN; 
                    } else if (setting.equals(unknown)) {
                        s = Turnout.UNKNOWN;
                    } else if (setting.equals(inconsistent)) {
                        s = Turnout.INCONSISTENT;
                    }
                    if (s==bs.getSetting()) {
                        break;
                    }
                    _path.removeSetting(bs);
                    t = InstanceManager.turnoutManagerInstance().
                                    provideTurnout(bs.getBean().getSystemName());
                    if (t!=null) {
                        _path.addSetting(new BeanSetting(t, s));
                    }
                    fireTableRowsUpdated(row,row);
                    break;
                case DELETE_COL:
                    if (JOptionPane.showConfirmDialog(null, rbx.getString("DeleteTurnoutConfirm"),
                                                      AbstractTableAction.rb.getString("WarningTitle"),
                                                      JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                                        ==  JOptionPane.YES_OPTION) {
                        _path.removeSetting(bs);
                        fireTableDataChanged();
                    }
            }
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public Class<?> getColumnClass(int col) {
            if (col==DELETE_COL) {
                return JButton.class;
            } else if (col==SETTINGCOLUMN) {
                return JComboBox.class;
            }
            return String.class;
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case TURNOUT_NAME_COL: return new JTextField(16).getPreferredSize().width;
                case SETTINGCOLUMN: return new JTextField(8).getPreferredSize().width;
                case DELETE_COL: return new JButton("DELETE").getPreferredSize().width;
            }
            return 5;
        }

        public void propertyChange(PropertyChangeEvent e) {
            if (_path.getBlock().equals(e.getSource()) && e.getPropertyName().equals("pathCount")) {
                fireTableDataChanged();
            }
        }
    }

    /************************* DnD ******************************/

    public static final String TableCellFlavorMime = DataFlavor.javaJVMLocalObjectMimeType +
               ";class=jmri.jmrit.beantable.OBlockTableAction.TableCellSelection";
    public static DataFlavor TABLECELL_FLAVOR = new DataFlavor(
                jmri.jmrit.beantable.OBlockTableAction.TableCellSelection.class,
                "application/x-jmri.jmrit.beantable.OBlockTableAction.TableCellSelection");

    public class TableCellSelection extends StringSelection {
        int _row;
        int _col;
        int _who;
        TableCellSelection( String data, int row, int col, int who) {
            super(data);
            _row = row;
            _col = col;
            _who = who;
        }
        int getRow() { return _row; }
        int getCol() { return _col; }
        int getWho() { return _who; }
    }

    public class TableCellTransferable implements Transferable {
        TableCellSelection _tcss;
        TableCellTransferable(TableCellSelection tcss) {
            _tcss = tcss;
        }
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { TABLECELL_FLAVOR, DataFlavor.stringFlavor };
        }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (flavor.equals(TABLECELL_FLAVOR)) {
                return true;
            } else if (flavor.equals(DataFlavor.stringFlavor)) {
                return true;
            }
            return false;
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (flavor.equals(TABLECELL_FLAVOR)) {
                return _tcss;
            } else if (flavor.equals(DataFlavor.stringFlavor)) {
                return _tcss.getTransferData(DataFlavor.stringFlavor);
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }

    class DnDHandler extends TransferHandler {
        int _type;

        DnDHandler(int t) {
            _type = t;
        }

        public int getType() {
            return _type;
        }

        //////////////export
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
            if (log.isDebugEnabled()) log.debug("DnDHandler.createTransferable: at table "+
                                                _type+" from ("+row+", "+col+") data= \""
                                                +table.getModel().getValueAt(row, col)+"\"");
            TableCellSelection tcss = new TableCellSelection(
                                (String)table.getModel().getValueAt(row, col), row, col, _type);
            return new TableCellTransferable(tcss);
        }

        public void exportDone(JComponent c, Transferable t, int action) {
            if (log.isDebugEnabled()) log.debug("DnDHandler.exportDone at table "+_type);
        }

        /////////////////////import
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            if (log.isDebugEnabled()) log.debug("DnDHandler.canImport ");

            boolean canDoIt = false;
            for (int k=0; k<transferFlavors.length; k++){
                if (transferFlavors[k].equals(TABLECELL_FLAVOR) || 
                    transferFlavors[k].equals(DataFlavor.stringFlavor)) {
                    if (comp instanceof JTable) { 
                        canDoIt = true; 
                        break;
                    }
                }
            }
            if (!canDoIt) { return false; }
            return true;
        }

        public boolean importData(JComponent comp, Transferable tr) {
            if (log.isDebugEnabled()) log.debug("DnDHandler.importData ");
            DataFlavor[] flavors = new DataFlavor[] {TABLECELL_FLAVOR, DataFlavor.stringFlavor};

            if (!canImport(comp, flavors)) {
                return false;
            }

            try {
                if (tr.isDataFlavorSupported(TABLECELL_FLAVOR) ||
                            tr.isDataFlavorSupported(DataFlavor.stringFlavor) ) {
                    DnDJTable table = (DnDJTable)comp;
                    AbstractTableModel model = (AbstractTableModel)table.getModel();
                    int col = table.getSelectedColumn();
                    int row = table.getSelectedRow();
                    if (col>=0 && row>=0) {
                        String data = (String)tr.getTransferData(DataFlavor.stringFlavor);
                        model.setValueAt(data, row, col);
                        model.fireTableDataChanged();
                        if (log.isDebugEnabled()) 
                            log.debug("DnDHandler.canImport: data= "+data+" dropped at ("+row+", "+col+")");
                        return true;
                    }
                }
            }
            catch (UnsupportedFlavorException ufe) { 
                log.warn("DnDHandler.importData: at table e= "+ufe);
            }
            catch (IOException ioe) { 
                log.warn("DnDHandler.importData: at table e= "+ioe);
            }
            return false;
        }
    }

    class DnDJTable extends JTable implements DropTargetListener,  
                    DragGestureListener, DragSourceListener, Transferable {
        Point _dropPoint;

        DnDJTable (TableModel model) {
            super (model);
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(this,
                        DnDConstants.ACTION_COPY_OR_MOVE, this);
            new DropTarget(this, DnDConstants.ACTION_COPY, this);
        }

        Point getDropPoint() {
            return _dropPoint;
        }

        private boolean dropOK(DropTargetDragEvent evt) {
            Transferable tr = evt.getTransferable();
            if (tr.isDataFlavorSupported(TABLECELL_FLAVOR) ||
                            tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                _dropPoint = evt.getLocation();
                DnDHandler handler = (DnDHandler)getTransferHandler();
                int col = columnAtPoint(_dropPoint);
                int row = rowAtPoint(_dropPoint);
                int type = handler.getType();
                switch (type) {
                    case BLOCK_TABLE:
                        if ((col==OBlockTableModel.SYSNAMECOL) ||
                            (col==OBlockTableModel.DELETE_COL) || 
                            (col==OBlockTableModel.EDIT_COL)) {  return false; }
                        break;
                    case PORTAL_TABLE:
                        if ((col==PortalTableModel.DELETE_COL)) {  return false; }
                        break;
                    case BLOCK_PATH_TABLE:
                        if ((col==BlockPathTableModel.DELETE_COL) || 
                            (col==BlockPathTableModel.EDIT_COL)) { return false; }
                        break;
                    case XREF_TABLE:
                        return false;
                    case TURNOUT_TABLE:
                        if (col==PathTurnoutTableModel.SETTINGCOLUMN) { return false; }
                        break;
                }
                if (tr.isDataFlavorSupported(TABLECELL_FLAVOR)) {
                    try {
                        // don't allow a cell import back into the cell exported from 
                        TableCellSelection tcss = (TableCellSelection)tr.getTransferData(TABLECELL_FLAVOR);
                        if (row==tcss.getRow() && col==tcss.getCol() && type==tcss.getWho()) {
                            return false;
                        }
                    }
                    catch (UnsupportedFlavorException ufe) { 
                        log.warn("DnDJTable.importData: at table "+type+" e= "+ufe);
                        return false; 
                    }
                    catch (IOException ioe) { 
                        log.warn("DnDJTable.importData: at table "+type+" e= "+ioe);
                        return false;
                    }
                }
            } else {
                return false;
            }
            return true;
        }
        /*************************** DropTargetListener ************************/
        public void dragExit(DropTargetEvent evt) {
            //if (log.isDebugEnabled()) log.debug("DnDJTable.dragExit ");
            //evt.getDropTargetContext().acceptDrag(DnDConstants.ACTION_COPY);
        }
        public void dragEnter(DropTargetDragEvent evt) {
            //if (log.isDebugEnabled()) log.debug("DnDJTable.dragEnter ");
            if (!dropOK(evt)) {
                evt.rejectDrag();
            }
        }
        public void dragOver(DropTargetDragEvent evt) {
            if (!dropOK(evt)) {
                evt.rejectDrag();
            }
        }
        public void dropActionChanged(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DnDJTable.dropActionChanged ");
        }
        public void drop(DropTargetDropEvent evt) {
            try {
                Point pt = evt.getLocation();
                String data = null;
                Transferable tr = evt.getTransferable();
                if (tr.isDataFlavorSupported(TABLECELL_FLAVOR) ||
                            tr.isDataFlavorSupported(DataFlavor.stringFlavor) ) {
                    AbstractTableModel model = (AbstractTableModel)getModel();
                    int col = columnAtPoint(pt);
                    int row = rowAtPoint(pt);
                    if (col>=0 && row>=0) {
                        TableCellSelection sel = (TableCellSelection)tr.getTransferData(TABLECELL_FLAVOR);
                        data = (String)sel.getTransferData(DataFlavor.stringFlavor);
                        model.setValueAt(data, row, col);
                        model.fireTableDataChanged();
                        if (log.isDebugEnabled()) 
                            log.debug("DnDJTable.drop: data= "+data+" dropped at ("+row+", "+col+")");
                        evt.dropComplete(true);
                        return;
                    }
                } else {
                    log.warn("TransferHandler.importData: supported DataFlavors not avaialable at table from "
                             +tr.getClass().getName());
                }
            } catch(IOException ioe) {
                ioe.printStackTrace();
            } catch(UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
            }
            if (log.isDebugEnabled()) log.debug("DropJTree.drop REJECTED!");
            evt.rejectDrop();
        }
        /**************** DragGestureListener ***************/
        public void dragGestureRecognized(DragGestureEvent e) {
            if (log.isDebugEnabled()) log.debug("DnDJTable.dragGestureRecognized ");
            //Transferable t = getTransferable(this);
            //e.startDrag(DragSource.DefaultCopyDrop, this, this); 
        }
        /**************** DragSourceListener ************/
        public void dragDropEnd(DragSourceDropEvent e) {
            if (log.isDebugEnabled()) log.debug("DnDJTable.dragDropEnd ");
            }
        public void dragEnter(DragSourceDragEvent e) {
            //if (log.isDebugEnabled()) log.debug("DnDJTable.DragSourceDragEvent ");
            }
        public void dragExit(DragSourceEvent e) {
            //if (log.isDebugEnabled()) log.debug("DnDJTable.dragExit ");
            }
        public void dragOver(DragSourceDragEvent e) {
            //if (log.isDebugEnabled()) log.debug("DnDJTable.dragOver ");
            }
        public void dropActionChanged(DragSourceDragEvent e) {
            //if (log.isDebugEnabled()) log.debug("DnDJTable.dropActionChanged ");
            }
        /*************** Transferable *********************/
        public DataFlavor[] getTransferDataFlavors() {
            //if (log.isDebugEnabled()) log.debug("DnDJTable.getTransferDataFlavors ");
            return new DataFlavor[] { TABLECELL_FLAVOR };
        }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            //if (log.isDebugEnabled()) log.debug("DragJLabel.isDataFlavorSupported ");
            return TABLECELL_FLAVOR.equals(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (log.isDebugEnabled()) log.debug("DnDJTable.getTransferData ");
            if (isDataFlavorSupported(TABLECELL_FLAVOR)) {
                int row = getSelectedRow();
                int col = getSelectedColumn();
                if (col>=0 && row>=0) {
                    return getValueAt(row, col);
                }
            }
            return null;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OBlockTableAction.class.getName());
}
