// TrainsTableModel.java

package jmri.jmrit.operations.trains;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
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
 * @version   $Revision: 1.15 $
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
    public final int SORTBYTIME = 2;
    public final int SORTBYID = 3;
    
    private int _sort = SORTBYNAME;
    
    public void setSort (int sort){
    	_sort = sort;
        updateList();
        fireTableStructureChanged();
        initTable(table, frame);
    }
     
    synchronized void updateList() {
		// first, remove listeners from the individual objects
    	removePropertyChangeTrains();
    	
		if (_sort == SORTBYID)
			sysList = manager.getTrainsByIdList();
		else if (_sort == SORTBYNAME)
			sysList = manager.getTrainsByNameList();
		else if (_sort == SORTBYTIME)
			sysList = manager.getTrainsByTimeList();
		// and add them back in
		for (int i = 0; i < sysList.size(); i++){
//			log.debug("route ids: " + (String) sysList.get(i));
			manager.getTrainById(sysList.get(i))
					.addPropertyChangeListener(this);
		}
	}
    
    public List<String> getSelectedTrainList(){
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
		tcm.getColumn(MOVECOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(MOVECOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(BUILDCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(BUILDCOLUMN).setCellEditor(buttonEditor);
		table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());

		// set column preferred widths
		table.getColumnModel().getColumn(IDCOLUMN).setPreferredWidth(40);
		table.getColumnModel().getColumn(BUILDBOXCOLUMN).setPreferredWidth(40);
		table.getColumnModel().getColumn(BUILDCOLUMN).setPreferredWidth(64);
		table.getColumnModel().getColumn(NAMECOLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(DESCRIPTIONCOLUMN).setPreferredWidth(125);
		table.getColumnModel().getColumn(ROUTECOLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(DEPARTSCOLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(CURRENTCOLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(TERMINATESCOLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(STATUSCOLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(MOVECOLUMN).setPreferredWidth(64);
		table.getColumnModel().getColumn(EDITCOLUMN).setPreferredWidth(64);
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
    
    public int getRowCount() { return sysList.size(); }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case IDCOLUMN: {
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
        case MOVECOLUMN: return "";
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
    	// Funky code to put the tef frame in focus after the edit table buttons is used.
    	// The button editor for the table does a repaint of the button cells after the setValueAt code
    	// is called which then returns the focus back onto the table.  We need the edit frame
    	// in focus.
    	if (focusTef){
    		focusTef = false;
    		tef.requestFocus();
    	}
    	Train train = manager.getTrainById(sysList.get(row));
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
            boolean val = train.getBuild();
            return new Boolean(val);
        }
        case ROUTECOLUMN: return train.getTrainRouteName();
        case DEPARTSCOLUMN: return train.getTrainDepartsName();
        case CURRENTCOLUMN: return train.getCurrentLocationName();
        case TERMINATESCOLUMN: return train.getTrainTerminatesName();
        case STATUSCOLUMN: return train.getStatus();
        case BUILDCOLUMN: {
        	if (!train.getBuilt())
        		return rb.getString("Build");
        	return rb.getString("Print");
        }
        case MOVECOLUMN: return rb.getString("Move");
        case EDITCOLUMN: return rb.getString("Edit");
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
    private void editTrain (int row){
    	if (tef != null)
    		tef.dispose();
    	tef = new TrainEditFrame();
    	Train train = manager.getTrainById(sysList.get(row));
       	log.debug("Edit train ("+train.getName()+")");
     	tef.setTitle(rb.getString("TitleTrainEdit"));
    	tef.initComponents(train);
    	focusTef = true;
    }
    
    private void buildTrain (int row){
     	Train train = manager.getTrainById(sysList.get(row));
     	if (!train.getBuilt()){
     		frame.setModifiedFlag(true);
     		train.build(true);
     	} else {
     		train.printBuildReport();
     		train.printManifest();
     	}
    }
    
    private void moveTrain (int row){
    	Train train = manager.getTrainById(sysList.get(row));
       	if (log.isDebugEnabled()) log.debug("Move train ("+train.getName()+")");
       	frame.setModifiedFlag(true);
     	train.move();
    }

    public void propertyChange(PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(manager.LISTLENGTH_CHANGED_PROPERTY) ||
    			e.getPropertyName().equals(Train.DEPARTURETIME_CHANGED_PROPERTY)) {
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
    			Train l = manager.getTrainById(sysList.get(i));
    			if (l != null)
    				l.removePropertyChangeListener(this);
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TrainsTableModel.class.getName());
}

