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
import java.util.OptionalInt;
import java.util.Queue;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.dccpp.DCCppConstants;
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

/**
 * Displays DCC-EX EXRAIL Routes and Automations and allows triggering them.
 *
 * @author Chad Francis Copyright (C) 2026
 */
public class DCCppExrailFrame extends JmriJFrame implements DCCppListener {

    private static final int COL_ID = 0;
    private static final int COL_NAME = 1;
    private static final int COL_TRIGGER = 2;
    private static final int COL_TYPE = 3;

    private final DCCppTrafficController _tc;
    private final DCCppSystemConnectionMemo _memo;

    private final Map<Integer, DCCppExrailEntry> _entries = new LinkedHashMap<>();
    private final Queue<Integer> _pendingIds = new ArrayDeque<>();
    private ExrailTableModel _tableModel;
    private JTable _table;
    private PropertyChangeListener _connListener;
    private String _lastLocoAddress; // last valid address entered this session; null until first successful trigger

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
        _table = new JTable(_tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (column != COL_TRIGGER) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                }
                return c;
            }
        };
        _table.setFillsViewportHeight(true);
        _table.setPreferredScrollableViewportSize(new Dimension(560, 200));
        _table.setAutoCreateRowSorter(true);
        _table.setRowSelectionAllowed(false);
        _table.setColumnSelectionAllowed(false);
        _table.setCellSelectionEnabled(false);

        _table.getColumnModel().getColumn(COL_ID).setPreferredWidth(40);
        _table.getColumnModel().getColumn(COL_ID).setMaxWidth(60);
        _table.getColumnModel().getColumn(COL_TYPE).setPreferredWidth(90);
        _table.getColumnModel().getColumn(COL_TYPE).setMaxWidth(110);

        _table.getTableHeader().setFont(
                _table.getTableHeader().getFont().deriveFont(Font.BOLD));

        // Active entries show the trigger button pressed; disabled entries gray it out.
        _table.setDefaultRenderer(TriggerCellValue.class, new TriggerRenderer());
        _table.setDefaultEditor(TriggerCellValue.class, new ButtonEditor(new JButton()));
        JToggleButton sample = new JToggleButton(Bundle.getMessage("ExrailButtonSet"));
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

    /** Prompt for loco address if needed and fire the entry. */
    private void handleRowTrigger(DCCppExrailEntry entry) {
        if (entry == null) return;
        if (!isDisplayable()) return; // window already closing
        if (entry.isAutomation()) {
            OptionalInt addr = promptLocoAddress();
            if (addr.isPresent()) triggerEntry(entry, addr.getAsInt());
        } else {
            triggerEntry(entry, 0);
        }
    }

    /** Prompts repeatedly until a valid loco address is entered or the user cancels. */
    private OptionalInt promptLocoAddress() {
        while (true) {
            String input = (String) JmriJOptionPane.showInputDialog(this,
                    Bundle.getMessage("ExrailLabelLocoAddress"),
                    Bundle.getMessage("ExrailTitleLocoDialog"),
                    JmriJOptionPane.QUESTION_MESSAGE,
                    null, null, _lastLocoAddress);
            if (input == null) return OptionalInt.empty();
            try {
                int addr = Integer.parseInt(input.trim());
                if (addr >= 1 && addr <= DCCppConstants.MAX_LOCO_ADDRESS) {
                    _lastLocoAddress = input.trim();
                    return OptionalInt.of(addr);
                }
            } catch (NumberFormatException ex) { // invalid input — fall through to error dialog
            }
            showLocoAddressError();
        }
    }

    /** Shows an error dialog when the entered loco address is out of range or non-numeric. */
    private void showLocoAddressError() {
        JmriJOptionPane.showMessageDialog(this,
                Bundle.getMessage("ExrailLocoAddressInvalid", DCCppConstants.MAX_LOCO_ADDRESS),
                Bundle.getMessage("ExrailLocoAddressInvalidTitle"),
                JmriJOptionPane.ERROR_MESSAGE);
    }

    /** Returns the last loco address entered this session; used by tests. */
    String getLastLocoAddress() {
        return _lastLocoAddress;
    }

    void triggerEntry(DCCppExrailEntry entry, int locoAddress) {
        if (entry.isAutomation()) {
            log.debug("Triggering EXRAIL automation id={} locoAddress={}", entry.getId(), locoAddress);
            _tc.sendDCCppMessage(DCCppMessage.makeStartExrailMsg(entry.getId(), locoAddress), null);
        } else {
            log.debug("Triggering EXRAIL route id={}", entry.getId());
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
        if (_table != null && _table.isEditing()) {
            _table.getCellEditor().cancelCellEditing(); // prevent spurious trigger on window close
        }
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
        Object val = _tableModel.getValueAt(row, COL_TRIGGER);
        return val instanceof TriggerCellValue ? ((TriggerCellValue) val).label : "";
    }

    /** Returns the Name column value for the given visible row; used by tests. */
    String getRowName(int row) {
        return (String) _tableModel.getValueAt(row, COL_NAME);
    }

    /** Simulate a click on the trigger button for the given visible row; used by tests. */
    void triggerRowForTest(int row) {
        _tableModel.setValueAt(null, row, COL_TRIGGER);
    }

    private static final class TriggerCellValue {
        final String label;
        final boolean active;
        final boolean enabled;
        TriggerCellValue(String label, boolean active, boolean enabled) {
            this.label = label; this.active = active; this.enabled = enabled;
        }
        @Override public String toString() { return label; }
    }

    private static final class TriggerRenderer extends JToggleButton implements TableCellRenderer {

        TriggerRenderer() {
            setOpaque(true);
            putClientProperty("JComponent.sizeVariant", "small");
            putClientProperty("JButton.buttonType", "square");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof TriggerCellValue) {
                TriggerCellValue v = (TriggerCellValue) value;
                setText(v.label);
                setSelected(v.active);
                setEnabled(v.enabled);
            }
            return this;
        }
    }

    private class ExrailTableModel extends AbstractTableModel {

        private final String[] columns = {
            Bundle.getMessage("ExrailColId"),
            Bundle.getMessage("ExrailColName"),
            "", // trigger button column, no header
            Bundle.getMessage("ExrailColType"),
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
            if (col == COL_TRIGGER) return TriggerCellValue.class;
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
            if (col == COL_NAME) return entry.getDescription();
            if (col == COL_TRIGGER) {
                boolean active = entry.getState() == DCCppExrailEntry.State.ACTIVE;
                boolean enabled = entry.getState() != DCCppExrailEntry.State.DISABLED;
                String label = entry.getCaption() != null ? entry.getCaption() : Bundle.getMessage("ExrailButtonSet");
                return new TriggerCellValue(label, active, enabled);
            }
            if (col == COL_TYPE) return entry.isRoute()
                    ? Bundle.getMessage("ExrailTypeRoute")
                    : Bundle.getMessage("ExrailTypeAutomation");
            return "";
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col != COL_TRIGGER) return;
            DCCppExrailEntry entry = getEntryForRow(row);
            if (entry == null) return;
            ThreadingUtil.runOnGUIEventually(() -> handleRowTrigger(entry));
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DCCppExrailFrame.class);
}
