// RollingStockManager.java
package jmri.jmrit.operations.rollingstock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.trains.Train;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for rolling stock managers car and engine.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2011
 * @version $Revision$
 */
public class RollingStockManager {

    public static final String NONE = "";

    // RollingStock
    protected Hashtable<String, RollingStock> _hashTable = new Hashtable<String, RollingStock>();

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
     * @param road RollingStock road
     * @param number RollingStock number
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
     * @param type RollingStock type.
     * @param road RollingStock road.
     * @return the first RollingStock found with the specified type and road.
     */
    public RollingStock getByTypeAndRoad(String type, String road) {
        Enumeration<String> en = _hashTable.keys();
        while (en.hasMoreElements()) {
            RollingStock rs = getById(en.nextElement());
            if (rs.getTypeName().equals(type) && rs.getRoadName().equals(road)) {
                return rs;
            }
        }
        return null;
    }

    /**
     * Get a rolling stock by Radio Frequency Identification (RFID)
     *
     * @param rfid RollingStock's RFID.
     * @return the RollingStock with the specific RFID, or null if not found
     */
    public RollingStock getByRfid(String rfid) {
        Enumeration<String> en = _hashTable.keys();
        while (en.hasMoreElements()) {
            RollingStock rs = getById(en.nextElement());
            if (rs.getRfid().equals(rfid)) {
                return rs;
            }
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
     * Returns a list (no order) of RollingStock.
     *
     * @return list of RollingStock
     */
    public List<RollingStock> getList() {
        Enumeration<RollingStock> en = _hashTable.elements();
        List<RollingStock> out = new ArrayList<RollingStock>();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        return out;
    }

    /**
     * Sort by rolling stock id
     *
     * @return list of RollingStock ordered by id
     */
    public List<RollingStock> getByIdList() {
        Enumeration<String> en = _hashTable.keys();
        String[] arr = new String[_hashTable.size()];
        List<RollingStock> out = new ArrayList<RollingStock>();
        int i = 0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i = 0; i < arr.length; i++) {
            out.add(getById(arr[i]));
        }
        return out;
    }

    /**
     * Sort by rolling stock road name
     *
     * @return list of RollingStock ordered by road name
     */
    public List<RollingStock> getByRoadNameList() {
        return getByList(getByIdList(), BY_ROAD);
    }

    /**
     * Sort by rolling stock number, number can be alphanumeric.
     * RollingStock number can also be in the format of nnnn-N, where
     * the "-N" allows the user to enter RollingStock with similar numbers.
     *
     * @return list of RollingStock ordered by number
     */
    public List<RollingStock> getByNumberList() {
        // first get by road list
        List<RollingStock> sortIn = getByRoadNameList();
        // now re-sort
        List<RollingStock> out = new ArrayList<RollingStock>();
        int rsNumber = 0;
        int outRsNumber = 0;
        int notInteger = -999999999; // flag when rolling stock number isn't an
        // integer
        String[] number;
        boolean rsAdded = false;

        for (RollingStock rs : sortIn) {
            rsAdded = false;
            try {
                rsNumber = Integer.parseInt(rs.getNumber());
                rs.number = rsNumber;
            } catch (NumberFormatException e) {
                // maybe rolling stock number in the format nnnn-N
                try {
                    number = rs.getNumber().split("-");
                    rsNumber = Integer.parseInt(number[0]);
                    rs.number = rsNumber;
                    // two possible exceptions, ArrayIndexOutOfBoundsException on split, and NumberFormatException on
                    // parseInt
                } catch (Exception e2) {
                    rs.number = notInteger;
                    // sort alphanumeric numbers at the end of the out list
                    String numberIn = rs.getNumber();
                    // log.debug("rolling stock in road number ("+numberIn+") isn't a number");
                    for (int k = (out.size() - 1); k >= 0; k--) {
                        String numberOut = out.get(k).getNumber();
                        try {
                            Integer.parseInt(numberOut);
                            // done, place rolling stock with alphanumeric
                            // number after rolling stocks with real numbers.
                            out.add(k + 1, rs);
                            rsAdded = true;
                            break;
                        } catch (NumberFormatException e3) {
                            if (numberIn.compareToIgnoreCase(numberOut) >= 0) {
                                out.add(k + 1, rs);
                                rsAdded = true;
                                break;
                            }
                        }
                    }
                    if (!rsAdded) {
                        out.add(0, rs);
                    }
                    continue;
                }
            }

            int start = 0;
            // page to improve sort performance.
            int divisor = out.size() / pageSize;
            for (int k = divisor; k > 0; k--) {
                outRsNumber = out.get((out.size() - 1) * k / divisor).number;
                if (outRsNumber == notInteger) {
                    continue;
                }
                if (rsNumber >= outRsNumber) {
                    start = (out.size() - 1) * k / divisor;
                    break;
                }
            }
            for (int j = start; j < out.size(); j++) {
                outRsNumber = out.get(j).number;
                if (outRsNumber == notInteger) {
                    try {
                        outRsNumber = Integer.parseInt(out.get(j).getNumber());
                    } catch (NumberFormatException e) {
                        try {
                            number = out.get(j).getNumber().split("-");
                            outRsNumber = Integer.parseInt(number[0]);
                        } catch (Exception e2) {
                            // force add
                            outRsNumber = rsNumber + 1;
                        }
                    }
                }
                if (rsNumber < outRsNumber) {
                    out.add(j, rs);
                    rsAdded = true;
                    break;
                }
            }
            if (!rsAdded) {
                out.add(rs);
            }
        }
        // log.debug("end rolling stock sort by number list");
        return out;
    }

    /**
     * Sort by rolling stock type names
     *
     * @return list of RollingStock ordered by RollingStock type
     */
    public List<RollingStock> getByTypeList() {
        return getByList(getByRoadNameList(), BY_TYPE);
    }

    /**
     * Return rolling stock of a specific type
     *
     * @param type type of rolling stock
     * @return list of RollingStock that are specific type
     */
    public List<RollingStock> getByTypeList(String type) {
        List<RollingStock> typeList = getByTypeList();
        List<RollingStock> out = new ArrayList<RollingStock>();
        for (RollingStock rs : typeList) {
            if (rs.getTypeName().equals(type)) {
                out.add(rs);
            }
        }
        return out;
    }

    /**
     * Sort by rolling stock color names
     *
     * @return list of RollingStock ordered by RollingStock color
     */
    public List<RollingStock> getByColorList() {
        return getByList(getByTypeList(), BY_COLOR);
    }

    /**
     * Sort by rolling stock location
     *
     * @return list of RollingStock ordered by RollingStock location
     */
    public List<RollingStock> getByLocationList() {
        return getByList(getList(), BY_LOCATION);
    }

    /**
     * Sort by rolling stock destination
     *
     * @return list of RollingStock ordered by RollingStock destination
     */
    public List<RollingStock> getByDestinationList() {
        return getByList(getByLocationList(), BY_DESTINATION);
    }

    /**
     * Sort by rolling stocks in trains
     *
     * @return list of RollingStock ordered by trains
     */
    public List<RollingStock> getByTrainList() {
        List<RollingStock> byDest = getByList(getByIdList(), BY_DESTINATION);
        List<RollingStock> byLoc = getByList(byDest, BY_LOCATION);
        return getByList(byLoc, BY_TRAIN);
    }

    /**
     * Sort by rolling stock moves
     *
     * @return list of RollingStock ordered by RollingStock moves
     */
    public List<RollingStock> getByMovesList() {
        return getByList(getList(), BY_MOVES);
    }

    /**
     * Sort by when rolling stock was built
     *
     * @return list of RollingStock ordered by RollingStock built date
     */
    public List<RollingStock> getByBuiltList() {
        return getByList(getByIdList(), BY_BUILT);
    }

    /**
     * Sort by rolling stock owner
     *
     * @return list of RollingStock ordered by RollingStock owner
     */
    public List<RollingStock> getByOwnerList() {
        return getByList(getByIdList(), BY_OWNER);
    }

    /**
     * Sort by rolling stock value
     *
     * @return list of RollingStock ordered by value
     */
    public List<RollingStock> getByValueList() {
        return getByList(getByIdList(), BY_VALUE);
    }

    /**
     * Sort by rolling stock RFID
     *
     * @return list of RollingStock ordered by RFIDs
     */
    public List<RollingStock> getByRfidList() {
        return getByList(getByIdList(), BY_RFID);
    }

    /**
     * Get a list of all rolling stock sorted last date used
     *
     * @return list of RollingStock ordered by last date
     */
    public List<RollingStock> getByLastDateList() {
        return getByList(getByIdList(), BY_LAST);
    }

    /**
     * Sort a specific list of rolling stock last date used
     * 
     * @param inList list of rolling stock to sort.
     * @return list of RollingStock ordered by last date
     */
    public List<RollingStock> getByLastDateList(List<RollingStock> inList) {
        return getByList(inList, BY_LAST);
    }

    private static final int pageSize = 64;

    protected List<RollingStock> getByList(List<RollingStock> sortIn, int attribute) {
        List<RollingStock> out = new ArrayList<RollingStock>();
        sortIn.forEach(n -> out.add(n));
        Collections.sort(out, getComparator(attribute));
        return out;
    }

    // The various sort options for RollingStock
    // see CarManager and EngineManger for other values
    protected static final int BY_NUMBER = 0;
    protected static final int BY_ROAD = 1;
    protected static final int BY_TYPE = 2;
    protected static final int BY_COLOR = 3;
    // BY_LOAD = 4 
    // BY_MODEL = 4
    // BY_KERNEL = 5 
    // BY_CONSIST = 5
    protected static final int BY_LOCATION = 6;
    protected static final int BY_DESTINATION = 7;
    protected static final int BY_TRAIN = 8;
    protected static final int BY_MOVES = 9;
    protected static final int BY_BUILT = 10;
    protected static final int BY_OWNER = 11;
    protected static final int BY_RFID = 12;
    // BY_RWE = 13
    // BY_HP = 13
    // BY_FINAL_DEST = 14
    protected static final int BY_VALUE = 15;
    // BY_WAIT = 16
    protected static final int BY_LAST = 17;
    protected static final int BY_BLOCKING = 18;
    // BY_PICKUP = 19
    // BY_B_UNIT = 20

    protected java.util.Comparator<RollingStock> getComparator(int attribute) {
        switch (attribute) {
            case BY_NUMBER:
                return (r1, r2) -> (r1.getNumber().compareToIgnoreCase(r2.getNumber()));
            case BY_ROAD:
                return (r1, r2) -> (r1.getRoadName().compareToIgnoreCase(r2.getRoadName()));
            case BY_TYPE:
                return (r1, r2) -> (r1.getTypeName().compareToIgnoreCase(r2.getTypeName()));
            case BY_COLOR:
                return (r1, r2) -> (r1.getColor().compareToIgnoreCase(r2.getColor()));
            case BY_LOCATION:
                return (r1, r2) -> (r1.getStatus() + r1.getLocationName() + r1.getTrackName()).compareToIgnoreCase(r2.getStatus() +
                        r2.getLocationName() +
                        r2.getTrackName());
            case BY_DESTINATION:
                return (r1, r2) -> (r1.getDestinationName() + r1.getDestinationTrackName()).compareToIgnoreCase(r2.getDestinationName() +
                        r2.getDestinationTrackName());
            case BY_TRAIN:
                return (r1, r2) -> (r1.getTrainName().compareToIgnoreCase(r2.getTrainName()));
            case BY_MOVES:
                return (r1, r2) -> (r1.getMoves() - r2.getMoves());
            case BY_BUILT:
                return (r1, r2) -> (convertBuildDate(r1.getBuilt()).compareToIgnoreCase(convertBuildDate(r2.getBuilt())));
            case BY_OWNER:
                return (r1, r2) -> (r1.getOwner().compareToIgnoreCase(r2.getOwner()));
            case BY_RFID:
                return (r1, r2) -> (r1.getRfid().compareToIgnoreCase(r2.getRfid()));
            case BY_VALUE:
                return (r1, r2) -> (r1.getValue().compareToIgnoreCase(r2.getValue()));
            case BY_LAST:
                return (r1, r2) -> (r1.getLastMoveDate().compareTo(r2.getLastMoveDate()));
            case BY_BLOCKING:
                return (r1, r2) -> (r1.getBlocking() - r2.getBlocking());
            default:
                return (r1, r2) -> ((r1.getRoadName() + r1.getNumber()).compareToIgnoreCase(r2.getRoadName() +
                        r2.getNumber()));
        }
    }

    private String convertBuildDate(String date) {
        String[] built = date.split("-");
        if (built.length == 2) {
            try {
                int d = Integer.parseInt(built[1]);
                if (d < 100) {
                    d = d + 1900;
                }
                return Integer.toString(d);
            } catch (NumberFormatException e) {
                log.debug("Unable to parse built date ({})", date);
            }
        } else {
            try {
                int d = Integer.parseInt(date);
                if (d < 100) {
                    d = d + 1900;
                }
                return Integer.toString(d);
            } catch (NumberFormatException e) {
                log.debug("Unable to parse built date ({})", date);
            }
        }
        return date;
    }

    /**
     * Get a list of rolling stocks assigned to a train ordered by location
     *
     * @param train
     * @return List of RollingStock assigned to the train ordered by location
     */
    public List<RollingStock> getByTrainList(Train train) {
        // List<RollingStock> shuffle = shuffle(getList(train));
        List<RollingStock> out = getByList(getList(train), BY_LOCATION);
        return out;
    }

    /**
     * Returns a list (no order) of RollingStock in a train.
     *
     * @return list of RollingStock
     */
    public List<RollingStock> getList(Train train) {
        Enumeration<RollingStock> en = _hashTable.elements();
        List<RollingStock> out = new ArrayList<RollingStock>();
        while (en.hasMoreElements()) {
            RollingStock rs = en.nextElement();
            if (rs.getTrain() == train) {
                out.add(rs);
            }
        }
        return out;
    }

    /**
     * Returns a list (no order) of RollingStock at a location.
     * 
     * @param location location to search for.
     * @return list of RollingStock
     */
    public List<RollingStock> getList(Location location) {
        List<RollingStock> out = new ArrayList<RollingStock>();
        _hashTable.forEach((key, rs) -> {
            if (rs.getLocation() == location)
                out.add(rs);
        });
        return out;
    }

    /**
     * Returns a list (no order) of RollingStock at a location.
     * 
     * @param track Track to search for.
     * @return list of RollingStock
     */
    public List<RollingStock> getList(Track track) {
        List<RollingStock> out = new ArrayList<RollingStock>();
        _hashTable.forEach((key, rs) -> {
            if (rs.getTrack() == track)
                out.add(rs);
        });
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
