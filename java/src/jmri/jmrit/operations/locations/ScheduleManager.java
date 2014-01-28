// ScheduleManager.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import org.jdom.Element;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;

/**
 * Manages schedules.
 * 
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008, 2013
 * @version $Revision$
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
			if (log.isDebugEnabled())
				log.debug("ScheduleManager creating instance");
			// create and load
			_instance = new ScheduleManager();
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("ScheduleManager returns instance " + _instance);
		return _instance;
	}

	public void dispose() {
		_scheduleHashTable.clear();
	}

	// stores known Schedule instances by id
	protected Hashtable<String, Schedule> _scheduleHashTable = new Hashtable<String, Schedule>(); 

	/**
	 * @return Number of schedules
	 */
	public int numEntries() {
		return _scheduleHashTable.size();
	}

	/**
	 * @return requested Schedule object or null if none exists
	 */
	public Schedule getScheduleByName(String name) {
		Schedule s;
		Enumeration<Schedule> en = _scheduleHashTable.elements();
		while (en.hasMoreElements()) {
			s = en.nextElement();
			if (s.getName().equals(name))
				return s;
		}
		return null;
	}

	public Schedule getScheduleById(String id) {
		return _scheduleHashTable.get(id);
	}

	/**
	 * Finds an existing schedule or creates a new schedule if needed requires schedule's name creates a unique id for
	 * this schedule
	 * 
	 * @param name
	 * 
	 * @return new schedule or existing schedule
	 */
	public Schedule newSchedule(String name) {
		Schedule schedule = getScheduleByName(name);
		if (schedule == null) {
			_id++;
			schedule = new Schedule(Integer.toString(_id), name);
			Integer oldSize = Integer.valueOf(_scheduleHashTable.size());
			_scheduleHashTable.put(schedule.getId(), schedule);
			setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_scheduleHashTable
					.size()));
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
		setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_scheduleHashTable.size()));
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
		setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_scheduleHashTable.size()));
	}

	/**
	 * Sort by schedule name
	 * 
	 * @return list of schedules ordered by name
	 */
	public List<Schedule> getSchedulesByNameList() {
		List<Schedule> sortList = getList();
		// now re-sort
		List<Schedule> out = new ArrayList<Schedule>();
		for (int i = 0; i < sortList.size(); i++) {
			for (int j = 0; j < out.size(); j++) {
				if (sortList.get(i).getName().compareToIgnoreCase(out.get(j).getName()) < 0) {
					out.add(j, sortList.get(i));
					break;
				}
			}
			if (!out.contains(sortList.get(i))) {
				out.add(sortList.get(i));
			}
		}
		return out;

	}

	/**
	 * Sort by schedule number
	 * 
	 * @return list of schedules ordered by number
	 */
	public List<Schedule> getSchedulesByIdList() {
		List<Schedule> sortList = getList();
		// now re-sort
		List<Schedule> out = new ArrayList<Schedule>();
		for (int i = 0; i < sortList.size(); i++) {
			for (int j = 0; j < out.size(); j++) {
				try {
					if (Integer.parseInt(sortList.get(i).getId()) < Integer.parseInt(out.get(j).getId())) {
						out.add(j, sortList.get(i));
						break;
					}
				} catch (NumberFormatException e) {
					log.debug("list id number isn't a number");
				}
			}
			if (!out.contains(sortList.get(i))) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}

	private List<Schedule> getList() {
		List<Schedule> out = new ArrayList<Schedule>();
		Enumeration<Schedule> en = _scheduleHashTable.elements();
		while (en.hasMoreElements()) {
			out.add(en.nextElement());
		}
		return out;
	}

	/**
	 * Gets a JComboBox loaded with schedules.
	 * 
	 * @return JComboBox with a list of schedules.
	 */
	public JComboBox getComboBox() {
		JComboBox box = new JComboBox();
		box.addItem("");
		List<Schedule> schs = getSchedulesByNameList();
		for (int i = 0; i < schs.size(); i++) {
			box.addItem(schs.get(i));
		}
		return box;
	}

	/**
	 * Update a JComboBox with the latest schedules.
	 * 
	 * @param box
	 *            the JComboBox needing an update.
	 */
	public void updateComboBox(JComboBox box) {
		box.removeAllItems();
		box.addItem("");
		List<Schedule> schs = getSchedulesByNameList();
		for (int i = 0; i < schs.size(); i++) {
			box.addItem(schs.get(i));
		}
	}

	/**
	 * Replaces car type in all schedules.
	 * 
	 * @param oldType
	 *            car type to be replaced.
	 * @param newType
	 *            replacement car type.
	 */
	public void replaceType(String oldType, String newType) {
		List<Schedule> schs = getSchedulesByIdList();
		for (int i = 0; i < schs.size(); i++) {
			Schedule sch = schs.get(i);
			List<ScheduleItem> items = sch.getItemsBySequenceList();
			for (int j = 0; j < items.size(); j++) {
				ScheduleItem si = items.get(j);
				if (si.getTypeName().equals(oldType)) {
					si.setTypeName(newType);
				}
			}
		}
	}

	/**
	 * Replaces car roads in all schedules.
	 * 
	 * @param oldRoad
	 *            car road to be replaced.
	 * @param newRoad
	 *            replacement car road.
	 */
	public void replaceRoad(String oldRoad, String newRoad) {
		if (newRoad == null)
			return;
		List<Schedule> schs = getSchedulesByIdList();
		for (int i = 0; i < schs.size(); i++) {
			Schedule sch = schs.get(i);
			List<ScheduleItem> items = sch.getItemsBySequenceList();
			for (int j = 0; j < items.size(); j++) {
				ScheduleItem si = items.get(j);
				if (si.getRoadName().equals(oldRoad)) {
					si.setRoadName(newRoad);
				}
			}
		}
	}

	/**
	 * Replaces car loads in all schedules with specific car type.
	 * 
	 * @param type
	 *            car type.
	 * @param oldLoad
	 *            car load to be replaced.
	 * @param newLoad
	 *            replacement car load.
	 */
	public void replaceLoad(String type, String oldLoad, String newLoad) {
		List<Schedule> schs = getSchedulesByIdList();
		for (int i = 0; i < schs.size(); i++) {
			Schedule sch = schs.get(i);
			List<ScheduleItem> items = sch.getItemsBySequenceList();
			for (int j = 0; j < items.size(); j++) {
				ScheduleItem si = items.get(j);
				if (si.getTypeName().equals(type) && si.getReceiveLoadName().equals(oldLoad)) {
					if (newLoad != null)
						si.setReceiveLoadName(newLoad);
					else
						si.setReceiveLoadName("");
				}
				if (si.getTypeName().equals(type) && si.getShipLoadName().equals(oldLoad)) {
					if (newLoad != null)
						si.setShipLoadName(newLoad);
					else
						si.setShipLoadName("");
				}
			}
		}
	}

	/**
	 * Gets a JComboBox with a list of spurs that use this schedule.
	 * 
	 * @param schedule
	 *            The schedule for this JComboBox.
	 * @return JComboBox with a list of spurs using schedule.
	 */
	public JComboBox getSpursByScheduleComboBox(Schedule schedule) {
		JComboBox box = new JComboBox();
		// search all spurs for that use schedule
		LocationManager manager = LocationManager.instance();
		List<Location> locations = manager.getLocationsByNameList();
		for (int j = 0; j < locations.size(); j++) {
			Location location = locations.get(j);
			List<Track> spurs = location.getTrackByNameList(Track.SPUR);
			for (Track spur : spurs) {
				if (spur.getScheduleId().equals(schedule.getId())) {
					LocationTrackPair ltp = new LocationTrackPair(location, spur);
					box.addItem(ltp);
				}
			}
		}
		return box;
	}

	public void load(Element root) {
		if (root.getChild(Xml.SCHEDULES) != null) {
			@SuppressWarnings("unchecked")
			List<Element> l = root.getChild(Xml.SCHEDULES).getChildren(Xml.SCHEDULE);
			if (log.isDebugEnabled())
				log.debug("readFile sees " + l.size() + " schedules");
			for (int i = 0; i < l.size(); i++) {
				register(new Schedule(l.get(i)));
			}
		}
	}

	public void store(Element root) {
		Element values;
		root.addContent(values = new Element(Xml.SCHEDULES));
		// add entries
		List<Schedule> scheduleList = getSchedulesByIdList();
		for (int i = 0; i < scheduleList.size(); i++) {
			values.addContent(scheduleList.get(i).store());
		}
	}

	/**
	 * Check for car type and road name changes.
	 * 
	 */
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug("ScheduleManager sees property change: " + e.getPropertyName() + " old: " + e.getOldValue()
				+ " new " + e.getNewValue());	// NOI18N
		if (e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY)) {
			replaceType((String) e.getOldValue(), (String) e.getNewValue());
		}
		if (e.getPropertyName().equals(CarRoads.CARROADS_NAME_CHANGED_PROPERTY)) {
			replaceRoad((String) e.getOldValue(), (String) e.getNewValue());
		}
	}

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
		// set dirty
		LocationManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static Logger log = LoggerFactory.getLogger(ScheduleManager.class.getName());

}

/* @(#)ScheduleManager.java */
