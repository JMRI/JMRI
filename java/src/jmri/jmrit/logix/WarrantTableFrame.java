package jmri.jmrit.logix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import jmri.InstanceManager;
import jmri.Path;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.JmriMouseEvent;
import jmri.util.swing.JmriMouseListener;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * The WarrantTableFrame lists the existing Warrants and has controls to set
 * their routes, train IDs launch them and control their running (halt, resume,
 * abort. etc.
 *
 * The WarrantTableFrame also can initiate NX (eNtry/eXit) warrants
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
 * @author Pete Cressman Copyright (C) 2009, 2010
 */
public class WarrantTableFrame extends jmri.util.JmriJFrame implements JmriMouseListener {

    protected static final String ramp = Bundle.getMessage("SmoothHalt");
    protected static final String stop = Bundle.getMessage("Stop");
    protected static final String estop = Bundle.getMessage("EStop");
    protected static final String resume = Bundle.getMessage("Resume");
    protected static final String speedup = Bundle.getMessage("SpeedUp");
    protected static final String abort = Bundle.getMessage("Abort");
    protected static final String retryfwd = Bundle.getMessage("MoveToNext");
    protected static final String retrybkwd = Bundle.getMessage("MoveToPrevious");    // removed from drop down
    static final String[] controls = {" ", ramp, resume, stop, speedup, retryfwd, estop, abort,
                            (org.slf4j.LoggerFactory.getLogger(WarrantTableFrame.class).isDebugEnabled()?"Debug":"")};

    public static int _maxHistorySize = 40;

    private final JTextField _startWarrant = new JTextField(30);
    private final JTextField _endWarrant = new JTextField(30);
    private JDialog _concatDialog;
    private final JTextField _status = new JTextField(90);
    private final ArrayList<String> _statusHistory = new ArrayList<>();

    private final WarrantTableModel _model;

    /**
     * Get the default instance of a Warrant table window.
     *
     * @return the default instance; creating it if necessary
     */
    public static WarrantTableFrame getDefault() {
        WarrantTableFrame instance = InstanceManager.getOptionalDefault(WarrantTableFrame.class).orElseGet(() -> {
            WarrantTableFrame newInstance =
                InstanceManager.setDefault(WarrantTableFrame.class, new WarrantTableFrame());
            try {
                newInstance.initComponents();
            } catch (Exception ex) {
                log.error("Unable to initilize Warrant Table Frame", ex);
            }
            return newInstance;
        });
        if (jmri.util.ThreadingUtil.isGUIThread()) {
            instance.setVisible(true);
        }
        return instance;
    }

    protected WarrantTableModel getModel() {
        return _model;
    }

    private WarrantTableFrame() {
        super(true, true);
        setTitle(Bundle.getMessage("WarrantTable"));
        _model = new WarrantTableModel(this);
        _model.init();

    }

    /**
     * By default, Swing components should be created an installed in this
     * method, rather than in the ctor itself.
     */
    @Override
    public void initComponents() {

        log.debug("initComponents");
        //Casts at getTableCellEditorComponent() now fails with 3.0 ??
        JTable table = new JTable(_model);
        TableRowSorter<WarrantTableModel> sorter = new TableRowSorter<>(_model);
        table.setRowSorter(sorter);
        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        table.setColumnModel(tcm);
        table.getTableHeader().setReorderingAllowed(true);
        table.createDefaultColumnsFromModel();
        _model.addHeaderListener(table);

        JComboBox<String> cbox = new JComboBox<>();
        RouteBoxCellEditor comboEd = new RouteBoxCellEditor(cbox);
        ControlBoxCellEditor controlEd = new ControlBoxCellEditor(new JComboBox<>(controls));

        table.setDefaultRenderer(Boolean.class, new ButtonRenderer());
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

        table.getColumnModel().getColumn(WarrantTableModel.CONTROL_COLUMN).setCellEditor(controlEd);
        table.getColumnModel().getColumn(WarrantTableModel.ROUTE_COLUMN).setCellEditor(comboEd);
        table.getColumnModel().getColumn(WarrantTableModel.ALLOCATE_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
        table.getColumnModel().getColumn(WarrantTableModel.ALLOCATE_COLUMN).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(WarrantTableModel.DEALLOC_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
        table.getColumnModel().getColumn(WarrantTableModel.DEALLOC_COLUMN).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(WarrantTableModel.AUTO_RUN_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
        table.getColumnModel().getColumn(WarrantTableModel.AUTO_RUN_COLUMN).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(WarrantTableModel.MANUAL_RUN_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
        table.getColumnModel().getColumn(WarrantTableModel.MANUAL_RUN_COLUMN).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(WarrantTableModel.EDIT_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
        table.getColumnModel().getColumn(WarrantTableModel.EDIT_COLUMN).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(WarrantTableModel.DELETE_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
        table.getColumnModel().getColumn(WarrantTableModel.DELETE_COLUMN).setCellRenderer(new ButtonRenderer());
        //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < _model.getColumnCount(); i++) {
            int width = _model.getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        tcm.setColumnVisible(tcm.getColumnByModelIndex(WarrantTableModel.MANUAL_RUN_COLUMN), false);

        int rowHeight = comboEd.getComponent().getPreferredSize().height;
        table.setRowHeight(rowHeight);

        table.setDragEnabled(true);
        table.setTransferHandler(new jmri.util.DnDTableExportHandler());
        table.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int lastIndex = table.getRowCount()-1;
                table.changeSelection(lastIndex, 0,false,false);
            }
        });
        JScrollPane tablePane = new JScrollPane(table);

        JLabel title = new JLabel(Bundle.getMessage("ShowWarrants"));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel statusLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("status")));
        _status.addMouseListener(JmriMouseListener.adapt(this));
        _status.setBackground(Color.white);
        _status.setFont(_status.getFont().deriveFont(Font.BOLD));
        _status.setEditable(false);
        _status.setText(BLANK.substring(0, 90));

        JButton nxButton = new JButton(Bundle.getMessage("CreateNXWarrant"));
        nxButton.addActionListener((ActionEvent e) -> WarrantTableAction.getDefault().makeNXFrame());

        JButton haltAllButton = new JButton(Bundle.getMessage("HaltAllTrains"));
        haltAllButton.addActionListener((ActionEvent e) -> haltAllAction());
        haltAllButton.setForeground(Color.RED);

        JPanel footerLeft = new JPanel();
        footerLeft.setLayout(new BorderLayout());
        footerLeft.add(nxButton, BorderLayout.LINE_START);
        footerLeft.add(statusLabel, BorderLayout.LINE_END);

        JPanel footer = new JPanel();
        footer.setLayout(new BorderLayout());
        footer.add(footerLeft, BorderLayout.LINE_START);
        footer.add(_status, BorderLayout.CENTER);
        footer.add(haltAllButton, BorderLayout.LINE_END);

        Container pane = getContentPane();
        pane.add(title, BorderLayout.PAGE_START);
        pane.add(tablePane, BorderLayout.CENTER);
        pane.add(footer, BorderLayout.PAGE_END);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (_concatDialog !=null) {
                    _concatDialog.dispose();
                }
                _model.dispose();
                dispose();
            }
        });

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        fileMenu.add(new jmri.configurexml.StoreMenu());
        JMenu warrantMenu = new JMenu(Bundle.getMessage("MenuWarrant"));
        warrantMenu.add(new AbstractAction(Bundle.getMessage("ConcatWarrants")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                concatMenuAction();
            }
        });

        warrantMenu.add(new AbstractAction(Bundle.getMessage("CreateWarrant")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                WarrantTableAction.getDefault().makeWarrantFrame(null, null);
            }
         });
        warrantMenu.add(InstanceManager.getDefault(TrackerTableAction.class));
        warrantMenu.add(new AbstractAction(Bundle.getMessage("CreateNXWarrant")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                WarrantTableAction.getDefault().makeNXFrame();
            }
        });
        warrantMenu.add(WarrantTableAction.getDefault().makeLogMenu());
        menuBar.add(warrantMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.logix.WarrantTable", true);

        pack();
    }

    private void haltAllAction() {
        _model.haltAllTrains();
    }

    private void concatMenuAction() {
        _concatDialog = new JDialog(this, Bundle.getMessage("ConcatWarrants"), false);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
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
        concatButton.addActionListener((ActionEvent e) ->
            concatenate(_startWarrant.getText(), _endWarrant.getText()) );
        panel.add(concatButton, Component.CENTER_ALIGNMENT);

        mainPanel.add(panel);
        _concatDialog.getContentPane().add(mainPanel);
        _concatDialog.setLocation(getLocation().x + 200, getLocation().y + 200);
        _concatDialog.pack();
        _concatDialog.setVisible(true);
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "OPath extends Path")
    private void concatenate(String startName, String endName) {
        WarrantManager manager = InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class);
        Warrant startW = manager.getWarrant(startName.trim());
        Warrant endW = manager.getWarrant(endName.trim());
        if (startW == null || endW == null) {
            showWarning("BadWarrantNames");
            return;
        }
        BlockOrder last = startW.getLastOrder();
        BlockOrder next = endW.getfirstOrder();
        if (last == null || next == null) {
            showWarning("EmptyRoutes");
            return;
        }
        if (!last.getBlock().equals(next.getBlock())) {
            showWarning("BlocksDontMatch");
            return;
        }
        if (!last.getPathName().equals(next.getPathName())) {
            boolean foundPath = false;
            String entryName = last.getEntryName();
            String exitName = next.getExitName();
            Iterator<Path> iter = last.getBlock().getPaths().iterator();
            while (iter.hasNext()) {
                String pathName = ((OPath)iter.next()).getName();
                if (pathName.equals(entryName) && pathName.equals(exitName)) {
                    last.setPathName(pathName);
                    foundPath = true;
                    break;
                }
            }
            if (!foundPath) {
                showWarning("RoutesDontMatch");
                return;
            }
        }
        WarrantTableAction.getDefault().makeWarrantFrame(startW, endW);
        _concatDialog.dispose();
    }

    protected boolean askStopQuestion(String blockName) {
        boolean includeAllCmds = false;
        if (JmriJOptionPane.showConfirmDialog(this, Bundle.getMessage("stopAtBlock", blockName),
                Bundle.getMessage("QuestionTitle"), JmriJOptionPane.YES_NO_OPTION,
                JmriJOptionPane.QUESTION_MESSAGE) == JmriJOptionPane.YES_OPTION) {
            includeAllCmds = true;
        }
        return includeAllCmds;
    }

    public void showWarning(String msg) {
        setVisible(true);
        JmriJOptionPane.showMessageDialog(this, Bundle.getMessage(msg, _startWarrant.getText(), _endWarrant.getText()),
                Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
    }

    /**
     * *********************** Table ***************************************
     */
    private static class RouteBoxCellEditor extends DefaultCellEditor {

        RouteBoxCellEditor(JComboBox<String> comboBox) {
            super(comboBox);
            comboBox.setFont(new Font(null, Font.PLAIN, 12));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int r, int column) {
            TableModel m = table.getModel();
            WarrantTableModel model = null;
            if (m instanceof WarrantTableModel) {
                model = (WarrantTableModel) m;
            }
            if (model == null) {
                log.error("Unexpected table model: {}", m );
            }

            // If table has been sorted, table row no longer is the same as array index
            int row = r;
            if (table.getRowSorter() != null) {
                row = table.convertRowIndexToModel(row);
            }
            Warrant warrant = null;
            if (model != null) {
                warrant = model.getWarrantAt(row);
            }
            if (warrant == null) {
                log.warn("getWarrantAt row= {}, Warrant is null!", row);
                return getComponent();
            }
            Component component = getComponent();
            if (component instanceof JComboBox<?>) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) component;
                comboBox.removeAllItems();

                List<BlockOrder> orders = warrant.getBlockOrders();
                for (int i = 0; i < orders.size(); i++) {
                    BlockOrder order = orders.get(i);
                    comboBox.addItem(order.getBlock().getDisplayName() + ": - " + order.getPath().getName());
                }
            } else {
                log.error("Unexpected editor component: {}", component );
            }
            return component;
        }
    }

    private static class ControlBoxCellEditor extends DefaultCellEditor {

        ControlBoxCellEditor(JComboBox<String> comboBox) {
            super(comboBox);
            comboBox.setFont(new Font(null, Font.PLAIN, 12));
         }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int r, int column) {
            Component component = getComponent();
            if (component instanceof JComboBox<?>) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) component;
                comboBox.removeItemAt(0);
                comboBox.insertItemAt((String)value, 0);
                comboBox.setSelectedIndex(0);
                if (log.isDebugEnabled()) {
                    // If table has been sorted, table row no longer is the same as array index
                    int row = r;
                    if (table.getRowSorter() != null) {
                        row = table.convertRowIndexToModel(row);
                    }
                    WarrantTableModel model = (WarrantTableModel)table.getModel();
                    Warrant warrant = model.getWarrantAt(row);
                    log.debug("getTableCellEditorComponent warrant= {}, selection= {}",
                            warrant.getDisplayName(), comboBox.getSelectedItem());
                }
            } else {
                log.error("Unexpected editor component: {}", component );
            }
            return component;
        }
    }

    private long lastClicktime; // keep double clicks from showing dialogs

    /**
     * Return error message if warrant cannot be run.
     *
     * @param w    warrant
     * @param mode running type
     * @return null if warrant is started
     */
    public String runTrain(Warrant w, int mode) {
        long time = System.currentTimeMillis();
        if (time - lastClicktime < 1000) {
            return null;
        }
        lastClicktime = time;

        String msg = null;
        WarrantFrame frame = WarrantTableAction.getDefault().getOpenFrame();
        if (frame != null) {
            Warrant warrant = frame.getWarrant();
            if ( warrant != null && w.equals(warrant) && frame.isRunning() ) {
                msg = Bundle.getMessage("CannotRun", w.getDisplayName(),
                    Bundle.getMessage("TrainRunning", warrant.getTrainName()));
            }
        }

        if (msg == null) {
            msg = _model.checkAddressInUse(w);
        }

        if (msg == null) {
            msg = w.checkforTrackers();
        }

        if (msg == null) {
            msg = w.setRunMode(mode, null, null, null, w.getRunBlind());
            if (msg != null) {
                w.deAllocate();
            }
        }
        if (msg != null) {
            return Bundle.getMessage("CannotRun", w.getDisplayName(), msg);
        }
        return null;
    }

    @Override
    public void mouseClicked(JmriMouseEvent event) {
        int clicks = event.getClickCount();
        if (clicks > 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = _statusHistory.size() - 1; i >= 0; i--) {
                sb.append(_statusHistory.get(i));
                sb.append('\n');
            }
            Transferable transferable = new StringSelection(sb.toString());
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(transferable, null);

        } else {
            JPopupMenu popup = new JPopupMenu();
            for (int i = _statusHistory.size() - 1; i >= 0; i--) {
                popup.add(_statusHistory.get(i));
            }
            popup.show(_status, 0, 0);
        }
    }

    @Override
    public void mousePressed(JmriMouseEvent event) {
    }
    @Override
    public void mouseEntered(JmriMouseEvent event) {
    }
    @Override
    public void mouseExited(JmriMouseEvent event) {
    }
    @Override
    public void mouseReleased(JmriMouseEvent event) {
    }

    void setStatusText(String msg, Color c, boolean save) {
        _status.setForeground(c);
        _status.setText(msg);
        if (save && msg != null && msg.length() > 0) {
            _statusHistory.add(msg);
            WarrantTableAction.getDefault().writetoLog(msg);
            while (_statusHistory.size() > _maxHistorySize) {
                _statusHistory.remove(0);
            }
        }
    }

    protected String getStatus() {
        return _status.getText();
    }

    private static final String BLANK = "                                                                                                 ";

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WarrantTableFrame.class);

}
