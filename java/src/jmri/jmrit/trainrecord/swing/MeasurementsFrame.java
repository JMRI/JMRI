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
import javax.swing.JTabbedPane;
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
 * under a train; for each boundary, how much later the Block behind went
 * empty than the train's passing explains; and for each train, how its
 * arrivals compared with its speed profile's predictions. With no layout
 * hardware it says that the trains measured are simulated. Opened from
 * Tools &gt; Train Tracking &gt; Measurements.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public class MeasurementsFrame extends JmriJFrame {

    private final MeasurementsModel model = new MeasurementsModel();
    private final JTable table = new JTable(model);
    private final BoundariesModel boundariesModel = new BoundariesModel();
    private final ArrivalsModel arrivalsModel = new ArrivalsModel();
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
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(Bundle.getMessage("MeasurementsTabBlocks"), scrolled(table));
        tabs.addTab(Bundle.getMessage("MeasurementsTabBoundaries"), scrolled(new JTable(boundariesModel)));
        tabs.addTab(Bundle.getMessage("MeasurementsTabTrains"), scrolled(new JTable(arrivalsModel)));
        main.add(tabs, BorderLayout.CENTER);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(main, BorderLayout.CENTER);
        addHelpMenu("package.jmri.jmrit.trainrecord.TrainTracking", true);

        InstanceManager.getDefault(TrainTrackingMeasurements.class)
                .addPropertyChangeListener(TrainTrackingMeasurements.PROPERTY_MEASUREMENT, changed);
        refresh();
    }

    private static JScrollPane scrolled(JTable t) {
        t.setAutoCreateRowSorter(true);
        JScrollPane pane = new JScrollPane(t);
        pane.setPreferredSize(new java.awt.Dimension(640, 240));
        return pane;
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
            boundariesModel.rows = m.getBoundaries();
            boundariesModel.fireTableDataChanged();
            arrivalsModel.rows = m.getArrivals();
            arrivalsModel.fireTableDataChanged();
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

    /**
     * The detector offset, in words.
     *
     * @param m a measurement
     * @return such as "+0.25 s", or "" if crossings at different speeds
     *         haven't been measured
     */
    public static String detectorOffset(Measurement m) {
        if (!m.isFitted()) {
            return "";
        }
        return seconds(m.getDetectorOffset());
    }

    /**
     * A time in seconds, signed, in words.
     *
     * @param seconds the time
     * @return such as "+0.25 s"
     */
    static String seconds(double seconds) {
        return Bundle.getMessage("MeasurementsSeconds",
                String.format(java.util.Locale.getDefault(), "%+.2f", seconds));
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
        static final int OFFSET = 7;
        static final int WITHOUT_OFFSET = 8;

        private List<Row> rows = new ArrayList<>();
        private TrainTrackingMeasurements measurements = null;

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 9;
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
                case OFFSET:
                    return Bundle.getMessage("MeasurementsDetectorOffset");
                case WITHOUT_OFFSET:
                    return Bundle.getMessage("MeasurementsAgainstProfileWithoutOffset");
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
                case OFFSET:
                    return m == null ? "" : detectorOffset(m);
                case WITHOUT_OFFSET:
                    return m == null || !m.isFitted() ? ""
                            : Bundle.getMessage("MeasurementsPercent",
                                    Math.round(m.getAgainstProfileWithoutOffset() * 100));
                case DROPOUTS:
                default:
                    return measurements == null ? 0 : measurements.getDropOuts(b);
            }
        }
    }

    static final class BoundariesModel extends AbstractTableModel {

        static final int BEHIND = 0;
        static final int AHEAD = 1;
        static final int COUNT = 2;
        static final int DELAY = 3;
        static final int SPREAD = 4;

        private List<TrainTrackingMeasurements.Boundary> rows = new ArrayList<>();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case BEHIND:
                    return Bundle.getMessage("MeasurementsBehind");
                case AHEAD:
                    return Bundle.getMessage("MeasurementsAhead");
                case COUNT:
                    return Bundle.getMessage("MeasurementsTimes");
                case DELAY:
                    return Bundle.getMessage("MeasurementsEmptyDelay");
                case SPREAD:
                default:
                    return Bundle.getMessage("MeasurementsSpread");
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == COUNT ? Integer.class : String.class;
        }

        @Override
        public Object getValueAt(int row, int column) {
            TrainTrackingMeasurements.Boundary b = rows.get(row);
            switch (column) {
                case BEHIND:
                    return b.getBehind().getDisplayName();
                case AHEAD:
                    return b.getAhead().getDisplayName();
                case COUNT:
                    return b.getCount();
                case DELAY:
                    return seconds(b.getEmptyDelay());
                case SPREAD:
                default:
                    return b.getCount() < 2 ? "" : Bundle.getMessage("MeasurementsSeconds",
                            String.format(java.util.Locale.getDefault(), "%.2f", b.getSpread()));
            }
        }
    }

    static final class ArrivalsModel extends AbstractTableModel {

        static final int TRAIN = 0;
        static final int COUNT = 1;
        static final int LATE = 2;
        static final int SPREAD = 3;

        private List<TrainTrackingMeasurements.Arrivals> rows = new ArrayList<>();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case TRAIN:
                    return Bundle.getMessage("MeasurementsTrain");
                case COUNT:
                    return Bundle.getMessage("MeasurementsCrossings");
                case LATE:
                    return Bundle.getMessage("MeasurementsLate");
                case SPREAD:
                default:
                    return Bundle.getMessage("MeasurementsSpread");
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == COUNT ? Integer.class : String.class;
        }

        @Override
        public Object getValueAt(int row, int column) {
            TrainTrackingMeasurements.Arrivals a = rows.get(row);
            switch (column) {
                case TRAIN:
                    return a.getRecord().getName();
                case COUNT:
                    return a.getCount();
                case LATE:
                    return Bundle.getMessage("MeasurementsPercent",
                            String.format(java.util.Locale.getDefault(), "%+d", Math.round(a.getLate() * 100)));
                case SPREAD:
                default:
                    return a.getCount() < 2 ? "" : Bundle.getMessage("MeasurementsPercent",
                            Math.round(a.getSpread() * 100));
            }
        }
    }
}
