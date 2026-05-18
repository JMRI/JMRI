package jmri.jmrix.dccpp.swing.exrail;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
        super(false, false);
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

        JScrollPane scrollPane = new JScrollPane(_table);

        JButton triggerButton = new JButton(Bundle.getMessage("ExrailButtonTrigger"));
        triggerButton.addActionListener(e -> triggerSelected());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(triggerButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        pack();
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
        public Object getValueAt(int row, int col) {
            List<DCCppExrailEntry> list = List.copyOf(_entries.values());
            if (row >= list.size()) return "";
            DCCppExrailEntry e = list.get(row);
            if (col == 0) return e.isRoute()
                    ? Bundle.getMessage("ExrailTypeRoute")
                    : Bundle.getMessage("ExrailTypeAutomation");
            if (col == 1) return e.getDisplayName();
            if (col == 2) return e.getState() < 0 ? "" : String.valueOf(e.getState());
            return "";
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DCCppExrailFrame.class);
}
