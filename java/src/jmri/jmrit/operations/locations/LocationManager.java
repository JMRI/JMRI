// LocationManager.java

package jmri.jmrit.operations.locations;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;

/**
 * Manages locations.
 * 
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class LocationManager implements java.beans.PropertyChangeListener {
	public static final String LISTLENGTH_CHANGED_PROPERTY = "locationsListLength"; // NOI18N

	public LocationManager() {
		CarTypes.instance().addPropertyChangeListener(this);
		CarRoads.instance().addPropertyChangeListener(this);
		EngineTypes.instance().addPropertyChangeListener(this);
	}

	/** record the single instance **/
	private static LocationManager _instance = null;
	private int _id = 0;

	public static synchronized LocationManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("LocationManager creating instance");
			// create and load
			_instance = new LocationManager();
			OperationsSetupXml.instance(); // load setup
			LocationManagerXml.instance(); // load locations
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("LocationManager returns instance " + _instance);
		return _instance;
	}

	public void dispose() {
		_locationHashTable.clear();
		_id = 0;
	}

	protected Hashtable<String, Location> _locationHashTable = new Hashtable<String, Location>(); // stores known
																									// Location
																									// instances by id

	/**
	 * @return requested Location object or null if none exists
	 */

	public Location getLocationByName(String name) {
		Location l;
		Enumeration<Location> en = _locationHashTable.elements();
		for (int i = 0; i < _locationHashTable.size(); i++) {
			l = en.nextElement();
			if (l.getName().equals(name))
				return l;
		}
		return null;
	}

	public Location getLocationById(String id) {
		return _locationHashTable.get(id);
	}

	/**
	 * Finds an existing location or creates a new location if needed requires location's name creates a unique id for
	 * this location
	 * 
	 * @param name
	 * 
	 * @return new location or existing location
	 */
	public Location newLocation(String name) {
		Location location = getLocationByName(name);
		if (location == null) {
			_id++;
			location = new Location(Integer.toString(_id), name);
			Integer oldSize = Integer.valueOf(_locationHashTable.size());
			_locationHashTable.put(location.getId(), location);
			firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
					Integer.valueOf(_locationHashTable.size()));
		}
		return location;
	}

	/**
	 * Remember a NamedBean Object created outside the manager.
	 */
	public void register(Location location) {
		Integer oldSize = Integer.valueOf(_locationHashTable.size());
		_locationHashTable.put(location.getId(), location);
		// find last id created
		int id = Integer.parseInt(location.getId());
		if (id > _id)
			_id = id;
		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
				Integer.valueOf(_locationHashTable.size()));
	}

	/**
	 * Forget a NamedBean Object created outside the manager.
	 */
	public void deregister(Location location) {
		if (location == null)
			return;
		location.dispose();
		Integer oldSize = Integer.valueOf(_locationHashTable.size());
		_locationHashTable.remove(location.getId());
		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
				Integer.valueOf(_locationHashTable.size()));
	}

	/**
	 * Sort by location name
	 * 
	 * @return list of location ids ordered by name
	 */
	public List<String> getLocationsByNameList() {
		// first get id list
		List<String> sortList = getList();
		// now re-sort
		List<String> out = new ArrayList<String>();
		String locName = "";
		boolean locAdded = false;
		Location l;

		for (int i = 0; i < sortList.size(); i++) {
			locAdded = false;
			l = getLocationById(sortList.get(i));
			locName = l.getName();
			for (int j = 0; j < out.size(); j++) {
				l = getLocationById(out.get(j));
				String outLocName = l.getName();
				if (locName.compareToIgnoreCase(outLocName) < 0) {
					out.add(j, sortList.get(i));
					locAdded = true;
					break;
				}
			}
			if (!locAdded) {
				out.add(sortList.get(i));
			}
		}
		return out;

	}

	/**
	 * Sort by location number, number can alpha numeric
	 * 
	 * @return list of location ids ordered by number
	 */
	public List<String> getLocationsByIdList() {
		// first get id list
		List<String> sortList = getList();
		// now re-sort
		List<String> out = new ArrayList<String>();
		int locationNumber = 0;
		boolean locationAdded = false;
		Location l;

		for (int i = 0; i < sortList.size(); i++) {
			locationAdded = false;
			l = getLocationById(sortList.get(i));
			try {
				locationNumber = Integer.parseInt(l.getId());
			} catch (NumberFormatException e) {
				log.debug("location id number isn't a number");
			}
			for (int j = 0; j < out.size(); j++) {
				l = getLocationById(out.get(j));
				try {
					int outLocationNumber = Integer.parseInt(l.getId());
					if (locationNumber < outLocationNumber) {
						out.add(j, sortList.get(i));
						locationAdded = true;
						break;
					}
				} catch (NumberFormatException e) {
					log.debug("list out id number isn't a number");
				}
			}
			if (!locationAdded) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}

	private List<String> getList() {
		List<String> out = new ArrayList<String>();
		Enumeration<String> en = _locationHashTable.keys();
		String[] arr = new String[_locationHashTable.size()];
		int i = 0;
		while (en.hasMoreElements()) {
			arr[i] = en.nextElement();
			i++;
		}
		jmri.util.StringUtil.sort(arr);
		for (i = 0; i < arr.length; i++)
			out.add(arr[i]);
		return out;
	}

	/**
	 * Returns all tracks of type
	 * 
	 * @param type
	 *            Siding, Yard, Interchange, Staging, or null (returns all track types)
	 * @return List of tracks ordered by use
	 */
	public List<Track> getTracks(String type) {
		List<String> sortList = getList();
		List<Track> trackList = new ArrayList<Track>();
		Location l;
		for (int i = 0; i < sortList.size(); i++) {
			l = getLocationById(sortList.get(i));
			List<String> tracks = l.getTrackIdsByNameList(type);
			for (int j = 0; j < tracks.size(); j++) {
				Track track = l.getTrackById(tracks.get(j));
				trackList.add(track);
			}
		}
		// now re-sort
		List<Track> moveList = new ArrayList<Track>();
		boolean locAdded = false;
		Track track;
		Track trackOut;
		for (int i = 0; i < trackList.size(); i++) {
			locAdded = false;
			track = trackList.get(i);
			for (int j = 0; j < moveList.size(); j++) {
				trackOut = moveList.get(j);
				if (track.getMoves() < trackOut.getMoves()) {
					moveList.add(j, track);
					locAdded = true;
					break;
				}
			}
			if (!locAdded) {
				moveList.add(track);
			}
		}
		return moveList;
	}

	public JComboBox getComboBox() {
		JComboBox box = new JComboBox();
		box.addItem("");
		List<String> locs = getLocationsByNameList();
		for (int i = 0; i < locs.size(); i++) {
			String locId = locs.get(i);
			Location l = getLocationById(locId);
			box.addItem(l);
		}
		return box;
	}

	public void updateComboBox(JComboBox box) {
		box.removeAllItems();
		box.addItem("");
		List<String> locs = getLocationsByNameList();
		for (int i = 0; i < locs.size(); i++) {
			String locId = locs.get(i);
			Location l = getLocationById(locId);
			box.addItem(l);
		}
	}

	public void replaceType(String oldType, String newType) {
		List<String> locs = getLocationsByIdList();
		for (int i = 0; i < locs.size(); i++) {
			Location loc = getLocationById(locs.get(i));
			if (loc.acceptsTypeName(oldType)) {
				loc.addTypeName(newType);
				// now adjust tracks
				List<String> tracks = loc.getTrackIdsByNameList(null);
				for (int j = 0; j < tracks.size(); j++) {
					Track track = loc.getTrackById(tracks.get(j));
					if (track.acceptsTypeName(oldType)) {
						track.deleteTypeName(oldType);
						track.addTypeName(newType);
					}
				}
				loc.deleteTypeName(oldType);
			}
		}
	}

	public void replaceRoad(String oldRoad, String newRoad) {
		List<String> locs = getLocationsByIdList();
		for (int i = 0; i < locs.size(); i++) {
			Location loc = getLocationById(locs.get(i));
			// now adjust any track locations
			List<String> tracks = loc.getTrackIdsByNameList(null);
			for (int j = 0; j < tracks.size(); j++) {
				Track track = loc.getTrackById(tracks.get(j));
				if (track.containsRoadName(oldRoad)) {
					track.deleteRoadName(oldRoad);
					if (newRoad != null)
						track.addRoadName(newRoad);
				}
			}
		}
	}

	public void replaceLoad(String oldLoadName, String newLoadName) {
		List<String> locs = getLocationsByIdList();
		for (int i = 0; i < locs.size(); i++) {
			Location loc = getLocationById(locs.get(i));
			// now adjust tracks
			List<String> tracks = loc.getTrackIdsByNameList(null);
			for (int j = 0; j < tracks.size(); j++) {
				Track track = loc.getTrackById(tracks.get(j));
				String[] loadNames = track.getLoadNames();
				for (int k = 0; k < loadNames.length; k++) {
					if (loadNames[k].equals(oldLoadName)) {
						track.deleteLoadName(oldLoadName);
						if (newLoadName != null)
							track.addLoadName(newLoadName);
					}
				}
			}
		}
	}

	/**
	 * Check for car type and road name replacements. Also check for engine type replacement.
	 * 
	 */
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug("LocationManager sees property change: " + e.getPropertyName() + " old: "
				+ e.getOldValue() + " new: " + e.getNewValue());	// NOI18N
		if (e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(EngineTypes.ENGINETYPES_NAME_CHANGED_PROPERTY)) {
			replaceType((String) e.getOldValue(), (String) e.getNewValue());
		}
		if (e.getPropertyName().equals(CarRoads.CARROADS_NAME_CHANGED_PROPERTY)) {
			replaceRoad((String) e.getOldValue(), (String) e.getNewValue());
		}
	}

	/**
	 * @return Number of locations
	 */
	public int numEntries() {
		return _locationHashTable.size();
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
		pcs.firePropertyChange(p, old, n);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocationManager.class
			.getName());

}

/* @(#)LocationManager.java */
