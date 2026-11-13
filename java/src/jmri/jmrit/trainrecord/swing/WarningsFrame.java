package jmri.jmrit.trainrecord.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import jmri.Block;
import jmri.InstanceManager;
import jmri.TrainRecordManager;
import jmri.jmrit.trainrecord.TrainRecordReasons;
import jmri.jmrit.trainrecord.TrainTrackingWarnings;
import jmri.jmrit.trainrecord.TrainTrackingWarnings.Warning;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;

/**
 * The Warnings window: the train tracking warnings still open, including
 * those answered "Later", and the Blocks whose detector is not trusted.
 * Opened from Tools &gt; Train Tracking &gt; Warnings.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public class WarningsFrame extends JmriJFrame {

    private final WarningsModel warningsModel = new WarningsModel();
    private final JTable warningsTable = new JTable(warningsModel);
    private final UntrustedModel untrustedModel = new UntrustedModel();
    private final JTable untrustedTable = new JTable(untrustedModel);
    private final transient PropertyChangeListener changed = e -> refresh();

    /**
     * The menu item that opens the window.
     */
    public static class Action extends AbstractAction {

        /**
         * Make the menu item.
         */
        public Action() {
            super(Bundle.getMessage("MenuItemWarnings"));
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            WarningsFrame f = InstanceManager.getOptionalDefault(WarningsFrame.class).orElse(null);
            if (f == null || !f.isDisplayable()) {
                f = new WarningsFrame();
                InstanceManager.setDefault(WarningsFrame.class, f);
                f.initComponents();
                f.pack();
            }
            f.setVisible(true);
            f.toFront();
        }
    }

    /**
     * @return the text of the Train Tracking menu, which holds the menu item
     *         for this window
     */
    public static String menuText() {
        return Bundle.getMessage("MenuTrainTracking");
    }

    /**
     * Make the window. Call {@link #initComponents()} to fill it.
     */
    public WarningsFrame() {
        super(Bundle.getMessage("WarningsTitle"), true, true);
    }

    /** {@inheritDoc} */
    @Override
    public void initComponents() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        warningsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        warningsTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        warningsTable.getColumnModel().getColumn(1).setPreferredWidth(420);
        JScrollPane warningsPane = new JScrollPane(warningsTable);
        warningsPane.setPreferredSize(new java.awt.Dimension(500, 160));
        main.add(warningsPane);
        JPanel warningButtons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        JButton open = new JButton(Bundle.getMessage("WarningShowMe"));
        open.setToolTipText(Bundle.getMessage("WarningShowMeHint"));
        open.addActionListener(e -> {
            Warning w = selectedWarning();
            if (w != null) {
                BlockDialog.open(w.getBlock(), true);
            }
        });
        JButton stuck = new JButton(Bundle.getMessage("WarningDetectorStuck"));
        stuck.setToolTipText(Bundle.getMessage("WarningDetectorStuckHint"));
        stuck.addActionListener(e -> {
            Warning w = selectedWarning();
            if (w != null && w.getKind() == Warning.Kind.UNEXPLAINED) {
                InstanceManager.getDefault(TrainTrackingWarnings.class).detectorStuck(w.getBlock());
            }
        });
        warningButtons.add(stuck);
        warningButtons.add(open);
        main.add(warningButtons);

        JLabel untrustedLabel = new JLabel(Bundle.getMessage("WarningsUntrusted"));
        main.add(untrustedLabel);
        JScrollPane untrustedPane = new JScrollPane(untrustedTable);
        untrustedPane.setPreferredSize(new java.awt.Dimension(500, 80));
        main.add(untrustedPane);
        JPanel untrustedButtons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        JButton trust = new JButton(Bundle.getMessage("WarningsTrustAgain"));
        trust.setToolTipText(Bundle.getMessage("WarningsTrustAgainHint"));
        trust.addActionListener(e -> {
            int row = untrustedTable.getSelectedRow();
            if (row >= 0) {
                Block b = untrustedModel.blocks.get(row);
                InstanceManager.getDefault(TrainRecordManager.class).setUntrusted(b, false);
            }
        });
        untrustedButtons.add(trust);
        main.add(untrustedButtons);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(main, BorderLayout.CENTER);
        addHelpMenu("package.jmri.jmrit.trainrecord.TrainTracking", true);

        InstanceManager.getDefault(TrainTrackingWarnings.class)
                .addPropertyChangeListener(TrainTrackingWarnings.PROPERTY_WARNINGS, changed);
        InstanceManager.getDefault(TrainRecordManager.class)
                .addPropertyChangeListener(TrainRecordManager.PROPERTY_UNTRUSTED, changed);
        refresh();
    }

    private Warning selectedWarning() {
        int row = warningsTable.getSelectedRow();
        return row < 0 ? null : warningsModel.warnings.get(row);
    }

    private void refresh() {
        ThreadingUtil.runOnGUIEventually(() -> {
            warningsModel.warnings = InstanceManager.getDefault(TrainTrackingWarnings.class).getWarnings();
            warningsModel.fireTableDataChanged();
            untrustedModel.blocks = InstanceManager.getDefault(TrainRecordManager.class).getUntrusted();
            untrustedModel.fireTableDataChanged();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        InstanceManager.getDefault(TrainTrackingWarnings.class)
                .removePropertyChangeListener(TrainTrackingWarnings.PROPERTY_WARNINGS, changed);
        InstanceManager.getDefault(TrainRecordManager.class)
                .removePropertyChangeListener(TrainRecordManager.PROPERTY_UNTRUSTED, changed);
        super.dispose();
    }

    private static final class WarningsModel extends AbstractTableModel {

        private List<Warning> warnings = new ArrayList<>();

        @Override
        public int getRowCount() {
            return warnings.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            return Bundle.getMessage(column == 0 ? "WarningsTime" : "WarningsWarning");
        }

        @Override
        public Object getValueAt(int row, int column) {
            Warning w = warnings.get(row);
            return column == 0 ? TrainRecordReasons.time(w.getTime()) : w.getTitle();
        }
    }

    private static final class UntrustedModel extends AbstractTableModel {

        private List<Block> blocks = new ArrayList<>();

        @Override
        public int getRowCount() {
            return blocks.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            return Bundle.getMessage("BeanNameBlock");
        }

        @Override
        public Object getValueAt(int row, int column) {
            Block b = blocks.get(row);
            jmri.Sensor sensor = b.getSensor();
            return b.getDisplayName() + (sensor == null ? "" : " (" + sensor.getDisplayName() + ")");
        }
    }
}
