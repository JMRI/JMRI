package jmri.jmrit.operations.locations;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.setup.Control;

/**
 * Table Model for edit of spurs used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class SpurTableModel extends TrackTableModel {

    public SpurTableModel() {
        super();
    }

    public void initTable(JTable table, Location location) {
        super.initTable(table, location, Track.SPUR);
        table.setDefaultRenderer(Object.class, new MyTableCellRenderer());
    }

    @Override
    protected void editTrack(int row) {
        log.debug("Edit spur");
        if (tef != null) {
            tef.dispose();
        }
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(() -> {
            tef = new SpurEditFrame();
            Track spur = _tracksList.get(row);
            tef.initComponents(spur);
        });
    }
    
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case NAME_COLUMN:
                return Bundle.getMessage("SpurName");
            default:
                return super.getColumnName(col);
        }
    }

    protected Color getForegroundColor(int row) {
        Track spur = _tracksList.get(row);
        if (!spur.checkScheduleValid().equals(Schedule.SCHEDULE_OKAY)) {
            return Color.red;
        }
        return _table.getForeground();
    }

    class MyTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                int modelRow = table.convertRowIndexToModel(row);
                component.setForeground(getForegroundColor(modelRow));
            }
            return component;
        }
    }

    // this table listens for changes to a location and it's spurs
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        super.propertyChange(e);
        if (e.getSource().getClass().equals(Track.class)) {
            Track track = ((Track) e.getSource());
            if (track.isSpur()) {
                int row = _tracksList.indexOf(track);
                if (Control.SHOW_PROPERTY) {
                    log.debug("Update spur table row: {} track: {}", row, track.getName());
                }
                if (row >= 0) {
                    fireTableRowsUpdated(row, row);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SpurTableModel.class);
}
