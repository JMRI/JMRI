// YardTableModel.java

package jmri.jmrit.operations.locations;

import java.beans.*;
import javax.swing.*;
import jmri.jmrit.operations.setup.Control;

/**
 * Table Model for edit of yards used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision: 1.6 $
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
		case NAMECOLUMN: return rb.getString("YardName");
		}
		return super.getColumnName(col);
	}

	YardEditFrame yef = null;

	protected void editTrack (int row){
		log.debug("Edit yard");
		if (yef != null){
			yef.dispose();
		}
		yef = new YardEditFrame();
		String yardId = (String)tracksList.get(row);
		Track yard = _location.getTrackById(yardId);
		yef.initComponents(_location, yard);
		yef.setTitle(rb.getString("EditYard"));
	}

	public void dispose() {
		super.dispose();
		if (yef != null)
			yef.dispose();
	}
 
    // this table listens for changes to a location and it's yards
    public void propertyChange(PropertyChangeEvent e) {
    	if (Control.showProperty && log.isDebugEnabled()) 
    		log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(Location.YARDLISTLENGTH_CHANGED_PROPERTY)) {
    		updateList();
    		fireTableDataChanged();
    	}

    	if (e.getSource() != _location){
    		String type = ((Track) e.getSource()).getLocType();
    		if (type.equals(Track.YARD)){
    			String yardId = ((Track) e.getSource()).getId();
    			int row = tracksList.indexOf(yardId);
    			if (Control.showProperty && log.isDebugEnabled()) 
    				log.debug("Update yard table row: "+ row + " id: " + yardId);
    			if (row >= 0)
    				fireTableRowsUpdated(row, row);
    		}
    	}
    }
  
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(YardTableModel.class.getName());
}

