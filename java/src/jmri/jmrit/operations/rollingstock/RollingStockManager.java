// RollingStockManager.java

package jmri.jmrit.operations.rollingstock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;

import jmri.jmrit.operations.trains.Train;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Base class for rolling stock managers car and engine.
 * 
 * @author Daniel Boudreau Copyright (C) 2010, 2011
 * @version $Revision$
 */
public class RollingStockManager {

	protected Hashtable<String, RollingStock> _hashTable = new Hashtable<String, RollingStock>(); // RollingStock
																									// by
																									// id

	public static final String LISTLENGTH_CHANGED_PROPERTY = "RollingStockListLength"; // NOI18N

	public RollingStockManager() {
	}

	/**
	 * Get the number of items in the roster
	 * 
	 * @return Number of rolling stock in the Roster
	 */
	public int getNumEntries() {
		return _hashTable.size();
	}

	public void dispose() {
		deleteAll();
	}

	/**
	 * Get rolling stock by id
	 * 
	 * @return requested RollingStock object or null if none exists
	 */
	public RollingStock getById(String id) {
		return _hashTable.get(id);
	}

	/**
	 * Get rolling stock by road and number
	 * 
	 * @param road
	 *            RollingStock road
	 * @param number
	 *            RollingStock number
	 * @return requested RollingStock object or null if none exists
	 */
	public RollingStock getByRoadAndNumber(String road, String number) {
		String id = RollingStock.createId(road, number);
		return getById(id);
	}

	/**
	 * Get a rolling stock by type and road. Used to test that rolling stock
	 * with a specific type and road exists.
	 * 
	 * @param type
	 *            RollingStock type.
	 * @param road
	 *            RollingStock road.
	 * @return the first RollingStock found with the specified type and road.
	 */
	public RollingStock getByTypeAndRoad(String type, String road) {
		Enumeration<String> en = _hashTable.keys();
		while (en.hasMoreElements()) {
			RollingStock rs = getById(en.nextElement());
			if (rs.getTypeName().equals(type) && rs.getRoadName().equals(road))
				return rs;
		}
		return null;
	}

	/**
	 * Get a rolling stock by Radio Frequency Identification (RFID)
	 * 
	 * @param rfid
	 *            RollingStock's RFID.
	 * @return the RollingStock with the specific RFID, or null if not found
	 */
	public RollingStock getByRfid(String rfid) {
		Enumeration<String> en = _hashTable.keys();
		while (en.hasMoreElements()) {
			RollingStock rs = getById(en.nextElement());
			if (rs.getRfid().equals(rfid))
				return rs;
		}
		return null;
	}

