// TrainsScheduleTableModel.java

package jmri.jmrit.operations.trains;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import jmri.jmrit.beantable.EnablingCheckboxRenderer;
import jmri.jmrit.operations.setup.Control;
import jmri.util.JmriJFrame;

/**
 * Table Model for edit of train schedules used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2010
 * @version   $Revision: 1.1 $
 */
public class TrainsScheduleTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
   
	TrainManager trainManager = TrainManager.instance();
    TrainScheduleManager scheduleManager = TrainScheduleManager.instance();
 
    // Defines the columns
    private static final int IDCOLUMN   = 0;
    private static final int NAMECOLUMN = IDCOLUMN+1;
    private static final int DESCRIPTIONCOLUMN = NAMECOLUMN+1;
   
    private static final int FIXEDCOLUMN = DESCRIPTIONCOLUMN+1;

    public TrainsScheduleTableModel() {
        super();
        trainManager.addPropertyChangeListener(this);
        updateList();
    }
    
    public final int SORTBYNAME = 1;
    public final int SORTBYTIME = 2;
    public final int SORTBYDEPARTS = 3;
    public final int SORTBYTERMINATES = 4;
    public final int SORTBYROUTE = 5;
    public final int SORTBYID = 6;
    
    private int _sort = SORTBYNAME;
    
    public void setSort (int sort){
    	synchronized(this){
    		_sort = sort;
    	}
        updateList();
        fireTableStructureChanged();
        initTable(table, frame);
    }
     
    private synchronized void updateList() {
		// first, remove listeners from the individual objects
    	removePropertyChangeTrains();
    	
		if (_sort == SORTBYID)
			sysList = trainManager.getTrainsByIdList();
		else if (_sort == SORTBYNAME)
			sysList = trainManager.getTrainsByNameList();
		else if (_sort == SORTBYTIME)
			sysList = trainManager.getTrainsByTimeList();
		else if (_sort == SORTBYDEPARTS)
			sysList = trainManager.getTrainsByDepartureList();
		else if (_sort == SORTBYTERMINATES)
			sysList = trainManager.getTrainsByTerminatesList();
		else if (_sort == SORTBYROUTE)
			sysList = trainManager.getTrainsByRouteList();
		
		// and add listeners back in
		addPropertyChangeTrains();
	}
    
    public synchronized List<String> getSelectedTrainList(){
		return sysList;
    }

	List<String> sysList = null;
	JTable table = null;
	JmriJFrame frame = null;
    
	void initTable(JTable table, JmriJFrame frame) {
		this.table = table;
		this.frame = frame;
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());

		// set column preferred widths
		int[] widths = trainManager.getTrainScheduleFrameTableColumnWidths();
		int numCol = widths.length;
		if (widths.length > getColumnCount())
			numCol = getColumnCount();
		for (int i=0; i<numCol; i++){
			tcm.getColumn(i).setPreferredWidth(widths[i]);
		}
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
    
    public synchronized int getRowCount() { return sysList.size(); }

    public int getColumnCount(){ 
    	return FIXEDCOLUMN + scheduleManager.numEntries();
    }

    public String getColumnName(int col) {
        switch (col) {
        case IDCOLUMN: synchronized(this){
        	if (_sort == SORTBYID)
        		return rb.getString("Id");
        	return rb.getString("Time");
        }
        case NAMECOLUMN: return rb.getString("Name");
        case DESCRIPTIONCOLUMN: return rb.getString("Description");
        }
        TrainSchedule ts = getSchedule(col);
        if (ts != null){
        	return ts.getName();
        }
        return "unknown";
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
        case IDCOLUMN: return String.class;
        case NAMECOLUMN: return String.class;
        case DESCRIPTIONCOLUMN: return String.class;
        }
        if (col >= FIXEDCOLUMN && col < getColumnCount()){
        	return Boolean.class;
        }
        return null;
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case IDCOLUMN: 
        case NAMECOLUMN: 
        case DESCRIPTIONCOLUMN: 
        	return false;
        default: 
        	return true;
        }
    }

    public synchronized Object getValueAt(int row, int col) {
       	if (row >= sysList.size())
    		return "ERROR row "+row;
    	Train train = trainManager.getTrainById(sysList.get(row));
      	if (train == null)
    		return "ERROR train unknown "+row;
        switch (col) {
        case IDCOLUMN: {
        	if (_sort == SORTBYID){
           		return train.getId();
        	}
        	return train.getDepartureTime();
        }
        case NAMECOLUMN: return train.getIconName();
        case DESCRIPTIONCOLUMN: return train.getDescription();
        }
        TrainSchedule ts = getSchedule(col);
        if (ts != null){
        	return ts.containsTrainId(train.getId());
        }
        return "unknown "+col;
    }

    public synchronized void setValueAt(Object value, int row, int col) {
    	TrainSchedule ts = getSchedule(col);
        if (ts != null){
           	Train train = trainManager.getTrainById(sysList.get(row));
          	if (train == null){
          		log.error("train not found");
          		return;
          	}
          	if (((Boolean) value).booleanValue()){
          		ts.addTrainId(train.getId());
          	} else {
          		ts.removeTrainId(train.getId());
          	}
        }
    }
    


    public void propertyChange(PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY) ||
    			e.getPropertyName().equals(TrainManager.TRAIN_ACTION_CHANGED_PROPERTY)){
    		updateList();
    		fireTableDataChanged();
    	} else synchronized(this) {
    		String trainId = ((Train) e.getSource()).getId();
    		int row = sysList.indexOf(trainId);
    		if(Control.showProperty && log.isDebugEnabled()) log.debug("Update train table row: "+row + " id: " + trainId);
    		if (row >= 0)
    			fireTableRowsUpdated(row, row);
    	}
    }
    
    private TrainSchedule getSchedule(int col){
        if (col >= FIXEDCOLUMN && col < getColumnCount()){
        	List<String> l = scheduleManager.getSchedulesByIdList();
        	TrainSchedule schedule = scheduleManager.getScheduleById(l.get(col-FIXEDCOLUMN));
        	return schedule;
        }else{
        	return null;
        }
    }
    
    private synchronized void removePropertyChangeTrains() {
    	if (sysList != null) {
    		for (int i = 0; i < sysList.size(); i++) {
    			// if object has been deleted, it's not here; ignore it
    			Train t = trainManager.getTrainById(sysList.get(i));
    			if (t != null)
    				t.removePropertyChangeListener(this);
    		}
    	}
    }
    
    private synchronized void addPropertyChangeTrains() {
    	if (sysList != null) {
    		for (int i = 0; i < sysList.size(); i++) {
    			// if object has been deleted, it's not here; ignore it
    			Train t = trainManager.getTrainById(sysList.get(i));
    			if (t != null)
    				t.addPropertyChangeListener(this);
    		}
    	}
    }


    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        trainManager.removePropertyChangeListener(this);
        removePropertyChangeTrains();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainsScheduleTableModel.class.getName());
}

