package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javax.annotation.CheckForNull;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import jmri.InstanceManager;
import jmri.Transit;
import jmri.TransitManager;
import jmri.TransitSection;
import jmri.TransitSectionAction;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriMouseAdapter;
import jmri.util.swing.JmriMouseEvent;
import jmri.util.swing.JmriMouseListener;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.table.JTableToCsvAction;

/**
 * Lists the Special Actions of every Transit, or of one, in one table, with any
 * names they use that are not defined. The table can be filtered, copied,
 * saved as CSV and printed; double-clicking a row edits its Transit.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TransitActionsFrame extends JmriJFrame {

    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.SectionTransitTableBundle");

    private final transient Consumer<Transit> editor;
    private final ActionsTableModel model = new ActionsTableModel();
    private final JTable table = new JTable(model);
    private final TableRowSorter<ActionsTableModel> sorter = new TableRowSorter<>(model);
    private final JComboBox<String> transitBox = new JComboBox<>();
    private final JTextField findField = new JTextField(15);
    private final JCheckBox problemsOnly = new JCheckBox(rbx.getString("ActionsReportProblemsOnly"));
    private final JLabel summary = new JLabel();

    /**
     * Create the window.
     *
     * @param editor opens a Transit for editing on a double-click; null for none
     */
    public TransitActionsFrame(@CheckForNull Consumer<Transit> editor) {
        super(rbx.getString("ActionsReportTitle"));
        this.editor = editor;
    }

    @Override
    public void initComponents() {
        addHelpMenu("package.jmri.jmrit.beantable.TransitTable", true);
        getContentPane().setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel(rbx.getString("ActionsReportTransit")));
        top.add(transitBox);
        top.add(new JLabel(rbx.getString("ActionsReportFind")));
        findField.setToolTipText(rbx.getString("ActionsReportFindHint"));
        top.add(findField);
        top.add(problemsOnly);
        getContentPane().add(top, BorderLayout.NORTH);

        table.setRowSorter(sorter);
        table.setToolTipText(editor == null ? null : rbx.getString("ActionsReportEditHint"));
        table.addMouseListener(JmriMouseListener.adapt(new JmriMouseAdapter() {
            @Override
            public void mouseClicked(JmriMouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    editRow(table.convertRowIndexToModel(table.getSelectedRow()));
                }
            }
        }));
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(summary, BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton(rbx.getString("ActionsReportRefresh"));
        refresh.addActionListener(e -> refresh());
        buttons.add(refresh);
        JButton copy = new JButton(rbx.getString("ActionsReportCopy"));
        copy.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(visibleText()), null));
        buttons.add(copy);
        buttons.add(new JButton(new JTableToCsvAction(rbx.getString("ActionsReportSaveCsv"), table, model,
                "TransitActions.csv", new int[]{})));
        JButton print = new JButton(rbx.getString("ActionsReportPrint"));
        print.addActionListener(e -> print());
        buttons.add(print);
        bottom.add(buttons, BorderLayout.EAST);
        getContentPane().add(bottom, BorderLayout.SOUTH);

        transitBox.addActionListener(e -> applyFilter());
        problemsOnly.addActionListener(e -> applyFilter());
        findField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });

        refresh();
        pack();
    }

    /**
     * Read the Transits and their actions again.
     */
    public void refresh() {
        Object chosen = transitBox.getSelectedItem();
        List<Row> rows = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (Transit t : InstanceManager.getDefault(TransitManager.class).getNamedBeanSet()) {
            names.add(t.getDisplayName());
            for (TransitSection ts : t.getTransitSectionList()) {
                for (TransitSectionAction tsa : ts.getTransitSectionActionList()) {
                    rows.add(new Row(t, ts, tsa));
                }
            }
        }
        Collections.sort(names);
        transitBox.removeAllItems();
        transitBox.addItem(rbx.getString("ActionsReportAll"));
        names.forEach(transitBox::addItem);
        if (chosen != null && names.contains(chosen.toString())) {
            transitBox.setSelectedItem(chosen);
        }
        model.setRows(rows);
        applyFilter();
    }

    /**
     * Choose the Transit to show.
     *
     * @param name a Transit's display name; null for all
     */
    public void setTransit(@CheckForNull String name) {
        transitBox.setSelectedItem(name == null ? rbx.getString("ActionsReportAll") : name);
    }

    void applyFilter() {
        String transit = transitBox.getSelectedIndex() <= 0 ? null : (String) transitBox.getSelectedItem();
        String find = findField.getText().trim().toLowerCase(Locale.ROOT);
        boolean onlyProblems = problemsOnly.isSelected();
        sorter.setRowFilter(new RowFilter<ActionsTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends ActionsTableModel, ? extends Integer> entry) {
                Row r = model.rows.get(entry.getIdentifier());
                if (transit != null && !transit.equals(r.transitName)) {
                    return false;
                }
                if (onlyProblems && r.problems.isEmpty()) {
                    return false;
                }
                if (!find.isEmpty()) {
                    for (int c = 0; c < entry.getValueCount(); c++) {
                        if (entry.getStringValue(c).toLowerCase(Locale.ROOT).contains(find)) {
                            return true;
                        }
                    }
                    return false;
                }
                return true;
            }
        });
        int problems = 0;
        List<String> transits = new ArrayList<>();
        for (int v = 0; v < table.getRowCount(); v++) {
            Row r = model.rows.get(table.convertRowIndexToModel(v));
            if (!r.problems.isEmpty()) {
                problems++;
            }
            if (!transits.contains(r.transitName)) {
                transits.add(r.transitName);
            }
        }
        summary.setText(MessageFormat.format(rbx.getString("ActionsReportSummary"),
                table.getRowCount(), transits.size(), problems));
    }

    /**
     * The rows shown, as tab-separated text with a header line.
     *
     * @return the text
     */
    String visibleText() {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < model.getColumnCount(); c++) {
            sb.append(c == 0 ? "" : "\t").append(model.getColumnName(c));
        }
        sb.append('\n');
        for (int v = 0; v < table.getRowCount(); v++) {
            int m = table.convertRowIndexToModel(v);
            for (int c = 0; c < model.getColumnCount(); c++) {
                sb.append(c == 0 ? "" : "\t").append(model.getValueAt(m, c));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    String summaryText() {
        return summary.getText();
    }

    JTable getTable() {
        return table;
    }

    JTextField getFindField() {
        return findField;
    }

    JCheckBox getProblemsOnly() {
        return problemsOnly;
    }

    void editRow(int modelRow) {
        if (editor != null) {
            editor.accept(model.rows.get(modelRow).transit);
        }
    }

    private void print() {
        try {
            table.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat(getTitle()),
                    new MessageFormat(rbx.getString("ActionsReportPage")));
        } catch (PrinterException ex) {
            JmriJOptionPane.showMessageDialog(this, ex.getLocalizedMessage(),
                    Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
        }
    }

    private static final class Row {

        final Transit transit;
        final String transitName;
        final int sequence;
        final String section;
        final String when;
        final String what;
        final List<String> problems;

        Row(Transit t, TransitSection ts, TransitSectionAction tsa) {
            transit = t;
            transitName = t.getDisplayName();
            sequence = ts.getSequenceNumber();
            section = ts.getSection() == null ? "" : ts.getSection().getDisplayName();
            when = TransitActionText.whenText(tsa);
            what = TransitActionText.whatText(tsa);
            problems = TransitActionText.problems(tsa);
        }
    }

    static final class ActionsTableModel extends AbstractTableModel {

        static final int TRANSIT = 0;
        static final int SEQUENCE = 1;
        static final int SECTION = 2;
        static final int WHEN = 3;
        static final int WHAT = 4;
        static final int PROBLEMS = 5;
        private static final String[] KEYS = {"ActionsReportColTransit", "ActionsReportColSeq",
            "ActionsReportColSection", "ActionsReportColWhen", "ActionsReportColWhat", "ActionsReportColProblems"};

        private List<Row> rows = new ArrayList<>();

        void setRows(List<Row> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return KEYS.length;
        }

        @Override
        public String getColumnName(int col) {
            return rbx.getString(KEYS[col]);
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return col == SEQUENCE ? Integer.class : String.class;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Row r = rows.get(row);
            switch (col) {
                case TRANSIT:
                    return r.transitName;
                case SEQUENCE:
                    return r.sequence;
                case SECTION:
                    return r.section;
                case WHEN:
                    return r.when;
                case WHAT:
                    return r.what;
                case PROBLEMS:
                    return String.join("; ", r.problems);
                default:
                    return "";
            }
        }
    }
}
