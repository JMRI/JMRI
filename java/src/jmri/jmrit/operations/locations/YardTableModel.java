// YardTableModel.java
package jmri.jmrit.operations.locations;

import java.beans.PropertyChangeEvent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import jmri.jmrit.operations.setup.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table Model for edit of yards used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class YardTableModel extends TrackTableModel {

    /**
     *
     */
    private static final long serialVersionUID = -7919234279596604386L;

    public YardTableModel() {
        super();
    }

    public void initTable(JTable table, Location location) {
        super.initTable(table, location, Track.YARD);
    }

    public String getColumnName(int col) {
        switch (col) {
            case NAME_COLUMN:
                return Bundle.getMessage("YardName");
        }
        return super.getColumnName(col);
    }

    protected void editTrack(int row) {
        log.debug("Edit yard");
        if (tef != null) {
            tef.dispose();
        }
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tef = new YardEditFrame();
                Track yard = tracksList.get(row);
                tef.initComponents(_location, yard);
                tef.setTitle(Bundle.getMessage("EditYard"));
            }
        });
    }

    // this table listens for changes to a location and it's yards
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.showProperty) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        super.propertyChange(e);
        if (e.getSource().getClass().equals(Track.class)) {
            Track track = ((Track) e.getSource());
            if (track.getTrackType().equals(Track.YARD)) {
                int row = tracksList.indexOf(track);
                if (Control.showProperty) {
                    log.debug("Update yard table row: {} track: ({})", row, track.getName());
                }
                if (row >= 0) {
                    fireTableRowsUpdated(row, row);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(YardTableModel.class.getName());
}
