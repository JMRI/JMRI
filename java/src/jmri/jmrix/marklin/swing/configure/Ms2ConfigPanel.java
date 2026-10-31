package jmri.jmrix.marklin.swing.configure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import jmri.jmrit.roster.Roster;
import jmri.jmrix.marklin.McanDevice;
import jmri.jmrix.marklin.McanDeviceIdentity;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.jmrix.marklin.Ms2LocoInfoService;
import jmri.jmrix.marklin.Ms2LocoRecord;
import jmri.jmrix.marklin.Ms2RosterImporter;
import jmri.jmrix.marklin.SeniorThrottleService;
import jmri.jmrix.marklin.NativeDeviceService;
import jmri.jmrix.marklin.swing.MarklinPanel;

/** MS2 role and read-only connection local locomotive inspection. */
public class Ms2ConfigPanel extends MarklinPanel {
    private final McanDevice device;
    private final SeniorThrottleService senior;
    private final Ms2LocoInfoService locos;
    private final JLabel role = new JLabel();
    private final JLabel status = new JLabel();
    private final JButton getLocos = new JButton(Bundle.getMessage("Ms2GetLocos"));
    private final JButton importLoco = new JButton(Bundle.getMessage("Ms2Import"));
    private final LocoTableModel model = new LocoTableModel();
    private final JTable table = new JTable(model);
    private final JTextArea rawText = new JTextArea(15, 70);
    private final PropertyChangeListener listener = e -> refresh();
    private final Timer timer;
    private final String systemPrefix;
    private String importMessage;
    private boolean disposed;
    private List<Ms2LocoRecord> shown = List.of();

