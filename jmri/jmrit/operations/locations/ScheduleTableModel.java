// ScheduleTableModel.java

package jmri.jmrit.operations.locations;

import java.beans.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;

/**
 * Table Model for edit of a schedule used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2009
 * @version   $Revision: 1.21 $
 */
public class ScheduleTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
    
    // Defines the columns
    private static final int IDCOLUMN = 0;
    private static final int CURRENTCOLUMN = IDCOLUMN +1;
    private static final int TYPECOLUMN  = CURRENTCOLUMN +1;   
    private static final int ROADCOLUMN  = TYPECOLUMN +1;
    private static final int LOADCOLUMN  = ROADCOLUMN +1;
    private static final int SHIPCOLUMN  = LOADCOLUMN +1;
    private static final int DESTCOLUMN  = SHIPCOLUMN +1;
    private static final int TRACKCOLUMN  = DESTCOLUMN +1;
    private static final int COUNTCOLUMN  = TRACKCOLUMN +1;
    private static final int WAITCOLUMN  = COUNTCOLUMN +1;
    private static final int UPCOLUMN = WAITCOLUMN +1;
    private static final int DOWNCOLUMN = UPCOLUMN +1;
    private static final int DELETECOLUMN = DOWNCOLUMN +1;
    
    private static final int HIGHESTCOLUMN = DELETECOLUMN+1;

    public ScheduleTableModel() {
        super();
    }
 
    Schedule _schedule;
    Location _location;
    Track _track;
    
    synchronized void updateList() {
    	if (_schedule == null)
    		return;
		// first, remove listeners from the individual objects
    	removePropertyChangeScheduleItems();
 		list = _schedule.getItemsBySequenceList();
		// and add them back in
		for (int i = 0; i < list.size(); i++){
			log.debug("schedule ids: " + list.get(i));
			_schedule.getItemById(list.get(i))
					.addPropertyChangeListener(this);
		}
	}

	List<String> list = new ArrayList<String>();
    
	void initTable(JTable table, Schedule schedule, Location location, Track track) {
		_schedule = schedule;
		_location = location;
		_track = track;
		
		// add property listeners
		if (_schedule != null)
			_schedule.addPropertyChangeListener(this);
		// get notified if car type changes
		CarTypes.instance().addPropertyChangeListener(this);
		_location.addPropertyChangeListener(this);
		_track.addPropertyChangeListener(this);
		
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(UPCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(UPCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(DOWNCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(DOWNCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(DELETECOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(DELETECOLUMN).setCellEditor(buttonEditor);
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

		// set column preferred widths
		table.getColumnModel().getColumn(IDCOLUMN).setPreferredWidth(35);
		table.getColumnModel().getColumn(CURRENTCOLUMN).setPreferredWidth(50);
		table.getColumnModel().getColumn(TYPECOLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(ROADCOLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(LOADCOLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(SHIPCOLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(DESTCOLUMN).setPreferredWidth(130);
		table.getColumnModel().getColumn(TRACKCOLUMN).setPreferredWidth(130);
		table.getColumnModel().getColumn(COUNTCOLUMN).setPreferredWidth(45);
		table.getColumnModel().getColumn(WAITCOLUMN).setPreferredWidth(40);
		table.getColumnModel().getColumn(UPCOLUMN).setPreferredWidth(60);
		table.getColumnModel().getColumn(DOWNCOLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(DELETECOLUMN).setPreferredWidth(70);
        updateList();
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
    
    public int getRowCount() { return list.size(); }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case IDCOLUMN: return rb.getString("Id");
        case CURRENTCOLUMN: return rb.getString("Current");
        case TYPECOLUMN: return rb.getString("Type");
        case ROADCOLUMN: return rb.getString("Road");
        case LOADCOLUMN: return rb.getString("Receive");
        case SHIPCOLUMN: return rb.getString("Ship");
        case DESTCOLUMN: return rb.getString("Destination");
        case TRACKCOLUMN: return rb.getString("Track");
        case COUNTCOLUMN: return rb.getString("Count");
        case WAITCOLUMN: return rb.getString("Wait");
        case UPCOLUMN: return "";
        case DOWNCOLUMN: return "";
        case DELETECOLUMN: return "";		//edit column
        default: return "unknown";
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
        case IDCOLUMN: return String.class;
        case CURRENTCOLUMN: return String.class;
        case TYPECOLUMN: return String.class;
        case ROADCOLUMN: return JComboBox.class;
        case LOADCOLUMN: return JComboBox.class;
        case SHIPCOLUMN: return JComboBox.class;
        case DESTCOLUMN: return JComboBox.class;
        case TRACKCOLUMN: return JComboBox.class;
        case COUNTCOLUMN: return String.class;
        case WAITCOLUMN: return String.class;
        case UPCOLUMN: return JButton.class;
        case DOWNCOLUMN: return JButton.class;
        case DELETECOLUMN: return JButton.class;
        default: return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case ROADCOLUMN:
        case LOADCOLUMN:
        case SHIPCOLUMN:
        case DESTCOLUMN:
        case TRACKCOLUMN:
        case COUNTCOLUMN:
        case WAITCOLUMN:
        case UPCOLUMN:
        case DOWNCOLUMN:
        case DELETECOLUMN:
        	return true;
        default: 
        	return false;
        }
    }

    public Object getValueAt(int row, int col) {
      	if (row >= list.size())
    		return "ERROR row "+row;
    	ScheduleItem si = _schedule.getItemById(list.get(row));
       	if (si == null)
    		return "ERROR schedule item unknown "+row;
        switch (col) {
        case IDCOLUMN: return si.getId();
        case CURRENTCOLUMN: return getCurrentPointer(si);
        case TYPECOLUMN: return getType(si);
        case ROADCOLUMN: return getRoadComboBox(si);
        case LOADCOLUMN: return getLoadComboBox(si);
        case SHIPCOLUMN: return getShipComboBox(si);
        case DESTCOLUMN: return getDestComboBox(si);
        case TRACKCOLUMN: return getTrackComboBox(si);
        case COUNTCOLUMN: return si.getCount();
        case WAITCOLUMN: return si.getWait();
        case UPCOLUMN: return rb.getObject("Up");
        case DOWNCOLUMN: return rb.getObject("Down");
        case DELETECOLUMN: return rb.getObject("Delete");
        default: return "unknown "+col;
        }
    }

    public void setValueAt(Object value, int row, int col) {
    	if (value == null){
    		log.debug("Warning schedule table row "+row+" still in edit");
    		return;
    	}
        switch (col) {
        case ROADCOLUMN: setRoad(value, row);
        	break;
        case LOADCOLUMN: setLoad(value, row);
    		break;
        case SHIPCOLUMN: setShip(value, row);
			break;
        case DESTCOLUMN: setDestination(value, row);
			break;
        case TRACKCOLUMN: setTrack(value, row);
			break;
        case COUNTCOLUMN: setCount(value, row);
        	break;
        case WAITCOLUMN: setWait(value, row);
    		break;
        case UPCOLUMN: moveUpScheduleItem(row);
        	break;
        case DOWNCOLUMN: moveDownScheduleItem(row);
        	break;
        case DELETECOLUMN:
			deleteScheduleItem(row);
			break;
		default:
			break;
		}
	}
    
    private String getCurrentPointer(ScheduleItem si){
    	if (_track.getCurrentScheduleItem() == si)
    		if (si.getCount() > 1)
    			return " "+_track.getScheduleCount()+" -->";
    		else
    			return "    -->";
    	else
    		return "";
    }
    
    private String getType(ScheduleItem si){
    	if (_track.acceptsTypeName(si.getType()))
    		return si.getType();
    	else
    		return MessageFormat.format(rb.getString("NotValid"),new Object[]{si.getType()});
    }
    
    String notValidRoad =rb.getString("NotValid");
    private JComboBox getRoadComboBox(ScheduleItem si){
    	//log.debug("getRoadComboBox for ScheduleItem "+si.getType());
    	JComboBox cb = new JComboBox();
    	String[] roads = CarRoads.instance().getNames();
    	cb.addItem("");
    	CarManager cm = CarManager.instance();
    	for (int i=0; i<roads.length; i++){
    		if (_track.acceptsRoadName(roads[i])){
    			Car car = cm.getByTypeAndRoad(si.getType(), roads[i]);
    			if (car != null)
    				cb.addItem(roads[i]);
    		}
    	}
    	cb.setSelectedItem(si.getRoad());
    	if (!cb.getSelectedItem().equals(si.getRoad())){
    		notValidRoad = MessageFormat.format(rb.getString("NotValid"),new Object[]{si.getRoad()});
    		cb.addItem(notValidRoad);
    		cb.setSelectedItem(notValidRoad);
    	}
    	return cb;
    }
    
    private JComboBox getLoadComboBox(ScheduleItem si){
    	//log.debug("getLoadComboBox for ScheduleItem "+si.getType());
    	JComboBox cb = CarLoads.instance().getSelectComboBox(si.getType());
    	filterLoads(cb);	// remove loads not accepted by this track
    	cb.setSelectedItem(si.getLoad());
    	if (!cb.getSelectedItem().equals(si.getLoad())){
    		notValidRoad = MessageFormat.format(rb.getString("NotValid"),new Object[]{si.getLoad()});
    		cb.addItem(notValidRoad);
    		cb.setSelectedItem(notValidRoad);
    	}
    	return cb;
    }
    
    private JComboBox getShipComboBox(ScheduleItem si){
    	//log.debug("getShipComboBox for ScheduleItem "+si.getType());
    	JComboBox cb = CarLoads.instance().getSelectComboBox(si.getType());  
    	cb.setSelectedItem(si.getShip());
    	if (!cb.getSelectedItem().equals(si.getShip())){
    		notValidRoad = MessageFormat.format(rb.getString("NotValid"),new Object[]{si.getShip()});
    		cb.addItem(notValidRoad);
    		cb.setSelectedItem(notValidRoad);
    	}
    	return cb;
    }
    
    private JComboBox getDestComboBox(ScheduleItem si){
    	//log.debug("getDestComboBox for ScheduleItem "+si.getType());
    	JComboBox cb = LocationManager.instance().getComboBox();  
    	cb.setSelectedItem(si.getDestination());
    	return cb;
    }
    
    private JComboBox getTrackComboBox(ScheduleItem si){
    	//log.debug("getTrackComboBox for ScheduleItem "+si.getType());
    	JComboBox cb = new JComboBox();
    	if (si.getDestination() != null){
    		Location dest = si.getDestination();
        	dest.updateComboBox(cb);  
        	filterTracks(dest, cb, si.getType(), si.getRoad(), si.getShip());
        	cb.setSelectedItem(si.getDestinationTrack());
    	}
    	return cb;
    }
    
    private void setCount(Object value, int row){
    	ScheduleItem si = _schedule.getItemById(list.get(row));
    	int count;
    	try{
     		count = Integer.parseInt(value.toString());
    	} catch(NumberFormatException e) {
    		log.error("Schedule count must be a number");
    		return;
    	}
    	if (count < 1){
    		log.error("Schedule count must be greater than 0");
    		return;
    	}
    	if (count > 10){
    		log.error("Schedule count must be less than 11");
    		return;
    	}
    	si.setCount(count);
    }
    
    private void setWait(Object value, int row){
    	ScheduleItem si = _schedule.getItemById(list.get(row));
    	int wait;
    	try{
     		wait = Integer.parseInt(value.toString());
    	} catch(NumberFormatException e) {
    		log.error("Schedule wait must be a number");
    		return;
    	}
    	if (wait < 0){
    		log.error("Schedule wait must be a positive number");
    		return;
    	}
    	if (wait > 10){
    		log.error("Schedule wait must be less than 11");
    		return;
    	}
    	si.setWait(wait);
    }
    
    // note this method looks for String "Not Valid <>"
    private void setRoad(Object value, int row){
    	ScheduleItem si = _schedule.getItemById(list.get(row));
    	String road = (String)((JComboBox)value).getSelectedItem();
    	// String "Not Valid <>" is 12 char in length
    	if (road.length()<12){
    		si.setRoad(road);
    		return;
    	}
    	String test = road.substring(0, 11);
    	if (!test.equals(rb.getString("NotValid").substring(0, 11)))
    		si.setRoad(road);
    }
    
    // note this method looks for String "Not Valid <>"
    private void setLoad(Object value, int row){
    	ScheduleItem si = _schedule.getItemById(list.get(row));
    	String load = (String)((JComboBox)value).getSelectedItem();
    	// String "Not Valid <>" is 12 char in length
    	if (load.length()<12){
    		si.setLoad(load);
    		return;
    	}
    	String test = load.substring(0, 11);
    	if (!test.equals(rb.getString("NotValid").substring(0, 11)))
    		si.setLoad(load);
    }
    
    private void setShip(Object value, int row){
       	ScheduleItem si = _schedule.getItemById(list.get(row));
    	String load = (String)((JComboBox)value).getSelectedItem();
    	// String "Not Valid <>" is 12 char in length
    	if (load.length()<12){
    		si.setShip(load);
    		return;
    	}
    	String test = load.substring(0, 11);
    	if (!test.equals(rb.getString("NotValid").substring(0, 11)))
    		si.setShip(load);
    }
    
    private void setDestination(Object value, int row){ 	
    	if (((JComboBox)value).getSelectedItem() != null){
    		ScheduleItem si = _schedule.getItemById(list.get(row));
    		si.setDestinationTrack(null);
    		Location dest = null;
    		if (!((JComboBox)value).getSelectedItem().equals("")){
    			dest = (Location)((JComboBox)value).getSelectedItem();
    		}
    		si.setDestination(dest);
    		fireTableCellUpdated(row, TRACKCOLUMN);
    	}
    }
    
    private void setTrack(Object value, int row){
    	if (((JComboBox)value).getSelectedItem() != null){
    		ScheduleItem si = _schedule.getItemById(list.get(row));
    		Track track = null;
    		if (!((JComboBox)value).getSelectedItem().equals("")){
    			track = (Track)((JComboBox)value).getSelectedItem();
    		}
    		si.setDestinationTrack(track);
    	}
    }
    
    private void moveUpScheduleItem (int row){
    	log.debug("move schedule item up");
		ScheduleItem si = _schedule.getItemById(list.get(row));
    	_schedule.moveItemUp(si);
    }
    
    private void moveDownScheduleItem (int row){
    	log.debug("move schedule item down");
		ScheduleItem si = _schedule.getItemById(list.get(row));
    	_schedule.moveItemDown(si);
    }

    private void deleteScheduleItem (int row){
    	log.debug("Delete schedule item");
		ScheduleItem si = _schedule.getItemById(list.get(row));
    	_schedule.deleteItem(si);
    }
    
    // remove destination tracks that don't service the car's type, road, or load
    private void filterTracks (Location loc, JComboBox cb, String carType, String carRoad, String carLoad){
    	List<String> tracks = loc.getTracksByNameList(null);
    	for (int i=0; i<tracks.size(); i++){
    		Track track = loc.getTrackById(tracks.get(i));
    		if (!track.acceptsTypeName(carType) || track.getLocType().equals(Track.STAGING) 
    				|| (!carRoad.equals("") && !track.acceptsRoadName(carRoad)) 
    				|| (!carLoad.equals("") && !track.acceptsLoadName(carLoad)))
    			cb.removeItem(track);
    	}
    }
    
    // remove receive loads not serviced by track
    private void filterLoads (JComboBox cb){
    	for (int i=cb.getItemCount()-1; i>0; i--){
    		String loadName = (String)cb.getItemAt(i);
    		if (!loadName.equals("") && !_track.acceptsLoadName(loadName))
    			cb.removeItem(loadName);
    	}
    }

    private int _trainDirection = Setup.getDirectionInt((String)Setup.getComboBox().getItemAt(0));

    public int getLastTrainDirection(){
    	return _trainDirection;
    }

    // this table listens for changes to a schedule and it's car types
    public void propertyChange(PropertyChangeEvent e) {
    	if (Control.showProperty && log.isDebugEnabled()) log.debug("ScheduleTableModel sees property change: " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(Schedule.LISTCHANGE_CHANGED_PROPERTY)) {
    		updateList();
    		fireTableDataChanged();
    	}
    	if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY) ||
    			e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY) ||
    			e.getPropertyName().equals(Track.ROADS_CHANGED_PROPERTY) ||
    			e.getPropertyName().equals(Track.SCHEDULE_CHANGED_PROPERTY) ||
    			e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)){
    		fireTableDataChanged();
    	}

    }

    private void removePropertyChangeScheduleItems() {
    	for (int i = 0; i < list.size(); i++) {
    		// if object has been deleted, it's not here; ignore it
    		ScheduleItem si = _schedule.getItemById(list.get(i));
    		if (si != null)
    			si.removePropertyChangeListener(this);
    	}
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        if (_schedule != null){
        	removePropertyChangeScheduleItems();
        	_schedule.removePropertyChangeListener(this);
        }
        CarTypes.instance().removePropertyChangeListener(this);
		_location.removePropertyChangeListener(this);
		_track.removePropertyChangeListener(this);

    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ScheduleTableModel.class.getName());
}

