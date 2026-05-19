package jmri.jmrix.dccpp.swing.exrail;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import jmri.PowerManager;
import jmri.jmrix.dccpp.DCCppExrailEntry;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.ThreadingUtil;
import jmri.jmrix.dccpp.DCCppInterface;
import jmri.jmrix.dccpp.DCCppListener;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppTrafficController;
import jmri.util.JmriJFrame;

/**
 * Displays DCC-EX EXRAIL Routes and Automations and allows triggering them.
 *
 * @author Chad Francis Copyright (C) 2026
 */
public class DCCppExrailFrame extends JmriJFrame implements DCCppListener {

    private final DCCppTrafficController _tc;
    private final DCCppSystemConnectionMemo _memo;

    private final Map<Integer, DCCppExrailEntry> _entries = new LinkedHashMap<>();
    private ExrailTableModel _tableModel;
    private JTable _table;
    private JButton _triggerButton;
    private PowerManager _powerManager;
    private PropertyChangeListener _powerListener;

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
        _table = new JTable(_tableModel);
        _table.setFillsViewportHeight(true);
        _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _table.setPreferredScrollableViewportSize(new Dimension(500, 200));
        _table.setAutoCreateRowSorter(true);

        _table.getColumnModel().getColumn(0).setPreferredWidth(40);
        _table.getColumnModel().getColumn(0).setMaxWidth(60);
        _table.getColumnModel().getColumn(1).setPreferredWidth(90);
        _table.getColumnModel().getColumn(1).setMaxWidth(110);

        _table.getTableHeader().setFont(
                _table.getTableHeader().getFont().deriveFont(Font.BOLD));

        _table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                DCCppExrailEntry entry = _tableModel.getEntryForRow(table.convertRowIndexToModel(row));
                boolean disabled = entry != null && entry.getState() == DCCppExrailEntry.State.DISABLED;
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                    setForeground(disabled ? Color.GRAY : table.getForeground());
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(_table);

        _triggerButton = new JButton(Bundle.getMessage("ExrailButtonTrigger"));
        _triggerButton.addActionListener(e -> triggerSelected());

        _tc.addDCCppListener(DCCppInterface.FEEDBACK, this);
        _tc.sendDCCppMessage(DCCppMessage.makeAutomationIDsMsg(), this);

        _powerManager = _memo.getPowerManager();
        if (_powerManager != null) {
            _powerListener = evt -> ThreadingUtil.runOnGUI(this::updateTriggerEnabled);
            _powerManager.addPropertyChangeListener(PowerManager.POWER, _powerListener);
            updateTriggerEnabled();
        }

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(_triggerButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        pack();
        if (getX() == 0 && getY() == 0) {
            setLocationRelativeTo(null); // center on first-ever open
        }
    }

    private void updateTriggerEnabled() {
        _triggerButton.setEnabled(_powerManager.getPower() == PowerManager.ON);
    }

    private void triggerSelected() {
        int row = _table.getSelectedRow();
        if (row < 0) return;
        DCCppExrailEntry entry = _tableModel.getEntryForRow(_table.convertRowIndexToModel(row));
        if (entry == null) return;
        if (entry.getState() == DCCppExrailEntry.State.DISABLED) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("ExrailDisabledAlert"),
                    Bundle.getMessage("ExrailDisabledTitle"),
                    JmriJOptionPane.WARNING_MESSAGE);
            return;
        }
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

    @Override
    public void message(DCCppReply reply) {
        if (reply.isAutomationIDsReply()) {
            for (int id : reply.getAutomationIDList()) {
                _tc.sendDCCppMessage(DCCppMessage.makeAutomationIDMsg(id), this);
            }
        } else if (reply.isAutomationIDReply()) {
            int id = reply.getAutomationIDInt();
            _entries.put(id, new DCCppExrailEntry(id, reply.getAutomationTypeString(), reply.getAutomationDescString()));
            ThreadingUtil.runOnGUIEventually(() -> _tableModel.fireTableDataChanged());
        } else if (reply.isAutomationStateReply()) {
            DCCppExrailEntry entry = _entries.get(reply.getAutomationIDInt());
            if (entry != null) {
                entry.setState(DCCppExrailEntry.State.fromValue(Integer.parseInt(reply.getAutomationStateString())));
                ThreadingUtil.runOnGUIEventually(() -> _tableModel.fireTableDataChanged());
            }
        } else if (reply.isAutomationCaptionReply()) {
            DCCppExrailEntry entry = _entries.get(reply.getAutomationIDInt());
            if (entry != null) {
                entry.setCaption(reply.getAutomationCaptionString());
                ThreadingUtil.runOnGUIEventually(() -> _tableModel.fireTableDataChanged());
            }
        }
    }

    @Override
    public void message(DCCppMessage msg) {}

    @Override
    public void notifyTimeout(DCCppMessage msg) {}

    @Override
    public void dispose() {
        if (_powerListener != null && _powerManager != null) {
            _powerManager.removePropertyChangeListener(PowerManager.POWER, _powerListener);
            _powerListener = null;
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

    /** Returns whether the trigger button is enabled; used by tests. */
    boolean isTriggerEnabled() {
        return _triggerButton.isEnabled();
    }

    private class ExrailTableModel extends AbstractTableModel {

        private final String[] columns = {
            Bundle.getMessage("ExrailColId"),
            Bundle.getMessage("ExrailColType"),
            Bundle.getMessage("ExrailColName"),
            Bundle.getMessage("ExrailColState")
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
            return col == 0 ? Integer.class : String.class;
        }

        @Override
        public Object getValueAt(int row, int col) {
            List<DCCppExrailEntry> list = visibleEntries();
            if (row >= list.size()) return "";
            DCCppExrailEntry entry = list.get(row);
            if (col == 0) return entry.getId();
            if (col == 1) return entry.isRoute()
                    ? Bundle.getMessage("ExrailTypeRoute")
                    : Bundle.getMessage("ExrailTypeAutomation");
            if (col == 2) return entry.getDisplayName();
            if (col == 3) {
                DCCppExrailEntry.State state = entry.getState();
                if (state == null || state == DCCppExrailEntry.State.INACTIVE) return Bundle.getMessage("ExrailStateIdle");
                if (state == DCCppExrailEntry.State.ACTIVE)   return Bundle.getMessage("ExrailStateRunning");
                if (state == DCCppExrailEntry.State.DISABLED) return Bundle.getMessage("ExrailStateDisabled");
                return String.valueOf(state.value);
            }
            return "";
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DCCppExrailFrame.class);
}
