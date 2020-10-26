package jmri.jmrit.beantable.oblock;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import jmri.*;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.jmrit.logix.WarrantTableAction;
import jmri.util.JmriJFrame;
import jmri.util.SystemType;
import jmri.util.com.sun.TransferActionListener;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to define OBlocks.
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
 * @author Pete Cressman (C) 2010
 * @author Egbert Broerse (C) 2020
 */
public class TableFrames extends jmri.util.JmriJFrame implements InternalFrameListener {

    private static int ROW_HEIGHT = 10;
    public static final int STRUT_SIZE = 10;
    private static String oblockPrefix;
    private final static String portalPrefix = "IP";

    private JTable _oBlockTable;
    private final OBlockTableModel _oBlockModel;
    private JTable _portalTable;
    private final PortalTableModel _portalModel;
    private JTable _blockPortalTable;
    private final BlockPortalTableModel _blockPortalXRefModel;
    private JTable _signalTable;
    private final SignalTableModel _signalModel;

    protected boolean _tabbed; // updated from prefs
//    private JScrollPane _blockTablePane;
//    private JScrollPane _portalTablePane;
//    private JScrollPane _blockPortalTablePane;
//    private JScrollPane _signalTablePane;

    private JDesktopPane _desktop;
    private final int maxHeight = 600;
    private final int maxWidth = 1100;
    private JInternalFrame _blockTableFrame;
    private JInternalFrame _portalTableFrame;
    private JInternalFrame _blockPortalXRefFrame;
    private JInternalFrame _signalTableFrame;

    private boolean _showWarnings = true;
    private JMenuItem _showWarnItem;
    private JMenu tablesMenu;
    private JMenuItem openBlock;
    private JMenuItem openPortal;
    private JMenuItem openXRef;
    private JMenuItem openSignal;

    private final HashMap<String, BlockPathFrame> _blockPathMap = new HashMap<>();
    private final HashMap<String, PathTurnoutFrame> _PathTurnoutMap = new HashMap<>();
    private final HashMap<String, PathTurnoutPane> _PathTurnoutPaneMap = new HashMap<>(); // same, for tabbed

    public TableFrames() {
        this("OBlock Table");
    } // NOI18N, title will be updated when !_tabbed, or is hidden

    public TableFrames(String actionName) {
        super(actionName);
        _tabbed = InstanceManager.getDefault(GuiLafPreferencesManager.class).isOblockEditTabbed();
        log.debug("_tabbed = {}", _tabbed);
        if (_tabbed) {
            this.setVisible(false); // TODO hide the separate _desktop panel, not this
        }
        // create the tables
        _oBlockModel = new OBlockTableModel(this);
        _portalModel = new PortalTableModel(this);
        _signalModel = new SignalTableModel(this);
        _signalModel.init();
        _blockPortalXRefModel = new BlockPortalTableModel(_oBlockModel);
        // keep more tables in memory: Path-Turnout, BlockPath?
    }

    public OBlockTableModel getOblockTableModel() {
        return _oBlockModel;
    }
    public PortalTableModel getPortalTableModel() {
        return _portalModel;
    }
    public SignalTableModel getSignalTableModel() {
        return _signalModel;
    }
    public BlockPortalTableModel getPortalXRefTableModel() {
        return _blockPortalXRefModel;
    }
    public BlockPathTableModel getBlockPathTableModel(OBlock block) {
        return new BlockPathTableModel(block, this);
    }

    @Override
    public void initComponents() {
        if (!_tabbed) { // classic floating desktop interface
            setTitle(Bundle.getMessage("TitleOBlocks"));
            setJMenuBar(addMenus(this.getJMenuBar()));
            addHelpMenu("package.jmri.jmrit.logix.OBlockTable", true);

            // build tables
            _blockTableFrame = buildFrame(_oBlockModel, Bundle.getMessage("TitleBlockTable"), Bundle.getMessage("AddPortalPrompt"));
            _blockTableFrame.setVisible(true);

            _portalTableFrame = buildFrame(_portalModel, Bundle.getMessage("TitlePortalTable"), Bundle.getMessage("AddPortalPrompt"));
            _portalTableFrame.setVisible(true);

            _signalTableFrame = buildFrame(_signalModel, Bundle.getMessage("TitleSignalTable"), Bundle.getMessage("AddSignalPrompt"));
            _signalTableFrame.setVisible(false);

            _blockPortalXRefFrame = buildFrame(_blockPortalXRefModel, Bundle.getMessage("TitleBlockPortalXRef"), Bundle.getMessage("XRefPrompt"));
            _blockPortalXRefFrame.setVisible(false); // start with frame hidden

            createDesktop(); // adds tables as windows on _desktop
            setLocation(10, 30);
            setVisible(true);
            pack();

            WarrantTableAction.getDefault().errorCheck();
        }
    }