    public Ms2ConfigPanel(McanDevice device, MarklinSystemConnectionMemo memo) {
        this.device = device;
        super.initComponents(memo);
        senior = memo.getSeniorThrottleService();
        locos = memo.getMs2LocoInfoService();
        setLayout(new BorderLayout());
        JPanel header = new JPanel(new java.awt.GridLayout(0, 1));
        header.add(role);
        header.add(new JLabel(Bundle.getMessage("Ms2RoleHelp")));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(getLocos);
        actions.add(importLoco);
        actions.add(status);
        header.add(actions);
        header.add(new JLabel(Bundle.getMessage("Ms2FieldsHelp")));
        add(header, BorderLayout.NORTH);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getSelectionModel().addListSelectionListener(e -> {
            importMessage = null;
            showRaw();
            updateImport();
        });
        rawText.setEditable(false);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(table), new JScrollPane(rawText));
        split.setResizeWeight(0.5);
        split.setPreferredSize(new Dimension(950, 550));
        add(split, BorderLayout.CENTER);
        getLocos.addActionListener(e -> {
            importMessage = null;
            refresh();
            if (getLocos.isEnabled()) {
                locos.requestLocos(device);
            }
        });
        systemPrefix = memo.getSystemPrefix();
        importLoco.setToolTipText(Bundle.getMessage("Ms2ImportTip"));
        importLoco.addActionListener(e -> importSelected());
        senior.addPropertyChangeListener(listener);
        locos.addPropertyChangeListener(listener);
        // Expiry also changes the selected device to unknown when another master remains.
        timer = new Timer(1_000, e -> refresh());
        timer.start();
        refresh();
    }

    private void refresh() {
        if (disposed) {
            return;
        }
        SeniorThrottleService.Role mode = senior.getRole(device);
        if (mode == SeniorThrottleService.Role.MASTER) {
            role.setText(Bundle.getMessage("Ms2Master"));
        } else if (mode == SeniorThrottleService.Role.JUNIOR) {
            role.setText(Bundle.getMessage("Ms2Junior",
                    senior.getMaster().map(McanDevice::getDisplayName).orElse("")));
        } else {
            role.setText(Bundle.getMessage("Ms2Unknown"));
        }
        getLocos.setVisible(mode == SeniorThrottleService.Role.MASTER);
        getLocos.setEnabled(mode == SeniorThrottleService.Role.MASTER && !locos.isBusy()
                && NativeDeviceService.get(device).map(nativeState ->
                        nativeState.getObservedHash().isPresent()).orElse(false));
        if (importMessage != null) {
            status.setText(importMessage);
            status.setToolTipText(importMessage);
        } else if (locos.getTarget() == device) {
            status.setText(Bundle.getMessage("Ms2State" + locos.getState().name())
                    + (locos.getDetail().isEmpty() ? "" : " - " + locos.getDetail()));
            status.setToolTipText(locos.getDetail());
        } else {
            status.setText(locos.isBusy() ? Bundle.getMessage("Ms2Busy") : "");
        }
        List<Ms2LocoRecord> records = locos.getLocos(device);
        if (!records.equals(shown)) {
            shown = records;
            model.setRecords(records);
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 180 : 130);
            }
            showRaw();
        }
        if (records.isEmpty()) {
            rawText.setText(locos.getListText(device));
        }
        updateImport();
    }

    private Ms2LocoRecord selectedRecord() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= table.getRowCount()) {
            return null;
        }
        return model.records.get(table.convertRowIndexToModel(row));
    }

    private void updateImport() {
        Ms2LocoRecord record = selectedRecord();
        importLoco.setEnabled(record != null && Ms2RosterImporter.isImportable(record));
    }

    private void importSelected() {
        Ms2LocoRecord record = selectedRecord();
        if (record == null || !(device.getIdentity() instanceof McanDeviceIdentity.Native)) {
            return;
        }
        String ms2Uid = ((McanDeviceIdentity.Native) device.getIdentity()).getUid().toHexString();
        try {
            Ms2RosterImporter.Result result = new Ms2RosterImporter(Roster.getDefault())
                    .importLoco(record, ms2Uid, systemPrefix);
            importMessage = Bundle.getMessage(result.getOutcome() == Ms2RosterImporter.Outcome.CREATED
                    ? "Ms2ImportCreated" : "Ms2ImportUpdated", result.getEntry().getId());
        } catch (Ms2RosterImporter.ImportException ex) {
            importMessage = ex.getMessage();
        }
        refresh();
    }

    private void showRaw() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= table.getRowCount()) {
            rawText.setText(locos.getListText(device));
            return;
        }
        Ms2LocoRecord record = model.records.get(table.convertRowIndexToModel(row));
        rawText.setText(record.getListText() + "\n" + record.getInfoText());
        rawText.setCaretPosition(0);
    }

    @Override
    public String getTitle() { return Bundle.getMessage("Ms2Title"); }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        timer.stop();
        senior.removePropertyChangeListener(listener);
        locos.removePropertyChangeListener(listener);
        super.dispose();
    }

    public JLabel getRoleLabel() { return role; }
    public JButton getGetLocosButton() { return getLocos; }
    public JButton getImportButton() { return importLoco; }
    public JLabel getStatusLabel() { return status; }
    public JTable getLocoTable() { return table; }
    public JTextArea getRawText() { return rawText; }

    private static final class LocoTableModel extends AbstractTableModel {
        private List<Ms2LocoRecord> records = List.of();

        void setRecords(List<Ms2LocoRecord> values) {
            records = values;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return records.size(); }
        @Override public int getColumnCount() { return 6; }
        @Override public String getColumnName(int column) {
            switch (column) {
                case 0: return Bundle.getMessage("Ms2Name");
                case 1: return Bundle.getMessage("Ms2Uid");
                case 2: return Bundle.getMessage("Ms2Type");
                case 3: return Bundle.getMessage("Ms2MfxUid");
                case 4: return Bundle.getMessage("Ms2Protocol");
                case 5: return Bundle.getMessage("Ms2Id");
                default: return "";
            }
        }
        @Override public Object getValueAt(int row, int column) {
            Ms2LocoRecord record = records.get(row);
            switch (column) {
                case 0: return record.getName();
                case 1: return record.getField(".uid");
                case 2: return record.getField(".typ");
                case 3: return record.getField(".mfxuid");
                case 4: return record.getProtocol();
                case 5: return record.getProtocolId();
                default: return "";
            }
        }
    }
}
