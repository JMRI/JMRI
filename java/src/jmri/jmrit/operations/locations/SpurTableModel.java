//SpurTableModel.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.*;
import javax.swing.*;
import jmri.jmrit.operations.setup.Control;

/**
 * Table Model for edit of spurs used by operations
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class SpurTableModel extends TrackTableModel {

	public SpurTableModel() {
		super();
	}

	public void initTable(JTable table, Location location) {
		super.initTable(table, location, Track.SPUR);
	}

	public String getColumnName(int col) {
		switch (col) {
		case NAME_COLUMN:
			return Bundle.getMessage("SpurName");
		}
		return super.getColumnName(col);
	}

	protected void editTrack(int row) {
		log.debug("Edit spur");
		if (tef != null) {
			tef.dispose();
		}
		tef = new SpurEditFrame();
		String spurId = tracksList.get(row);
		Track spur = _location.getTrackById(spurId);
		tef.initComponents(_location, spur);
		tef.setTitle(Bundle.getMessage("EditSpur"));
		focusEditFrame = true;
	}

	// this table listens for changes to a location and it's spurs
	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue()
					+ " new: " + e.getNewValue());	// NOI18N
		super.propertyChange(e);
		if (e.getSource().getClass().equals(Track.class)) {
			String type = ((Track) e.getSource()).getTrackType();
			if (type.equals(Track.SPUR)) {
				String spurId = ((Track) e.getSource()).getId();
				int row = tracksList.indexOf(spurId);
				if (Control.showProperty && log.isDebugEnabled())
					log.debug("Update spur table row: " + row + " id: " + spurId);
				if (row >= 0)
					fireTableRowsUpdated(row, row);
			}
		}
	}

	static Logger log = LoggerFactory.getLogger(SpurTableModel.class
			.getName());
}
