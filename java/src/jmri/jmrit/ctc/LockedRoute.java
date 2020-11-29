package jmri.jmrit.ctc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;
import jmri.Sensor;

/**
 * Each of these objects describe a route consisting of all of the
 * occupancy sensors(s) that specify a route.  This is the "topology"
 * information needed to determine if a route is available or not.
 * <p>
 * For O.S. sections, this routine will get the reference to the
 * "_mOSSectionOccupiedExternalSensor" associated with that O.S. section.  For
 * the occupancy sensors, just a reference to those occupancy sensors.
 * <p>
 * It will register with the occupancy sensors for state change.  As
 * each occupancy sensor goes unoccupied (INACTIVE), it will subtract one from
 * a counter of outstanding reservations.  If the count becomes zero,
 * it will remove from it's list that resource, thereby freeing it up for any
 * other allocation in the future.
 * <p>
 * This object is NOT multi-thread safe (nor does it need to be).
 * I DO NOT (nor do I make any assumptions) about JMRI's threading as regards
 * this objects design.
 * It is "hardened" against multi-threading issues of ONLY this form:
 * It is possible that while we are (say) adding a new route by merging it
 * to an existing route, for an de-occupancy event to occur.  If we didn't
 * harden the object, then it is possible for an increment and decrement
 * to occur "in parallel" thus screwing up the count.
 * So whenever a merge or de-occupy event occurs, we lock down the object
 * "CountedSensor" only as long as needed to get to a "safe" state.
 * 
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019, 2020
 * 
*/

public class LockedRoute {
    
/**
 * This class implements the counted sensor concept.  When a CountedSensor is created,
 * the count is set to 1.  If another allocation occurs of the same sensor,
 * the count is increased by 1.  When a de-allocation occurs, the count is
 * decreased by one.  IF the count goes to zero, the object is deleted.
 * Both of these increment / decrement operations are atomic, i.e. protected
 * by locks.
 */    
    private static class CountedSensor {
        private final Sensor        _mSensor;
        private int                 _mCount;
        private final ReentrantLock _mLock = new ReentrantLock();
        private CountedSensor(Sensor sensor) {
            _mSensor = sensor;
            _mCount = 1;
        }
        private Sensor getSensor() { return _mSensor; }
        private void lockedIncrementCount() {
            _mLock.lock();
            _mCount++;
            _mLock.unlock();
        }
        private boolean lockedDecrementCountAndCheckIfZero() {
            boolean returnValue;
            _mLock.lock();
            _mCount--;                  // Can't throw
            returnValue = _mCount == 0; // Ditto
            _mLock.unlock();
            return returnValue;
        }
        private String dumpIt() { return _mSensor.getDisplayName() + "(" + String.valueOf(_mCount) + ")"; }
    }
    
    private HashSet<CountedSensor> getCountedSensorHashSet(HashSet<Sensor> sensors) {
        HashSet<CountedSensor> returnValue = new HashSet<>();
        sensors.forEach((sensor) -> {
            returnValue.add(new CountedSensor(sensor));
        });
        return returnValue;
    }
    
    private final LockedRoutesManager _mLockedRoutesManager;
    private final String _mOSSectionDescription;                // For debugging
    private final String _mRuleDescription;                     // Ditto
    private final HashSet<CountedSensor> _mCountedSensors;
    private final boolean _mRightTraffic;
    private final PropertyChangeListener _mSensorPropertyChangeListener = (PropertyChangeEvent e) -> { occupancyStateChange(e); };
    
    public LockedRoute(LockedRoutesManager lockedRoutesManager, HashSet<Sensor> sensors, String osSectionDescription, String ruleDescription, boolean rightTraffic) {
        _mLockedRoutesManager = lockedRoutesManager;    // Reference to our parent.
        _mOSSectionDescription = osSectionDescription;
        _mRuleDescription = ruleDescription;
        _mCountedSensors = getCountedSensorHashSet(sensors);
        _mRightTraffic = rightTraffic;
    }
    public HashSet<CountedSensor> getCountedSensors() { return _mCountedSensors; }
    
    public HashSet<Sensor> getSensors() {
        HashSet<Sensor> returnValue = new HashSet<>();
        _mCountedSensors.forEach((countedSensor) -> {
            returnValue.add(countedSensor.getSensor());
        });
        return returnValue;
    }
    
    /**
     * This routine is ONLY called by the higher level if this is NOT a merged route,
     * but a brand new route.
     * <p>
     * Once the higher level has determined that this route is valid and available,
     * then finish the process here.  Here we register occupancy changes for all
     * sensors, and as they report "unoccupied", we prune that entry from our
     * set, thereby releasing that segment to the rest of the system.  See
     * "occupancyStateChange".
     */
    public void allocateRoute() {
        _mCountedSensors.forEach((countedSensor) -> {
            countedSensor.getSensor().addPropertyChangeListener(_mSensorPropertyChangeListener);
        });
    }
    
