// ScheduleManager.java

package jmri.jmrit.operations.locations;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;


/**
 * Manages schedules.
 * @author      Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision$
 */
public class ScheduleManager implements java.beans.PropertyChangeListener {
	public static final String LISTLENGTH_CHANGED_PROPERTY = "scheduleListLength"; // NOI18N
    
	public ScheduleManager() {
		CarTypes.instance().addPropertyChangeListener(this);
		CarRoads.instance().addPropertyChangeListener(this);
    }
    
	/** record the single instance **/
	private static ScheduleManager _instance = null;
	private int _id = 0;

	public static synchronized ScheduleManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("ScheduleManager creating instance");
			// create and load
			_instance = new ScheduleManager();
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("ScheduleManager returns instance "+_instance);
		return _instance;
	}

    public void dispose() {
        _scheduleHashTable.clear();
    }

    protected Hashtable<String, Schedule> _scheduleHashTable = new Hashtable<String, Schedule>();   // stores known Schedule instances by id

    /**
     * @return Number of schedules
     */
    public int numEntries() { return _scheduleHashTable.size(); }
    
    /**
     * @return requested Schedule object or null if none exists
     */
    public Schedule getScheduleByName(String name) {
    	Schedule s;
    	Enumeration<Schedule> en =_scheduleHashTable.elements();
    	for (int i = 0; i < _scheduleHashTable.size(); i++){
    		s = en.nextElement();
    		if (s.getName().equals(name))
    			return s;
      	}
        return null;
    }
    
    public Schedule getScheduleById (String id){
    	return _scheduleHashTable.get(id);
    }
 
    /**
     * Finds an exsisting schedule or creates a new schedule if needed
     * requires schedule's name creates a unique id for this schedule
     * @param name
     * 
     * @return new schedule or existing schedule
     */
    public Schedule newSchedule (String name){
    	Schedule schedule = getScheduleByName(name);
    	if (schedule == null){
    		_id++;						
    		schedule = new Schedule(Integer.toString(_id), name);
    		Integer oldSize = Integer.valueOf(_scheduleHashTable.size());
    		_scheduleHashTable.put(schedule.getId(), schedule);
    		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_scheduleHashTable.size()));
    	}
    	return schedule;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
 	 */
    public void register(Schedule schedule) {
    	Integer oldSize = Integer.valueOf(_scheduleHashTable.size());
        _scheduleHashTable.put(schedule.getId(), schedule);
        // find last id created
        int id = Integer.parseInt(schedule.getId());
        if (id > _id)
        	_id = id;
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_scheduleHashTable.size()));
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     */
    public void deregister(Schedule schedule) {
    	if (schedule == null)
    		return;
        schedule.dispose();
        Integer oldSize = Integer.valueOf(_scheduleHashTable.size());
    	_scheduleHashTable.remove(schedule.getId());
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_scheduleHashTable.size()));
    }

    /**
     * Sort by schedule name
     * @return list of schedule ids ordered by name
     */
    public List<String> getSchedulesByNameList() {
		// first get id list
		List<String> sortList = getList();
		// now re-sort
		List<String> out = new ArrayList<String>();
		String schName = "";
		boolean schAdded = false;
		Schedule s;

		for (int i = 0; i < sortList.size(); i++) {
			schAdded = false;
			s = getScheduleById(sortList.get(i));
			schName = s.getName();
			for (int j = 0; j < out.size(); j++) {
				s = getScheduleById(out.get(j));
				String outLocName = s.getName();
				if (schName.compareToIgnoreCase(outLocName) < 0) {
					out.add(j, sortList.get(i));
					schAdded = true;
					break;
				}
			}
			if (!schAdded) {
				out.add(sortList.get(i));
			}
		}
		return out;

	}
    
    /**
	 * Sort by schedule number
	 * 
	 * @return list of schedule ids ordered by number
	 */
    public List<String> getSchedulesByIdList() {
    	// first get id list
    	List<String> sortList = getList();
    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	int scheduleNumber = 0;
    	boolean scheduleAdded = false;
    	Schedule s;
    	
    	for (int i=0; i<sortList.size(); i++){
    		scheduleAdded = false;
    		s = getScheduleById (sortList.get(i));
    		try{
    			scheduleNumber = Integer.parseInt (s.getId());
    		}catch (NumberFormatException e) {
    			log.debug("schedule id number isn't a number");
    		}
    		for (int j=0; j<out.size(); j++ ){
    			s = getScheduleById (out.get(j));
        		try{
        			int outScheduleNumber = Integer.parseInt (s.getId());
        			if (scheduleNumber < outScheduleNumber){
        				out.add(j, sortList.get(i));
        				scheduleAdded = true;
        				break;
        			}
        		}catch (NumberFormatException e) {
        			log.debug("list out id number isn't a number");
        		}
    		}
    		if (!scheduleAdded){
    			out.add( sortList.get(i));
    		}
    	}
        return out;
    }
    
    private List<String> getList() {
        String[] arr = new String[_scheduleHashTable.size()];
        List<String> out = new ArrayList<String>();
        Enumeration<String> en = _scheduleHashTable.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }
    
    /**
     * Gets a JComboBox loaded with schedules.
     * @return JComboBox with a list of schedules.
     */
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
    	box.addItem("");
		List<String> schs = getSchedulesByNameList();
		for (int i = 0; i < schs.size(); i++){
			String id = schs.get(i);
			box.addItem(getScheduleById(id));
		}
    	return box;
    }
    
    /**
     * Update a JComboBox with the latest schedules.
     * @param box the JComboBox needing an update.
     */
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
		List<String> schs = getSchedulesByNameList();
		for (int i = 0; i < schs.size(); i++){
			String id = schs.get(i);
			box.addItem(getScheduleById(id));
		}
    }
    
    /**
     * Replaces car type in all schedules.
     * @param oldType car type to be replaced.
     * @param newType replacement car type.
     */
    public void replaceType(String oldType, String newType){
		List<String> schs = getSchedulesByIdList();
		for (int i=0; i<schs.size(); i++){
			Schedule sch = getScheduleById(schs.get(i));
			List<String> items = sch.getItemsBySequenceList();
			for(int j=0; j<items.size(); j++ ){
				ScheduleItem si = sch.getItemById(items.get(j));
				if (si.getType().equals(oldType)){
					si.setType(newType);
				}
			}
		}
    }
    
    /**
     * Replaces car roads in all schedules.
     * @param oldRoad car road to be replaced.
     * @param newRoad replacement car road.
     */
	public void replaceRoad(String oldRoad, String newRoad){
		if (newRoad == null)
			return;
		List<String> schs = getSchedulesByIdList();
		for (int i=0; i<schs.size(); i++){
			Schedule sch = getScheduleById(schs.get(i));
			List<String> items = sch.getItemsBySequenceList();
			for(int j=0; j<items.size(); j++ ){
				ScheduleItem si = sch.getItemById(items.get(j));
				if (si.getRoad().equals(oldRoad)){
					si.setRoad(newRoad);
				}
			}
		}
	}
	
	/**
	 * Replaces car loads in all schedules with specific car type.
	 * @param type car type.
	 * @param oldLoad car load to be replaced.
	 * @param newLoad replacement car load.
	 */
	public void replaceLoad(String type, String oldLoad, String newLoad){
		List<String> schs = getSchedulesByIdList();
		for (int i=0; i<schs.size(); i++){
			Schedule sch = getScheduleById(schs.get(i));
			List<String> items = sch.getItemsBySequenceList();
			for(int j=0; j<items.size(); j++ ){
				ScheduleItem si = sch.getItemById(items.get(j));
				if (si.getType().equals(type) && si.getLoad().equals(oldLoad)){
					si.setLoad(newLoad);
				}
				if (si.getType().equals(type) && si.getShip().equals(oldLoad)){
					si.setShip(newLoad);
				}
			}
		}
	}
	
	/**
	 * Gets a JComboBox with a list of spurs that use this schedule.
	 * @param schedule The schedule for this JComboBox. 
	 * @return JComboBox with a list of spurs using schedule.
	 */
	public JComboBox getSidingsByScheduleComboBox(Schedule schedule){
		JComboBox box = new JComboBox();
    	// search all spurs for that use schedule
    	LocationManager manager = LocationManager.instance();
    	List<String> locations = manager.getLocationsByNameList();
    	for (int j=0; j<locations.size(); j++){
			Location location = manager.getLocationById(locations.get(j));
			List<String> sidings = location.getTrackIdsByNameList(Track.SIDING);
			for (int k=0; k<sidings.size(); k++){
				Track siding = location.getTrackById(sidings.get(k));
				if (siding.getScheduleId().equals(schedule.getId())){
					LocationTrackPair ltp = new LocationTrackPair(location, siding);
					box.addItem(ltp);
				}
			}
    	}
    	return box;
	}
	
	/**
	 * Check for car type and road name changes. 
	 * 
	 */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	log.debug("ScheduleManager sees property change: " + e.getPropertyName() + " old: " + e.getOldValue() + " new " + e.getNewValue());
    	if (e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY)){
    		replaceType((String)e.getOldValue(), (String)e.getNewValue());
    	}
    	if (e.getPropertyName().equals(CarRoads.CARROADS_NAME_CHANGED_PROPERTY)){
    		replaceRoad((String)e.getOldValue(), (String)e.getNewValue());
    	}
    }
    
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) {
   	   	// set dirty
    	LocationManagerXml.instance().setDirty(true);
    	pcs.firePropertyChange(p,old,n);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ScheduleManager.class.getName());

}

/* @(#)ScheduleManager.java */
