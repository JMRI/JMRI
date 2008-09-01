// TrainsTableModel.java

package jmri.jmrit.operations.trains;

import java.awt.event.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import jmri.*;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import jmri.jmrit.beantable.EnablingCheckboxRenderer;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;

/**
 * Table Model for edit of routes used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision: 1.1 $
 */
public class TrainsTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
   
    TrainManager manager = TrainManager.instance(); 	// There is only one manager
 
    // Defines the columns
    private static final int IDCOLUMN   = 0;
    private static final int BUILDBOXCOLUMN = IDCOLUMN+1;
    private static final int BUILDCOLUMN = BUILDBOXCOLUMN+1;
    private static final int NAMECOLUMN = BUILDCOLUMN+1;
    private static final int DESCRIPTIONCOLUMN = NAMECOLUMN+1;
    private static final int ROUTECOLUMN = DESCRIPTIONCOLUMN+1;
    private static final int DEPARTSCOLUMN = ROUTECOLUMN+1;
    private static final int TERMINATESCOLUMN = DEPARTSCOLUMN+1;
    private static final int CURRENTCOLUMN = TERMINATESCOLUMN+1;
    private static final int STATUSCOLUMN = CURRENTCOLUMN+1;
    private static final int MOVECOLUMN = STATUSCOLUMN+1;
    private static final int EDITCOLUMN = MOVECOLUMN+1;
    
    private static final int HIGHESTCOLUMN = EDITCOLUMN+1;

    public TrainsTableModel() {
        super();
        manager.addPropertyChangeListener(this);
        updateList();
    }
    
    public final int SORTBYNAME = 1;
    public final int SORTBYID = 2;
    
    private int _sort = SORTBYNAME;
    
    public void setSort (int sort){
    	_sort = sort;
        updateList();
        fireTableDataChanged();
    }
     
    synchronized void updateList() {
		// first, remove listeners from the individual objects
    	removePropertyChangeTrains();
    	
		if (_sort == SORTBYID)
			sysList = manager.getTrainsByIdList();
		else
			sysList = manager.getTrainsByNameList();
		// and add them back in
		for (int i = 0; i < sysList.size(); i++){
//			log.debug("route ids: " + (String) sysList.get(i));
			manager.getTrainById((String) sysList.get(i))
					.addPropertyChangeListener(this);
		}
	}

	List sysList = null;
    
	void initTable(JTable table) {
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(EDITCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDITCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(MOVECOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(MOVECOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(BUILDCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(BUILDCOLUMN).setCellEditor(buttonEditor);
		table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());

		// set column preferred widths
		table.getColumnModel().getColumn(IDCOLUMN).setPreferredWidth(30);
		table.getColumnModel().getColumn(BUILDBOXCOLUMN).setPreferredWidth(50);
		table.getColumnModel().getColumn(BUILDCOLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(NAMECOLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(DESCRIPTIONCOLUMN).setPreferredWidth(125);
		table.getColumnModel().getColumn(ROUTECOLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(DEPARTSCOLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(CURRENTCOLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(TERMINATESCOLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(STATUSCOLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(MOVECOLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(EDITCOLUMN).setPreferredWidth(70);
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
    
    public int getRowCount() { return sysList.size(); }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case IDCOLUMN: return rb.getString("Id");
        case BUILDBOXCOLUMN: return rb.getString("Build");
        case BUILDCOLUMN: return "";
        case NAMECOLUMN: return rb.getString("Name");
        case DESCRIPTIONCOLUMN: return rb.getString("Description");
        case ROUTECOLUMN: return rb.getString("Route");
        case DEPARTSCOLUMN: return rb.getString("Departs");
        case CURRENTCOLUMN: return rb.getString("Current");
        case TERMINATESCOLUMN: return rb.getString("Terminates");
        case STATUSCOLUMN: return rb.getString("Status");
        case MOVECOLUMN: return "";
        case EDITCOLUMN: return "";		//edit column
        default: return "unknown";
        }
    }

    public Class getColumnClass(int col) {
        switch (col) {
        case IDCOLUMN: return String.class;
        case BUILDBOXCOLUMN: return Boolean.class;
        case BUILDCOLUMN: return JButton.class;
        case NAMECOLUMN: return String.class;
        case DESCRIPTIONCOLUMN: return String.class;
        case ROUTECOLUMN: return String.class;
        case DEPARTSCOLUMN: return String.class;
        case CURRENTCOLUMN: return String.class;
        case TERMINATESCOLUMN: return String.class;
        case STATUSCOLUMN: return String.class;
        case MOVECOLUMN: return JButton.class;
        case EDITCOLUMN: return JButton.class;
        default: return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case BUILDCOLUMN: 
        case BUILDBOXCOLUMN:
        case MOVECOLUMN: 
        case EDITCOLUMN: 
        	return true;
        default: 
        	return false;
        }
    }

    public Object getValueAt(int row, int col) {
    	Train train = manager.getTrainById((String)sysList.get(row));
        switch (col) {
        case IDCOLUMN: return train.getId();
        case NAMECOLUMN: return train.getName();
        case DESCRIPTIONCOLUMN: return train.getDescription();
        case BUILDBOXCOLUMN: {
            boolean val = train.getBuild();
            return new Boolean(val);
        }
        case ROUTECOLUMN: return train.getTrainRouteName();
        case DEPARTSCOLUMN: return train.getTrainDepartsName();
        case CURRENTCOLUMN: return train.getCurrentName();
        case TERMINATESCOLUMN: return train.getTrainTerminatesName();
        case STATUSCOLUMN: return train.getStatus();
        case BUILDCOLUMN: {
        	if (!train.getBuilt())
        		return "Build";
        	else
        		return "Print";
        }
        case MOVECOLUMN: return "Move";
        case EDITCOLUMN: return "Edit";
        default: return "unknown "+col;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        switch (col) {
        case EDITCOLUMN: editTrain (row);
        	break;
        case BUILDCOLUMN: buildTrain (row);
    		break;
        case MOVECOLUMN: moveTrain (row);
			break;
        case BUILDBOXCOLUMN:{
        	Train train = manager.getTrainById((String)sysList.get(row));
        	train.setBuild(((Boolean) value).booleanValue());
        }
        default:
            break;
        }
    }
    TrainEditFrame lef = null;
    private void editTrain (int row){
    	if (lef != null)
    		lef.dispose();
    	lef = new TrainEditFrame();
    	Train train = manager.getTrainById((String)sysList.get(row));
       	log.debug("Edit train ("+train.getName()+")");
     	lef.setTitle("Edit Train");
    	lef.initComponents(train);
    }
    
    private void buildTrain (int row){
     	Train train = manager.getTrainById((String)sysList.get(row));
     	if (!train.getBuilt())
     		train.build();
     	else {
     		train.printBuildReport();
     		train.printManifest();
     	}
    }
    
    private void moveTrain (int row){
    	Train train = manager.getTrainById((String)sysList.get(row));
       	if (log.isDebugEnabled()) log.debug("Move train ("+train.getName()+")");
     	train.move();
    }

    public void propertyChange(PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals("listLength")) {
    		updateList();
    		fireTableDataChanged();
    	}
    	else {
    		String trainId = ((Train) e.getSource()).getId();
    		int row = sysList.indexOf(trainId);
    		if(Control.showProperty && log.isDebugEnabled()) log.debug("Update train table row: "+row + " id: " + trainId);
    		if (row >= 0)
    			fireTableRowsUpdated(row, row);
    	}
    }
    
    private void removePropertyChangeTrains() {
    	if (sysList != null) {
    		for (int i = 0; i < sysList.size(); i++) {
    			// if object has been deleted, it's not here; ignore it
    			Train l = manager.getTrainById((String) sysList.get(i));
    			if (l != null)
    				l.removePropertyChangeListener(this);
    		}
    	}
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
    	if (lef != null)
    		lef.dispose();
        manager.removePropertyChangeListener(this);
        removePropertyChangeTrains();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TrainsTableModel.class.getName());
}

