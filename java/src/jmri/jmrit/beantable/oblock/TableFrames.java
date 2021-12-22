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
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import jmri.*;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.jmrit.logix.WarrantTableAction;
import jmri.swing.NamedBeanComboBox;
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
 *     <li>original "desktop" InternalFrames (displays as InternalJFrames inside a JmriJFrame)
 *     <li>JMRI standard Tabbed tables (displays as Js inside a ListedTableFrame)
 * </ul>
 * The _tabbed field decides, it is set in prefs (restart required). TableFrames itself has no UI.
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
public class TableFrames implements InternalFrameListener {

    public static final int ROW_HEIGHT = (new JButton("X").getPreferredSize().height)*9/10;
    public static final int STRUT_SIZE = 10;
    protected static final String SET_CLOSED = jmri.InstanceManager.turnoutManagerInstance().getClosedText();
    protected static final String SET_THROWN = jmri.InstanceManager.turnoutManagerInstance().getThrownText();
    private static String oblockPrefix;
    private final static String portalPrefix = "IP";
    private String _title;

    private JTable _oBlockTable;
    private final OBlockTableModel _oBlockModel;
    private JTable _portalTable;
    private final PortalTableModel _portalModel;
    private JTable _blockPortalTable;
    private final BlockPortalTableModel _blockPortalXRefModel;
    private JTable _signalTable;
    private final SignalTableModel _signalModel;

    private final boolean _tabbed; // updated from prefs (restart required)
    private boolean pathEdit = false;

    private JmriJFrame desktopframe;
    private JDesktopPane _desktop;
    private final int maxHeight = 600;
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
    private JMenuItem _setUnits;

    private final HashMap<String, BlockPathFrame> _blockPathMap = new HashMap<>();
    private final HashMap<String, PathTurnoutFrame> _pathTurnoutMap = new HashMap<>();
    // _tabbed edit panes are not stored in a map

    public TableFrames() {
        this("OBlock Tables");
    } // NOI18N, title will be updated during init

