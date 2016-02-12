// InterchangeTableModel.java
package jmri.jmrit.operations.locations;

import java.beans.PropertyChangeEvent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import jmri.jmrit.operations.setup.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table Model for edit of interchanges used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class InterchangeTableModel extends TrackTableModel {

    /**
     *
     */
    private static final long serialVersionUID = -3985319043804319680L;

    public InterchangeTableModel() {
        super();
    }

    public void initTable(JTable table, Location location) {
        super.initTable(table, location, Track.INTERCHANGE);
    }

    public String getColumnName(int col) {
        switch (col) {
            case NAME_COLUMN:
                return Bundle.getMessage("InterchangeName");
        }
        return super.getColumnName(col);
    }

    protected void editTrack(int row) {
        log.debug("Edit interchange");
        if (tef != null) {
            tef.dispose();
        }
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tef = new InterchangeEditFrame();
                Track interchange = tracksList.get(row);
                tef.initComponents(_location, interchange);
                tef.setTitle(Bundle.getMessage("EditInterchange"));
            }
        });
    }

    // this table listens for changes to a location and it's interchanges
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.showProperty) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        super.propertyChange(e);
        if (e.getSource().getClass().equals(Track.class)) {
            Track track = ((Track) e.getSource());
            if (track.getTrackType().equals(Track.INTERCHANGE)) {
                int row = tracksList.indexOf(track);
                if (Control.showProperty) {
                    log.debug("Update interchange table row: {} track: {}", row, track.getName());
                }
                if (row >= 0) {
                    fireTableRowsUpdated(row, row);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(InterchangeTableModel.class.getName());
}
