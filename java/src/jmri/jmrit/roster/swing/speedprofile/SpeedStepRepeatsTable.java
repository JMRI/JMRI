package jmri.jmrit.roster.swing.speedprofile;

import java.awt.BorderLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;

/**
 * Show the speed steps repeated at the end of a profile: for each step and
 * direction, the speed measured in the profile, and the mean, range and
 * spread of the repeats.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
class SpeedStepRepeatsTable extends jmri.util.JmriJFrame {

    final RepeatsTableModel model;

    SpeedStepRepeatsTable(SpeedStepRepeats repeats, String rosterId) {
        super(false, true);
        setTitle(Bundle.getMessage("RepeatsTitle", rosterId));
        getContentPane().setLayout(new BorderLayout(5, 5));

        JPanel top = new JPanel();
        top.setBorder(new EmptyBorder(8, 8, 0, 8));
        top.setLayout(new BoxLayout(top, BoxLayout.PAGE_AXIS));
        top.add(new JLabel(Bundle.getMessage("RepeatsInfo_1")));
        top.add(new JLabel(Bundle.getMessage("RepeatsInfo_2")));
        getContentPane().add(top, BorderLayout.NORTH);

        model = new RepeatsTableModel(repeats);
        JTable table = new JTable(model);
        JScrollPane pane = new JScrollPane(table);
        getContentPane().add(pane, BorderLayout.CENTER);
        pack();
    }

    static class RepeatsTableModel extends AbstractTableModel {

        static final int STEP_COL = 0;
        static final int THROTTLE_COL = 1;
        static final int DIRECTION_COL = 2;
        static final int FIRST_COL = 3;
        static final int MEAN_COL = 4;
        static final int MIN_COL = 5;
        static final int MAX_COL = 6;
        static final int SPREAD_COL = 7;
        static final int CHANGE_COL = 8;
        static final int NUM_COLS = 9;

        private final DecimalFormat speedFormat = new DecimalFormat("0.0");
        private final DecimalFormat throttleFormat = new DecimalFormat("0.000");

        private final List<Row> rows = new ArrayList<>();

        private static final class Row {

            final int key;
            final SpeedStepRepeats.Entry entry;
            final boolean isForward;

            Row(int key, SpeedStepRepeats.Entry entry, boolean isForward) {
                this.key = key;
                this.entry = entry;
                this.isForward = isForward;
            }
        }

        RepeatsTableModel(SpeedStepRepeats repeats) {
            for (Map.Entry<Integer, SpeedStepRepeats.Entry> e : repeats.getRepeated().entrySet()) {
                rows.add(new Row(e.getKey(), e.getValue(), true));
                rows.add(new Row(e.getKey(), e.getValue(), false));
            }
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return NUM_COLS;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case STEP_COL:
                    return Bundle.getMessage("step");
                case THROTTLE_COL:
                    return Bundle.getMessage("throttle");
                case DIRECTION_COL:
                    return Bundle.getMessage("RepeatsDirection");
                case FIRST_COL:
                    return Bundle.getMessage("RepeatsFirst");
                case MEAN_COL:
                    return Bundle.getMessage("RepeatsMean");
                case MIN_COL:
                    return Bundle.getMessage("RepeatsMin");
                case MAX_COL:
                    return Bundle.getMessage("RepeatsMax");
                case SPREAD_COL:
                    return Bundle.getMessage("RepeatsSpread");
                case CHANGE_COL:
                    return Bundle.getMessage("RepeatsChange");
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int col) {
            Row row = rows.get(rowIndex);
            SpeedStepRepeats.Samples s = row.entry.get(row.isForward);
            switch (col) {
                case STEP_COL:
                    return row.entry.step > 0 ? Integer.toString(row.entry.step) : "";
                case THROTTLE_COL:
                    return throttleFormat.format(row.key / 1000.0);
                case DIRECTION_COL:
                    return Bundle.getMessage(row.isForward ? "LabelTestStepFwd" : "LabelTestStepRev");
                case FIRST_COL:
                    return s.getFirst() == null ? "" : speedFormat.format(s.getFirst());
                case MEAN_COL:
                    return s.getRepeats().isEmpty() ? "" : speedFormat.format(s.mean());
                case MIN_COL:
                    return s.getRepeats().isEmpty() ? "" : speedFormat.format(s.min());
                case MAX_COL:
                    return s.getRepeats().isEmpty() ? "" : speedFormat.format(s.max());
                case SPREAD_COL:
                    Float spread = s.spreadPercent();
                    return spread == null ? "" : "\u00b1" + speedFormat.format(spread);
                case CHANGE_COL:
                    Float change = s.changePercent();
                    return change == null ? "" : (change > 0 ? "+" : "") + speedFormat.format(change);
                default:
                    return "";
            }
        }
    }
}
