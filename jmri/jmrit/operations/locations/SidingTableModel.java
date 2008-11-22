//SidingTableModel.java

package jmri.jmrit.operations.locations;

import java.beans.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import jmri.jmrit.operations.setup.Control;

/**
 * Table Model for edit of sidings used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision: 1.5 $
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
		case NAMECOLUMN: return rb.getString("SidingName");
		}
		return super.getColumnName(col);
	}

	SidingEditFrame sef = null;

	protected void editTrack (int row){
		log.debug("Edit siding");
		if (sef != null){
			sef.dispose();
		}
		sef = new SidingEditFrame();
		String sidingId = (String)tracksList.get(row);
		Track siding = _location.getTrackById(sidingId);
		sef.initComponents(_location, siding);
		sef.setTitle(rb.getString("EditSiding"));
	}
	
	public void dispose() {
		super.dispose();
		if (sef != null)
			sef.dispose();
	}

	// this table listens for changes to a location and it's sidings
	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(Location.SIDINGLISTLENGTH_CHANGED_PROPERTY)) {
			updateList();
			fireTableDataChanged();
		}

		if (e.getSource() != _location){
			String type = ((Track) e.getSource()).getLocType();
			if (type.equals(Track.SIDING)){
				String sidingId = ((Track) e.getSource()).getId();
				int row = tracksList.indexOf(sidingId);
				if (Control.showProperty && log.isDebugEnabled()) 
					log.debug("Update siding table row: "+ row + " id: " + sidingId);
				if (row >= 0)
					fireTableRowsUpdated(row, row);
			}
		}
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SidingTableModel.class.getName());
}

