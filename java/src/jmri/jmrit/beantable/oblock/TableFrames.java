package jmri.jmrit.beantable.oblock;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.TableRowSorter;
import jmri.InstanceManager;
import jmri.Path;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.logix.WarrantTableAction;
import jmri.util.SystemType;
import jmri.util.com.sun.TransferActionListener;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to define OBlocks
 * <BR>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * </P><P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * </P>
 *
 * @author Pete Cressman (C) 2010
 */
public class TableFrames extends jmri.util.JmriJFrame implements InternalFrameListener {

    static int ROW_HEIGHT;
    public static final int STRUT_SIZE = 10;

    JTable _oBlockTable;
    OBlockTableModel _oBlockModel;
    JTable _portalTable;
    PortalTableModel _portalModel;
    JTable _blockPortalTable;
    BlockPortalTableModel _blockPortalXRefModel;
    JTable _signalTable;
    SignalTableModel _signalModel;

    JScrollPane _blockTablePane;
    JScrollPane _portalTablePane;
    JScrollPane _signalTablePane;

    JDesktopPane _desktop;
    JInternalFrame _blockTableFrame;
    JInternalFrame _portalTableFrame;
    JInternalFrame _blockPortalXRefFrame;
    JInternalFrame _signalTableFrame;

    boolean _showWarnings = true;
    JMenuItem _showWarnItem;
    JMenu _openMenu;
    HashMap<String, JInternalFrame> _blockPathMap = new HashMap<>();
    HashMap<String, JInternalFrame> _PathTurnoutMap = new HashMap<>();

    public TableFrames() {
        this("OBlock Table");
    }

    public TableFrames(String actionName) {
        super(actionName);
    }

