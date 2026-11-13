package jmri.jmrit.trainrecord.swing;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import jmri.Block;
import jmri.InstanceManager;
import jmri.jmrit.trainrecord.TrainTrackingDetectors;
import jmri.jmrit.trainrecord.TrainTrackingMeasurements;
import jmri.jmrit.trainrecord.TrainTrackingMeasurements.Measurement;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;

/**
 * The Measurements window: for each Block, and each way through it (the
 * Blocks it was entered from and left for), how many times a train's
 * leading end has crossed it this session, how fast, and how that compares
 * with the trains' speed profiles; and how often its detector dropped out
 * under a train. With no layout hardware it says that the trains measured
 * are simulated. Opened from Tools &gt; Train Tracking &gt; Measurements.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public class MeasurementsFrame extends JmriJFrame {

    private final MeasurementsModel model = new MeasurementsModel();
    private final JTable table = new JTable(model);
    private final transient PropertyChangeListener changed = e -> refresh();

    /**
     * The menu item that opens the window.
     */
    public static class Action extends AbstractAction {

        /**
         * Make the menu item.
         */
        public Action() {
            super(Bundle.getMessage("MenuItemMeasurements"));
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            MeasurementsFrame f = InstanceManager.getOptionalDefault(MeasurementsFrame.class).orElse(null);
            if (f == null || !f.isDisplayable()) {
                f = new MeasurementsFrame();
                InstanceManager.setDefault(MeasurementsFrame.class, f);
                f.initComponents();
                f.pack();
            }
            f.setVisible(true);
            f.toFront();
        }
    }

    /**
     * Make the window. Call {@link #initComponents()} to fill it.
     */
    public MeasurementsFrame() {
        super(Bundle.getMessage("MeasurementsTitle"), true, true);
    }

    /** {@inheritDoc} */
    @Override
    public void initComponents() {
        JPanel main = new JPanel(new BorderLayout(0, 6));
        main.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        main.add(new JLabel(Bundle.getMessage(TrainTrackingDetectors.isSimulated()
                ? "MeasurementsIntroSimulated" : "MeasurementsIntro")), BorderLayout.NORTH);
        table.setAutoCreateRowSorter(true);
        JScrollPane pane = new JScrollPane(table);
        pane.setPreferredSize(new java.awt.Dimension(640, 240));
        main.add(pane, BorderLayout.CENTER);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(main, BorderLayout.CENTER);
        addHelpMenu("package.jmri.jmrit.trainrecord.TrainTracking", true);

        InstanceManager.getDefault(TrainTrackingMeasurements.class)
                .addPropertyChangeListener(TrainTrackingMeasurements.PROPERTY_MEASUREMENT, changed);
        refresh();
    }

    private void refresh() {
        ThreadingUtil.runOnGUIEventually(() -> {
            TrainTrackingMeasurements m = InstanceManager.getDefault(TrainTrackingMeasurements.class);
            List<Row> rows = new ArrayList<>();
            List<Block> measured = new ArrayList<>();
            for (Measurement x : m.getMeasurements()) {
                rows.add(new Row(x.getBlock(), x));
                measured.add(x.getBlock());
            }
            for (Block b : m.getDropOutBlocks()) {
                if (!measured.contains(b)) {
                    rows.add(new Row(b, null));
                }
            }
            model.rows = rows;
            model.measurements = m;
            model.fireTableDataChanged();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        InstanceManager.getDefault(TrainTrackingMeasurements.class)
                .removePropertyChangeListener(TrainTrackingMeasurements.PROPERTY_MEASUREMENT, changed);
        super.dispose();
    }

    /**
     * The speed compared with the profile, in words.
     *
     * @param m a measurement
     * @return such as "96% (4 of 5)", or "" if no crossing could be compared
     */
    public static String againstProfile(Measurement m) {
        if (m.getCompared() == 0) {
            return "";
        }
        return Bundle.getMessage("MeasurementsAgainstProfileValue", Math.round(m.getAgainstProfile() * 100),
                m.getCompared(), m.getCrossings());
    }

    private static final class Row {

        private final Block block;
        private final Measurement measurement;

        Row(Block block, Measurement measurement) {
            this.block = block;
            this.measurement = measurement;
        }
    }

    static final class MeasurementsModel extends AbstractTableModel {

        static final int BLOCK = 0;
        static final int FROM = 1;
        static final int TO = 2;
        static final int CROSSINGS = 3;
        static final int SPEED = 4;
        static final int PROFILE = 5;
        static final int DROPOUTS = 6;

        private List<Row> rows = new ArrayList<>();
        private TrainTrackingMeasurements measurements = null;

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 7;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case BLOCK:
                    return Bundle.getMessage("BeanNameBlock");
                case FROM:
                    return Bundle.getMessage("MeasurementsFrom");
                case TO:
                    return Bundle.getMessage("MeasurementsTo");
                case CROSSINGS:
                    return Bundle.getMessage("MeasurementsCrossings");
                case SPEED:
                    return Bundle.getMessage("MeasurementsSpeed");
                case PROFILE:
                    return Bundle.getMessage("MeasurementsAgainstProfile");
                case DROPOUTS:
                default:
                    return Bundle.getMessage("MeasurementsDropOuts");
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == CROSSINGS || column == SPEED || column == DROPOUTS ? Integer.class : String.class;
        }

        @Override
        public Object getValueAt(int row, int column) {
            Measurement m = rows.get(row).measurement;
            Block b = rows.get(row).block;
            switch (column) {
                case BLOCK:
                    return b.getDisplayName();
                case FROM:
                    return m == null ? "" : m.getFrom().getDisplayName();
                case TO:
                    return m == null ? "" : m.getTo().getDisplayName();
                case CROSSINGS:
                    return m == null ? 0 : m.getCrossings();
                case SPEED:
                    return m == null ? null : (int) Math.round(m.getAverageSpeed());
                case PROFILE:
                    return m == null ? "" : againstProfile(m);
                case DROPOUTS:
                default:
                    return measurements == null ? 0 : measurements.getDropOuts(b);
            }
        }
    }
}