    public TableFrames(String actionName) {
        _tabbed = InstanceManager.getDefault(GuiLafPreferencesManager.class).isOblockEditTabbed();
        _title = actionName;
        if (!_tabbed) {
            desktopframe = new JmriJFrame(actionName);
        }
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

    public void initComponents() {
        // build and display the classic floating "OBlock and its..." desktop interface
        if (!_tabbed) { // just to be sure
            setTitle(Bundle.getMessage("TitleOBlocks"));

            // build tables
            _blockTableFrame = buildFrame(_oBlockModel, Bundle.getMessage("TitleBlockTable"), Bundle.getMessage("AddBlockPrompt"));
            _blockTableFrame.setVisible(true);

            _portalTableFrame = buildFrame(_portalModel, Bundle.getMessage("TitlePortalTable"), Bundle.getMessage("AddPortalPrompt"));
            _portalTableFrame.setVisible(true);

            _signalTableFrame = buildFrame(_signalModel, Bundle.getMessage("TitleSignalTable"), Bundle.getMessage("AddSignalPrompt"));
            _signalTableFrame.setVisible(false);

            _blockPortalXRefFrame = buildFrame(_blockPortalXRefModel, Bundle.getMessage("TitleBlockPortalXRef"), Bundle.getMessage("XRefPrompt"));
            _blockPortalXRefFrame.setVisible(false); // start with frame hidden

            // build the print menu after the tables have been created
            desktopframe.setTitle(getTitle());
            desktopframe.setJMenuBar(addMenus(desktopframe.getJMenuBar()));
            desktopframe.addHelpMenu("package.jmri.jmrit.logix.OBlockTable", true);

            createDesktop(); // adds tables as windows on desktopframe._desktop
            desktopframe.setLocation(10, 30);
            desktopframe.setVisible(true);
            desktopframe.pack();
            addCloseListener(desktopframe);

            // finally check table contents for errors
            WarrantTableAction.getDefault().errorCheck();
        }
    }

    public JMenuBar addMenus(JMenuBar mBar) {
        if (mBar == null) {
            mBar = new JMenuBar();
        }
        // create and add the menus
        if (!_tabbed) { // _tabbed Print is handled via getPrintItem() in OBlockTablePanel
            // File menu
            JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
            fileMenu.add(new jmri.configurexml.StoreMenu());
            fileMenu.add(getPrintMenuItems(_oBlockTable, _portalTable, _signalTable, _blockPortalTable)); // add the print items
            mBar.add(fileMenu);

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
        mBar.add(getTablesMenu());
        return mBar;
    }

    public JMenu getPrintMenuItems(JTable oBlockTable, JTable portalTable, JTable signalTable, JTable blockPortalTable) {
        JMenu print = new JMenu(Bundle.getMessage("PrintTable"));
        JMenuItem printItem = new JMenuItem(Bundle.getMessage("PrintOBlockTable"));
        print.add(printItem);
        printItem.addActionListener(e -> {
            try {
                MessageFormat headerFormat = new MessageFormat(Bundle.getMessage("TitleOBlockTable"));
                MessageFormat footerFormat = new MessageFormat(getTitle() + " page {0,number}");
                oBlockTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);
            } catch (java.awt.print.PrinterException e1) {
                log.warn("error printing: {}", e1, e1);
            }
        });
        printItem = new JMenuItem(Bundle.getMessage("PrintPortalTable"));
        print.add(printItem);
        printItem.addActionListener(e -> {
            try {
                MessageFormat headerFormat = new MessageFormat(Bundle.getMessage("TitlePortalTable"));
                MessageFormat footerFormat = new MessageFormat(getTitle() + " page {0,number}");
                portalTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);
            } catch (java.awt.print.PrinterException e1) {
                log.warn("error printing: {}", e1, e1);
            }
        });
        printItem = new JMenuItem(Bundle.getMessage("PrintSignalTable"));
        print.add(printItem);
        printItem.addActionListener(e -> {
            try {
                MessageFormat headerFormat = new MessageFormat(Bundle.getMessage("TitleSignalTable"));
                MessageFormat footerFormat = new MessageFormat(getTitle() + " page {0,number}");
                signalTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);
            } catch (java.awt.print.PrinterException e1) {
                log.warn("error printing: {}", e1, e1);
            }
        });
        printItem = new JMenuItem(Bundle.getMessage("PrintXRef"));
        print.add(printItem);
        printItem.addActionListener(e -> {
            try {
                MessageFormat headerFormat = new MessageFormat(Bundle.getMessage("OpenXRefMenu", ""));
                MessageFormat footerFormat = new MessageFormat(getTitle() + " page {0,number}");
                blockPortalTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);
            } catch (java.awt.print.PrinterException e1) {
                log.warn("error printing: {}", e1, e1);
            }
        });
        return print;
    }

    // for desktop style interface, ignored for _tabbed
    private void createDesktop() {
        _desktop = new JDesktopPane();
        _desktop.putClientProperty("JDesktopPane.dragMode", "outline"); // slower or faster?
        int deskWidth = _blockTableFrame.getWidth();
        int deskHeight = _blockTableFrame.getHeight();
//        _desktop.setPreferredSize(new Dimension(deskWidth,
//                deskHeight + _portalTableFrame.getHeight() + 100));
        _desktop.setBackground(new Color(180,180,180));
        desktopframe.setContentPane(_desktop);
        desktopframe.setPreferredSize(new Dimension(deskWidth + 16,
                deskHeight + _portalTableFrame.getHeight() + 64));

        // placed at 0,0
        _desktop.add(_blockTableFrame);
        _portalTableFrame.setLocation(0, deskHeight);
        _desktop.add(_portalTableFrame);
        _signalTableFrame.setLocation(200, deskHeight+100);
        _desktop.add(_signalTableFrame);
        _blockPortalXRefFrame.setLocation(deskWidth - _blockPortalXRefFrame.getWidth(), deskHeight);
        _desktop.add(_blockPortalXRefFrame);
    }

    public JMenu getOptionMenu() {
        // Options menu
        JMenu optionMenu = new JMenu(Bundle.getMessage("MenuOptions"));
        _showWarnItem = new JMenuItem(Bundle.getMessage("SuppressWarning"));
        _showWarnItem.addActionListener(event -> {
            String cmd = event.getActionCommand();
            setShowWarnings(cmd);
        });
        optionMenu.add(_showWarnItem);
        setShowWarnings("ShowWarning");

        JMenuItem importBlocksItem = new JMenuItem(Bundle.getMessage("ImportBlocksMenu"));
        importBlocksItem.addActionListener((ActionEvent event) -> importBlocks());
        optionMenu.add(importBlocksItem);
        // disable ourself if there is no primary Block manager available
        if (jmri.InstanceManager.getNullableDefault(jmri.BlockManager.class) == null) { // means Block list is empty
            importBlocksItem.setEnabled(false);
        }
        _setUnits = new JMenuItem(Bundle.getMessage("changeUnits",
                (_oBlockModel.isMetric() ? Bundle.getMessage("LengthInches") : Bundle.getMessage("LengthCentimeters"))));
        _setUnits.addActionListener(event -> setUnits());
        optionMenu.add(_setUnits);
        return optionMenu;
    }

    public JMenu getTablesMenu() {
        // Tables menu
        tablesMenu = new JMenu(Bundle.getMessage("OpenMenu"));
        updateOBlockTablesMenu(); // replaces the last 2 menu items with appropriate submenus
        return tablesMenu;
    }

    private String oblockPrefix() {
        if (oblockPrefix == null) {
            oblockPrefix = InstanceManager.getDefault(OBlockManager.class).getSystemNamePrefix();
        }
        return oblockPrefix;
    }

    /**
     * Get the JFrame containig all UI windows.
     *
     * @return the contentframe
     */
    protected JmriJFrame getDesktopFrame() {
        return desktopframe;
    }

    /**
     * Convert a copy of your current JMRI Blocks to OBlocks and connect them with Portals and Paths.
     * Accessed from the Options menu.
     * @throws IllegalArgumentException exception
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
            JOptionPane.showMessageDialog(desktopframe, Bundle.getMessage("ImportNoBlocks"),
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
                    for (BeanSetting bs : pa.getSettings()) {
                        opa.addSetting(bs);
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

    private void setUnits() {
        _oBlockModel.changeUnits();
        _setUnits.setText(Bundle.getMessage("changeUnits",
                (_oBlockModel.isMetric() ? Bundle.getMessage("LengthInches") : Bundle.getMessage("LengthCentimeters"))));
    }

    // listen for _desktopframe closing
    void addCloseListener(JmriJFrame desktop) {
        desktop.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                WarrantTableAction.getDefault().errorCheck();
                desktop.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                // closing instead of hiding removes name from Windows menu.handle menu to read Show...
                log.debug("windowClosing: {}", toString());
                desktop.dispose();
            }
        });
    }

    private String getTitle() {
        return _title;
    }

    private void setTitle(String title) {
        _title = title;
    }

    /**
     * Fill in the Open/Hide Tables menu on tablesMenu.
     */
    protected void updateOBlockTablesMenu() {
        if (tablesMenu == null) {
            return;
        }
        tablesMenu.removeAll();
        if (!_tabbed) { // full menu in _desktop, open/show not available in _tabbed interface
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
        }

        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);

        // Block-Path submenus
        JMenu openBlockPath = new JMenu(Bundle.getMessage("OpenBlockPathMenu"));
        ActionListener openFrameAction = e -> {
            String blockSystemName = e.getActionCommand();
            openBlockPathPane(blockSystemName, Bundle.getMessage("TitlePaths")); // handles both interfaces
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
                    openPathTurnoutEditPane(pathTurnoutName); // handles both interfaces
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
        if (_tabbed) {
            log.debug("openPathTurnoutEditPane for {}", pathTurnoutName);
            openPathTurnoutEditor(pathTurnoutName);
        } else { // stand alone frame only used for _desktop, created from/stored in Portal
            openPathTurnoutFrame(pathTurnoutName);
        }
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
        JTable table = new JTable();
        if (tableModel instanceof OBlockTableModel) {
            table = makeOBlockTable((OBlockTableModel) tableModel);
        } else if (tableModel instanceof PortalTableModel) {
            table = makePortalTable((PortalTableModel) tableModel);
        } else if (tableModel instanceof BlockPortalTableModel) {
            table = makeBlockPortalTable((BlockPortalTableModel) tableModel);
        } else if (tableModel instanceof SignalTableModel) {
            table = makeSignalTable((SignalTableModel) tableModel);
        } // no case here for BlockPathTableModel, it is handled directly from OBlockTable

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
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.REPORT_CURRENTCOL).setCellRenderer(
                new ToggleButtonRenderer(Bundle.getMessage("Current"), Bundle.getMessage("Last")));
        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.REPORT_CURRENTCOL).setCellEditor(
                new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("Current"), Bundle.getMessage("Last")));
        model.configSpeedColumn(_oBlockTable); // use real combo
        //        JComboBox<String> box = new JComboBox<>(OBlockTableModel.curveOptions);
        //        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.CURVECOL).setCellEditor(new DefaultCellEditor(box));
        model.configCurveColumn(_oBlockTable); // use real combo
        //        box = new JComboBox<>(jmri.InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames());