	/**
	 * Load RollingStock.
	 */
	public void register(RollingStock rs) {
		Integer oldSize = Integer.valueOf(_hashTable.size());
		_hashTable.put(rs.getId(), rs);
		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_hashTable.size()));
	}

	/**
	 * Unload RollingStock.
	 */
	public void deregister(RollingStock rs) {
		rs.dispose();
		Integer oldSize = Integer.valueOf(_hashTable.size());
		_hashTable.remove(rs.getId());
		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_hashTable.size()));
	}

	public void changeId(RollingStock rs, String road, String number) {
		_hashTable.remove(rs.getId());
		rs._id = RollingStock.createId(road, number);
		register(rs);
	}

	/**
	 * Remove all RollingStock from roster
	 */
	public void deleteAll() {
		Integer oldSize = Integer.valueOf(_hashTable.size());
		Enumeration<String> en = _hashTable.keys();
		while (en.hasMoreElements()) {
			RollingStock rs = getById(en.nextElement());
			rs.dispose();
			_hashTable.remove(rs.getId());
		}
		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_hashTable.size()));
	}

	public void resetMoves() {
		Enumeration<String> en = _hashTable.keys();
		while (en.hasMoreElements()) {
			RollingStock rs = getById(en.nextElement());
			rs.setMoves(0);
		}
	}

	/**
	 * Returns a list (no order) of RollingStock ids.
	 * 
	 * @return list of RollingStock
	 */
	public List<String> getList() {
		Enumeration<String> en = _hashTable.keys();
		List<String> out = new ArrayList<String>();
		while (en.hasMoreElements()) {
			out.add(en.nextElement());
		}
		return out;
	}

	/**
	 * Sort by rolling stock id
	 * 
	 * @return list of RollingStock ids ordered by id
	 */
	public List<String> getByIdList() {
		Enumeration<String> en = _hashTable.keys();
		String[] arr = new String[_hashTable.size()];
		List<String> out = new ArrayList<String>();
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
	 * Sort by rolling stock road name
	 * 
	 * @return list of RollingStock ids ordered by road name
	 */
	public List<String> getByRoadNameList() {
		return getByList(getByIdList(), BY_ROAD);
	}

	/**
	 * Sort by rolling stock number, number can alpha numeric
	 * 
	 * @return list of RollingStock ids ordered by number
	 */
	public List<String> getByNumberList() {
		// log.debug("start rolling stock sort by number list");
		// first get by road list
		List<String> sortIn = getByRoadNameList();
		// now re-sort
		List<String> out = new ArrayList<String>();
		int rsNumber = 0;
		int outRsNumber = 0;
		int notInteger = -999999999; // flag when rolling stock number isn't an
										// integer
		String[] number;
		boolean rsAdded = false;

		for (int i = 0; i < sortIn.size(); i++) {
			rsAdded = false;
			try {
				rsNumber = Integer.parseInt(getById(sortIn.get(i)).getNumber());
				getById(sortIn.get(i)).number = rsNumber;
			} catch (NumberFormatException e) {
				// maybe rolling stock number in the format xxx-y
				try {
					number = getById(sortIn.get(i)).getNumber().split("-");
					rsNumber = Integer.parseInt(number[0]);
					getById(sortIn.get(i)).number = rsNumber;
				} catch (NumberFormatException e2) {
					getById(sortIn.get(i)).number = notInteger;
					// sort alpha numeric numbers at the end of the out list
					String numberIn = getById(sortIn.get(i)).getNumber();
					// log.debug("rolling stock in road number ("+numberIn+") isn't a number");
					for (int k = (out.size() - 1); k >= 0; k--) {
						String numberOut = getById(out.get(k)).getNumber();
						try {
							Integer.parseInt(numberOut);
							// done, place rolling stock with alpha numeric
							// number after
							// rolling stocks with real numbers.
							out.add(k + 1, sortIn.get(i));
							rsAdded = true;
							break;
						} catch (NumberFormatException e3) {
							if (numberIn.compareToIgnoreCase(numberOut) >= 0) {
								out.add(k + 1, sortIn.get(i));
								rsAdded = true;
								break;
							}
						}
					}
					if (!rsAdded)
						out.add(0, sortIn.get(i));
					continue;
				}
			}

			int start = 0;
			// page to improve sort performance.
			int divisor = out.size() / pageSize;
			for (int k = divisor; k > 0; k--) {
				outRsNumber = getById(out.get((out.size() - 1) * k / divisor)).number;
				if (outRsNumber == notInteger)
					continue;
				if (rsNumber >= outRsNumber) {
					start = (out.size() - 1) * k / divisor;
					break;
				}
			}
			for (int j = start; j < out.size(); j++) {
				outRsNumber = getById(out.get(j)).number;
				if (outRsNumber == notInteger) {
					try {
						outRsNumber = Integer.parseInt(getById(out.get(j)).getNumber());
					} catch (NumberFormatException e) {
						try {
							number = getById(out.get(j)).getNumber().split("-");
							outRsNumber = Integer.parseInt(number[0]);
						} catch (NumberFormatException e2) {
							// RollingStock rs = getById(out.get(j));
							// log.debug("RollingStock ("+rs.getId()+") road number ("+rs.getNumber()+") isn't a number");
							// force add
							outRsNumber = rsNumber + 1;
						}
					}
				}
				if (rsNumber < outRsNumber) {
					out.add(j, sortIn.get(i));
					rsAdded = true;
					break;
				}
			}
			if (!rsAdded) {
				out.add(sortIn.get(i));
			}
		}
		// log.debug("end rolling stock sort by number list");
		return out;
	}

	/**
	 * Sort by rolling stock type names
	 * 
	 * @return list of RollingStock ids ordered by RollingStock type
	 */
	public List<String> getByTypeList() {
		return getByList(getByRoadNameList(), BY_TYPE);
	}

	/**
	 * Return rolling stock ids of a specific type
	 * 
	 * @param type
	 *            type of rolling stock
	 * @return list of RollingStock ids that are specific type
	 */
	public List<String> getByTypeList(String type) {
		List<String> l = getByTypeList();
		List<String> out = new ArrayList<String>();
		for (int i = 0; i < l.size(); i++) {
			RollingStock rs = getById(l.get(i));
			if (rs.getTypeName().equals(type))
				out.add(l.get(i));
		}
		return out;
	}

	/**
	 * Sort by rolling stock color names
	 * 
	 * @return list of RollingStock ids ordered by RollingStock color
	 */
	public List<String> getByColorList() {
		return getByList(getByTypeList(), BY_COLOR);
	}

	/**
	 * Sort by rolling stock location
	 * 
	 * @return list of RollingStock ids ordered by RollingStock location
	 */
	public List<String> getByLocationList() {
		return getByList(getByNumberList(), BY_LOCATION);
	}

	/**
	 * Sort by rolling stock destination
	 * 
	 * @return list of RollingStock ids ordered by RollingStock destination
	 */
	public List<String> getByDestinationList() {
		return getByList(getByLocationList(), BY_DESTINATION);
	}

	/**
	 * Sort by rolling stocks in trains
	 * 
	 * @return list of RollingStock ids ordered by trains
	 */
	public List<String> getByTrainList() {
		List<String> byDest = getByList(getByIdList(), BY_DESTINATION);
		List<String> byLoc = getByList(byDest, BY_LOCATION);
		return getByList(byLoc, BY_TRAIN);
	}

	/**
	 * Sort by rolling stock moves
	 * 
	 * @return list of RollingStock ids ordered by RollingStock moves
	 */
	public List<String> getByMovesList() {
		return getByIntList(getList(), BY_MOVES);
	}

	/**
	 * Sort by when rolling stock was built
	 * 
	 * @return list of RollingStock ids ordered by RollingStock built date
	 */
	public List<String> getByBuiltList() {
		return getByList(getByIdList(), BY_BUILT);
	}

	/**
	 * Sort by rolling stock owner
	 * 
	 * @return list of RollingStock ids ordered by RollingStock owner
	 */
	public List<String> getByOwnerList() {
		return getByList(getByIdList(), BY_OWNER);
	}

	/**
	 * Sort by rolling stock value
	 * 
	 * @return list of RollingStock ids ordered by value
	 */
	public List<String> getByValueList() {
		return getByList(getByIdList(), BY_VALUE);
	}

	/**
	 * Sort by rolling stock RFID
	 * 
	 * @return list of RollingStock ids ordered by RFIDs
	 */
	public List<String> getByRfidList() {
		return getByList(getByIdList(), BY_RFID);
	}

	/**
	 * Sort by rolling stock last date used
	 * 
	 * @return list of RollingStock ids ordered by last date
	 */
	public List<String> getByLastDateList() {
		return getByList(getByIdList(), BY_LAST);
	}

	private static final int pageSize = 64;

	protected List<String> getByList(List<String> sortIn, int attribute) {
		List<String> out = new ArrayList<String>();
		String rsIn;
		for (int i = 0; i < sortIn.size(); i++) {
			boolean rsAdded = false;
			rsIn = (String) getRsAttribute(getById(sortIn.get(i)), attribute);
			int start = 0;
			// page to improve performance. Most have id = road+number
			int divisor = out.size() / pageSize;
			for (int k = divisor; k > 0; k--) {
				String rsOut = (String) getRsAttribute(getById(out.get((out.size() - 1) * k / divisor)), attribute);
				if (rsIn.compareToIgnoreCase(rsOut) >= 0) {
					start = (out.size() - 1) * k / divisor;
					break;
				}
			}
			for (int j = start; j < out.size(); j++) {
				String rsOut = (String) getRsAttribute(getById(out.get(j)), attribute);
				if (rsIn.compareToIgnoreCase(rsOut) < 0) {
					out.add(j, sortIn.get(i));
					rsAdded = true;
					break;
				}
			}
			if (!rsAdded) {
				out.add(sortIn.get(i));
			}
		}
		return out;
	}

	protected List<String> getByIntList(List<String> sortIn, int attribute) {
		List<String> out = new ArrayList<String>();
		int rsIn;
		for (int i = 0; i < sortIn.size(); i++) {
			boolean rsAdded = false;
			rsIn = (Integer) getRsAttribute(getById(sortIn.get(i)), attribute);
			int start = 0;
			// page to improve performance. Most have id = road+number
			int divisor = out.size() / pageSize;
			for (int k = divisor; k > 0; k--) {
				int rsOut = (Integer) getRsAttribute(getById(out.get((out.size() - 1) * k / divisor)), attribute);
				if (rsIn >= rsOut) {
					start = (out.size() - 1) * k / divisor;
					break;
				}
			}
			for (int j = start; j < out.size(); j++) {
				int rsOut = (Integer) getRsAttribute(getById(out.get(j)), attribute);
				if (rsIn < rsOut) {
					out.add(j, sortIn.get(i));
					rsAdded = true;
					break;
				}
			}
			if (!rsAdded) {
				out.add(sortIn.get(i));
			}
		}
		return out;
	}

	// The various sort options for RollingStock
	// see CarManager and EngineManger for other values
	protected static final int BY_NUMBER = 0;
	protected static final int BY_ROAD = 1;
	protected static final int BY_TYPE = 2;
	protected static final int BY_COLOR = 3;
	// BY_LOAD = 4 BY_MODEL = 4
	// BY_KERNEL = 5 BY_CONSIST = 5
	protected static final int BY_LOCATION = 6;
	protected static final int BY_DESTINATION = 7;
	protected static final int BY_TRAIN = 8;
	protected static final int BY_MOVES = 9;
	protected static final int BY_BUILT = 10;
	protected static final int BY_OWNER = 11;
	protected static final int BY_RFID = 12;
	// BY_RWE = 13
	// BY_FINAL_DEST = 14
	protected static final int BY_VALUE = 15;
	// BY_WAIT = 16
	protected static final int BY_LAST = 17;

	protected Object getRsAttribute(RollingStock rs, int attribute) {
		switch (attribute) {
		case BY_NUMBER:
			return rs.getNumber();
		case BY_ROAD:
			return rs.getRoadName();
		case BY_TYPE:
			return rs.getTypeName();
		case BY_COLOR:
			return rs.getColor();
		case BY_LOCATION:
			return rs.getStatus() + rs.getLocationName() + rs.getTrackName();
		case BY_DESTINATION:
			return rs.getDestinationName() + rs.getDestinationTrackName();
		case BY_TRAIN:
			return rs.getTrainName();
		case BY_MOVES:
			return rs.getMoves(); // returns an integer
		case BY_BUILT:
			return convertBuildDate(rs.getBuilt());
		case BY_OWNER:
			return rs.getOwner();
		case BY_RFID:
			return rs.getRfid();
		case BY_VALUE:
			return rs.getValue();
		case BY_LAST:
			return convertLastDate(rs.getLastDate());
		default:
			return "unknown"; // NOI18N
		}
	}

	private String convertBuildDate(String date) {
		String[] built = date.split("-");
		if (built.length > 1)
			try {
				int d = Integer.parseInt(built[1]);
				if (d < 100)
					d = d + 1900;
				return Integer.toString(d);
			} catch (NumberFormatException e2) {
				log.debug("Unable to parse car built date " + date);
			}
		return date;
	}

	/**
	 * The input format is month/day/year time. The problem is month and day can
	 * be a single character, and the order is all wrong so sorting doesn't work
	 * well. This converts the format to yyyy/mm/dd time for proper sorting.
	 * 
	 * @param date
	 * @return
	 */
	private String convertLastDate(String date) {
		String[] newDate = date.split("/");
		if (newDate.length < 3)
			return date;
		String month = newDate[0];
		String day = newDate[1];
		if (newDate[0].length() == 1)
			month = "0" + month;
		if (newDate[1].length() == 1)
			day = "0" + day;
		String[] yearTime = newDate[2].split(" ");
		String year = yearTime[0];
		String time = yearTime[1];
		return month = year + "/" + month + "/" + day + " " + time;
	}

	/**
	 * Return a list available rolling stock (no assigned train or rolling stock
	 * already assigned to this train) on a route, RollingStock is ordered least
	 * recently moved to most recently moved.
	 * 
	 * @param train
	 * @return List of RollingStock ids with no assigned train on a route
	 */
	public List<String> getAvailableTrainList(Train train) {
		List<String> out = new ArrayList<String>();
		Route route = train.getRoute();
		if (route == null)
			return out;
		// get a list of locations served by this route
		List<String> routeList = route.getLocationsBySequenceList();
		// don't include RollingStock at route destination
		RouteLocation destination = null;
		if (routeList.size() > 1) {
			destination = route.getLocationById(routeList.get(routeList.size() - 1));
			// However, if the destination is visited more than once, must
			// include all cars
			RouteLocation test;
			for (int i = 0; i < routeList.size() - 1; i++) {
				test = route.getLocationById(routeList.get(i));
				if (destination.getName().equals(test.getName())) {
					destination = null;
					break;
				}
			}
		}
		// get rolling stock by moves list
		List<String> sortByMoves = getByMovesList();
		List<String> sortByPriority = sortByPriority(sortByMoves);
		// now build list of available RollingStock for this route
		RollingStock rs;
		for (int i = 0; i < sortByPriority.size(); i++) {
			rs = getById(sortByPriority.get(i));
			// only use RollingStock with a location
			if (rs.getLocationName().equals(""))
				continue;
			RouteLocation rl = route.getLastLocationByName(rs.getLocationName());
			// get RollingStock that don't have an assigned train, or the
			// assigned train is this one
			if (rl != null && rl != destination && (rs.getTrain() == null || train.equals(rs.getTrain()))) {
				out.add(sortByPriority.get(i));
			}
		}
		return out;
	}

	// sorts the high priority cars to the start of the list
	private List<String> sortByPriority(List<String> list) {
		List<String> out = new ArrayList<String>();
		RollingStock rs;
		// move high priority ids to the start
		for (int i = 0; i < list.size(); i++) {
			rs = getById(list.get(i));
			if (rs.getLoadPriority().equals(CarLoad.PRIORITY_HIGH)) {
				out.add(list.get(i));
				list.remove(i);
				i--;
			}
		}
		// now load all of the remaining low priority ids
		for (int i = 0; i < list.size(); i++) {
			out.add(list.get(i));
		}
		return out;
	}

	/**
	 * Get a list of rolling stocks assigned to a train
	 * 
	 * @param train
	 * @return List of RollingStock ids assigned to the train
	 */
	public List<String> getByTrainList(Train train) {
		List<String> byLoc = getByLocationList();
		List<String> inTrain = new ArrayList<String>();
		RollingStock rs;

		for (int i = 0; i < byLoc.size(); i++) {
			rs = getById(byLoc.get(i));
			// get only rolling stock that is assigned to this train
			if (rs.getTrain() == train)
				inTrain.add(byLoc.get(i));
		}
		return inTrain;
	}

	// Common sort routine
	protected List<String> sortList(List<String> list) {
		List<String> out = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			int j;
			for (j = 0; j < out.size(); j++) {
				if (list.get(i).compareToIgnoreCase(out.get(j)) < 0)
					break;
			}
			out.add(j, list.get(i));
		}
		return out;
	}

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(String p, Object old, Object n) {
		pcs.firePropertyChange(p, old, n);
	}

	static Logger log = LoggerFactory.getLogger(RollingStockManager.class.getName());

}
