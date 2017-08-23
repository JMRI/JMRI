package jmri.jmrit.logix;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import jmri.InstanceManager;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The WarrantTableFrame lists the existing Warrants and has controls to set
 * their routes, train IDs launch them and control their running (halt, resume,
 * abort. etc.
 *
 * The WarrantTableFrame also can initiate NX (eNtry/eXit) warrants
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
 * @author Pete Cressman Copyright (C) 2009, 2010
 */
public class WarrantTableFrame extends jmri.util.JmriJFrame implements MouseListener {

    static final String halt = Bundle.getMessage("Halt");
    static final String stop = Bundle.getMessage("EStop");
    static final String resume = Bundle.getMessage("Resume");
    static final String abort = Bundle.getMessage("Abort");
    static final String retry = Bundle.getMessage("Retry");
    static final String[] controls = {halt, resume, retry, stop, abort};

    public static int _maxHistorySize = 30;

    private JTextField _startWarrant = new JTextField(30);
    private JTextField _endWarrant = new JTextField(30);
    private JDialog _concatDialog;
    private JTextField _status = new JTextField(90);
    private ArrayList<String> _statusHistory = new ArrayList<String>();
    private JScrollPane _tablePane;

    private WarrantTableModel _model;

    /**
     * Get the default instance of a Warrant table window.
     *
     * @return the default instance; creating it if necessary
     * @deprecated since 4.7.4; use {@link #getDefault() } instead
     */
    @Deprecated
    public static WarrantTableFrame getInstance() {
        return getDefault();
    }

    /**
     * Get the default instance of a Warrant table window.
     *
     * @return the default instance; creating it if necessary
     */
    public static WarrantTableFrame getDefault() {
        WarrantTableFrame instance = InstanceManager.getOptionalDefault(WarrantTableFrame.class).orElseGet(() -> {
            WarrantTableFrame newInstance = InstanceManager.setDefault(WarrantTableFrame.class, new WarrantTableFrame());
            try {
                newInstance.initComponents();
                if (log.isDebugEnabled()) log.debug("newInstance");
            } catch (Exception ex) {
                log.error("Unable to initilize Warrant Table Frame", ex);
            }
            return newInstance;
        });
        instance.setVisible(true);
        instance.pack();
        return instance;
    }

    /**
     * Reset the WarrantTableFrame default instance (for unit testing only).
     * @return a new WarrantTableFrame instance
     * @deprecated since 4.7.4 without direct replacement
     */
    @Deprecated
    protected static WarrantTableFrame reset() {
        InstanceManager.reset(WarrantTableFrame.class);
        return getDefault();
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
    public void initComponents() throws Exception {

        if (log.isDebugEnabled()) log.debug("initComponents");
        //Casts at getTableCellEditorComponent() now fails with 3.0 ??
        JTable table = new JTable(_model);
        ComboBoxCellEditor comboEd;
        TableRowSorter<WarrantTableModel> sorter = new TableRowSorter<>(_model);
        comboEd = new ComboBoxCellEditor(new JComboBox<>());
        table.setRowSorter(sorter);
        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        table.setColumnModel(tcm);
        table.getTableHeader().setReorderingAllowed(true);
        table.createDefaultColumnsFromModel();
        _model.addHeaderListener(table);

        table.setDefaultRenderer(Boolean.class, new ButtonRenderer());
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
        JComboBox<String> box = new JComboBox<>(controls);
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
        for (int i = 0; i < _model.getColumnCount(); i++) {
            int width = _model.getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        tcm.setColumnVisible(tcm.getColumnByModelIndex(WarrantTableModel.MANUAL_RUN_COLUMN), false);

        int rowHeight = box.getPreferredSize().height;
        table.setRowHeight(rowHeight);
        table.setDragEnabled(true);
        table.setTransferHandler(new jmri.util.DnDTableExportHandler());
        _tablePane = new JScrollPane(table);

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.add(Box.createVerticalGlue());
        JLabel title = new JLabel(Bundle.getMessage("ShowWarrants"));
        tablePanel.add(title);
        tablePanel.add(_tablePane);

        JPanel bottom = new JPanel();
        JPanel panel = new JPanel();
        JButton nxButton = new JButton(Bundle.getMessage("CreateNXWarrant"));
        nxButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nxAction();
            }
        });
        panel.add(nxButton);
        panel.add(Box.createGlue());
        panel.add(new JLabel("status"));
        _status.addMouseListener(this);
        _status.setBackground(Color.white);
        _status.setFont(_status.getFont().deriveFont(Font.BOLD));
        _status.setEditable(false);
        setStatusText(BLANK.substring(0, 90), null, false);
        panel.add(_status);
        JButton haltAllButton = new JButton(Bundle.getMessage("HaltAllTrains"));
        haltAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                haltAllAction();
            }
        });
        haltAllButton.setForeground(Color.RED);
        panel.add(Box.createGlue());
        panel.add(haltAllButton);
        bottom.add(panel);///
        tablePanel.add(bottom);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dispose();
            }
        });
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        fileMenu.add(new jmri.configurexml.SaveMenu());
        menuBar.add(fileMenu);
        JMenu warrantMenu = new JMenu(Bundle.getMessage("MenuWarrant"));
        warrantMenu.add(new AbstractAction(Bundle.getMessage("ConcatWarrants")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                concatMenuAction();
            }
        });
        warrantMenu.add(new jmri.jmrit.logix.WarrantTableAction("CreateWarrant"));
        warrantMenu.add(WarrantTableAction._trackerTable);
        warrantMenu.add(new AbstractAction(Bundle.getMessage("CreateNXWarrant")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                nxAction();
            }
        });
        warrantMenu.add(WarrantTableAction.makeLogMenu());
        menuBar.add(warrantMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.logix.WarrantTable", true);

        getContentPane().add(tablePanel);
        pack();
    }

    protected void scrollTable() {
        JScrollBar bar = _tablePane.getVerticalScrollBar();
        bar.setValue(bar.getMaximum());
    }

    protected static void nxAction() {
        NXFrame nxFrame = NXFrame.getDefault();
        nxFrame.setVisible(true);
    }

    private void haltAllAction() {
        _model.haltAllTrains();
    }

    protected void concatMenuAction() {
        _concatDialog = new JDialog(this, Bundle.getMessage("ConcatWarrants"), false);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        panel.add(Box.createVerticalStrut(WarrantTableAction.STRUT_SIZE));
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
            @Override
            public void actionPerformed(ActionEvent e) {
                concatenate();
            }
        });