    public JMenuBar addMenus(JMenuBar mBar) {
        if (mBar == null) {
            mBar = new JMenuBar();
        }
        // create and add the menus
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        fileMenu.add(new jmri.configurexml.StoreMenu());

        JMenuItem printItem = new JMenuItem(Bundle.getMessage("PrintOBlockTable"));
        fileMenu.add(printItem);
        printItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // MessageFormat headerFormat = new MessageFormat(getTitle());  // not used below
                    MessageFormat footerFormat = new MessageFormat(getTitle() + " page {0,number}");
                    _oBlockTable.print(JTable.PrintMode.FIT_WIDTH, null, footerFormat);
                } catch (java.awt.print.PrinterException e1) {
                    log.warn("error printing: {}", e1, e1);
                }
            }
        });
        printItem = new JMenuItem(Bundle.getMessage("PrintPortalTable"));
        fileMenu.add(printItem);
        printItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // MessageFormat headerFormat = new MessageFormat(getTitle());  // not used below
                    MessageFormat footerFormat = new MessageFormat(getTitle() + " page {0,number}");
                    _portalTable.print(JTable.PrintMode.FIT_WIDTH, null, footerFormat);
                } catch (java.awt.print.PrinterException e1) {
                    log.warn("error printing: {}", e1, e1);
                }
            }
        });
        printItem = new JMenuItem(Bundle.getMessage("PrintSignalTable"));
        fileMenu.add(printItem);
        printItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // MessageFormat headerFormat = new MessageFormat(getTitle());  // not used below
                    MessageFormat footerFormat = new MessageFormat(getTitle() + " page {0,number}");
                    _signalTable.print(JTable.PrintMode.FIT_WIDTH, null, footerFormat);
                } catch (java.awt.print.PrinterException e1) {
                    log.warn("error printing: {}", e1, e1);
                }
            }
        });
        printItem = new JMenuItem(Bundle.getMessage("PrintXRef"));
        fileMenu.add(printItem);
        printItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // MessageFormat headerFormat = new MessageFormat(getTitle());  // not used below
                    MessageFormat footerFormat = new MessageFormat(getTitle() + " page {0,number}");
                    _blockPortalTable.print(JTable.PrintMode.FIT_WIDTH, null, footerFormat);
                } catch (java.awt.print.PrinterException e1) {
                    log.warn("error printing: {}", e1, e1);
                }
            }
        });
        mBar.add(fileMenu);

        JMenu editMenu = new JMenu(Bundle.getMessage("MenuEdit"));
        editMenu.setMnemonic(KeyEvent.VK_E);
        TransferActionListener actionListener = new TransferActionListener();

        JMenuItem menuItem = new JMenuItem(Bundle.getMessage("MenuItemCut"));
        menuItem.setActionCommand((String) TransferHandler.getCutAction().getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        if (SystemType.isMacOSX()) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.META_DOWN_MASK));
        } else {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        }
        menuItem.setMnemonic(KeyEvent.VK_T);
        editMenu.add(menuItem);

        menuItem = new JMenuItem(Bundle.getMessage("MenuItemCopy"));
        menuItem.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        if (SystemType.isMacOSX()) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_DOWN_MASK));
        } else {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        }
        menuItem.setMnemonic(KeyEvent.VK_C);
        editMenu.add(menuItem);

        menuItem = new JMenuItem(Bundle.getMessage("MenuItemPaste"));
        menuItem.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        if (SystemType.isMacOSX()) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.META_DOWN_MASK));
        } else {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        }
        menuItem.setMnemonic(KeyEvent.VK_P);
        editMenu.add(menuItem);
        mBar.add(editMenu);

        JMenu optionMenu = new JMenu(Bundle.getMessage("MenuOptions"));
        _showWarnItem = new JMenuItem(Bundle.getMessage("SuppressWarning"));
        _showWarnItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String cmd = event.getActionCommand();
                setShowWarnings(cmd);
            }
        });
        optionMenu.add(_showWarnItem);
        setShowWarnings("ShowWarning");

        JMenuItem importBlocksItem = new JMenuItem(Bundle.getMessage("ImportBlocksMenu"));
        importBlocksItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                importBlocks();
            }
        });
        optionMenu.add(importBlocksItem);
        // disable ourself if there is no primary Block manager available
        if (jmri.InstanceManager.getNullableDefault(jmri.BlockManager.class) == null) { // means Block list is empty
            importBlocksItem.setEnabled(false);
        }

        // Options menu
        //menuBar.add(optionMenu);
        // Tables menu
        tablesMenu = new JMenu(Bundle.getMessage("OpenMenu"));
        updateOblockTablesMenu();   // replaces the last 2 menu items with appropriate submenus
        mBar.add(tablesMenu);
        return mBar; //new JMenu[]{fileMenu, optionMenu, tablesMenu};
    }

    // for desktop style interface, ignored for _tabbed
    private void createDesktop() {
        _desktop = new JDesktopPane();
        //_desktop.putClientProperty("JDesktopPane.dragMode", "outline"); // too slow
        _desktop.setPreferredSize(new Dimension(1100, 600));
        _desktop.setBackground(new Color(180,180,180));
        setContentPane(_desktop);

        // placed at 0,0
        _desktop.add(_blockTableFrame);
        _portalTableFrame.setLocation(0, 320);
        _desktop.add(_portalTableFrame);
        _signalTableFrame.setLocation(350, 400);
        _desktop.add(_signalTableFrame);
        _blockPortalXRefFrame.setLocation(700, 20);
        _desktop.add(_blockPortalXRefFrame);
        // BlockPathTableFrame(block) is created later on demand
        //maxHeight = _desktop.getPreferredSize().height; // update
    }
    private String oblockPrefix() {
        if (oblockPrefix == null) {
            oblockPrefix = InstanceManager.getDefault(OBlockManager.class).getSystemNamePrefix();
        }
        return oblockPrefix;
    }

    /**
     * Convert a copy of JMRI Blocks to OBlocks and connect them with Portals and Paths.
     *
     * @author Egbert Broerse 2019
     */
    protected void importBlocks() throws IllegalArgumentException {
        Manager<Block> bm = InstanceManager.getDefault(jmri.BlockManager.class);
        OBlockManager obm = InstanceManager.getDefault(OBlockManager.class);
        PortalManager pom = InstanceManager.getDefault(PortalManager.class);
        SortedSet<Block> blkList = bm.getNamedBeanSet();
        // don't return an element if there are no Blocks to include
        if (blkList.isEmpty()) {
            log.warn("no Blocks to convert"); // NOI18N
            JOptionPane.showMessageDialog(this, Bundle.getMessage("ImportNoBlocks"),
                    Bundle.getMessage("InfoTitle"), JOptionPane.INFORMATION_MESSAGE);
            return;
        } else {
            if (_showWarnings) {
                int reply = JOptionPane.showOptionDialog(null,
                        Bundle.getMessage("ImportBlockConfirm", oblockPrefix(), blkList.size()),
                        Bundle.getMessage("QuestionTitle"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        new Object[]{Bundle.getMessage("ButtonYes"),
                                Bundle.getMessage("ButtonCancel")},
                        Bundle.getMessage("ButtonYes")); // standard JOptionPane can't be found in Jemmy log4J
                if (reply > 0) {
                    return;
                }
            }
        }
        for (Block b : blkList) {
            try {
                // read Block properties
                String sName = b.getSystemName();
                String uName = b.getUserName();
                String blockNumber = sName.substring(sName.startsWith("IB:AUTO:") ? 8 : 3);
                String oBlockName = oblockPrefix() + blockNumber;
                String sensor = "";
                Sensor s = b.getSensor();
                if (s != null) {
                    sensor = s.getDisplayName();
                }
                float length = b.getLengthMm(); // length is stored in Mm in OBlock.setLength(float)
                int curve = b.getCurvature();
                List<Path> blockPaths = b.getPaths();
                String toBlockName;
                Portal port = null;
                int n = 0;
                Portal prevPortal = null;

                log.debug("start creating OBlock {} from Block {}", oBlockName, sName);
                if ((uName != null) && (obm.getOBlock(uName) != null)) {
                    log.warn("an OBlock with this user name already exists, replacing {}", uName);
                }
                // create the OBlock by systemName
                OBlock oBlock = obm.provideOBlock(oBlockName);
                oBlock.setUserName(uName);
                if (!sensor.isEmpty()) {
                    oBlock.setSensor(sensor);
                }
                oBlock.setMetricUnits(true); // length always stored in Mm in Block, so copy that for OBlock
                oBlock.setLength(length);
                oBlock.setCurvature(curve);

                for (Path pa : blockPaths) {
                    log.debug("Start loop: path {} on block {}", n, oBlockName);
                    String toBlockNumber = pa.getBlock().getSystemName().substring(sName.startsWith("IB:AUTO:") ? 8 : 3);
                    toBlockName = oblockPrefix() + toBlockNumber;
                    String portalName = portalPrefix + toBlockNumber + "-" + blockNumber; // reversed name for new Portal
                    port = pom.getPortal(portalName);
                    if (port == null) {
                        portalName = portalPrefix + blockNumber + "-" + toBlockNumber; // normal name for new Portal
                        log.debug("new Portal {} on block {}, path #{}", portalName, toBlockName, n);
                        port = pom.providePortal(portalName); // normally, will create a new Portal
                        port.setFromBlock(oBlock, false);
                        port.setToBlock(obm.provideOBlock(toBlockName), false); // create one if required
                    } else {
                        log.debug("duplicate Portal {} on block {}, path #{}", portalName, toBlockName, n);
                        // Portal port already set
                    }
                    oBlock.addPortal(port);

                    // create OPath from this Path
                    OPath opa = new OPath(oBlock, "IP" + n++); // only needs to be unique within oBlock
                    opa.setLength(oBlock.getLengthMm()); // simple assumption, works for default OBlock/OPath
                    log.debug("new OPath #{} - {} on OBlock {}", n, opa.getName(), opa.getBlock().getDisplayName());
                    oBlock.addPath(opa); // checks for duplicates, will add OPath to any Portals on oBlock as well
                    log.debug("number of paths: {}", oBlock.getPaths().size());

                    // set _fromPortal and _toPortal for each OPath in OBlock
                    if (opa.getFromPortal() == null) {
                        opa.setFromPortal(port);
                    }
                    if ((opa.getToPortal() == null) && (prevPortal != null)) {
                        opa.setToPortal(prevPortal);
                        // leaves ToPortal in previously (first) created OPath n-1 empty
                    }
                    prevPortal = port; // remember the new portal for use as ToPortal in opposing OPath
                    // user must remove nonsense manually unless...
                }
                // we use the last FromPortal as ToPortal in OPath P0
                OPath p0 = oBlock.getPathByName("IP0");
                if ((p0 != null) && (n > 1) && (p0.getToPortal() == null)) {
                    p0.setToPortal(port);
                }
            } catch (IllegalArgumentException iae) {
                log.error(iae.toString());
            }
            // finished setting up 1 OBlock
        }
        // add recursive Path elements to FromBlock/ToBlock
        SortedSet<OBlock> oblkList = obm.getNamedBeanSet();
        for (OBlock oblk : oblkList) {
            for (Portal po : oblk.getPortals()) {
                OBlock oob = obm.getByUserName(po.getFromBlockName());
                if (oob !=null) {
                    oob.addPortal(po);
                }
                oob = obm.getByUserName(po.getToBlockName());
                if (oob !=null) {
                    oob.addPortal(po);
                }
            }
        }
        // storing and reloading will add in these items
        WarrantTableAction.getDefault().errorCheck();
        if (_showWarnings) {
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("ImportBlockComplete", blkList.size(), oblkList.size()),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE); // standard JOptionPane can't be found in Jemmy log4J
        }
    }
    // End of importBlocks() menu method

    protected void setShowWarnings(String cmd) {
        if (cmd.equals("ShowWarning")) {
            _showWarnings = true;
            _showWarnItem.setActionCommand("SuppressWarning");
            _showWarnItem.setText(Bundle.getMessage("SuppressWarning"));
        } else {
            _showWarnings = false;
            _showWarnItem.setActionCommand("ShowWarning");
            _showWarnItem.setText(Bundle.getMessage("ShowWarning"));
        }
        log.debug("setShowWarnings: _showWarnings= {}", _showWarnings);
    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        WarrantTableAction.getDefault().errorCheck();
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE); // closing instead of hiding removes name from Windows menu.handle menu to read Show...
        log.debug("windowClosing: {}", toString());
    }

    protected void updateOblockTablesMenu() {
        // fill Table menu
        tablesMenu.removeAll();
        // include string Bundle.getMessage("HideTable") for all table open at start

        openBlock = new JMenuItem(Bundle.getMessage("OpenBlockMenu", Bundle.getMessage("HideTable")));
        tablesMenu.add(openBlock);
        openBlock.addActionListener(event -> showHideFrame(_blockTableFrame, openBlock, "OpenBlockMenu"));

        openPortal = new JMenuItem(Bundle.getMessage("OpenPortalMenu", Bundle.getMessage("HideTable")));
        tablesMenu.add(openPortal);
        openPortal.addActionListener(event -> showHideFrame(_portalTableFrame, openPortal, "OpenPortalMenu"));

        openXRef = new JMenuItem(Bundle.getMessage("OpenXRefMenu", Bundle.getMessage("ShowTable")));
        tablesMenu.add(openXRef);
        openXRef.addActionListener(event -> showHideFrame(_blockPortalXRefFrame, openXRef, "OpenXRefMenu"));

        openSignal = new JMenuItem(Bundle.getMessage("OpenSignalMenu", Bundle.getMessage("ShowTable")));
        tablesMenu.add(openSignal);
        openSignal.addActionListener(event -> showHideFrame(_signalTableFrame, openSignal, "OpenSignalMenu"));

        // get the OBlock manager
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);

        JMenu openBlockPath = new JMenu(Bundle.getMessage("OpenBlockPathMenu"));
        ActionListener openFrameAction = e -> {
            String sysName = e.getActionCommand();
            openBlockPathPane(sysName);
        };

        if (manager.getNamedBeanSet().size() == 0) {
            JMenuItem mi = new JMenuItem(Bundle.getMessage("NoBlockPathYet"));
            mi.setEnabled(false);
            openBlockPath.add(mi);
        } else {
            for (OBlock block : manager.getNamedBeanSet()) {
                JMenuItem mi = new JMenuItem(Bundle.getMessage("OpenPathMenu", block.getDisplayName()));
                mi.setActionCommand(block.getSystemName());
                mi.addActionListener(openFrameAction);
                openBlockPath.add(mi);
            }
        }
        tablesMenu.add(openBlockPath);

        JMenu openTurnoutPath = new JMenu(Bundle.getMessage("OpenBlockPathTurnoutMenu"));
        if (manager.getNamedBeanSet().size() == 0) {
            JMenuItem mi = new JMenuItem(Bundle.getMessage("NoPathTurnoutYet"));
            mi.setEnabled(false);
            openTurnoutPath.add(mi);
        } else {
            for (OBlock block : manager.getNamedBeanSet()) {
                JMenu openTurnoutMenu = new JMenu(Bundle.getMessage("OpenTurnoutMenu", block.getDisplayName()));
                openTurnoutPath.add(openTurnoutMenu);
                openFrameAction = e -> {
                    String pathTurnoutName = e.getActionCommand();
                    openPathTurnoutPane(pathTurnoutName);
                };
                for (Path p : block.getPaths()) {
                    if (p instanceof OPath) {
                        OPath path = (OPath) p;
                        JMenuItem mi = new JMenuItem(Bundle.getMessage("OpenPathTurnoutMenu", path.getName()));
                        mi.setActionCommand(makePathTurnoutName(block.getSystemName(), path.getName()));
                        mi.addActionListener(openFrameAction);
                        openTurnoutMenu.add(mi);
                    }
                }
            }
        }
        tablesMenu.add(openTurnoutPath);
    }

    public void openBlockPathPane(String blockSysName) {
        if (_tabbed) {
            OBlock block = InstanceManager.getDefault(OBlockManager.class).getOBlock(blockSysName);
            if (block == null) {
                log.error("null OBlock {}", blockSysName);
            } else {
                BlockPathTableModel model = new BlockPathTableModel(block, this);
                JTable table = makeBlockPathTable(block, model);
                // set up the table
                JmriJFrame frame = new JmriJFrame(Bundle.getMessage("TitleBlockPathTable", blockSysName));
                frame.setPreferredSize(new Dimension(200, 100));
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.add(table);
                frame.add(panel);
                frame.setVisible(true);
            }
        } else {
            openBlockPathFrame(blockSysName);
        }
    }

    public void openPathTurnoutPane(String pathTurnoutName) {
        if (_tabbed) {
            // opslaan in lijst?
            PathTurnoutPane pane = _PathTurnoutPaneMap.get(pathTurnoutName);
            log.debug("openPathTurnoutFrame for {}", pathTurnoutName);
            if (pane == null) {
                int index = pathTurnoutName.indexOf('&');
                String pathName = pathTurnoutName.substring(1, index);
                String sysName = pathTurnoutName.substring(index + 1);
                OBlock block = InstanceManager.getDefault(OBlockManager.class).getBySystemName(sysName);
                if (block == null) {
                    log.error("null OBlock {}", pathTurnoutName);
                } else {
                    String title = Bundle.getMessage("TitlePathTurnoutTable", block.getDisplayName(), pathName);
                    if (log.isDebugEnabled()) {
                        log.debug("makePathTurnoutPane for Block {} and Path {}", block.getDisplayName(), pathName);
                    }
                    pane = new PathTurnoutPane(title);
                    pane.setName(makePathTurnoutName(block.getSystemName(), pathName));
                    OPath path = block.getPathByName(pathName);
                    if (path == null) {
                        log.error("Null path Turnout");
                    }
                    PathTurnoutTableModel pathTurnoutModel = new PathTurnoutTableModel(path, null);
                    pane.setModel(pathTurnoutModel);

                    JTable pathTurnoutTable = makePathTurnoutTable(pathTurnoutModel);

                    JScrollPane tablePane = new JScrollPane(pathTurnoutTable);

                    //pane = makePathTurnoutPane(pathTurnoutName);
                    //                // set up the table

                    JmriJFrame frame = new JmriJFrame(title);
                    frame.setPreferredSize(new Dimension(200, 100));
                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.add(pathTurnoutTable);
                    frame.add(panel);
                    frame.setVisible(true);

                    _PathTurnoutPaneMap.put(pathTurnoutName, pane);
                }
            }
        } else {
            openPathTurnoutFrame(pathTurnoutName);
        }
    }

    private void showHideFrame(JInternalFrame frame, JMenuItem menu, String menuName) {
        if (!frame.isVisible()) {
            frame.setVisible(true);
            try {
                frame.setIcon(false);
            } catch (PropertyVetoException pve) {
                log.warn("{} Frame vetoed setIcon {}", frame.getTitle(), pve.toString());
            }
            frame.moveToFront();
        } else {
            frame.setVisible(false);
        }
        menu.setText(Bundle.getMessage(menuName,
                (frame.isVisible() ? Bundle.getMessage("HideTable") : Bundle.getMessage("ShowTable"))));
    }

    private synchronized static void setRowHeight(int newVal) {
        ROW_HEIGHT = Math.max(newVal, 10); // for safety
    }

    /**
     * Wrapper for shared code around each Table.
     *
     * @param tableModel underlying model for the table
     * @param title text displayed as title of frame
     * @param prompt text below bottom line
     * @return frame to put on _desktop interface
     */
    protected JInternalFrame buildFrame(AbstractTableModel tableModel, String title, String prompt) {
        JInternalFrame frame = new JInternalFrame(title, true, false, false, true);

        // specifics for table
        JTable table = new JTable(); // just to make sure it is there
        if (tableModel instanceof OBlockTableModel) {
            table = makeOBlockTable((OBlockTableModel) tableModel);
        } else if (tableModel instanceof PortalTableModel) {
            table = makePortalTable((PortalTableModel) tableModel);
        } else if (tableModel instanceof BlockPortalTableModel) {
            table = makeBlockPortalTable((BlockPortalTableModel) tableModel);
        } else if (tableModel instanceof SignalTableModel) {
            table = makeSignalTable((SignalTableModel) tableModel);
        } // no case for BlockPathTableModel, handled separately

        JScrollPane scroll = new JScrollPane(table);
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        JLabel _prompt = new JLabel(prompt);
        contentPane.add(_prompt, BorderLayout.NORTH);
        contentPane.add(scroll, BorderLayout.CENTER);

        frame.setContentPane(contentPane);
        frame.pack();
        return frame;
    }

    /*
     * ********************* BlockFrame *****************************
     */
    protected JTable makeOBlockTable(OBlockTableModel model) {
        _oBlockTable = new JTable(model);
        TableRowSorter<OBlockTableModel> sorter = new TableRowSorter<>(_oBlockModel);
        // use NamedBean's built-in Comparator interface for sorting
        _oBlockTable.setRowSorter(sorter);
        _oBlockTable.setTransferHandler(new jmri.util.DnDTableImportExportHandler(new int[]{OBlockTableModel.EDIT_COL,
            OBlockTableModel.DELETE_COL, OBlockTableModel.REPORT_CURRENTCOL, OBlockTableModel.SPEEDCOL,
            OBlockTableModel.PERMISSIONCOL, OBlockTableModel.UNITSCOL}));
        _oBlockTable.setDragEnabled(true);

        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        _oBlockTable.setColumnModel(tcm);
        _oBlockTable.getTableHeader().setReorderingAllowed(true);
        _oBlockTable.createDefaultColumnsFromModel();
        _oBlockModel.addHeaderListener(_oBlockTable);

        _oBlockTable.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.EDIT_COL).setCellEditor(new ButtonEditor(new JButton()));
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.EDIT_COL).setCellRenderer(new ButtonRenderer());
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.DELETE_COL).setCellRenderer(new ButtonRenderer());
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.UNITSCOL).setCellRenderer(
                new MyBooleanRenderer(Bundle.getMessage("cm"), Bundle.getMessage("in")));
        JComboBox<String> box = new JComboBox<>(OBlockTableModel.curveOptions);
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.CURVECOL).setCellEditor(new DefaultCellEditor(box));
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.REPORT_CURRENTCOL).setCellRenderer(
                new MyBooleanRenderer(Bundle.getMessage("Current"), Bundle.getMessage("Last")));
        box = new JComboBox<>(jmri.InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames());
        box.addItem("");
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.SPEEDCOL).setCellEditor(new DefaultCellEditor(box));
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.PERMISSIONCOL).setCellRenderer(
                new MyBooleanRenderer(Bundle.getMessage("Permissive"), Bundle.getMessage("Absolute")));
        _oBlockTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) { // for macOS, Linux
                showPopup(me);
            }

            @Override
            public void mouseReleased(MouseEvent me) { // for Windows
                showPopup(me);
            }
        });

        for (int i = 0; i < _oBlockModel.getColumnCount(); i++) {
            int width = _oBlockModel.getPreferredWidth(i);
            _oBlockTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        _oBlockTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setRowHeight(9 * (new JButton().getPreferredSize().height)/10);

        _oBlockTable.setRowHeight(ROW_HEIGHT);
        _oBlockTable.setPreferredScrollableViewportSize(new java.awt.Dimension(maxWidth,
                Math.min(TableFrames.ROW_HEIGHT * 10, maxHeight)));
        //_blockTablePane = new JScrollPane(_oBlockTable);

        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.REPORTERCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.REPORT_CURRENTCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.PERMISSIONCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.WARRANTCOL), false);
        // tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.ERR_SENSORCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.CURVECOL), false);

        return _oBlockTable;
    }

    private void showPopup(MouseEvent me) {
        Point p = me.getPoint();
        int col = _oBlockTable.columnAtPoint(p);
        if (!me.isPopupTrigger() && !me.isMetaDown() && !me.isAltDown() && col == OBlockTableModel.STATECOL) {
            int row = _oBlockTable.rowAtPoint(p);
            String stateStr = (String) _oBlockModel.getValueAt(row, col);
            int state = Integer.parseInt(stateStr, 2);
            stateStr = OBlockTableModel.getValue(state);
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(new JMenuItem(stateStr));
            popupMenu.show(_oBlockTable, me.getX(), me.getY());
        }
    }

    /*
     * ********************* PortalFrame *****************************
     */
    protected JTable makePortalTable(PortalTableModel model) {
        _portalTable = new JTable(model);
        TableRowSorter<PortalTableModel> sorter = new TableRowSorter<>(model);
        _portalTable.setRowSorter(sorter);
        _portalTable.setTransferHandler(new jmri.util.DnDTableImportExportHandler(new int[]{PortalTableModel.DELETE_COL}));
        _portalTable.setDragEnabled(true);

        _portalTable.getColumnModel().getColumn(PortalTableModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
        _portalTable.getColumnModel().getColumn(PortalTableModel.DELETE_COL).setCellRenderer(new ButtonRenderer());
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            _portalTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        _portalTable.sizeColumnsToFit(-1);
        int tableWidth = _portalTable.getPreferredSize().width;
        _portalTable.setRowHeight(ROW_HEIGHT);
        _portalTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth, TableFrames.ROW_HEIGHT * 10));
        return _portalTable;
    }

    /*
     * ********************* BlockPortalTable *****************************
     */
    protected JTable makeBlockPortalTable(BlockPortalTableModel model) {
        _blockPortalTable = new JTable(model);
        _blockPortalTable.setTransferHandler(new jmri.util.DnDTableExportHandler());
        _blockPortalTable.setDragEnabled(true);

        _blockPortalTable.setDefaultRenderer(String.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        _blockPortalTable.setDefaultEditor(String.class, new jmri.jmrit.symbolicprog.ValueEditor());
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            _blockPortalTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        _blockPortalTable.sizeColumnsToFit(-1);
        int tableWidth = _blockPortalTable.getPreferredSize().width;
        _blockPortalTable.setRowHeight(ROW_HEIGHT);
        _blockPortalTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth,
                Math.min(TableFrames.ROW_HEIGHT * 20, maxHeight)));

        return _blockPortalTable;
    }

    /*
     * ********************* SignalTable *****************************
     */
    protected JTable makeSignalTable(SignalTableModel model) {
        _signalTable = new JTable(model);
        TableRowSorter<SignalTableModel> sorter = new TableRowSorter<>(model);
        _signalTable.setRowSorter(sorter);
        _signalTable.setTransferHandler(new jmri.util.DnDTableImportExportHandler(
                new int[]{SignalTableModel.UNITSCOL, SignalTableModel.DELETE_COL}));
        _signalTable.setDragEnabled(true);

        _signalTable.getColumnModel().getColumn(SignalTableModel.UNITSCOL).setCellRenderer(
                new MyBooleanRenderer(Bundle.getMessage("cm"), Bundle.getMessage("in")));
        _signalTable.getColumnModel().getColumn(SignalTableModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
        _signalTable.getColumnModel().getColumn(SignalTableModel.DELETE_COL).setCellRenderer(new ButtonRenderer());
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = SignalTableModel.getPreferredWidth(i);
            _signalTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        _signalTable.sizeColumnsToFit(-1);
        int tableWidth = _signalTable.getPreferredSize().width;
        _signalTable.setRowHeight(ROW_HEIGHT);
        _signalTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth*2/3,
                Math.min(TableFrames.ROW_HEIGHT * 8, maxHeight)));

        return _signalTable;
    }

    /**
     * ********************* BlockPathFrame *****************************
     */
    protected static class BlockPathFrame extends JInternalFrame {

        BlockPathTableModel blockPathModel;

        BlockPathFrame(String title, boolean resizable, boolean closable,
                boolean maximizable, boolean iconifiable) {
            super(title, resizable, closable, maximizable, iconifiable);
        }

        BlockPathTableModel getModel() {
            return blockPathModel;
        }

        void setModel(BlockPathTableModel model, String blockName) {
            blockPathModel = model;
            setName(blockName);
        }
    }

    protected BlockPathFrame makeBlockPathFrame(OBlock block) {

        String title = Bundle.getMessage("TitleBlockPathTable", block.getDisplayName());
        BlockPathFrame frame = new BlockPathFrame(title, true, true, false, true);
        if (log.isDebugEnabled()) {
            log.debug("makeBlockPathFrame for OBlock {}", block.getDisplayName());
        }
        // get table
        BlockPathTableModel model = new BlockPathTableModel(block, this);
        JTable table = makeBlockPathTable(block, model);

        frame.setModel(model, block.getSystemName());

        JScrollPane tablePane = new JScrollPane(table);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        JLabel prompt = new JLabel(Bundle.getMessage("AddPathPrompt"));
        contentPane.add(prompt, BorderLayout.NORTH);
        contentPane.add(tablePane, BorderLayout.CENTER);

        frame.addInternalFrameListener(this);
        frame.setContentPane(contentPane);
        //frame.setClosable(true); // set in ctor
        frame.setLocation(50, 30);
        frame.pack();
        return frame;
    }

    /*
     * ********************* BlockPathTable *****************************
     */
    protected JTable makeBlockPathTable(OBlock block, BlockPathTableModel model) {
        JTable blockPathTable = new JTable(model);
        blockPathTable.setTransferHandler(new jmri.util.DnDTableImportExportHandler(new int[]{
            BlockPathTableModel.EDIT_COL, BlockPathTableModel.DELETE_COL, BlockPathTableModel.UNITSCOL}));
        blockPathTable.setDragEnabled(true);

        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.UNITSCOL).setCellRenderer(
                new MyBooleanRenderer(Bundle.getMessage("cm"), Bundle.getMessage("in")));
        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.EDIT_COL).setCellEditor(new ButtonEditor(new JButton()));
        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.EDIT_COL).setCellRenderer(new ButtonRenderer());
        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.DELETE_COL).setCellRenderer(new ButtonRenderer());

        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            blockPathTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        blockPathTable.sizeColumnsToFit(-1);
        int tableWidth = blockPathTable.getPreferredSize().width;
        blockPathTable.setRowHeight(ROW_HEIGHT);
        blockPathTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth,
                Math.min(TableFrames.ROW_HEIGHT * 10, maxHeight)));
        return blockPathTable;
    }

    /**
     * ********************* PathTurnoutFrame *****************************
     */
    protected static class PathTurnoutFrame extends JInternalFrame {

        /**
         * Remember the tableModel
         */
        PathTurnoutTableModel pathTurnoutModel;

        PathTurnoutFrame(String title, boolean resizable, boolean closable,
                boolean maximizable, boolean iconifiable) {
            super(title, resizable, closable, maximizable, iconifiable);
        }

        PathTurnoutTableModel getModel() {
            return pathTurnoutModel;
        }

        void setModel(PathTurnoutTableModel model) {
            pathTurnoutModel = model;
        }
    }

    /**
     * ********************* PathTurnoutPane *****************************
     */
    protected static class PathTurnoutPane extends JmriJFrame {

        /**
         * Remember the tableModel
         */
        PathTurnoutTableModel pathTurnoutModel;

        PathTurnoutPane(String title) {
            super(title);
        }

        PathTurnoutTableModel getModel() {
            return pathTurnoutModel;
        }

        void setModel(PathTurnoutTableModel model) {
            pathTurnoutModel = model;
        }
    }

    /*
     * ********************* PathTurnoutFrame *****************************
     */
    protected PathTurnoutFrame makePathTurnoutFrame(OBlock block, String pathName) {
        String title = Bundle.getMessage("TitlePathTurnoutTable", block.getDisplayName(), pathName);
        PathTurnoutFrame frame = new PathTurnoutFrame(title, true, true, false, true);
        if (log.isDebugEnabled()) {
            log.debug("makePathTurnoutFrame for Block {} and Path {}", block.getDisplayName(), pathName);
        }
        frame.setName(makePathTurnoutName(block.getSystemName(), pathName));
        OPath path = block.getPathByName(pathName);
        if (path == null) {
            return null;
        }
        //frame.init(path);
        PathTurnoutTableModel pathTurnoutModel = new PathTurnoutTableModel(path, frame);
        frame.setModel(pathTurnoutModel);
        //PathTurnoutTableModel PathTurnoutModel = frame.getModel();

        JTable pathTurnoutTable = makePathTurnoutTable(pathTurnoutModel);

        JScrollPane tablePane = new JScrollPane(pathTurnoutTable);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        JLabel prompt = new JLabel(Bundle.getMessage("AddTurnoutPrompt"));
        contentPane.add(prompt, BorderLayout.NORTH);
        contentPane.add(tablePane, BorderLayout.CENTER);

        frame.addInternalFrameListener(this);
        frame.setContentPane(contentPane);
        //frame.setClosable(true); // set in ctor
        frame.setLocation(10, 270);
        frame.pack();
        return frame;
    }

    /*
     * ********************* PathTurnoutTable *****************************
     */
    protected JTable makePathTurnoutTable(PathTurnoutTableModel model) {
        JTable PathTurnoutTable = new JTable(model);
        PathTurnoutTable.setTransferHandler(new jmri.util.DnDTableImportExportHandler(
                new int[]{PathTurnoutTableModel.STATE_COL, PathTurnoutTableModel.DELETE_COL}));
        PathTurnoutTable.setDragEnabled(true);

        model.configTurnoutStateColumn(PathTurnoutTable); // use real combo
        //JComboBox<String> box = new JComboBox<>(model.turnoutStates);
        //PathTurnoutTable.getColumnModel().getColumn(PathTurnoutTableModel.STATE_COL).setCellEditor(new DefaultCellEditor(box));
        PathTurnoutTable.getColumnModel().getColumn(PathTurnoutTableModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
        PathTurnoutTable.getColumnModel().getColumn(PathTurnoutTableModel.DELETE_COL).setCellRenderer(new ButtonRenderer());
        //PathTurnoutTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            PathTurnoutTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        PathTurnoutTable.sizeColumnsToFit(-1);
        int tableWidth = PathTurnoutTable.getPreferredSize().width;
        PathTurnoutTable.setRowHeight(ROW_HEIGHT);
        PathTurnoutTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth,
            Math.min(TableFrames.ROW_HEIGHT * 5, maxHeight)));

        return PathTurnoutTable;
    }

    /*
     * ********************* end of InternalFrame definitions *****************************
     */

