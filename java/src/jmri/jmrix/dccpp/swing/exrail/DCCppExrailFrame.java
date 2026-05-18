package jmri.jmrix.dccpp.swing.exrail;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import jmri.jmrix.dccpp.DCCppExrailEntry;
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
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(_table);

        JButton triggerButton = new JButton(Bundle.getMessage("ExrailButtonTrigger"));
        triggerButton.addActionListener(e -> triggerSelected());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(triggerButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        pack();
        if (getX() == 0 && getY() == 0) {
            setLocationRelativeTo(null); // center on first-ever open
        }
    }

    private void triggerSelected() {
        int row = _table.getSelectedRow();
        if (row < 0) return;
        List<DCCppExrailEntry> entries = List.copyOf(_entries.values());
        DCCppExrailEntry entry = entries.get(_table.convertRowIndexToModel(row));
        if (entry.isAutomation()) {
            String input = JOptionPane.showInputDialog(this,
                    Bundle.getMessage("ExrailLabelLocoAddress"),
                    Bundle.getMessage("ExrailTitleLocoDialog"),
                    JOptionPane.QUESTION_MESSAGE);
            if (input == null) return;
            try {
                int address = Integer.parseInt(input.trim());
                _tc.sendDCCppMessage(DCCppMessage.makeStartExrailMsg(entry.getId(), address), null);
            } catch (NumberFormatException ex) {
                log.warn("Invalid loco address entered: {}", input);
            }
        } else {
            _tc.sendDCCppMessage(DCCppMessage.makeStartExrailMsg(entry.getId()), null);
        }
    }

    @Override
    public void message(DCCppReply r) {
        if (r.isAutomationIDsReply()) {
            for (int id : r.getAutomationIDList()) {
                _tc.sendDCCppMessage(DCCppMessage.makeAutomationIDMsg(id), this);
            }
        } else if (r.isAutomationIDReply()) {
            int id = r.getAutomationIDInt();
            _entries.put(id, new DCCppExrailEntry(id, r.getAutomationTypeString(), r.getAutomationDescString()));
            _tableModel.fireTableDataChanged();
        } else if (r.isAutomationStateReply()) {
            DCCppExrailEntry e = _entries.get(r.getAutomationIDInt());
            if (e != null) {
                e.setState(Integer.parseInt(r.getAutomationStateString()));
                _tableModel.fireTableDataChanged();
            }
        } else if (r.isAutomationCaptionReply()) {
            DCCppExrailEntry e = _entries.get(r.getAutomationIDInt());
            if (e != null) {
                e.setCaption(r.getAutomationCaptionString());
                _tableModel.fireTableDataChanged();
            }
        }
    }

    @Override
    public void message(DCCppMessage m) {}

    @Override
    public void notifyTimeout(DCCppMessage m) {}

    @Override
    public void dispose() {
        _tc.removeDCCppListener(DCCppInterface.FEEDBACK, this);
        super.dispose();
    }

    /** Returns number of loaded entries; used by tests. */
    public int getEntryCount() {
        return _entries.size();
    }

    /** Returns entry by id; used by tests. */
    public DCCppExrailEntry getEntry(int id) {
        return _entries.get(id);
    }

    private class ExrailTableModel extends AbstractTableModel {

        private final String[] COLUMNS = {
            Bundle.getMessage("ExrailColId"),
            Bundle.getMessage("ExrailColType"),
            Bundle.getMessage("ExrailColName"),
            Bundle.getMessage("ExrailColState")
        };

        @Override
        public int getRowCount() { return _entries.size(); }

        @Override
        public int getColumnCount() { return COLUMNS.length; }

        @Override
        public String getColumnName(int col) { return COLUMNS[col]; }

        @Override
        public Class<?> getColumnClass(int col) {
            return col == 0 ? Integer.class : String.class;
        }

        @Override
        public Object getValueAt(int row, int col) {
            List<DCCppExrailEntry> list = List.copyOf(_entries.values());
            if (row >= list.size()) return "";
            DCCppExrailEntry e = list.get(row);
            if (col == 0) return e.getId();
            if (col == 1) return e.isRoute()
                    ? Bundle.getMessage("ExrailTypeRoute")
                    : Bundle.getMessage("ExrailTypeAutomation");
            if (col == 2) return e.getDisplayName();
            if (col == 3) {
                int s = e.getState();
                if (s < 0) return "";
                if (s == 0) return Bundle.getMessage("ExrailStateIdle");
                if (s == 1) return Bundle.getMessage("ExrailStateRunning");
                if (s == 2) return Bundle.getMessage("ExrailStateHidden");
                if (s == 4) return Bundle.getMessage("ExrailStateDisabled");
                return String.valueOf(s);
            }
            return "";
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DCCppExrailFrame.class);
}
