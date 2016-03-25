// StagingTableModel.java
package jmri.jmrit.operations.locations;

import java.beans.PropertyChangeEvent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import jmri.jmrit.operations.setup.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table Model for edit of staging tracks used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class StagingTableModel extends TrackTableModel {

    /**
     *
     */
    private static final long serialVersionUID = 406205617566439045L;

    public StagingTableModel() {
        super();
    }

    public void initTable(JTable table, Location location) {
        super.initTable(table, location, Track.STAGING);
    }

    public String getColumnName(int col) {
        switch (col) {
            case NAME_COLUMN:
                return Bundle.getMessage("StagingName");
        }
        return super.getColumnName(col);
    }

    protected void editTrack(int row) {
        log.debug("Edit staging");
        if (tef != null) {
            tef.dispose();
        }
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tef = new StagingEditFrame();
                Track staging = tracksList.get(row);
                tef.initComponents(_location, staging);
                tef.setTitle(Bundle.getMessage("EditStaging"));
            }
        });
    }

    // this table listens for changes to a location and it's staging tracks
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        super.propertyChange(e);
        if (e.getSource().getClass().equals(Track.class)) {
            Track track = ((Track) e.getSource());
            if (track.getTrackType().equals(Track.STAGING)) {
                int row = tracksList.indexOf(track);
                if (Control.SHOW_PROPERTY) {
                    log.debug("Update staging table row: {} track: {}", row, track.getName());
                }
                if (row >= 0) {
                    fireTableRowsUpdated(row, row);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(StagingTableModel.class.getName());
}
