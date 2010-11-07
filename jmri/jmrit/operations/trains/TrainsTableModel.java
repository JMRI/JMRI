// TrainsTableModel.java

package jmri.jmrit.operations.trains;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import jmri.jmrit.beantable.EnablingCheckboxRenderer;
import jmri.jmrit.operations.setup.Control;
import jmri.util.JmriJFrame;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of trains used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision: 1.33 $
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
    private static final int ACTIONCOLUMN = STATUSCOLUMN+1;
    private static final int EDITCOLUMN = ACTIONCOLUMN+1;
    
    private static final int HIGHESTCOLUMN = EDITCOLUMN+1;

    public TrainsTableModel() {
        super();
        manager.addPropertyChangeListener(this);
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
			sysList = manager.getTrainsByIdList();
		else if (_sort == SORTBYNAME)
			sysList = manager.getTrainsByNameList();
		else if (_sort == SORTBYTIME)
			sysList = manager.getTrainsByTimeList();
		else if (_sort == SORTBYDEPARTS)
			sysList = manager.getTrainsByDepartureList();
		else if (_sort == SORTBYTERMINATES)
			sysList = manager.getTrainsByTerminatesList();
		else if (_sort == SORTBYROUTE)
			sysList = manager.getTrainsByRouteList();
		
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
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(EDITCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDITCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(ACTIONCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(ACTIONCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(BUILDCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(BUILDCOLUMN).setCellEditor(buttonEditor);
		table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());

		// set column preferred widths
		int[] tableColumnWidths = manager.getTrainsFrameTableColumnWidths();
		for (int i=0; i<tcm.getColumnCount(); i++)
			tcm.getColumn(i).setPreferredWidth(tableColumnWidths[i]);
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
    
    public synchronized int getRowCount() { return sysList.size(); }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case IDCOLUMN: synchronized(this){
        	if (_sort == SORTBYID)
        		return rb.getString("Id");
        	return rb.getString("Time");
        }
        case BUILDBOXCOLUMN: return rb.getString("Build");
        case BUILDCOLUMN: return "";
        case NAMECOLUMN: return rb.getString("Name");
        case DESCRIPTIONCOLUMN: return rb.getString("Description");
        case ROUTECOLUMN: return rb.getString("Route");
        case DEPARTSCOLUMN: return rb.getString("Departs");
        case CURRENTCOLUMN: return rb.getString("Current");
        case TERMINATESCOLUMN: return rb.getString("Terminates");
        case STATUSCOLUMN: return rb.getString("Status");
        case ACTIONCOLUMN: return rb.getString("Action");
        case EDITCOLUMN: return "";		//edit column
        default: return "unknown";
        }
    }

    public Class<?> getColumnClass(int col) {
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
        case ACTIONCOLUMN: return JButton.class;
        case EDITCOLUMN: return JButton.class;
        default: return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case BUILDCOLUMN: 
        case BUILDBOXCOLUMN:
        case ACTIONCOLUMN: 
        case EDITCOLUMN: 
        	return true;
        default: 
        	return false;
        }
    }

    public synchronized Object getValueAt(int row, int col) {
    	// Funky code to put the tef frame in focus after the edit table buttons is used.
    	// The button editor for the table does a repaint of the button cells after the setValueAt code
    	// is called which then returns the focus back onto the table.  We need the edit frame
    	// in focus.
    	if (focusTef){
    		focusTef = false;
    		tef.requestFocus();
    	}
       	if (row >= sysList.size())
    		return "ERROR row "+row;
    	Train train = manager.getTrainById(sysList.get(row));
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
        case BUILDBOXCOLUMN: {
            return Boolean.valueOf(train.getBuild());
        }
        case ROUTECOLUMN: return train.getTrainRouteName();
        case DEPARTSCOLUMN: return train.getTrainDepartsName();
        case CURRENTCOLUMN: return train.getCurrentLocationName();
        case TERMINATESCOLUMN: return train.getTrainTerminatesName();
        case STATUSCOLUMN: return train.getStatus();
        case BUILDCOLUMN: {
        	if (train.getBuilt())
        		if (manager.isPrintPreviewEnabled())
        			return rb.getString("Preview");
        		else
        			return rb.getString("Print");
        	return rb.getString("Build");
        }
        case ACTIONCOLUMN: {
        	if (train.getBuildFailed())
        		return rb.getString("Report");
        	return manager.getTrainsFrameTrainAction();
        }
        case EDITCOLUMN: return rb.getString("Edit");
        default: return "unknown "+col;
        }
    }

    public synchronized void setValueAt(Object value, int row, int col) {
        switch (col) {
        case EDITCOLUMN: editTrain (row);
        	break;
        case BUILDCOLUMN: buildTrain (row);
    		break;
        case ACTIONCOLUMN: actionTrain (row);
			break;
        case BUILDBOXCOLUMN:{
        	Train train = manager.getTrainById(sysList.get(row));
        	train.setBuild(((Boolean) value).booleanValue());
        	break;
        }
        default:
            break;
        }
    }
    
    boolean focusTef = false;
    TrainEditFrame tef = null;
    private synchronized void editTrain (int row){
    	if (tef != null)
    		tef.dispose();
    	tef = new TrainEditFrame();
    	Train train = manager.getTrainById(sysList.get(row));
       	log.debug("Edit train ("+train.getName()+")");
     	tef.setTitle(rb.getString("TitleTrainEdit"));
    	tef.initComponents(train);
    	focusTef = true;
    }
    
    private synchronized void buildTrain (int row){
     	Train train = manager.getTrainById(sysList.get(row));
     	if (!train.getBuilt()){
     		train.build();
     	// print
     	} else {
     		if (manager.isBuildReportEnabled())
     			train.printBuildReport();
     		train.printManifestIfBuilt();
     	}
    }
    
    // one of three buttons, report, move, terminate
    private synchronized void actionTrain (int row){
    	Train train = manager.getTrainById(sysList.get(row));
    	// move button become report if failure
    	if (train.getBuildFailed()){
    		train.printBuildReport();
    	} else if (manager.getTrainsFrameTrainAction().equals(TrainsTableFrame.MOVE)) {
       		if (log.isDebugEnabled()) log.debug("Move train ("+train.getName()+")");
     		train.move();
    	} else if (train.getBuilt()){
       		if (log.isDebugEnabled()) log.debug("Terminate train ("+train.getName()+")");
			int status = JOptionPane.showConfirmDialog(null,
					"Terminate Train ("+train.getName()+") "+train.getDescription()+"?",
					"Do you want to terminate train ("+train.getName()+")?", JOptionPane.YES_NO_OPTION);
			if (status == JOptionPane.YES_OPTION)
				train.terminate();
    	}
    }

    public void propertyChange(PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
       	if (e.getPropertyName().equals(Train.STATUS_CHANGED_PROPERTY) ||
       			e.getPropertyName().equals(Train.TRAIN_LOCATION_CHANGED_PROPERTY)){
       		manager.setFilesDirty();
       		frame.setModifiedFlag(true);
       	}
    	if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY) ||
    			e.getPropertyName().equals(TrainManager.PRINTPREVIEW_CHANGED_PROPERTY) ||
    			e.getPropertyName().equals(TrainManager.TRAIN_ACTION_CHANGED_PROPERTY) ||
    			e.getPropertyName().equals(Train.DEPARTURETIME_CHANGED_PROPERTY)) {
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
    
    private synchronized void removePropertyChangeTrains() {
    	if (sysList != null) {
    		for (int i = 0; i < sysList.size(); i++) {
    			// if object has been deleted, it's not here; ignore it
    			Train t = manager.getTrainById(sysList.get(i));
    			if (t != null)
    				t.removePropertyChangeListener(this);
    		}
    	}
    }
    
    private synchronized void addPropertyChangeTrains() {
    	if (sysList != null) {
    		for (int i = 0; i < sysList.size(); i++) {
    			// if object has been deleted, it's not here; ignore it
    			Train t = manager.getTrainById(sysList.get(i));
    			if (t != null)
    				t.addPropertyChangeListener(this);
    		}
    	}
    }


    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
    	if (tef != null)
    		tef.dispose();
        manager.removePropertyChangeListener(this);
        removePropertyChangeTrains();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainsTableModel.class.getName());
}