//        box.addItem("");
//        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.SPEEDCOL).setCellRenderer(new DefaultCellRenderer(new _oBlockModel.SpeedComboBoxPanel()));
//        _oBlockTable.getColumnModel().getColumn(OBlockTableModel.SPEEDCOL).setCellEditor(new DefaultCellEditor(box));
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
        _oBlockTable.setRowHeight(ROW_HEIGHT);

        TableColumn column = tcm.getColumnByModelIndex(OBlockTableModel.REPORTERCOL);
        tcm.setColumnVisible(column, false);
        column = tcm.getColumnByModelIndex(OBlockTableModel.REPORT_CURRENTCOL);
        tcm.setColumnVisible(column, false);
        column = tcm.getColumnByModelIndex(OBlockTableModel.PERMISSIONCOL);
        tcm.setColumnVisible(column, false);
        column = tcm.getColumnByModelIndex(OBlockTableModel.ERR_SENSORCOL);
        tcm.setColumnVisible(column, false);
        column = tcm.getColumnByModelIndex(OBlockTableModel.CURVECOL);
        tcm.setColumnVisible(column, false);

        _oBlockTable.setPreferredScrollableViewportSize(new java.awt.Dimension(_oBlockTable.getPreferredSize().width,
                ROW_HEIGHT * Math.min(20, InstanceManager.getDefault(OBlockManager.class).getObjectCount())));
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
    protected boolean openOBlockEditor(String blockSystemName, String tabname) {
        boolean result = false;
        if (blockSystemName != null) {
            // this is for Edit (new OBlocks are created from [Add OBlock...] button in table)
            OBlock oblock = InstanceManager.getDefault(OBlockManager.class).getBySystemName(blockSystemName);
            if (oblock != null) {
                BlockPathJPanel panel = makeBlockPathEditPanel(oblock);
                // BeanEdit UI, adapted from jmri.jmrit.beantable.BlockTableAction
                jmri.jmrit.beantable.beanedit.OBlockEditAction beanEdit = new jmri.jmrit.beantable.beanedit.OBlockEditAction(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, tabname));
                beanEdit.setBean(oblock);
                beanEdit.setTablePanel(panel);
                beanEdit.actionPerformed(null);
                // run on separate thread? does not update new Paths in table!
                //                class WindowMaker implements Runnable {
                //                    final OBlock ob;
                //                    final BlockPathJPanel panel;
                //                    WindowMaker(OBlock oblock, BlockPathJPanel panel) {
                //                        ob = oblock;
                //                        this.panel = panel;
                //                    }
                //                    @Override
                //                    public void run() {
                //                        jmri.jmrit.beantable.beanedit.OBlockEditAction beanEdit = new jmri.jmrit.beantable.beanedit.OBlockEditAction();
                //                        beanEdit.setBean(oblock);
                //                        beanEdit.setTablePanel(panel);
                //                        beanEdit.actionPerformed(null);
                //                    }
                //                }
                //                WindowMaker t = new WindowMaker(oblock, panel);
                //                javax.swing.SwingUtilities.invokeLater(t);
                log.debug("path table created for oblock {}", blockSystemName);
                result = true;
            }
        }
        return result;
    }

    /**
     * Open the Edit Path panel for _tabbed.
     * Compare with openOBlockEditor(block, selectedtabname) and OBlockTableAction.
     *
     * @param blockName system or user name of the owning oblock
     * @param pathName name of the path under edit, or null to create a new path
     * @param bpmodel blockpathtablemodel that should be informed about changes
     * @return true if successful
     */
    protected boolean openPathEditor(@Nonnull String blockName, @CheckForNull String pathName, BlockPathTableModel bpmodel) {
        OBlock block = InstanceManager.getDefault(OBlockManager.class).getOBlock(blockName);
        if (block == null) {
            log.error("OBlock {} not found", blockName);
            return false;
        }
        OPath path;
        String title;
        PathTurnoutJPanel turnouttable = makePathTurnoutPanel(block, pathName); // shows the turnouts on path, includes Add Turnout button, checks for null path
        if (pathName == null) { // new Path, empty TurnoutTable
            // a new Path is created from [Add Path...] button in Path table on OBlock Editor pane.
            path = null;
            title = Bundle.getMessage("AddPathTitle", blockName);
        } else {
            path = block.getPathByName(pathName);
            title = Bundle.getMessage("EditPathTitle", pathName, blockName);
        }
        BlockPathEditFrame bpef = new BlockPathEditFrame(title, block, path, turnouttable, bpmodel, this);
        bpef.setVisible(true);
        // run on separate thread? combos are final, difficult to store Partals in Path/see them show up in the table
        //        class WindowMaker implements Runnable {
        //            final String title;
        //            final OBlock ob;
        //            final OPath path;
        //            final PathTurnoutTableModel tomodel;
        //            final BlockPathTableModel bpmodel;
        //            final TableFrames parent;
        //            WindowMaker(String title, OBlock ob, OPath path, PathTurnoutTableModel turnoutmodel, BlockPathTableModel blockpathmodel, TableFrames tf) {
        //                this.title = title;
        //                this.ob = ob;
        //                this.path = path;
        //                this.tomodel = turnoutmodel;
        //                this.bpmodel = blockpathmodel;
        //                parent = tf;
        //            }
        //            @Override
        //            public void run() {
        //                BlockPathEditFrame bpef = new BlockPathEditFrame(title, block, path, turnouttable, bpmodel, parent);
        //                bpef.setVisible(true);
        //            }
        //        }
        //        WindowMaker t = new WindowMaker(title, block, path, turnouttable.getModel(), bpmodel, this);
        //        javax.swing.SwingUtilities.invokeLater(t);

        log.debug("Path editor created for path {} on block {}", pathName, blockName);
        return true;
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
        _portalTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth,
                ROW_HEIGHT * Math.min(20, InstanceManager.getDefault(PortalManager.class).getPortalCount())));
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
        int tableWidth = _blockPortalTable.getPreferredSize().width;
        _blockPortalTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth,
                ROW_HEIGHT * Math.min(20, InstanceManager.getDefault(PortalManager.class).getPortalCount())));

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
        _signalTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth,
                ROW_HEIGHT * Math.min(10, _signalTable.getRowCount())));
        return _signalTable;
    }

    /*
     * ***************** end of permanent Tables + InternalFrame definitions *****************
     */


    /*
     * ***************** On Demand Tables + InternalFrame definitions *****************
     */

    /*
     * ********************* Block-Path Frame *****************************
     */

    // called from Tables menu and the OBlockTable EDIT buttons
    public void openBlockPathPane(String blockSystemName, String editorTabName) {
        if (_tabbed) {
            if (!openOBlockEditor(blockSystemName, editorTabName)) {
                // pass on to Per OBlock Edit panel, includes a BlockPath table
                log.error("Failed to open OBlock Path table for {}", blockSystemName);
            }
        } else {
            openBlockPathFrame(blockSystemName); // an editable table of all paths on this block
        }
    }

    // ***************** Block-Path Frame for _desktop **************************
    /**
     * Open a block-specific Block-Path table in _desktop interface.
     *
     * @param blockSystemName of the OBlock
     */
    protected void openBlockPathFrame(String blockSystemName) {
        BlockPathFrame frame = _blockPathMap.get(blockSystemName);
        if (frame == null) {
            OBlock block = InstanceManager.getDefault(OBlockManager.class).getBySystemName(blockSystemName);
            if (block == null) {
                return;
            }
            frame = makeBlockPathFrame(block);
            // store frame in Map
            _blockPathMap.put(blockSystemName, frame);
            frame.setVisible(true);
            desktopframe.getContentPane().add(frame);
        } else {
            frame.setVisible(true);
            try {
                frame.setIcon(false);
            } catch (PropertyVetoException pve) {
                log.warn("BlockPath Table Frame for \"{}\" vetoed setIcon {}", blockSystemName, pve);
            }
        }
        frame.moveToFront();
    }

    // common dispose
    protected void disposeBlockPathFrame(OBlock block) {
        if (!_tabbed) {
            //BlockPathFrame frame = _blockPathMap.get(block.getSystemName());
            // TODO frame.getModel().removeListener();
            //_blockPathMap.remove(block.getSystemName()); // block not stored in map, required to remove listener?
            // frame.dispose(); not required (closeable window)
            //} else {
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

    protected BlockPathJPanel makeBlockPathEditPanel(OBlock block) {
        // Path Table placed on jmri.jmrit.beanedit OBlockEditAction - Paths tab
        String title = Bundle.getMessage("TitleBlockPathEditor", block.getDisplayName());
        // create table
        BlockPathTableModel model = new BlockPathTableModel(block, this);
        JPanel bpTablePane = makeBlockPathTablePanel(model);
        BlockPathJPanel panel = new BlockPathJPanel(title);
        panel.setModel(model, block.getSystemName());
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(bpTablePane);

        // Add Path Button
        JPanel tblButtons = new JPanel();
        tblButtons.setLayout(new BorderLayout(10, 10));
        tblButtons.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        tblButtons.setLayout(new BoxLayout(tblButtons, BoxLayout.Y_AXIS));

        JButton addPathButton = new JButton(Bundle.getMessage("ButtonAddPath"));
        ActionListener addPathAction = e -> {
            // New Path uses the same editor pane as Edit Path
            if (!isPathEdit()) {
                setPathEdit(true);
                log.debug("makeBlockPathEditPanel pathEdit=True");
                openPathEditor(block.getDisplayName(), null, model);
            } else {
                log.warn("Close BlockPath Editor to reopen");
            }
        };
        addPathButton.addActionListener(addPathAction);
        addPathButton.setToolTipText(Bundle.getMessage("AddPathTabbedPrompt"));
        tblButtons.add(addPathButton);
        panel.add(tblButtons);

        //panel.pack();
        return panel;
    }

    // prevent more than 1 edit pane being opened at the same time
    protected void setPathEdit(boolean edit) {
        pathEdit = edit;
    }

    protected boolean isPathEdit() {
        return pathEdit;
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
     * ********************* Block-Path Table Panel for _desktop and _tabbed ***********************
     */
    protected JPanel makeBlockPathTablePanel(BlockPathTableModel _model) {
        JTable blockPathTable = makeBlockPathTable(_model); // styled

        // get table
        JScrollPane tablePane = new JScrollPane(blockPathTable);
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        if (_tabbed) {
            // a bit more styling
            blockPathTable.setPreferredScrollableViewportSize(new Dimension(600, 100));
        } else {
            JLabel prompt = new JLabel(Bundle.getMessage("AddPathPrompt"));
            contentPane.add(prompt, BorderLayout.NORTH);
        }
        contentPane.add(tablePane, BorderLayout.CENTER);

        return contentPane;
    }

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
        // units, etc renderer

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

        PathTurnoutJPanel(String pathname) {
            super();
            setName(pathname);
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
            log.debug("makePathTurnoutFrame for Block {} and Path {} on _desktop", block.getDisplayName(), pathName);
        }
        frame.setName(makePathTurnoutName(block.getSystemName(), pathName));
        OPath path = block.getPathByName(pathName);
        if (path == null) {
            return null;
        }
        PathTurnoutTableModel pathTurnoutModel = new PathTurnoutTableModel(path, frame);
        frame.setModel(pathTurnoutModel);

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
        JButton addTurnoutButton = new JButton(Bundle.getMessage("ButtonAddTurnout"));
        addTurnoutButton.setToolTipText(Bundle.getMessage("AddTurnoutTabbedPrompt"));
        JLabel prompt = new JLabel();
        prompt.setFont(prompt.getFont().deriveFont(0.9f * new JLabel().getFont().getSize())); // a bit smaller
        prompt.setForeground(Color.gray);

        if (pathName == null) {
            panel.setName(makePathTurnoutName(block.getSystemName(), "<new Path>"));
            String[] columnHeaders = {Bundle.getMessage("Turnouts")};
            String[][] emptyTable = new String[][] {{Bundle.getMessage("None")}};
            pathTurnoutTable = new JTable(emptyTable, columnHeaders); // dummy table
            addTurnoutButton.setEnabled(false);
            prompt.setText(Bundle.getMessage("TurnoutTablePromptNew"));
        } else {
            panel.setName(makePathTurnoutName(block.getSystemName(), pathName));
            final OPath path = block.getPathByName(pathName); // final for actionhandler
            if (path == null) {
                return null; // unexpected
            }
            pathTurnoutModel = new PathTurnoutTableModel(path);
            pathTurnoutTable = makePathTurnoutTable(pathTurnoutModel);
            panel.setModel(pathTurnoutModel);
            ActionListener addTurnoutAction= e -> addTurnoutPane(path, pathTurnoutModel);
            addTurnoutButton.addActionListener(addTurnoutAction);
            prompt.setText(Bundle.getMessage("TurnoutTablePrompt"));
        }
        JScrollPane tablePane = new JScrollPane(pathTurnoutTable);

        JPanel tblButtons = new JPanel();
        tblButtons.setLayout(new BorderLayout(10, 10));
        tblButtons.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        tblButtons.setLayout(new BoxLayout(tblButtons, BoxLayout.Y_AXIS));
        tblButtons.add(addTurnoutButton);
        // add more to frame?

        panel.setLayout(new BorderLayout(5, 5));

        panel.add(prompt, BorderLayout.NORTH);
        panel.add(tablePane, BorderLayout.CENTER);
        panel.add(tblButtons, BorderLayout.SOUTH);

        return panel;
    }

    /*
     * ********************* Path-Turnout Table *****************************
     */
    protected JTable makePathTurnoutTable(PathTurnoutTableModel model) {
        JTable pathTurnoutTable = new JTable(model);
        pathTurnoutTable.setTransferHandler(new jmri.util.DnDTableImportExportHandler(
                new int[]{PathTurnoutTableModel.STATE_COL, PathTurnoutTableModel.DELETE_COL}));
        pathTurnoutTable.setDragEnabled(true);

        model.configTurnoutStateColumn(pathTurnoutTable); // use real combo
        pathTurnoutTable.getColumnModel().getColumn(PathTurnoutTableModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
        pathTurnoutTable.getColumnModel().getColumn(PathTurnoutTableModel.DELETE_COL).setCellRenderer(new ButtonRenderer());
        //pathTurnoutTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            pathTurnoutTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        pathTurnoutTable.doLayout();
        int tableWidth = pathTurnoutTable.getPreferredSize().width;
        pathTurnoutTable.setRowHeight(ROW_HEIGHT);
        pathTurnoutTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth,
            Math.min(TableFrames.ROW_HEIGHT * 5, maxHeight)));

        return pathTurnoutTable;
    }

    /**
     * Create a coded id for a path turnout.
     *
     * @param blockSysName oblock system name
     * @param pathName the path through the oblock for which to display turnouts set
     * @return name of the pathTurnout, example "%path 1-3&amp;block-1"
     */
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
            String blockName = pathTurnoutName.substring(index + 1);
            OBlock block = InstanceManager.getDefault(OBlockManager.class).getBySystemName(blockName);
            if (block == null) {
                return;
            }
            frame = makePathTurnoutFrame(block, pathName);
            if (frame == null) {
                return;
            }
            _pathTurnoutMap.put(pathTurnoutName, frame);
            frame.setVisible(true);
            desktopframe.getContentPane().add(frame);
        } else {
            frame.setVisible(true);
            try {
                frame.setIcon(false);
            } catch (PropertyVetoException pve) {
                log.warn("PathTurnout Table Frame for \"{}\" vetoed setIcon", pathTurnoutName, pve);
            }
        }
        frame.moveToFront();
    }

    // *********** Open stand alone Path-Turnout Edit Panel for _tabbed *********************
    /**
     * Open a block-specific PathTurnouts edit pane as a JmriJFrame for _tabbed from menu.
     * TODO fix menu access to pathturnouts on _tabbed in ListedTableView, single table menus OK
     *
     * @param pathTurnoutName name of turnout configured on Path
     */
    protected void openPathTurnoutEditor(String pathTurnoutName) {
        int index = pathTurnoutName.indexOf('&');
        String pathName = pathTurnoutName.substring(1, index);
        String blockName = pathTurnoutName.substring(index + 1);
        OBlock block = InstanceManager.getDefault(OBlockManager.class).getBySystemName(blockName);
        if (block == null) {
            return;
        }
        OPath path = block.getPathByName(pathName);
        if (path == null) {
            return;
        }
        PathTurnoutJPanel turnouttable = makePathTurnoutPanel(block, pathName);
        // shows the turnouts on this path, already includes [Add Turnout...] button
        JmriJFrame frame = new JmriJFrame(Bundle.getMessage("TitlePathTurnoutTable", block.getDisplayName(), pathName));
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
        frame.setSize(370, 250);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        p.add(turnouttable);
        JButton ok;
        p.add(ok = new JButton(Bundle.getMessage("ButtonOK"))); // no need to save things, handled by TurnoutTable
        ok.addActionListener((ActionEvent e) -> frame.dispose());
        frame.getContentPane().add(p);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Add new Turnout pane, called from makePathTurnoutPanel on _tabbed interface.
     *
     * @param path to link this turnout setting to
     * @param pathTurnoutModel displayed table of turnouts currently set on this path
     */
    protected void addTurnoutPane(OPath path, PathTurnoutTableModel pathTurnoutModel) {
        JmriJFrame frame = new JmriJFrame(Bundle.getMessage("NewTurnoutTitle", path.getName()));
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
        frame.setSize(200, 150);

        JPanel p = new JPanel();

        final NamedBeanComboBox<Turnout> turnoutBox = new NamedBeanComboBox<>(InstanceManager.getDefault(TurnoutManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
        JComboBox<String> stateCombo = new JComboBox<>();
        JLabel statusBar = new JLabel(Bundle.getMessage("AddXStatusInitial1", Bundle.getMessage("BeanNameTurnout"), Bundle.getMessage("ButtonOK")), JLabel.LEADING);
        stateCombo.addItem(SET_THROWN);
        stateCombo.addItem(SET_CLOSED);
        turnoutBox.setToolTipText(Bundle.getMessage("TurnoutEditToolTip"));

        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.LINE_AXIS));
        p1.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))));
        p1.add(turnoutBox);
        p.add(p1);

        p1 = new JPanel();
        p1.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnLabelSetState"))));
        p1.add(stateCombo);
        p.add(p1);

        p.add(Box.createVerticalGlue());

        JPanel p2 = new JPanel();
        statusBar.setFont(statusBar.getFont().deriveFont(0.9f * (new JLabel()).getFont().getSize())); // a bit smaller
        if (turnoutBox.getItemCount() < 1) {
            statusBar.setText(Bundle.getMessage("NotEnoughTurnouts"));
            statusBar.setForeground(Color.red);
        } else {
            statusBar.setForeground(Color.gray);
        }
        p2.add(statusBar);
        p.add(p2);

        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.LINE_AXIS));
        JButton cancel;
        btns.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener((ActionEvent e) -> frame.dispose());
        JButton ok;
        btns.add(ok = new JButton(Bundle.getMessage("ButtonOK")));
        ok.addActionListener((ActionEvent e) -> {
            if (turnoutBox.getSelectedItem() == null || turnoutBox.getSelectedIndex() == 0) {
                statusBar.setText(Bundle.getMessage("WarningSelectionEmpty"));
                statusBar.setForeground(Color.red);
            } else {
                String user = turnoutBox.getSelectedItemDisplayName();
                Turnout t = InstanceManager.turnoutManagerInstance().getTurnout(user);
                if (t != null) {
                    int s;
                    if (stateCombo.getSelectedItem() != null && stateCombo.getSelectedItem().equals(SET_CLOSED)) {
                        s = Turnout.CLOSED;
                    } else {
                        s = Turnout.THROWN;
                    }
                    BeanSetting bs = new BeanSetting(t, user, s);
                    path.addSetting(bs);
                    if (pathTurnoutModel != null) {
                        pathTurnoutModel.fireTableDataChanged();
                    }
                } else {
                    log.error("PathTurnout {} not found", user);
                }
                frame.dispose();
            }
        });
        p.add(btns, BorderLayout.SOUTH);

        frame.getContentPane().add(p);
        frame.pack();
        frame.setVisible(true);
    }

    /*
     * ********************* End of tables and frames methods *****************************
     */

    // Shared warning dialog method. Store user pref to suppress further mentions.
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
                    JOptionPane.showMessageDialog(desktopframe, msg,
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
                JOptionPane.showMessageDialog(desktopframe, msg,
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