//    /**
//     * Create and configure a new table using the given model and row sorter.
//     * Adapted from BeanTableDataModel for special OBlockxyz tables. 10/2020
//     *
//     * @param name   the name of the table
//     * @param model  the data model for the table
//     * @param sorter the row sorter for the table; if null, the table will not
//     *               be sortable
//     * @return the table
//     * @throws NullPointerException if name or model is null
//     */
//    public JTable makeJTable(@Nonnull String name, @Nonnull TableModel model, @CheckForNull RowSorter<? extends TableModel> sorter) {
//        Objects.requireNonNull(name, "the table name must be nonnull");
//        Objects.requireNonNull(model, "the table model must be nonnull");
//        return this.configureJTable(name, new JTable(model), sorter);
//    }

    /**
     * Open a block-specific Block-Path table.
     *
     * @param sysName of the OBlock
     */
    protected void openBlockPathFrame(String sysName) {
        BlockPathFrame frame = _blockPathMap.get(sysName);
        if (frame == null) {
            OBlock block = InstanceManager.getDefault(OBlockManager.class).getBySystemName(sysName);
            if (block == null) {
                return;
            }
            frame = makeBlockPathFrame(block);
            // wrap a table in a frame

            _blockPathMap.put(sysName, frame);
            frame.setVisible(true);
            _desktop.add(frame);
        } else {
            frame.setVisible(true);
            try {
                frame.setIcon(false);
            } catch (PropertyVetoException pve) {
                log.warn("BlockPath Table Frame for \"{}\" vetoed setIcon {}", sysName, pve);
            }
        }
        frame.moveToFront();
    }

    protected void disposeBlockPathFrame(OBlock block) {
        BlockPathFrame frame = _blockPathMap.get(block.getSystemName());
        frame.getModel().removeListener();
        _blockPathMap.remove(block.getSystemName());
        frame.dispose();
    }

    protected String makePathTurnoutName(String blockSysName, String pathName) {
        return "%" + pathName + "&" + blockSysName;
    }

    protected void openPathTurnoutFrame(String pathTurnoutName) {
        PathTurnoutFrame frame = _PathTurnoutMap.get(pathTurnoutName);
        log.debug("openPathTurnoutFrame for {}", pathTurnoutName);
        if (frame == null) {
            int index = pathTurnoutName.indexOf('&');
            String pathName = pathTurnoutName.substring(1, index);
            String sysName = pathTurnoutName.substring(index + 1);
            OBlock block = InstanceManager.getDefault(OBlockManager.class).getBySystemName(sysName);
            if (block == null) {
                return;
            }
            frame = makePathTurnoutFrame(block, pathName);
            if (frame == null) {
                return;
            }
            _PathTurnoutMap.put(pathTurnoutName, frame);
            frame.setVisible(true);
            _desktop.add(frame);
            frame.moveToFront();
        } else {
            frame.setVisible(true);
            try {
                frame.setIcon(false);
            } catch (PropertyVetoException pve) {
                log.warn("PathTurnout Table Frame for \"{}\" vetoed setIcon {}", pathTurnoutName, pve);
            }
            frame.moveToFront();
        }
    }

    static class MyBooleanRenderer extends javax.swing.table.DefaultTableCellRenderer {

        String _trueValue;
        String _falseValue;

        MyBooleanRenderer(String trueValue, String falseValue) {
            _trueValue = trueValue;
            _falseValue = falseValue;
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            JLabel val;
            if (value instanceof Boolean) {
                if (((Boolean) value)) {
                    val = new JLabel(_trueValue);
                } else {
                    val = new JLabel(_falseValue);
                }
            } else {
                val = new JLabel("");
            }
            val.setFont(table.getFont().deriveFont(java.awt.Font.PLAIN));
            return val;
        }
    }

    protected int verifyWarning(String message) {
        int val = 0;
        if (_showWarnings) {
            // verify deletion
            val = JOptionPane.showOptionDialog(null,
                    message, Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonYesPlus"),
                        Bundle.getMessage("ButtonNo")},
                    Bundle.getMessage("ButtonNo")); // default choice = No
            if (val == 1) { // suppress future warnings
                _showWarnings = false;
            }
        }
        return val;
    }

    /*
     * ********************* InternalFrameListener implementation *****************
     */
    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        JInternalFrame frame = (JInternalFrame)e.getSource();
        log.debug("Internal frame closing: {}", frame.getTitle());
        if (frame.getTitle().equals(Bundle.getMessage("TitleBlockTable"))) {
            showHideFrame(_blockTableFrame, openBlock, "OpenBlockMenu");
        }
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
        JInternalFrame frame = (JInternalFrame) e.getSource();
        String name = frame.getName();
        if (log.isDebugEnabled()) {
            log.debug("Internal frame closed: {}, name= {} size ({}, {})",
                    frame.getTitle(), name,
                    frame.getSize().getWidth(), frame.getSize().getHeight());
        }
        if (name != null && name.startsWith("OB")) {
            _blockPathMap.remove(name);
            if (frame instanceof BlockPathFrame) {
                String msg = WarrantTableAction.getDefault().checkPathPortals(((BlockPathFrame) frame).getModel().getBlock());
                if (!msg.isEmpty()) {
                    JOptionPane.showMessageDialog(this, msg,
                            Bundle.getMessage("InfoTitle"), JOptionPane.INFORMATION_MESSAGE);
                }
                ((BlockPathFrame) frame).getModel().removeListener();
            }
        } else {
            if (frame instanceof PathTurnoutFrame) {
                ((PathTurnoutFrame) frame).getModel().removeListener();
            }
            _PathTurnoutMap.remove(name);
        }
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {
        /*  JInternalFrame frame = (JInternalFrame)e.getSource();
         if (log.isDebugEnabled()) {
             log.debug("Internal frame Opened: {}, name= {} size ({}, {})",
                    frame.getTitle(), frame.getName(),
                    frame.getSize().getWidth(), frame.getSize().getHeight());
          }*/
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {
        JInternalFrame frame = (JInternalFrame) e.getSource();
        String name = frame.getName();
        if (log.isDebugEnabled()) {
            log.debug("Internal frame Iconified: {}, name= {} size ({}, {})",
                    frame.getTitle(), name,
                    frame.getSize().getWidth(), frame.getSize().getHeight());
        }
        if (name != null && name.startsWith(oblockPrefix())) {
            if (frame instanceof BlockPathFrame) {
                String msg = WarrantTableAction.getDefault().checkPathPortals(((BlockPathFrame) frame).getModel().getBlock());
                JOptionPane.showMessageDialog(this, msg,
                        Bundle.getMessage("InfoTitle"), JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {
        //JInternalFrame frame = (JInternalFrame)e.getSource();
        //log.debug("Internal frame deiconified: {}", frame.getTitle());
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
        //JInternalFrame frame = (JInternalFrame)e.getSource();
        //log.debug("Internal frame activated: {}", frame.getTitle());
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
        //JInternalFrame frame = (JInternalFrame)e.getSource();
        //log.debug("Internal frame deactivated: {}", frame.getTitle());
    }

    private final static Logger log = LoggerFactory.getLogger(TableFrames.class);

}
