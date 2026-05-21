package jmri.jmrix.dccpp.swing.exrail;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.dccpp.DCCppExrailEntry;
import jmri.jmrix.dccpp.DCCppInterface;
import jmri.jmrix.dccpp.DCCppListener;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppTrafficController;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Displays DCC-EX EXRAIL Routes and Automations and allows triggering them.
 *
 * @author Chad Francis Copyright (C) 2026
 */
public class DCCppExrailFrame extends JmriJFrame implements DCCppListener {

    private static final int COL_ID = 0;
    private static final int COL_TYPE = 1;
    private static final int COL_NAME = 2;
    private static final int COL_STATE = 3;
    private static final int COL_TRIGGER = 4;

    private final DCCppTrafficController _tc;
    private final DCCppSystemConnectionMemo _memo;

    private final Map<Integer, DCCppExrailEntry> _entries = new LinkedHashMap<>();
    private final Queue<Integer> _pendingIds = new ArrayDeque<>();
    private ExrailTableModel _tableModel;
    private JTable _table;
    private PropertyChangeListener _connListener;

    public DCCppExrailFrame(DCCppSystemConnectionMemo memo) {
        super(true, false); // save position; let user resize freely
        _memo = memo;
        _tc = memo.getDCCppTrafficController();
        _tableModel = new ExrailTableModel();
        setTitle(Bundle.getMessage("ExrailFrameTitle") + " (" + _memo.getSystemPrefix() + ")");
    }

    @Override
    public void initComponents() {
        super.initComponents();
        // Override prepareRenderer so row striping + disabled-row gray apply
        // uniformly to every column, including Integer (ID) and JButton (Set).
        _table = new JTable(_tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                DCCppExrailEntry entry = _tableModel.getEntryForRow(convertRowIndexToModel(row));
                boolean disabled = entry != null && entry.getState() == DCCppExrailEntry.State.DISABLED;
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                if (column != COL_TRIGGER) {
                    c.setForeground(disabled ? Color.GRAY : getForeground());
                }
                return c;
            }
        };
        _table.setFillsViewportHeight(true);
        _table.setPreferredScrollableViewportSize(new Dimension(560, 200));
        _table.setAutoCreateRowSorter(true);
        // Each row has its own Set button, so row selection no longer drives anything.
        _table.setRowSelectionAllowed(false);
        _table.setColumnSelectionAllowed(false);
        _table.setCellSelectionEnabled(false);

        _table.getColumnModel().getColumn(COL_ID).setPreferredWidth(40);
        _table.getColumnModel().getColumn(COL_ID).setMaxWidth(60);
        _table.getColumnModel().getColumn(COL_TYPE).setPreferredWidth(90);
        _table.getColumnModel().getColumn(COL_TYPE).setMaxWidth(110);

        _table.getTableHeader().setFont(
                _table.getTableHeader().getFont().deriveFont(Font.BOLD));