    @Override
    public void initComponents() {
        setTitle(Bundle.getMessage("TitleOBlocks"));
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        fileMenu.add(new jmri.configurexml.SaveMenu());

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
                    log.warn("error printing: " + e1, e1);
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
                    log.warn("error printing: " + e1, e1);
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
                    log.warn("error printing: " + e1, e1);
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
                    log.warn("error printing: " + e1, e1);
                }
            }
        });

        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu(Bundle.getMessage("MenuEdit"));
        editMenu.setMnemonic(KeyEvent.VK_E);
        TransferActionListener actionListener = new TransferActionListener();

        JMenuItem menuItem = new JMenuItem(Bundle.getMessage("MenuItemCut"));
        menuItem.setActionCommand((String) TransferHandler.getCutAction().getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        if (SystemType.isMacOSX()) {
            menuItem.setAccelerator(
                    KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.META_MASK));
        } else {
            menuItem.setAccelerator(
                    KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        }
        menuItem.setMnemonic(KeyEvent.VK_T);
        editMenu.add(menuItem);

        menuItem = new JMenuItem(Bundle.getMessage("MenuItemCopy"));
        menuItem.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        if (SystemType.isMacOSX()) {
            menuItem.setAccelerator(
                    KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.META_MASK));
        } else {
            menuItem.setAccelerator(
                    KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        }
        menuItem.setMnemonic(KeyEvent.VK_C);
        editMenu.add(menuItem);

        menuItem = new JMenuItem(Bundle.getMessage("MenuItemPaste"));
        menuItem.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        if (SystemType.isMacOSX()) {
            menuItem.setAccelerator(
                    KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.META_MASK));
        } else {
            menuItem.setAccelerator(
                    KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        }
        menuItem.setMnemonic(KeyEvent.VK_P);
        editMenu.add(menuItem);
        menuBar.add(editMenu);

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
        menuBar.add(optionMenu);

        _openMenu = new JMenu(Bundle.getMessage("OpenMenu"));
        updateOpenMenu();   // replaces the last item with appropriate
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

        _signalTableFrame = makeSignalFrame();
        _signalTableFrame.setVisible(true);
        _desktop.add(_signalTableFrame);

        _blockPortalXRefFrame = makeBlockPortalFrame();
        _blockPortalXRefFrame.setVisible(false);
        _desktop.add(_blockPortalXRefFrame);

        setLocation(10, 30);
        setVisible(true);
        pack();
        errorCheck();
    }

    protected final JScrollPane getBlockTablePane() {
        return _blockTablePane;
    }

    protected final JScrollPane getPortalTablePane() {
        return _portalTablePane;
    }

    protected final JScrollPane getSignalTablePane() {
        return _signalTablePane;
    }

    protected final OBlockTableModel getBlockModel() {
        return _oBlockModel;
    }

    protected final PortalTableModel getPortalModel() {
        return _portalModel;
    }

    protected final SignalTableModel getSignalModel() {
        return _signalModel;
    }

    protected final BlockPortalTableModel getXRefModel() {
        return _blockPortalXRefModel;
    }

    private void setShowWarnings(String cmd) {
        if (cmd.equals("ShowWarning")) {
            _showWarnings = true;
            _showWarnItem.setActionCommand("SuppressWarning");
            _showWarnItem.setText(Bundle.getMessage("SuppressWarning"));
        } else {
            _showWarnings = false;
            _showWarnItem.setActionCommand("ShowWarning");
            _showWarnItem.setText(Bundle.getMessage("ShowWarning"));
        }
        if (log.isDebugEnabled()) {
            log.debug("setShowWarnings: _showWarnings= " + _showWarnings);
        }
    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        errorCheck();
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        if (log.isDebugEnabled()) {
            log.debug("windowClosing: " + toString());
        }
    }

    private void errorCheck() {
        WarrantTableAction.initPathPortalCheck();
        OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        String[] sysNames = manager.getSystemNameArray();
        for (String sysName : sysNames) {
            WarrantTableAction.checkPathPortals(manager.getBySystemName(sysName));
        }
        if (_showWarnings) {
            WarrantTableAction.showPathPortalErrors();
        }
    }

    protected void updateOpenMenu() {
        _openMenu.removeAll();
        JMenuItem openBlock = new JMenuItem(Bundle.getMessage("OpenBlockMenu"));
        _openMenu.add(openBlock);
        openBlock.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                _blockTableFrame.setVisible(true);
                try {
                    _blockTableFrame.setIcon(false);
                } catch (PropertyVetoException pve) {
                    log.warn("Block Table Frame vetoed setIcon " + pve.toString());
                }
                _blockTableFrame.moveToFront();
            }
        });
        JMenuItem openPortal = new JMenuItem(Bundle.getMessage("OpenPortalMenu"));
        _openMenu.add(openPortal);
        openPortal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                _portalTableFrame.setVisible(true);
                try {
                    _portalTableFrame.setIcon(false);
                } catch (PropertyVetoException pve) {
                    log.warn("Portal Table Frame vetoed setIcon " + pve.toString());
                }
                _portalTableFrame.moveToFront();
            }
        });
        JMenuItem openXRef = new JMenuItem(Bundle.getMessage("OpenXRefMenu"));
        _openMenu.add(openXRef);
        openXRef.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                _blockPortalXRefFrame.setVisible(true);
                try {
                    _blockPortalXRefFrame.setIcon(false);
                } catch (PropertyVetoException pve) {
                    log.warn("XRef Table Frame vetoed setIcon " + pve.toString());
                }
                _blockPortalXRefFrame.moveToFront();
            }
        });
        JMenuItem openSignal = new JMenuItem(Bundle.getMessage("OpenSignalMenu"));
        _openMenu.add(openSignal);
        openSignal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                _signalTableFrame.setVisible(true);
                try {
                    _signalTableFrame.setIcon(false);
                } catch (PropertyVetoException pve) {
                    log.warn("Signal Table Frame vetoed setIcon " + pve.toString());
                }
                _signalTableFrame.moveToFront();
            }
        });

        JMenu openBlockPath = new JMenu(Bundle.getMessage("OpenBlockPathMenu"));
        ActionListener openFrameAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sysName = e.getActionCommand();
                openBlockPathFrame(sysName);
            }
        };
        OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        String[] sysNames = manager.getSystemNameArray();
        for (String sysName : sysNames) {
            OBlock block = manager.getBySystemName(sysName);
            JMenuItem mi = new JMenuItem(Bundle.getMessage("OpenPathMenu", block.getDisplayName()));
            mi.setActionCommand(sysName);
            mi.addActionListener(openFrameAction);
            openBlockPath.add(mi);
        }
        _openMenu.add(openBlockPath);

        JMenu openTurnoutPath = new JMenu(Bundle.getMessage("OpenBlockPathTurnoutMenu"));
        sysNames = manager.getSystemNameArray();
        for (String sysName : sysNames) {
            OBlock block = manager.getBySystemName(sysName);
            JMenu openTurnoutMenu = new JMenu(Bundle.getMessage("OpenTurnoutMenu", block.getDisplayName()));
            openTurnoutPath.add(openTurnoutMenu);
            openFrameAction = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String pathTurnoutName = e.getActionCommand();
                    openPathTurnoutFrame(pathTurnoutName);
                }
            };
            Iterator<Path> iter = block.getPaths().iterator();
            while (iter.hasNext()) {
                OPath path = (OPath) iter.next();
                JMenuItem mi = new JMenuItem(Bundle.getMessage("OpenPathTurnoutMenu", path.getName()));
                mi.setActionCommand(makePathTurnoutName(sysName, path.getName()));
                mi.addActionListener(openFrameAction);
                openTurnoutMenu.add(mi);
            }
        }
        _openMenu.add(openTurnoutPath);
    }

    /*
     * ********************* BlockFrame *****************************
     */
    protected JInternalFrame makeBlockFrame() {
        JInternalFrame frame = new JInternalFrame(Bundle.getMessage("TitleBlockTable"), true, false, false, true);
        _oBlockModel = new OBlockTableModel(this);
        _oBlockTable = new JTable(_oBlockModel);
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
            public void mousePressed(MouseEvent me) {
                showPopup(me);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                showPopup(me);
            }
        });

        for (int i = 0; i < _oBlockModel.getColumnCount(); i++) {
            int width = _oBlockModel.getPreferredWidth(i);
            _oBlockTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        _oBlockTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        ROW_HEIGHT = _oBlockTable.getRowHeight();
        int tableWidth = _desktop.getPreferredSize().width;
        _oBlockTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth, ROW_HEIGHT * 10));
        _blockTablePane = new JScrollPane(_oBlockTable);

        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.REPORTERCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.REPORT_CURRENTCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.PERMISSIONCOL), false);
