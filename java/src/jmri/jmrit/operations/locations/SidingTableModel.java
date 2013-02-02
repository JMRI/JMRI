//SidingTableModel.java

package jmri.jmrit.operations.locations;

import org.apache.log4j.Logger;
import java.beans.*;
import javax.swing.*;
import jmri.jmrit.operations.setup.Control;

/**
 * Table Model for edit of spurs used by operations
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class SidingTableModel extends TrackTableModel {

	public SidingTableModel() {
		super();
	}

	public void initTable(JTable table, Location location) {
		super.initTable(table, location, Track.SIDING);
	}

	public String getColumnName(int col) {
		switch (col) {
		case NAMECOLUMN:
			return Bundle.getMessage("SidingName");
		}
		return super.getColumnName(col);
	}

	protected void editTrack(int row) {
		log.debug("Edit spur");
		if (tef != null) {
			tef.dispose();
		}
		tef = new SidingEditFrame();
		String sidingId = tracksList.get(row);
		Track siding = _location.getTrackById(sidingId);
		tef.initComponents(_location, siding);
		tef.setTitle(Bundle.getMessage("EditSiding"));
		focusEditFrame = true;
	}

	// this table listens for changes to a location and it's spurs
	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue()
					+ " new: " + e.getNewValue());	// NOI18N
		super.propertyChange(e);
		if (e.getSource().getClass().equals(Track.class)) {
			String type = ((Track) e.getSource()).getLocType();
			if (type.equals(Track.SIDING)) {
				String sidingId = ((Track) e.getSource()).getId();
				int row = tracksList.indexOf(sidingId);
				if (Control.showProperty && log.isDebugEnabled())
					log.debug("Update spur table row: " + row + " id: " + sidingId);
				if (row >= 0)
					fireTableRowsUpdated(row, row);
			}
		}
	}

	static Logger log = Logger.getLogger(SidingTableModel.class
			.getName());
}
