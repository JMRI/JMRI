// YardTableModel.java

package jmri.jmrit.operations.locations;

import java.beans.*;
import javax.swing.*;
import jmri.jmrit.operations.setup.Control;

/**
 * Table Model for edit of yards used by operations
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class YardTableModel extends TrackTableModel {

	public YardTableModel() {
		super();
	}

	public void initTable(JTable table, Location location) {
		super.initTable(table, location, Track.YARD);
	}

	public String getColumnName(int col) {
		switch (col) {
		case NAMECOLUMN:
			return Bundle.getString("YardName");
		}
		return super.getColumnName(col);
	}

	protected void editTrack(int row) {
		log.debug("Edit yard");
		if (tef != null) {
			tef.dispose();
		}
		tef = new YardEditFrame();
		String yardId = tracksList.get(row);
		Track yard = _location.getTrackById(yardId);
		tef.initComponents(_location, yard);
		tef.setTitle(Bundle.getString("EditYard"));
		focusEditFrame = true;
	}

	// this table listens for changes to a location and it's yards
	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue()
					+ " new: " + e.getNewValue());
		super.propertyChange(e);
		if (e.getSource().getClass().equals(Track.class)) {
			String type = ((Track) e.getSource()).getLocType();
			if (type.equals(Track.YARD)) {
				String yardId = ((Track) e.getSource()).getId();
				int row = tracksList.indexOf(yardId);
				if (Control.showProperty && log.isDebugEnabled())
					log.debug("Update yard table row: " + row + " id: " + yardId);
				if (row >= 0)
					fireTableRowsUpdated(row, row);
			}
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(YardTableModel.class
			.getName());
}
