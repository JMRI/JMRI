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
 * names they use that are not defined and any that cannot run where they are.
 * The table can be filtered, copied, saved as CSV and printed; double-clicking
 * a row edits its Transit. Actions can be copied to another Transit's Section,
 * and a name replaced in all the actions that use it.
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
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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
        JButton copyTo = new JButton(rbx.getString("ActionsReportCopyTo"));
        copyTo.setToolTipText(rbx.getString("ActionsReportCopyToHint"));
        copyTo.addActionListener(e -> copyToDialog());
        buttons.add(copyTo);
        JButton replace = new JButton(rbx.getString("ActionsReportReplace"));
        replace.setToolTipText(rbx.getString("ActionsReportReplaceHint"));
        replace.addActionListener(e -> replaceDialog());
        buttons.add(replace);
        JButton refresh = new JButton(rbx.getString("ActionsReportRefresh"));
        refresh.addActionListener(e -> refresh());
        buttons.add(refresh);
        JButton copy = new JButton(rbx.getString("ActionsReportCopy"));
        copy.setToolTipText(rbx.getString("ActionsReportCopyHint"));
        copy.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(reportText()), null));
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

    /**
     * The rows shown as text to read, as Copy puts on the clipboard: each
     * Transit, then each of its Sections with actions, then each action's When
     * and What, and any problems.
     *
     * @return the text
     */
    String reportText() {
        StringBuilder sb = new StringBuilder();
        String transit = null;
        String section = null;
        for (int v = 0; v < table.getRowCount(); v++) {
            Row r = model.rows.get(table.convertRowIndexToModel(v));
            if (!r.transitName.equals(transit)) {
                transit = r.transitName;
                section = null;
                sb.append(MessageFormat.format(rbx.getString("ActionsReportTextTransit"), transit)).append('\n');
            }
            String thisSection = r.sequence + ": " + r.section;
            if (!thisSection.equals(section)) {
                section = thisSection;
                sb.append("  ").append(MessageFormat.format(rbx.getString("ActionsReportTextSection"),
                        String.valueOf(r.sequence), r.section)).append('\n');
            }
            sb.append("    ").append(MessageFormat.format(rbx.getString("ActionsReportTextWhen"), r.when)).append('\n');
            sb.append("    ").append(MessageFormat.format(rbx.getString("ActionsReportTextWhat"), r.what)).append('\n');
            for (String p : r.problems) {
                sb.append("    ").append(MessageFormat.format(rbx.getString("ActionsReportTextProblem"), p))
                        .append('\n');
            }
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

    /**
     * The model rows selected, in the order shown.
     *
     * @return the rows; empty if none is selected
     */
    List<Integer> selectedRows() {
        List<Integer> rows = new ArrayList<>();
        for (int v : table.getSelectedRows()) {
            rows.add(table.convertRowIndexToModel(v));
        }
        return rows;
    }

    /**
     * The model rows shown, in the order shown.
     *
     * @return the rows
     */
    List<Integer> shownRows() {
        List<Integer> rows = new ArrayList<>();
        for (int v = 0; v < table.getRowCount(); v++) {
            rows.add(table.convertRowIndexToModel(v));
        }
        return rows;
    }

    /**
     * Copy actions to the end of a Transit Section's actions, in the order
     * given, then read the Transits again.
     *
     * @param rows    model rows of the actions to copy
     * @param transit the Transit to copy them to; nothing is copied if it is
     *                in use by an Active Train
     * @param target  the Transit Section of that Transit to copy them to
     * @return the number of actions copied
     */
    int copyActions(List<Integer> rows, Transit transit, TransitSection target) {
        if (transit.getState() == Transit.ASSIGNED) {
            return 0;
        }
        for (int r : rows) {
            target.addAction(TransitActionText.copy(model.rows.get(r).action));
        }
        refresh();
        return rows.size();
    }

    /**
     * Replace a name in actions, then read the Transits again. Actions of a
     * Transit in use by an Active Train are left as they are.
     *
     * @param rows    model rows of the actions to change
     * @param oldName the name to replace
     * @param newName the name to use instead
     * @return the number of actions changed
     */
    int replaceName(List<Integer> rows, String oldName, String newName) {
        int changed = 0;
        for (int r : rows) {
            Row row = model.rows.get(r);
            if (row.transit.getState() != Transit.ASSIGNED
                    && TransitActionText.replaceName(row.action, oldName, newName)) {
                changed++;
            }
        }
        refresh();
        return changed;
    }

    /**
     * Count the actions in rows that use a name, and those of them in a
     * Transit in use by an Active Train.
     *
     * @param rows model rows
     * @param name the name
     * @return {actions using it, of which in use}
     */
    int[] countUses(List<Integer> rows, String name) {
        int uses = 0;
        int inUse = 0;
        for (int r : rows) {
            Row row = model.rows.get(r);
            if (TransitActionText.replaceName(TransitActionText.copy(row.action), name, name + "_")) {
                uses++;
                if (row.transit.getState() == Transit.ASSIGNED) {
                    inUse++;
                }
            }
        }
        return new int[]{uses, inUse};
    }

    private void copyToDialog() {
        List<Integer> rows = selectedRows();
        if (rows.isEmpty()) {
            JmriJOptionPane.showMessageDialog(this, rbx.getString("ActionsReportSelectFirst"),
                    getTitle(), JmriJOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JComboBox<Transit> transits = new JComboBox<>();
        List<Transit> list = new ArrayList<>(InstanceManager.getDefault(TransitManager.class).getNamedBeanSet());
        list.sort((a, b) -> a.getDisplayName().compareTo(b.getDisplayName()));
        list.forEach(transits::addItem);
        transits.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                return super.getListCellRendererComponent(l, v == null ? "" : ((Transit) v).getDisplayName(), i, s, f);
            }
        });
        JComboBox<String> sections = new JComboBox<>();
        Runnable fill = () -> {
            sections.removeAllItems();
            Transit t = (Transit) transits.getSelectedItem();
            if (t != null) {
                for (TransitSection ts : t.getTransitSectionList()) {
                    sections.addItem(ts.getSequenceNumber() + ": " + ts.getSectionName());
                }
            }
        };
        transits.addActionListener(e -> fill.run());
        fill.run();
        JPanel p = new JPanel(new java.awt.GridLayout(0, 2, 5, 5));
        p.add(new JLabel(rbx.getString("ActionsReportTransit")));
        p.add(transits);
        p.add(new JLabel(rbx.getString("ActionsReportColSection") + ":"));
        p.add(sections);
        if (JmriJOptionPane.showConfirmDialog(this, p, MessageFormat.format(rbx.getString("ActionsReportCopyTitle"),
                rows.size()), JmriJOptionPane.OK_CANCEL_OPTION) != JmriJOptionPane.OK_OPTION
                || transits.getSelectedItem() == null || sections.getSelectedIndex() < 0) {
            return;
        }
        Transit t = (Transit) transits.getSelectedItem();
        TransitSection target = t.getTransitSectionList().get(sections.getSelectedIndex());
        if (t.getState() == Transit.ASSIGNED) {
            JmriJOptionPane.showMessageDialog(this, MessageFormat.format(rbx.getString("ActionsReportInUse"),
                    t.getDisplayName()), getTitle(), JmriJOptionPane.WARNING_MESSAGE);
            return;
        }
        int n = copyActions(rows, t, target);
        JmriJOptionPane.showMessageDialog(this, MessageFormat.format(rbx.getString("ActionsReportCopied"),
                n, t.getDisplayName(), sections.getSelectedItem()), getTitle(), JmriJOptionPane.INFORMATION_MESSAGE);
    }

    private void replaceDialog() {
        List<Integer> rows = selectedRows().isEmpty() ? shownRows() : selectedRows();
        JTextField oldField = new JTextField(findField.getText().trim(), 20);
        JTextField newField = new JTextField(20);
        JPanel p = new JPanel(new java.awt.GridLayout(0, 2, 5, 5));
        p.add(new JLabel(rbx.getString("ActionsReportReplaceOld")));
        p.add(oldField);
        p.add(new JLabel(rbx.getString("ActionsReportReplaceNew")));
        p.add(newField);
        if (JmriJOptionPane.showConfirmDialog(this, p, rbx.getString("ActionsReportReplaceTitle"),
                JmriJOptionPane.OK_CANCEL_OPTION) != JmriJOptionPane.OK_OPTION) {
            return;
        }
        String oldName = oldField.getText().trim();
        String newName = newField.getText().trim();
        if (oldName.isEmpty() || newName.isEmpty() || oldName.equals(newName)) {
            return;
        }
        int[] uses = countUses(rows, oldName);
        if (uses[0] == 0) {
            JmriJOptionPane.showMessageDialog(this, MessageFormat.format(rbx.getString("ActionsReportNotUsed"),
                    oldName), getTitle(), JmriJOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String question = MessageFormat.format(rbx.getString("ActionsReportReplaceConfirm"),
                oldName, newName, uses[0] - uses[1]);
        if (uses[1] > 0) {
            question = question + "\n" + MessageFormat.format(rbx.getString("ActionsReportReplaceSkipped"), uses[1]);
        }
        if (JmriJOptionPane.showConfirmDialog(this, question, rbx.getString("ActionsReportReplaceTitle"),
                JmriJOptionPane.OK_CANCEL_OPTION) == JmriJOptionPane.OK_OPTION) {
            replaceName(rows, oldName, newName);
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
        final TransitSectionAction action;
        final String transitName;
        final int sequence;
        final String section;
        final String when;
        final String what;
        final List<String> problems;

        Row(Transit t, TransitSection ts, TransitSectionAction tsa) {
            transit = t;
            action = tsa;
            transitName = t.getDisplayName();
            sequence = ts.getSequenceNumber();
            section = ts.getSection() == null ? "" : ts.getSection().getDisplayName();
            when = TransitActionText.whenText(tsa);
            what = TransitActionText.whatText(tsa);
            problems = TransitActionText.problems(ts, tsa);
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
