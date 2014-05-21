// InterchangeTableModel.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.*;

import javax.swing.*;

import jmri.jmrit.operations.setup.Control;

/**
 * Table Model for edit of interchanges used by operations
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class InterchangeTableModel extends TrackTableModel {

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
		tef = new InterchangeEditFrame();
		Track interchange = tracksList.get(row);
		tef.initComponents(_location, interchange);
		tef.setTitle(Bundle.getMessage("EditInterchange"));
		focusEditFrame = true;
	}

	// this table listens for changes to a location and it's interchanges
	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
					.getNewValue());
		super.propertyChange(e);
		if (e.getSource().getClass().equals(Track.class)) {
			Track track = ((Track) e.getSource());
			if (track.getTrackType().equals(Track.INTERCHANGE)) {
				int row = tracksList.indexOf(track);
				if (Control.showProperty && log.isDebugEnabled())
					log.debug("Update interchange table row: " + row + " track: " + track.getName());
				if (row >= 0)
					fireTableRowsUpdated(row, row);
			}
		}
	}

	static Logger log = LoggerFactory.getLogger(InterchangeTableModel.class.getName());
}