//        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.SPEEDCOL), false);
//        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.ERR_SENSORCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.CURVECOL), false);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        JLabel prompt = new JLabel(Bundle.getMessage("AddBlockPrompt"));
        contentPane.add(prompt, BorderLayout.NORTH);
        contentPane.add(_blockTablePane, BorderLayout.CENTER);

        frame.setContentPane(contentPane);
        frame.pack();
        return frame;
    }

    void showPopup(MouseEvent me) {
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
    protected JInternalFrame makePortalFrame() {
        JInternalFrame frame = new JInternalFrame(Bundle.getMessage("TitlePortalTable"), true, false, false, true);
        _portalModel = new PortalTableModel(this);
        _portalTable = new JTable(_portalModel);
        TableRowSorter<PortalTableModel> sorter = new TableRowSorter<>(_portalModel);
        sorter.setComparator(PortalTableModel.FROM_BLOCK_COLUMN, new jmri.util.SystemNameComparator());
        sorter.setComparator(PortalTableModel.TO_BLOCK_COLUMN, new jmri.util.SystemNameComparator());
        _portalTable.setRowSorter(sorter);
        _portalTable.setTransferHandler(new jmri.util.DnDTableImportExportHandler(new int[]{PortalTableModel.DELETE_COL}));
        _portalTable.setDragEnabled(true);

        _portalTable.getColumnModel().getColumn(PortalTableModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
        _portalTable.getColumnModel().getColumn(PortalTableModel.DELETE_COL).setCellRenderer(new ButtonRenderer());
        for (int i = 0; i < _portalModel.getColumnCount(); i++) {
            int width = _portalModel.getPreferredWidth(i);
            _portalTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        _portalTable.sizeColumnsToFit(-1);
        int tableWidth = _portalTable.getPreferredSize().width;
        _portalTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth, ROW_HEIGHT * 10));
        _portalTablePane = new JScrollPane(_portalTable);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        JLabel prompt = new JLabel(Bundle.getMessage("AddPortalPrompt"));
        contentPane.add(prompt, BorderLayout.NORTH);
        contentPane.add(_portalTablePane, BorderLayout.CENTER);

        frame.setContentPane(contentPane);
        frame.setLocation(0, 200);
        frame.pack();
        return frame;
    }

    /*
     * ********************* BlockPortalFrame *****************************
     */
    protected JInternalFrame makeBlockPortalFrame() {
        JInternalFrame frame = new JInternalFrame(Bundle.getMessage("TitleBlockPortalXRef"), true, false, false, true);
        _blockPortalXRefModel = new BlockPortalTableModel(_oBlockModel);
        _blockPortalTable = new JTable(_blockPortalXRefModel);
        _blockPortalTable.setTransferHandler(new jmri.util.DnDTableExportHandler());
        _blockPortalTable.setDragEnabled(true);

        _blockPortalTable.setDefaultRenderer(String.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        _blockPortalTable.setDefaultEditor(String.class, new jmri.jmrit.symbolicprog.ValueEditor());
        for (int i = 0; i < _blockPortalXRefModel.getColumnCount(); i++) {
            int width = _blockPortalXRefModel.getPreferredWidth(i);
            _blockPortalTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        _blockPortalTable.sizeColumnsToFit(-1);
        int tableWidth = _blockPortalTable.getPreferredSize().width;
        _blockPortalTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth, ROW_HEIGHT * 25));
        JScrollPane tablePane = new JScrollPane(_blockPortalTable);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        contentPane.add(tablePane, BorderLayout.CENTER);

        frame.addInternalFrameListener(this);
        frame.setContentPane(contentPane);
        frame.setLocation(700, 30);
        frame.pack();
        return frame;
    }

    /*
     * ********************* SignalFrame *****************************
     */
    protected JInternalFrame makeSignalFrame() {
        JInternalFrame frame = new JInternalFrame(Bundle.getMessage("TitleSignalTable"), true, false, false, true);
        _signalModel = new SignalTableModel(this);
        _signalModel.init();
        _signalTable = new JTable(_signalModel);
        TableRowSorter<SignalTableModel> sorter = new TableRowSorter<>(_signalModel);
        _signalTable.setRowSorter(sorter);
        _signalTable.setTransferHandler(new jmri.util.DnDTableImportExportHandler(
                new int[]{SignalTableModel.UNITSCOL, SignalTableModel.DELETE_COL}));
        _signalTable.setDragEnabled(true);

        _signalTable.getColumnModel().getColumn(SignalTableModel.UNITSCOL).setCellRenderer(
                new MyBooleanRenderer(Bundle.getMessage("cm"), Bundle.getMessage("in")));
        _signalTable.getColumnModel().getColumn(SignalTableModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
        _signalTable.getColumnModel().getColumn(SignalTableModel.DELETE_COL).setCellRenderer(new ButtonRenderer());
        for (int i = 0; i < _signalModel.getColumnCount(); i++) {
            int width = SignalTableModel.getPreferredWidth(i);
            _signalTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        _signalTable.sizeColumnsToFit(-1);
        int tableWidth = _signalTable.getPreferredSize().width;
        _signalTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth, ROW_HEIGHT * 8));
        _signalTablePane = new JScrollPane(_signalTable);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        JLabel prompt = new JLabel(Bundle.getMessage("AddSignalPrompt"));
        contentPane.add(prompt, BorderLayout.NORTH);
        contentPane.add(_signalTablePane, BorderLayout.CENTER);

        frame.setContentPane(contentPane);
        frame.setLocation(200, 350);
        frame.pack();
        return frame;
    }

    /**
     * ********************* BlockPathFrame *****************************
     */
    protected static class BlockPathFrame extends JInternalFrame {

        /**
         *
         */
        BlockPathTableModel blockPathModel;

        public BlockPathFrame(String title, boolean resizable, boolean closable,
                boolean maximizable, boolean iconifiable) {
            super(title, resizable, closable, maximizable, iconifiable);
        }

        public void init(OBlock block, TableFrames parent) {
            blockPathModel = new BlockPathTableModel(block, parent);
        }

        public BlockPathTableModel getModel() {
            return blockPathModel;
        }
    }

    /*
     * ********************* BlockPathFrame *****************************
     */
    protected BlockPathFrame makeBlockPathFrame(OBlock block) {
        String title = Bundle.getMessage("TitleBlockPathTable", block.getDisplayName());
        BlockPathFrame frame = new BlockPathFrame(title, true, true, false, true);
        if (log.isDebugEnabled()) {
            log.debug("makeBlockPathFrame for Block " + block.getDisplayName());
        }
        frame.setName(block.getSystemName());
        frame.init(block, this);
        BlockPathTableModel blockPathModel = frame.getModel();
        blockPathModel.init();
        JTable blockPathTable = new JTable(blockPathModel);
        blockPathTable.setTransferHandler(new jmri.util.DnDTableImportExportHandler(new int[]{
            BlockPathTableModel.EDIT_COL, BlockPathTableModel.DELETE_COL, BlockPathTableModel.UNITSCOL}));
        blockPathTable.setDragEnabled(true);

        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.UNITSCOL).setCellRenderer(
                new MyBooleanRenderer(Bundle.getMessage("cm"), Bundle.getMessage("in")));
        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.EDIT_COL).setCellEditor(new ButtonEditor(new JButton()));
        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.EDIT_COL).setCellRenderer(new ButtonRenderer());
        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
        blockPathTable.getColumnModel().getColumn(BlockPathTableModel.DELETE_COL).setCellRenderer(new ButtonRenderer());

        for (int i = 0; i < blockPathModel.getColumnCount(); i++) {
            int width = blockPathModel.getPreferredWidth(i);
            blockPathTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        blockPathTable.sizeColumnsToFit(-1);
        int tableWidth = blockPathTable.getPreferredSize().width;
        blockPathTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth, ROW_HEIGHT * 10));
        JScrollPane tablePane = new JScrollPane(blockPathTable);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        JLabel prompt = new JLabel(Bundle.getMessage("AddPathPrompt"));
        contentPane.add(prompt, BorderLayout.NORTH);
        contentPane.add(tablePane, BorderLayout.CENTER);

        frame.addInternalFrameListener(this);
        frame.setContentPane(contentPane);
        frame.setLocation(50, 30);
        frame.pack();
        return frame;
    }

    /*
     * ********************* PathTurnoutFrame *****************************
     */
    protected JInternalFrame makePathTurnoutFrame(OBlock block, String pathName) {
        String title = Bundle.getMessage("TitlePathTurnoutTable", block.getDisplayName(), pathName);
        JInternalFrame frame = new JInternalFrame(title, true, true, false, true);
        if (log.isDebugEnabled()) {
            log.debug("makePathTurnoutFrame for Block " + block.getDisplayName() + " and Path " + pathName);
        }
        frame.setName(makePathTurnoutName(block.getSystemName(), pathName));
        OPath path = block.getPathByName(pathName);
        if (path == null) {
            return null;
        }
        PathTurnoutTableModel PathTurnoutModel = new PathTurnoutTableModel(path);
        PathTurnoutModel.init();
        JTable PathTurnoutTable = new JTable(PathTurnoutModel);
        PathTurnoutTable.setTransferHandler(new jmri.util.DnDTableImportExportHandler(
                new int[]{PathTurnoutTableModel.SETTINGCOLUMN, PathTurnoutTableModel.DELETE_COL}));
        PathTurnoutTable.setDragEnabled(true);

        JComboBox<String> box = new JComboBox<>(PathTurnoutTableModel.turnoutStates);
        PathTurnoutTable.getColumnModel().getColumn(PathTurnoutTableModel.SETTINGCOLUMN).setCellEditor(new DefaultCellEditor(box));
        PathTurnoutTable.getColumnModel().getColumn(PathTurnoutTableModel.DELETE_COL).setCellEditor(new ButtonEditor(new JButton()));
        PathTurnoutTable.getColumnModel().getColumn(PathTurnoutTableModel.DELETE_COL).setCellRenderer(new ButtonRenderer());
        //PathTurnoutTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < PathTurnoutModel.getColumnCount(); i++) {
            int width = PathTurnoutModel.getPreferredWidth(i);
            PathTurnoutTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        PathTurnoutTable.sizeColumnsToFit(-1);
        int tableWidth = PathTurnoutTable.getPreferredSize().width;
        PathTurnoutTable.setPreferredScrollableViewportSize(new java.awt.Dimension(tableWidth, ROW_HEIGHT * 5));
        JScrollPane tablePane = new JScrollPane(PathTurnoutTable);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        JLabel prompt = new JLabel(Bundle.getMessage("AddTurnoutPrompt"));
        contentPane.add(prompt, BorderLayout.NORTH);
        contentPane.add(tablePane, BorderLayout.CENTER);

        frame.addInternalFrameListener(this);
        frame.setContentPane(contentPane);
        frame.setLocation(10, 270);
        frame.pack();
        return frame;
    }

    protected void openBlockPathFrame(String sysName) {
        JInternalFrame frame = _blockPathMap.get(sysName);
        if (frame == null) {
            OBlock block = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getBySystemName(sysName);
            if (block == null) {
                return;
            }
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
                log.warn("BlockPath Table Frame for \"{}\" vetoed setIcon {}", sysName, pve);
            }
            frame.moveToFront();
        }
    }

    protected String makePathTurnoutName(String blockSysName, String pathName) {
        return "%" + pathName + "&" + blockSysName;
    }

    protected void openPathTurnoutFrame(String pathTurnoutName) {
        JInternalFrame frame = _PathTurnoutMap.get(pathTurnoutName);
        log.debug("openPathTurnoutFrame for {}", pathTurnoutName);
        if (frame == null) {
            int index = pathTurnoutName.indexOf('&');
            String pathName = pathTurnoutName.substring(1, index);
            String sysName = pathTurnoutName.substring(index + 1);
            OBlock block = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getBySystemName(sysName);
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

    /*
     * ********************* InternalFrameListener implementation *****************
     */
    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        //JInternalFrame frame = (JInternalFrame)e.getSource();
        //log.debug("Internal frame closing: "+frame.getTitle());
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
        JInternalFrame frame = (JInternalFrame) e.getSource();
        String name = frame.getName();
        if (log.isDebugEnabled()) {
            log.debug("Internal frame closed: "
                    + frame.getTitle() + ", name= " + name + " size ("
                    + frame.getSize().getWidth() + ", " + frame.getSize().getHeight() + ")");
        }
        if (name != null && name.startsWith("OB")) {
            _blockPathMap.remove(name);
            WarrantTableAction.initPathPortalCheck();
            WarrantTableAction.checkPathPortals(((BlockPathFrame) frame).getModel().getBlock());
            ((BlockPathFrame) frame).getModel().removeListener();
            if (_showWarnings) {
                WarrantTableAction.showPathPortalErrors();
            }
        } else {
            _PathTurnoutMap.remove(name);
        }
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {
        /*  JInternalFrame frame = (JInternalFrame)e.getSource();
         if (log.isDebugEnabled()) log.debug("Internal frame Opened: "+
         frame.getTitle()+", name= "+frame.getName()+" size ("+
         frame.getSize().getWidth()+", "+frame.getSize().getHeight()+")"); */
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {
        JInternalFrame frame = (JInternalFrame) e.getSource();
        String name = frame.getName();
        if (log.isDebugEnabled()) {
            log.debug("Internal frame Iconified: "
                    + frame.getTitle() + ", name= " + name + " size ("
                    + frame.getSize().getWidth() + ", " + frame.getSize().getHeight() + ")");
        }
        if (name != null && name.startsWith("OB")) {
            WarrantTableAction.initPathPortalCheck();
            WarrantTableAction.checkPathPortals(((BlockPathFrame) frame).getModel().getBlock());
            if (_showWarnings) {
                WarrantTableAction.showPathPortalErrors();
            }
        }
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {
        //JInternalFrame frame = (JInternalFrame)e.getSource();
        //log.debug("Internal frame deiconified: "+frame.getTitle());
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
        //JInternalFrame frame = (JInternalFrame)e.getSource();
        //log.debug("Internal frame activated: "+frame.getTitle());
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
        //JInternalFrame frame = (JInternalFrame)e.getSource();
        //log.debug("Internal frame deactivated: "+frame.getTitle());
    }

    private final static Logger log = LoggerFactory.getLogger(TableFrames.class);
}