        // Per-row trigger button column. Renderer subclass honours isCellEditable
        // so DISABLED-state rows render the button grayed out.
        ButtonRenderer triggerRenderer = new ButtonRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, false, row, column);
                c.setEnabled(table.isCellEditable(row, column));
                return c;
            }
        };
        _table.setDefaultRenderer(JButton.class, triggerRenderer);
        TableCellEditor triggerEditor = new ButtonEditor(new JButton());
        _table.setDefaultEditor(JButton.class, triggerEditor);
        JButton sample = new JButton(Bundle.getMessage("ExrailButtonSet"));
        _table.setRowHeight(sample.getPreferredSize().height);
        _table.getColumnModel().getColumn(COL_TRIGGER).setPreferredWidth(80);

        _tc.addDCCppListener(DCCppInterface.FEEDBACK, this);
        _tc.sendDCCppMessage(DCCppMessage.makeAutomationIDsMsg(), this);

        _connListener = evt -> {
            if (ConnectionStatus.CONNECTION_UP.equals(
                    ConnectionStatus.instance().getConnectionState(_memo))) {
                _entries.clear();
                _pendingIds.clear();
                _tc.sendDCCppMessage(DCCppMessage.makeAutomationIDsMsg(), this);
                ThreadingUtil.runOnGUI(this::redrawTable);
            }
        };
        ConnectionStatus.instance().addPropertyChangeListener(_memo, _connListener);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(_table), BorderLayout.CENTER);
        pack();
        if (getX() == 0 && getY() == 0) {
            setLocationRelativeTo(null); // center on first-ever open
        }
    }

    private void redrawTable() {
        _tableModel.fireTableDataChanged();
    }

    /**
     * Prompt for loco address if needed and fire the entry. Called from the
     * table model when a row's trigger button is clicked.
     */
    private void handleRowTrigger(DCCppExrailEntry entry) {
        if (entry == null) return;
        if (entry.isAutomation()) {
            String input = JmriJOptionPane.showInputDialog(this,
                    Bundle.getMessage("ExrailLabelLocoAddress"),
                    Bundle.getMessage("ExrailTitleLocoDialog"),
                    JmriJOptionPane.QUESTION_MESSAGE);
            if (input == null) return;
            try {
                triggerEntry(entry, Integer.parseInt(input.trim()));
            } catch (NumberFormatException ex) {
                log.warn("Invalid loco address entered: {}", input);
            }
        } else {
            triggerEntry(entry, 0);
        }
    }

    void triggerEntry(DCCppExrailEntry entry, int locoAddress) {
        if (entry.isAutomation()) {
            _tc.sendDCCppMessage(DCCppMessage.makeStartExrailMsg(entry.getId(), locoAddress), null);
        } else {
            _tc.sendDCCppMessage(DCCppMessage.makeStartExrailMsg(entry.getId()), null);
        }
    }

    private void requestNextId() {
        Integer id = _pendingIds.poll();
        if (id != null) {
            _tc.sendDCCppMessage(DCCppMessage.makeAutomationIDMsg(id), this);
        }
    }

    @Override
    public void message(DCCppReply reply) {
        if (reply.isAutomationIDsReply()) {
            for (int id : reply.getAutomationIDList()) {
                if (!_entries.containsKey(id) && !_pendingIds.contains(id)) {
                    _pendingIds.add(id);
                }
            }
            requestNextId();
        } else if (reply.isAutomationIDReply()) {
            int id = reply.getAutomationIDInt();
            _entries.put(id, new DCCppExrailEntry(id, reply.getAutomationTypeString(), reply.getAutomationDescString()));
            ThreadingUtil.runOnGUIEventually(this::redrawTable);
            requestNextId();
        } else if (reply.isAutomationStateReply()) {
            DCCppExrailEntry entry = _entries.get(reply.getAutomationIDInt());
            if (entry != null) {
                entry.setState(DCCppExrailEntry.State.fromValue(Integer.parseInt(reply.getAutomationStateString())));
                ThreadingUtil.runOnGUIEventually(this::redrawTable);
            }
        } else if (reply.isAutomationCaptionReply()) {
            DCCppExrailEntry entry = _entries.get(reply.getAutomationIDInt());
            if (entry != null) {
                entry.setCaption(reply.getAutomationCaptionString());
                ThreadingUtil.runOnGUIEventually(this::redrawTable);
            }
        }
    }

    @Override
    public void message(DCCppMessage msg) {}

    @Override
    public void notifyTimeout(DCCppMessage msg) {}

    @Override
    public void dispose() {
        if (_connListener != null) {
            ConnectionStatus.instance().removePropertyChangeListener(_memo, _connListener);
            _connListener = null;
        }
        _tc.removeDCCppListener(DCCppInterface.FEEDBACK, this);
        super.dispose();
    }

    /** Returns number of visible (non-hidden) entries; used by tests. */
    int getEntryCount() {
        return _tableModel.getRowCount();
    }

    /** Returns entry by id; used by tests. */
    DCCppExrailEntry getEntry(int id) {
        return _entries.get(id);
    }

    /** Returns whether the trigger button on the given visible row is enabled; used by tests. */
    boolean isRowTriggerEnabled(int row) {
        return _tableModel.isCellEditable(row, COL_TRIGGER);
    }

    /** Returns the trigger button label for the given visible row; used by tests. */
    String getRowButtonLabel(int row) {
        return (String) _tableModel.getValueAt(row, COL_TRIGGER);
    }

    /** Returns the Name column value for the given visible row; used by tests. */
    String getRowName(int row) {
        return (String) _tableModel.getValueAt(row, COL_NAME);
    }

    /** Simulate a click on the trigger button for the given visible row; used by tests. */
    void triggerRowForTest(int row) {
        _tableModel.setValueAt(null, row, COL_TRIGGER);
    }

    private class ExrailTableModel extends AbstractTableModel {

        private final String[] columns = {
            Bundle.getMessage("ExrailColId"),
            Bundle.getMessage("ExrailColType"),
            Bundle.getMessage("ExrailColName"),
            Bundle.getMessage("ExrailColState"),
            "" // trigger button column, no header
        };

        /** Entries visible in the table — Hidden (state 2) are excluded. */
        private List<DCCppExrailEntry> visibleEntries() {
            List<DCCppExrailEntry> list = new ArrayList<>();
            for (DCCppExrailEntry entry : _entries.values()) {
                if (entry.getState() != DCCppExrailEntry.State.HIDDEN) list.add(entry);
            }
            return list;
        }

        DCCppExrailEntry getEntryForRow(int modelRow) {
            List<DCCppExrailEntry> list = visibleEntries();
            if (modelRow < 0 || modelRow >= list.size()) return null;
            return list.get(modelRow);
        }

        @Override
        public int getRowCount() { return visibleEntries().size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Class<?> getColumnClass(int col) {
            if (col == COL_ID) return Integer.class;
            if (col == COL_TRIGGER) return JButton.class;
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col != COL_TRIGGER) return false;
            DCCppExrailEntry entry = getEntryForRow(row);
            if (entry == null) return false;
            return entry.getState() != DCCppExrailEntry.State.DISABLED;
        }

        @Override
        public Object getValueAt(int row, int col) {
            List<DCCppExrailEntry> list = visibleEntries();
            if (row >= list.size()) return "";
            DCCppExrailEntry entry = list.get(row);
            if (col == COL_ID) return entry.getId();
            if (col == COL_TYPE) return entry.isRoute()
                    ? Bundle.getMessage("ExrailTypeRoute")
                    : Bundle.getMessage("ExrailTypeAutomation");
            if (col == COL_NAME) return entry.getDescription();
            if (col == COL_STATE) {
                DCCppExrailEntry.State state = entry.getState();
                if (state == null || state == DCCppExrailEntry.State.INACTIVE) return Bundle.getMessage("ExrailStateIdle");
                if (state == DCCppExrailEntry.State.ACTIVE)   return Bundle.getMessage("ExrailStateRunning");
                if (state == DCCppExrailEntry.State.DISABLED) return Bundle.getMessage("ExrailStateDisabled");
                return String.valueOf(state.value);
            }
            if (col == COL_TRIGGER) return entry.getCaption() != null ? entry.getCaption() : Bundle.getMessage("ExrailButtonSet");
            return "";
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col != COL_TRIGGER) return;
            DCCppExrailEntry entry = getEntryForRow(row);
            if (entry == null) return;
            SwingUtilities.invokeLater(() -> handleRowTrigger(entry));
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DCCppExrailFrame.class);
}
