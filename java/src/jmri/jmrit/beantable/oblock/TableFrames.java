package jmri.jmrit.beantable.oblock;

import java.awt.*;
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
import jmri.util.table.ToggleButtonEditor;
import jmri.util.table.ToggleButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to define OBlocks.
 * <p>
 * Core code can be used with two interfaces:
 * <ul>
 *     <li>original "desktop" InternalFrames (parent class TableFrames, an extended JmriJFrame)
 *     <li>JMRI standard Tabbed tables (parent class JPanel, the TableFrames JmriJFrame is hidden in UI)
 * </ul>
 * The _tabbed field decides, it is set in prefs (restart required).
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

    private final boolean _tabbed; // updated from prefs (restart required)

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
//    private final HashMap<String, BlockPathJPanel> _blockPathJMap = new HashMap<>(); // same, for _tabbed
    private final HashMap<String, PathTurnoutFrame> _pathTurnoutMap = new HashMap<>();
//    private final HashMap<String, PathTurnoutPanel> _pathTurnoutJMap = new HashMap<>(); // same, for _tabbed

    public TableFrames() {
        this("Hidden");
    } // NOI18N, title will be updated when !_tabbed, or is hidden

    public TableFrames(String actionName) {
        //super(actionName);
        _tabbed = InstanceManager.getDefault(GuiLafPreferencesManager.class).isOblockEditTabbed();
        this.setVisible(!_tabbed); // also hide some stray separate _desktop panel, not "this"
        super.setVisible(!_tabbed);

        // create the tables
        _oBlockModel = new OBlockTableModel(this);
        _portalModel = new PortalTableModel(this);
        _blockPortalXRefModel = new BlockPortalTableModel(_oBlockModel);
        _signalModel = new SignalTableModel(this);
        _signalModel.init();
    }

    public OBlockTableModel getOblockTableModel() {
        return _oBlockModel;
    }
    public PortalTableModel getPortalTableModel() {
        return _portalModel;
    }
    public BlockPortalTableModel getPortalXRefTableModel() {
        return _blockPortalXRefModel;
    }
    public BlockPathTableModel getBlockPathTableModel(OBlock block) {
        return new BlockPathTableModel(block, this);
    }
    public SignalTableModel getSignalTableModel() {
        return _signalModel;
    }

    @Override
    public void initComponents() {
        // build and display the classic floating "OBlock and its..." desktop interface
        if (!_tabbed) { // just to be sure
            // ???? runs twice, might explain the extra pane
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

//    /**
//     * For test setup
//     * @param tabbed true for tabbed interface, false for original _desktop
//     */
//    public void setTabbed(boolean tabbed) {
//        _tabbed = tabbed;
//    }

    public JMenuBar addMenus(JMenuBar mBar) {
        if (mBar == null) {
            mBar = new JMenuBar();
        }
        // create and add the menus
        if (!_tabbed) { // _tabbed Print is handled via getPrintItem()
            // File menu
            JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
            fileMenu.add(new jmri.configurexml.StoreMenu());

            mBar.add(addPrintMenuItems(fileMenu)); // first adds the print items

            // Edit menu
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
        }

        mBar.add(getOptionMenu());

        // Tables menu
        tablesMenu = new JMenu(Bundle.getMessage("OpenMenu"));
        updateOBlockTablesMenu(); // replaces the last 2 menu items with appropriate submenus
        mBar.add(tablesMenu);
        return mBar; //new JMenu[]{fileMenu, optionMenu, tablesMenu};
    }

    private JMenu addPrintMenuItems(JMenu fileMenu) {
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
        return fileMenu;
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
    }

    public JMenu getOptionMenu() {
        // Options menu
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
        return optionMenu;
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
                    log.debug("Start loop: Path {} on Block {}", n, oBlockName);
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

    protected void updateOBlockTablesMenu() {
        // fill Table menu
        if (tablesMenu == null) {
            return; // TODO add menu to _tabbed interface, now only used in _desktop EBR
        }
        tablesMenu.removeAll();
        // use string Bundle.getMessage("HideTable") to correct action in menu for all table open at start

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

        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);

        // Block-Path submenus
        JMenu openBlockPath = new JMenu(Bundle.getMessage("OpenBlockPathMenu"));
        ActionListener openFrameAction = e -> { //  works for both interfaces
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

        // Path-Turnout submenus
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
                    if (_tabbed) {
                        // openPathTurnoutPanel(pathTurnoutName);
                    } else {
                        openPathTurnoutEditPane(pathTurnoutName);
                    }
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

    public void openPathTurnoutEditPane(String pathTurnoutName) {
        // only used for desktop
//        if (_tabbed) { // just to be sure
//            log.debug("openPathTurnoutEditPane for {}", pathTurnoutName);
//            // not stored separately as bean
//            PathTurnoutPanel panel = _pathTurnoutEditJMap.get(pathTurnoutName);
//            if (panel == null) { // create a new pane TODO embed in PathEditPanel, then this is not used
//                int index = pathTurnoutName.indexOf('&');
//                String pathName = pathTurnoutName.substring(1, index);
//                String sysName = pathTurnoutName.substring(index + 1);
//                OBlock block = InstanceManager.getDefault(OBlockManager.class).getBySystemName(sysName);
//                if (block == null) {
//                    log.error("null OBlock {}", pathTurnoutName);
//                } else {
//                    String title = Bundle.getMessage("TitlePathTurnoutTable", block.getDisplayName(), pathName);
//                    if (log.isDebugEnabled()) {
//                        log.debug("makePathTurnoutEditPane for Block {} and Path {}", block.getDisplayName(), pathName);
//                    }
//
//                    frame = new JmriJFrame(title); // L1157
//                        PathTurnoutPanel pathturnoutpanel = new PathTurnoutPanel(title);
//                    frame.setName(makePathTurnoutName(block.getSystemName(), pathName));
//                    OPath path = block.getPathByName(pathName);
//                    if (path == null) {
//                        log.error("Null path Turnout");
//                    }
//                    // create the pane for _tabbed interface
//                    frame.addHelpMenu("package.jmri.jmrit.beantable.oblock.OBlockTable", true);
//                    panel.setLayout(new BoxLayout(panel.getContentPane(), BoxLayout.Y_AXIS));
//                    panel.setPreferredSize(new Dimension(200, 100));
//                    // create table in JPanel

//                    PathTurnoutTableModel pathTurnoutModel = new PathTurnoutTableModel(path, panel);
//                    panel.setModel(pathTurnoutModel);
//                    JTable pathTurnoutTable = makePathTurnoutTable(pathTurnoutModel);
//
//                    JScrollPane tablePane = new JScrollPane(pathTurnoutTable);
//
//                    JPanel p = new JPanel();
//                    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
//                    p.add(tablePane);
//                    panel.add(p);
//                    panel.setVisible(true);
//
//                    //_pathTurnoutEditJMap.put(pathTurnoutName, panel);
//
//                    // TODO add listeners, action, see PathTurnoutFrame()
//                    // plus Add New entry pane
//                }
//            }
//        } else { // stand alone frame only used for _desktop, created from/stored in Portal
            openPathTurnoutFrame(pathTurnoutName);
//        }
    }

    /**
     * Show or hide a table in the _desktop interface.
     *
     * @param frame JInternalFrame to show (or hide, name property value contains {} var handled by frame)
     * @param menu menu item object
     * @param menuName base i18n string containing table name
     */
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
     * Wrapper for shared code around each Table in a JInternal window on _desktop interface.
     *
     * @param tableModel underlying model for the table
     * @param title text displayed as title of frame
     * @param prompt text below bottom line
     * @return iframe to put on _desktop interface
     */
    protected JInternalFrame buildFrame(AbstractTableModel tableModel, String title, String prompt) {
        JInternalFrame iframe = new JInternalFrame(title, true, false, false, true);

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
        } // no case for BlockPathTableModel, is handled directly from OBlockTable

        JScrollPane scroll = new JScrollPane(table);
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        JLabel _prompt = new JLabel(prompt);
        contentPane.add(_prompt, BorderLayout.NORTH);
        contentPane.add(scroll, BorderLayout.CENTER);

        iframe.setContentPane(contentPane);
        iframe.pack();
        return iframe;
    }

    /*
     * ********************* OBlock Table for _desktop ****************
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
                new ToggleButtonRenderer(Bundle.getMessage("cm"), Bundle.getMessage("in")));
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.UNITSCOL).setCellEditor(
                new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("cm"), Bundle.getMessage("in")));
        JComboBox<String> box = new JComboBox<>(OBlockTableModel.curveOptions);
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.CURVECOL).setCellEditor(new DefaultCellEditor(box));
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.REPORT_CURRENTCOL).setCellRenderer(
                new ToggleButtonRenderer(Bundle.getMessage("Current"), Bundle.getMessage("Last")));
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.REPORT_CURRENTCOL).setCellEditor(
                new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("Current"), Bundle.getMessage("Last")));
        box = new JComboBox<>(jmri.InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames());
        box.addItem("");
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.SPEEDCOL).setCellEditor(new DefaultCellEditor(box));
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.PERMISSIONCOL).setCellRenderer(
                new ToggleButtonRenderer(Bundle.getMessage("Permissive"), Bundle.getMessage("Absolute")));
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.PERMISSIONCOL).setCellEditor(
                new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("Permissive"), Bundle.getMessage("Absolute")));
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

        setRowHeight((new JButton().getPreferredSize().height)*9/10);

        _oBlockTable.setRowHeight(ROW_HEIGHT);
        _oBlockTable.setPreferredScrollableViewportSize(new java.awt.Dimension(maxWidth,
                Math.min(TableFrames.ROW_HEIGHT * 10, maxHeight)));

        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.REPORTERCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.REPORT_CURRENTCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.PERMISSIONCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.WARRANTCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.ERR_SENSORCOL), false);
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

    // Opens the Edit OBlock panel for _tabbed
    protected boolean openOBlockEditor(String sysName) {
        int result = 0;
        if (sysName != null) {
            // this is for Edit, new OBlocks created from [Add OBlock...] button in table
            OBlock block = InstanceManager.getDefault(OBlockManager.class).getBySystemName(sysName);
            if (block != null){
                BlockPathJPanel panel = makeBlockPathEditPanel(block);
                // Option 1: our own panel EBR
    //            OBlockEditFrame obef = new OBlockEditFrame(Bundle.getMessage("TitleBlockEditor", sysName), sysName,
    //                    _oBlockModel, panel, this);
    //            obef.setVisible(true);
                // Option 2: use BeanEdit UI, adapted from jmri.jmrit.beantable.BlockTableAction
                class WindowMaker implements Runnable {
                    final OBlock ob;
                    final BlockPathJPanel panel;
                    WindowMaker(OBlock ob, BlockPathJPanel panel) {
                        this.ob = ob;
                        this.panel = panel;
                    }
                    @Override
                    public void run() {
                        jmri.jmrit.beantable.beanedit.OBlockEditAction beanEdit = new jmri.jmrit.beantable.beanedit.OBlockEditAction();
                        beanEdit.setBean(ob);
                        beanEdit.setTablePanel(panel);
                        beanEdit.actionPerformed(null);
                    }
                }
                WindowMaker t = new WindowMaker(block, panel);
                javax.swing.SwingUtilities.invokeLater(t);
                log.debug("path table created for block {}", block);
                result = 1;
            }
        }
        return (result == 1);
    }

    // Opens the Edit Path panel for _tabbed, compare with openOBlockEditor()
    protected boolean openPathEditor(String blockSysName, @CheckForNull String pathName) {
        int result = 0;
        if (blockSysName != null) {
            OBlock block = InstanceManager.getDefault(OBlockManager.class).getBySystemName(blockSysName);
            if (block == null) {
                log.error("OBlock {} not found", blockSysName);
                return false;
            }
            BlockPathEditFrame bpef = null;
            PathTurnoutJPanel table = makePathTurnoutPanel(block, pathName); // shows the turnouts on path, includes Add button
            if (pathName == null) { // new Path
                // new Path is created from [Add Path...] button in Path table on OBlock Editor pane.
                bpef = new BlockPathEditFrame(("Create New Path on OBlock " + blockSysName), block, null, table, this);
            } else {
                OPath path = block.getPathByName(pathName);
                //PathTurnoutJPanel table = makePathTurnoutPanel(block, pathName); // shows the turnouts on path, includes Add button
                bpef = new BlockPathEditFrame(("Edit Path " + pathName + " on OBlock " + blockSysName), block, path, table, this);
//                class WindowMaker implements Runnable {
//                    final OBlock ob;
//                    final BlockPathJPanel panel;
//                    WindowMaker(OBlock ob, BlockPathJPanel panel) {
//                        this.ob = ob;
//                        this.panel = panel;
//                    }
//                    @Override
//                    public void run() {
//                        jmri.jmrit.beantable.beanedit.OBlockEditAction beanEdit = new jmri.jmrit.beantable.beanedit.OBlockEditAction();
//                    }
//                }
//                WindowMaker t = new WindowMaker(block, panel);
//                javax.swing.SwingUtilities.invokeLater(t);
            }
            bpef.setVisible(true);
            log.debug("Path editor created for path {} on block {}", pathName, blockSysName);
            result = 1;
        }
        return (result == 1);
    }

    /*
     * ********************* PortalTable for _desktop *****************************
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
        _portalTable.doLayout();
        int tableWidth = _portalTable.getPreferredSize().width;
        _portalTable.setRowHeight(ROW_HEIGHT);
        _portalTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth, TableFrames.ROW_HEIGHT * 10));
        return _portalTable;
    }

    /*
     * ********************* Block-Portal (XRef) Table for _desktop *****************************
     */
    protected JTable makeBlockPortalTable(BlockPortalTableModel model) {
        _blockPortalTable = new JTable(model);
        _blockPortalTable.setTransferHandler(new jmri.util.DnDTableExportHandler());
        _blockPortalTable.setDragEnabled(true);

        _blockPortalTable.setDefaultRenderer(String.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        _blockPortalTable.setDefaultEditor(String.class, new jmri.jmrit.symbolicprog.ValueEditor()); // useful on non-editable cell?
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            _blockPortalTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        _blockPortalTable.doLayout();
        _blockPortalTable.setRowHeight(ROW_HEIGHT);
        _blockPortalTable.setPreferredScrollableViewportSize(new java.awt.Dimension(maxWidth/3,
                Math.min(TableFrames.ROW_HEIGHT * 20, maxHeight)));

        return _blockPortalTable;
    }

    /*
     * ********************* Signal Table for _desktop *****************************
     */
    protected JTable makeSignalTable(SignalTableModel model) {
        _signalTable = new JTable(model);
        TableRowSorter<SignalTableModel> sorter = new TableRowSorter<>(model);
        _signalTable.setRowSorter(sorter);
        _signalTable.setTransferHandler(new jmri.util.DnDTableImportExportHandler(
                new int[]{SignalTableModel.UNITSCOL, SignalTableModel.DELETE_COL}));
        _signalTable.setDragEnabled(true);

        _signalTable.getColumnModel().getColumn(SignalTableModel.UNITSCOL).setCellRenderer(
                new ToggleButtonRenderer(Bundle.getMessage("cm"), Bundle.getMessage("in")));
        _signalTable.getColumnModel().getColumn(SignalTableModel.UNITSCOL).setCellEditor(
                new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("cm"), Bundle.getMessage("in")));
        _signalTable.getColumnModel().getColumn(SignalTableModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
        _signalTable.getColumnModel().getColumn(SignalTableModel.DELETE_COL).setCellRenderer(new ButtonRenderer());
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = SignalTableModel.getPreferredWidth(i);
            _signalTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        _signalTable.doLayout();
        int tableWidth = _signalTable.getPreferredSize().width;
        _signalTable.setRowHeight(ROW_HEIGHT);
        _signalTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth*2/3,
                Math.min(TableFrames.ROW_HEIGHT * 8, maxHeight)));

        return _signalTable;
    }

    /*
     * ***************** end of permanent Tables + InternalFrame definitions *****************
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

    /*
     * ***************** On Demand Tables + InternalFrame definitions *****************
     */

    /*
     * ********************* Block-Path Frame *****************************
     */

    // called from menu
    public void openBlockPathPane(String blockSysName) {
        if (_tabbed) {
            openOBlockEditor(blockSysName); // pass on to Per OBlock Edit panel, includes BlockPath table
        } else {
            openBlockPathFrame(blockSysName); // an editable table of all paths on this block
        }
    }

    // ***************** Block-Path Frame for _desktop **************************
    /**
     * Open a block-specific Block-Path table in _desktop interface. (see openBlockPathEditFrame)
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
            // store frame in Map
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

//    /**
//     * Open a block-specific Block-Path table pane in _tabbed interface (see openBlockPathFrame).
//     * no longer needed after table is embedded on EditOBlock pane. EBR
//     *
//     * @param blockSysName of the OBlock
//     */
//    protected void openBlockPathEditFrame(String blockSysName) {
//        JmriJFrame frame = new JmriJFrame("DEMO"); //= _blockPathJMap.get(blockSysName); // TODO regenerate each use? trash _blockPathJMap
//            OBlock block = InstanceManager.getDefault(OBlockManager.class).getBySystemName(blockSysName);
//            if (block == null) {
//                return;
//            }
//            BlockPathJPanel panel = makeBlockPathEditPanel(block);
//            panel.setSize(350, 200);
//
//            frame.add(panel);
//            frame.setVisible(true);
////        }
//    }

    // common dispose
    protected void disposeBlockPathFrame(OBlock block) {
        if (_tabbed) {
            BlockPathFrame frame = _blockPathMap.get(block.getSystemName());
            frame.getModel().removeListener();
            _blockPathMap.remove(block.getSystemName());
            frame.dispose();
        } else {
            BlockPathFrame frame = _blockPathMap.get(block.getSystemName());
            frame.getModel().removeListener();
            _blockPathMap.remove(block.getSystemName());
            frame.dispose();
        }
    }

    // *************** Block-Path InternalFrame for _desktop ***********************

    protected BlockPathFrame makeBlockPathFrame(OBlock block) {
        String title = Bundle.getMessage("TitleBlockPathTable", block.getDisplayName());
        // create table
        BlockPathTableModel model = new BlockPathTableModel(block, this);
        JPanel contentPane = makeBlockPathTablePanel(model);

        BlockPathFrame frame = new BlockPathFrame(title, true, true, false, true);
        frame.setModel(model, block.getSystemName());
        frame.addInternalFrameListener(this);
        frame.setContentPane(contentPane);
        //frame.setClosable(true); // set in ctor
        frame.setLocation(50, 30);
        frame.pack();
        return frame;
    }

    // *************** Block-Path Edit Panel for _tabbed ***********************

    protected BlockPathJPanel makeBlockPathEditPanel(OBlock block) { // still in use? EBR
        String title = Bundle.getMessage("TitleBlockPathEditor", block.getDisplayName());
        // create table
        BlockPathTableModel model = new BlockPathTableModel(block, this);
        JPanel bpTable = makeBlockPathTablePanel(model);
        // style table
        setRowHeight((new JButton().getPreferredSize().height)*9/10);
        // units, etc renderer TODO EBR

        BlockPathJPanel panel = new BlockPathJPanel(title);
        panel.setModel(model, block.getSystemName());

        panel.add(bpTable);

        // Add Path Button
        JPanel tblButtons = new JPanel();
        tblButtons.setLayout(new BorderLayout(10, 10));
        tblButtons.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        tblButtons.setLayout(new BoxLayout(tblButtons, BoxLayout.Y_AXIS));

        JButton addButton = new JButton(Bundle.getMessage("ButtonAddPath"));
        ActionListener addPathAction = e -> {
            //addPathPane(block); //
            openPathEditor(block.getDisplayName(), null);
        };
        addButton.addActionListener(addPathAction);
        addButton.setToolTipText(Bundle.getMessage("AddPathTabbedPrompt"));
        tblButtons.add(addButton);
        // add [Add Path...] button to frame
        panel.add(tblButtons);

        //panel.pack();
        return panel;
    }

    // ***************** Block-Path Frame class for _desktop **************************
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

    // ***************** Block-Path JPanel class for _tabbed **************************
    public static class BlockPathJPanel extends JPanel {

        BlockPathTableModel blockPathModel;

        BlockPathJPanel(String title) {
            super();
            super.setName(title);

            // Todo add more panes, Add button?
        }

        BlockPathTableModel getModel() {
            return blockPathModel;
        }

        void setModel(BlockPathTableModel model, String blockName) {
            blockPathModel = model;
            setName(blockName);
        }
    }

    /*
     * ********************* Block-Path Table Panel for _tabbed *****************************
     */
    protected JPanel makeBlockPathTablePanel(BlockPathTableModel _model) {
        JTable blockPathTable = makeBlockPathTable(_model);

        // get table
        JScrollPane tablePane = new JScrollPane(blockPathTable);
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        if (_tabbed) {
            blockPathTable.setPreferredScrollableViewportSize(new Dimension(480, 80));
            //JLabel prompt = new JLabel(Bundle.getMessage("AddPathTabbedPrompt"));

//            JPanel buttonPane = new JPanel();
//            buttonPane.setLayout(new BorderLayout(10, 10));
//            buttonPane.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
//            buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));
            //buttonPane.add(prompt);
//            contentPane.add(buttonPane, BorderLayout.SOUTH);
//            JButton addButton = new JButton(Bundle.getMessage("ButtonAddPath"));
//            ActionListener addPathAction = e -> {
//                //String sysName = e.getActionCommand();
//                addPathPane(_model.getBlock());
//            };
//            addButton.addActionListener(addPathAction);
//            buttonPane.add(addButton);
            // TODO add more, like a button Add... to frame?
        } else {
            JLabel prompt = new JLabel(Bundle.getMessage("AddPathPrompt"));
            contentPane.add(prompt, BorderLayout.NORTH);
        }
        contentPane.add(tablePane, BorderLayout.CENTER);

        return contentPane;
    }

//    // not used, _tabbed New Path uses same pane as Edit Path
//    protected void addPathPane(OBlock block) {
//        JmriJFrame frame = new JmriJFrame("Add New Path for " + block.getDisplayName());
//        JPanel p = new JPanel();
//        // TODO add something useful here:
//        // Name
//        // FromBlock
//        JButton cancel;
//        p.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
//        cancel.addActionListener((ActionEvent e) -> {
//            frame.dispose();
//        });
//        frame.add(p);
//        frame.setSize(200, 150);
//        frame.pack();
//        frame.setVisible(true);
//    }

    protected JTable makeBlockPathTable(BlockPathTableModel _model) {
        JTable blockPathTable = new JTable(_model);
        // configure DnD
        blockPathTable.setTransferHandler(new jmri.util.DnDTableImportExportHandler(new int[]{BlockPathTableModel.EDIT_COL, BlockPathTableModel.DELETE_COL, BlockPathTableModel.UNITSCOL}));
        blockPathTable.setDragEnabled(true);
        // style table
        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.UNITSCOL).setCellRenderer(new ToggleButtonRenderer(Bundle.getMessage("cm"), Bundle.getMessage("in")));
        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.UNITSCOL).setCellEditor(new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("cm"), Bundle.getMessage("in")));
        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.EDIT_COL).setCellEditor(new ButtonEditor(new JButton()));
        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.EDIT_COL).setCellRenderer(new ButtonRenderer());
        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.DELETE_COL).setCellRenderer(new ButtonRenderer());

        for (int i = 0; i < _model.getColumnCount(); i++) {
            int width = _model.getPreferredWidth(i);
            blockPathTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        blockPathTable.doLayout();
        int tableWidth = blockPathTable.getPreferredSize().width;
        blockPathTable.setRowHeight(ROW_HEIGHT);
        blockPathTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth, Math.min(TableFrames.ROW_HEIGHT * 10, maxHeight)));

        return blockPathTable;
    }

    /**
     * ********************* Path-Turnout Frame ***********************************
     */

    // ********************* Path-Turnout Frame class for _desktop ****************
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
     * ********************* Path-Turnout JPanel class for _tabbed *****************
     */
    protected static class PathTurnoutJPanel extends JPanel {

        /**
         * Remember the tableModel
         */
        PathTurnoutTableModel pathTurnoutModel;

        PathTurnoutJPanel(String name) {
            super();
            setName(name);
        }

        PathTurnoutTableModel getModel() {
            return pathTurnoutModel;
        }

        void setModel(PathTurnoutTableModel model) {
            pathTurnoutModel = model;
        }
    }

    /*
     * ********************* Path-TurnoutFrame for _desktop *************************
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
        //frame.setClosable(true); // is set in ctor
        frame.setLocation(10, 270);
        frame.pack();
        return frame;
    }

    /*
     * ********************* Path-TurnoutPanel for _tabbed *****************************
     */
    protected PathTurnoutJPanel makePathTurnoutPanel(@Nonnull OBlock block, @CheckForNull String pathName) {
        String title = Bundle.getMessage("TitlePathTurnoutTable", block.getDisplayName(), pathName);
        PathTurnoutJPanel panel = new PathTurnoutJPanel(title);
        PathTurnoutTableModel pathTurnoutModel;
        JTable pathTurnoutTable;
        if (pathName == null) {
            panel.setName(makePathTurnoutName(block.getSystemName(), "<new Path>"));
            pathTurnoutTable = new JTable(1, 1); // dummy table
        } else {
            panel.setName(makePathTurnoutName(block.getSystemName(), pathName));
            OPath path = block.getPathByName(pathName);
            if (path == null) {
                return null; // unexpected
            }
            pathTurnoutModel = new PathTurnoutTableModel(path);
            panel.setModel(pathTurnoutModel);
            pathTurnoutTable = makePathTurnoutTable(pathTurnoutModel);
        }
        JScrollPane tablePane = new JScrollPane(pathTurnoutTable);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        JLabel prompt = new JLabel(Bundle.getMessage("AddTurnoutPrompt"));
        contentPane.add(prompt, BorderLayout.NORTH);
        contentPane.add(tablePane, BorderLayout.CENTER);

        return panel;
    }

    /*
     * ********************* Path-Turnout Table *****************************
     */
    protected JTable makePathTurnoutTable(PathTurnoutTableModel model) {
        JTable PathTurnoutTable = new JTable(model);
        PathTurnoutTable.setTransferHandler(new jmri.util.DnDTableImportExportHandler(
                new int[]{PathTurnoutTableModel.STATE_COL, PathTurnoutTableModel.DELETE_COL}));
        PathTurnoutTable.setDragEnabled(true);

        model.configTurnoutStateColumn(PathTurnoutTable); // use real combo
        PathTurnoutTable.getColumnModel().getColumn(PathTurnoutTableModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
        PathTurnoutTable.getColumnModel().getColumn(PathTurnoutTableModel.DELETE_COL).setCellRenderer(new ButtonRenderer());
        //PathTurnoutTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            PathTurnoutTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        PathTurnoutTable.doLayout();
        int tableWidth = PathTurnoutTable.getPreferredSize().width;
        PathTurnoutTable.setRowHeight(ROW_HEIGHT);
        PathTurnoutTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth,
            Math.min(TableFrames.ROW_HEIGHT * 5, maxHeight)));

        return PathTurnoutTable;
    }

    protected String makePathTurnoutName(String blockSysName, String pathName) {
        return "%" + pathName + "&" + blockSysName;
    }

    // ********************* Open Path-Turnout Frame for _desktop *****************************
    /**
     * Open a block-specific PathTurnouts table as a JInternalFrame for _desktop from BlockPathTableModel
     *
     * @param pathTurnoutName name of turnout configured on Path
     */
    protected void openPathTurnoutFrame(String pathTurnoutName) {
        PathTurnoutFrame frame = _pathTurnoutMap.get(pathTurnoutName);
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
            _pathTurnoutMap.put(pathTurnoutName, frame);
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

    protected void addTurnoutPane(OPath path) {
        JmriJFrame frame = new JmriJFrame("Add New Turnout for " + path.getName());
        JPanel p = new JPanel();

        // TODO add something useful here:
        // Name (from Combo)
        // State
        JButton cancel;
        p.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener((ActionEvent e) -> {
            frame.dispose();
        });
        frame.add(p);
        frame.setSize(200, 150);
        frame.pack();
        frame.setVisible(true);
    }


    /*
     * ********************* End of tables and frames methods *****************************
     */

//    public static class BooleanAsStringsRenderer extends javax.swing.table.DefaultTableCellRenderer {
//
//        String _trueValue;
//        String _falseValue;
//
//        public BooleanAsStringsRenderer(String trueValue, String falseValue) {
//            _trueValue = trueValue;
//            _falseValue = falseValue;
//        }
//
//        @Override
//        public java.awt.Component getTableCellRendererComponent(JTable table,
//                Object value, boolean isSelected,
//                boolean hasFocus, int row, int column) {
//
//            log.debug("BooStriRender for value: {}", value.toString());
//            JLabel val;
//            if (value instanceof Boolean) {
//                if (((Boolean) value)) {
//                    val = new JLabel(_trueValue);
//                } else {
//                    val = new JLabel(_falseValue);
//                }
//            } else {
//                val = new JLabel("");
//            }
//            val.setFont(table.getFont().deriveFont(java.awt.Font.PLAIN));
//            return val;
//        }
//    } // end of class

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
     * ********************* InternalFrameListener implementation for _desktop *****************
     */
    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        JInternalFrame frame = (JInternalFrame)e.getSource();
        log.debug("Internal frame closing: {}", frame.getTitle());
        if (frame.getTitle().equals(Bundle.getMessage("TitleBlockTable"))) {
            showHideFrame(_blockTableFrame, openBlock, "OpenBlockMenu");
        }
    }

    // clean up on close on _desktop
    // for _tabbed this is handled in the Edit pane applyPressed() method
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
            _pathTurnoutMap.remove(name);
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