    /**
     * The higher level called us to merge "newLockedRoute" sensors into "this" object.
     * "newLockedRoute" has had NOTHING done to it other than construct it.
     * We iterate the sensors in it, and if there is a match in our sensor list
     * just increment the count, otherwise register a property change on the new added sensor.
     * <p>
     * After this, "newLockedRoute" is not needed for anything.
     * <p>
     * NOTE: The higher level routines saved a copy of "this" object somewhere,
     * so that when Signals Normal (all stop) is ever selected by the Dispatcher,
     * we can delete this entire object easily.  Ergo, we MUST merge the newLockedRoute
     * into "this", NOT the other way around.  Besides, merge to "this" is easier
     * anyways.....
     * @param newLockedRoute New route that needs to be merged to "this".
     */
    public void mergeRoutes(LockedRoute newLockedRoute) {
        HashSet<CountedSensor> newCountedSensors = newLockedRoute.getCountedSensors();
        for (CountedSensor newCountedSensor : newCountedSensors) { // Do the new HashSet first:
            boolean foundMatch = false;
            for (CountedSensor thisCountedSensor : _mCountedSensors) { // For all in "this"
                if (newCountedSensor.getSensor() == thisCountedSensor.getSensor()) { // Match, increment usage count:
                    thisCountedSensor.lockedIncrementCount();
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {  // Need to add to our list:
                _mCountedSensors.add(newCountedSensor); // Ok to add, not in loop referencing this HashSet!
                newCountedSensor.getSensor().addPropertyChangeListener(_mSensorPropertyChangeListener);
            }
        }
    }

    public enum AnyInCommonReturn { NONE,       // NOTHING matches at all.
                                    YES,        // Absolute overlap.
                                    FLEETING }  // One overlaps, we were asked to checkDirection, and the direction matches
    /**
     * Checks the sensors passed in as object type "LockedRoute" against the sensors in this object.
     * Support Fleeting requests.
     * 
     * @param lockedRoute Existing LockedRoute to check against.
     * @param checkDirection Pass false if this is a turnout resource request, else pass true to check traffic direction.
     * @param rightTraffic If right traffic was requested pass true, else false for left traffic.
     * @return AnyInCommonReturn enum value.  NONE = Nothing matches at all, YES = Absolute overlap, FLEETING = Overlap, but direction matches.
     */
    public AnyInCommonReturn anyInCommon(LockedRoute lockedRoute, boolean checkDirection, boolean rightTraffic) {
        //_mCountedSensors.
        boolean anyInCommon = !Collections.disjoint(getSensors(), lockedRoute.getSensors());
        if (anyInCommon) { // We need to check to see if it is a "fleeting" operations:
            if (checkDirection && rightTraffic == lockedRoute._mRightTraffic) return AnyInCommonReturn.FLEETING; // If we need to check direction, AND we are in the same direction, no problem.  Fleeting of some sort.
        }
        return anyInCommon ? AnyInCommonReturn.YES : AnyInCommonReturn.NONE;
    }
    
    /**
     * Simple routine to remove all listeners that were registered to each of our counted sensors.
     */
    public void removeAllListeners() {
        _mCountedSensors.forEach((countedSensor) -> {
            countedSensor.getSensor().removePropertyChangeListener(_mSensorPropertyChangeListener);
        });
    }
    
    /**
     * Simple routine to return in a string all information on this route.
     * 
     * @return Dump of routes information in returned string.  No specific format.  See code in routine.
     */
    public String dumpRoute() {
        String returnString = "";
        for (CountedSensor countedSensor : _mCountedSensors) {
            if (returnString.isEmpty()) returnString = countedSensor.dumpIt();
            else returnString += ", " + countedSensor.dumpIt();
        }
        returnString += _mRightTraffic ? " Dir:R" : " Dir:L";  // NOI18N
        return "O.S. " + _mOSSectionDescription + _mRuleDescription + " " + Bundle.getMessage("LockedRouteSensorsStillAllocatedList") + " " + returnString; // NOI18N
    }
    
/**
 * IF the sensor (NOT the NBHSensor!) went inactive (unoccupied), and the count
 * went to zero, remove it from our allocated resource list:
 */
    private void occupancyStateChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("KnownState") && (int)e.getNewValue() == Sensor.INACTIVE) {  // NOI18N  Went inactive, prune us:
            Sensor sensor = (Sensor)e.getSource();
            for (CountedSensor countedSensor : _mCountedSensors) {
                if (countedSensor.getSensor() == sensor) { // Free this resource.
                    if (countedSensor.lockedDecrementCountAndCheckIfZero()) { // Went to zero, remove:
                        sensor.removePropertyChangeListener(_mSensorPropertyChangeListener);    // Not watching this one anymore.
                        _mCountedSensors.remove(countedSensor);
                    }
                    break;      // Can be ONLY once in "_mCountedSensors", due to prior merges!
                }
            }
            if (_mCountedSensors.isEmpty()) { // Notify parent that we are empty, so it can purge us completely from it's master list:
                _mLockedRoutesManager.cancelLockedRoute(this);
            }
        }
    }
}