//        panel.add(Box.createVerticalStrut(WarrantTableAction.STRUT_SIZE));
        panel.add(concatButton, Box.CENTER_ALIGNMENT);
//        panel.add(Box.createVerticalStrut(WarrantTableAction.STRUT_SIZE));

        mainPanel.add(panel);
        _concatDialog.getContentPane().add(mainPanel);
        _concatDialog.setLocation(getLocation().x + 50, getLocation().y + 150);
        _concatDialog.pack();
        _concatDialog.setVisible(true);
    }

    private void concatenate() {
        /*
        WarrantManager manager = InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class);
        Warrant startW = manager.getWarrant(_startWarrant.getText().trim());
        Warrant endW = manager.getWarrant(_endWarrant.getText().trim());
         */
        Warrant startW = _model.getWarrant(_startWarrant.getText().trim());
        Warrant endW = _model.getWarrant(_endWarrant.getText().trim());
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
        if (!last.getPathName().equals(next.getPathName()) || !last.getBlock().equals(next.getBlock())) {
            showWarning("RoutesDontMatch");
            return;
        }
        WarrantTableAction.CreateWarrantFrame f = new WarrantTableAction.CreateWarrantFrame();
        try {
            f.initComponents();
            f.concatenate(startW, endW);
        } catch (Exception ex) {
            log.error("error making CreateWarrantFrame", ex);
        }
        f.setVisible(true);
        if (_concatDialog != null) {
            _concatDialog.dispose();
        }
    }

    public void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, Bundle.getMessage(msg, _startWarrant.getText(), _endWarrant.getText()),
                Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * *********************** Table ***************************************
     */
    static public class ComboBoxCellEditor extends DefaultCellEditor {

        ComboBoxCellEditor(JComboBox<String> comboBox) {
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
                log.error("Unexpected table model of class: {}", m.getClass().getName());
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
            Component component = getComponent();
            if (component instanceof JComboBox<?>) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) component;
                if (warrant == null) {
                    log.warn("getWarrantAt row= " + row + " Warrant is null!");
                    return comboBox;
                }
                comboBox.removeAllItems();

                List<BlockOrder> orders = warrant.getBlockOrders();
                for (int i = 0; i < orders.size(); i++) {
                    BlockOrder order = orders.get(i);
                    comboBox.addItem(order.getBlock().getDisplayName() + ": - " + order.getPath().getName());
                }
            } else {
                log.error("Unexpected editor component of class: {}", component.getClass().getName());
            }
            return component;
        }
    }

    /**
     * Return error message if warrant cannot be run.
     *
     * @param w    warrant
     * @param mode running type
     * @return null if warrant is started
     */
    public String runTrain(Warrant w, int mode) {
        String msg = null;
        if (w.getRunMode() != Warrant.MODE_NONE) {
            msg = w.getRunModeMessage();
            setStatusText(msg, Color.red, false);
            return msg;
        }
        msg = w.setRoute(0, null);
        setStatusText(msg, WarrantTableModel.myGold, false);
        if (msg != null) {
            setStatusText(msg, Color.red, false);
            return msg;
        }
        if (w.commandsHaveTrackSpeeds()) {
            w.getSpeedUtil().getValidSpeedProfile(this);            
        } else {
            setStatusText(Bundle.getMessage("NoTrackSpeeds", w.getDisplayName()), Color.red, true);
        }
        
        msg = w.setRunMode(mode, null, null, null, w.getRunBlind());
        if (msg != null) {
            setStatusText(msg, Color.red, false);
            return msg;
        }
        msg = w.checkStartBlock(mode);  // notify first block occupied by this train
        if (msg != null) {
            if (msg.equals("BlockDark")) {
                msg = Bundle.getMessage("BlockDark", w.getCurrentBlockName(), w.getTrainName());
            } else if (msg.equals("warnStart")) {
                msg = Bundle.getMessage("warnStart", w.getTrainName(), w.getCurrentBlockName());
            } else if (msg.equals("warnStartManual")) {
                msg = Bundle.getMessage("warnStartManual", w.getTrainName(), w.getCurrentBlockName());
            }
            setStatusText(msg, WarrantTableModel.myGold, false);
        }
        // From here on messages are status information, not abort info
        msg = w.checkRoute();   // notify about occupation ahead
        if (msg != null) {
            setStatusText(msg, WarrantTableModel.myGreen, false);
        }
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
        for (int i = _statusHistory.size() - 1; i >= 0; i--) {
            popup.add(_statusHistory.get(i));
        }
        popup.show(_status, 0, 0);
    }

    @Override
    public void mousePressed(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseExited(MouseEvent event) {
    }

    @Override
    public void mouseReleased(MouseEvent event) {
    }

    void setStatusText(String msg, Color c, boolean save) {
        /*      if (WarrantTableModel.myGold.equals(c)) {
            _status.setBackground(Color.lightGray);
        } else if (Color.red.equals(c)) {
            _status.setBackground(Color.white);
        } else {
            _status.setBackground(Color.white);
        }*/
        _status.setForeground(c);
        _status.setText(msg);
        if (save && msg != null && msg.length() > 0) {
            WarrantTableAction.writetoLog(msg);
            _statusHistory.add(msg);
            while (_statusHistory.size() > _maxHistorySize) {
                _statusHistory.remove(0);
            }
        }

    }
    static String BLANK = "                                                                                                 ";

    private final static Logger log = LoggerFactory.getLogger(WarrantTableFrame.class.getName());
}
