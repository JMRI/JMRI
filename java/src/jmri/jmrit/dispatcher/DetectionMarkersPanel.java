package jmri.jmrit.dispatcher;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import jmri.TrainRecord.DetectionMarker;
import jmri.TrainRecord.DetectionMarker.Placement;

/**
 * Edits a train's own list of detection markers, for train tracking: the
 * stretches of the train its detection sees, front first, when Train
 * Detection can't say, as for a lit coach in the middle of a train. Each has
 * a placement (the whole train, at the front, at the rear, or at an offset
 * from the front) and, where known, its length and offset in millimetres.
 * <p>
 * Dispatcher's own handling still follows Train Detection; only train
 * tracking uses the markers.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public class DetectionMarkersPanel extends JPanel {

    final MarkerTableModel model = new MarkerTableModel();

    /**
     * @param markers the markers to start from, front first
     */
    public DetectionMarkersPanel(@Nonnull List<DetectionMarker> markers) {
        super(new BorderLayout());
        for (DetectionMarker m : markers) {
            model.rows.add(new Row(m.getPlacement(), m.getOffset(), m.getLength()));
        }
        JTable table = new JTable(model);
        table.getColumnModel().getColumn(MarkerTableModel.PLACEMENT)
                .setCellEditor(new DefaultCellEditor(new JComboBox<>(PlacementItem.all())));
        table.setPreferredScrollableViewportSize(new java.awt.Dimension(420, 120));
        add(new JLabel(Bundle.getMessage("DetectionMarkersIntro")), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        JButton addButton = new JButton(Bundle.getMessage("ButtonAddMarker"));
        addButton.addActionListener(e -> model.addRow());
        JButton removeButton = new JButton(Bundle.getMessage("ButtonDelete"));
        removeButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                if (table.isEditing()) {
                    table.getCellEditor().cancelCellEditing();
                }
                model.removeRow(table.convertRowIndexToModel(row));
            }
        });
        buttons.add(addButton);
        buttons.add(removeButton);
        add(buttons, BorderLayout.SOUTH);
    }

    /**
     * @return the markers as edited, front first; a row whose values don't
     *         make a marker is left out
     */
    @Nonnull
    public List<DetectionMarker> getMarkers() {
        List<DetectionMarker> markers = new ArrayList<>();
        for (Row r : model.rows) {
            DetectionMarker m = r.toMarker();
            if (m != null) {
                markers.add(m);
            }
        }
        return markers;
    }

    private static final class Row {

        Placement placement;
        Double offset;
        Double length;

        Row(Placement placement, @CheckForNull Double offset, @CheckForNull Double length) {
            this.placement = placement;
            this.offset = offset;
            this.length = length;
        }

        @CheckForNull
        DetectionMarker toMarker() {
            try {
                switch (placement) {
                    case WHOLE_TRAIN:
                        return DetectionMarker.wholeTrain();
                    case FRONT:
                        return DetectionMarker.front(length);
                    case REAR:
                        return DetectionMarker.rear(length);
                    case OFFSET:
                    default:
                        return DetectionMarker.atOffset(offset, length);
                }
            } catch (IllegalArgumentException e) {
                return null; // a negative or zero length or offset
            }
        }
    }

    /**
     * A placement as shown in the table.
     */
    static final class PlacementItem {

        final Placement placement;

        PlacementItem(Placement placement) {
            this.placement = placement;
        }

        static PlacementItem[] all() {
            Placement[] values = Placement.values();
            PlacementItem[] items = new PlacementItem[values.length];
            for (int i = 0; i < values.length; i++) {
                items[i] = new PlacementItem(values[i]);
            }
            return items;
        }

        @Override
        public String toString() {
            return Bundle.getMessage("DetectionMarker" + placement.name());
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof PlacementItem && ((PlacementItem) o).placement == placement;
        }

        @Override
        public int hashCode() {
            return placement.hashCode();
        }
    }

    /**
     * The markers, one row each.
     */
    static final class MarkerTableModel extends AbstractTableModel {

        static final int PLACEMENT = 0;
        static final int OFFSET = 1;
        static final int LENGTH = 2;

        final List<Row> rows = new ArrayList<>();

        void addRow() {
            rows.add(new Row(Placement.OFFSET, null, null));
            fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
        }

        void removeRow(int row) {
            rows.remove(row);
            fireTableRowsDeleted(row, row);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case PLACEMENT:
                    return Bundle.getMessage("DetectionMarkerPlacement");
                case OFFSET:
                    return Bundle.getMessage("DetectionMarkerOffset");
                default:
                    return Bundle.getMessage("DetectionMarkerLength");
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == PLACEMENT ? Object.class : Double.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            // an offset is given only for a marker placed at an offset; the whole train has no length of its own
            Placement p = rows.get(row).placement;
            return column == PLACEMENT || (column == OFFSET && p == Placement.OFFSET)
                    || (column == LENGTH && p != Placement.WHOLE_TRAIN);
        }

        @Override
        public Object getValueAt(int row, int column) {
            Row r = rows.get(row);
            switch (column) {
                case PLACEMENT:
                    return new PlacementItem(r.placement);
                case OFFSET:
                    return r.offset;
                default:
                    return r.length;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            Row r = rows.get(row);
            switch (column) {
                case PLACEMENT:
                    if (value instanceof PlacementItem) {
                        r.placement = ((PlacementItem) value).placement;
                        if (r.placement != Placement.OFFSET) {
                            r.offset = null;
                        }
                        if (r.placement == Placement.WHOLE_TRAIN) {
                            r.length = null;
                        }
                    }
                    break;
                case OFFSET:
                    r.offset = (Double) value;
                    break;
                default:
                    r.length = (Double) value;
                    break;
            }
            fireTableRowsUpdated(row, row);
        }
    }
}
