package jmri.jmrit.ctc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import jmri.Sensor;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 * 
 * Each of these objects describe a route consisting of all of the
 * occupancy sensors(s) that specify a route.  This is the "topology"
 * information needed to determine if a route is available or not.
 * 
 * For O.S. sections, this routine will get the reference to the
 * "_mOSSectionOccupiedExternalSensor" associated with that O.S. section.  For
 * the occupancy sensors, just a reference to those occupancy sensors.
 * 
 * It will register with the occupancy sensors for state change.  As
 * each occupancy sensor goes unoccupied (INACTIVE), it will remove from it's
 * list that resource, thereby freeing it up for any other allocation in the
 * future.
*/

public class LockedRoute {
    private final LockedRoutesManager _mLockedRoutesManager;
    private final String _mOSSectionDescription;                // For debugging
    private final String _mRuleDescription;                     // Ditto
    private final HashSet<Sensor> _mSensors;
    private final PropertyChangeListener _mSensorPropertyChangeListener = (PropertyChangeEvent e) -> { occupancyStateChange(e); };
    
    public LockedRoute(LockedRoutesManager lockedRoutesManager, HashSet<Sensor> sensors, String osSectionDescription, String ruleDescription) {
        _mLockedRoutesManager = lockedRoutesManager;    // Reference to our parent.
        _mOSSectionDescription = osSectionDescription;
        _mRuleDescription = ruleDescription;
        _mSensors = sensors;
    }
    public HashSet<Sensor> getSensors() { return _mSensors; }
    
/**
 * Once the higher level has determined that this route is valid and available,
 * then finish the process here.  Here we register occupancy changes for all
 * sensors, and as they report "unoccupied", we prune that entry from our
 * set, thereby releasing that segment to the rest of the system.  See
 * "occupancyStateChange".
 */
    public void allocateRoute() {
        for (Sensor sensor : _mSensors) {
            sensor.addPropertyChangeListener(_mSensorPropertyChangeListener);
        }
    }
    
    public boolean anyInCommon(LockedRoute lockedRoute) {
        return !Collections.disjoint(_mSensors, lockedRoute.getSensors());
    }
    
    public void removeAllListeners() {
        for (Sensor sensor : _mSensors) {
            sensor.removePropertyChangeListener(_mSensorPropertyChangeListener);
        }
    }
    
    public String dumpIt() {
        String returnString = "";
        for (Sensor sensor : _mSensors) {
            if (returnString.isEmpty()) returnString = sensor.getDisplayName();
            else returnString += ", " + sensor.getDisplayName();
        }
        return "O.S. " + _mOSSectionDescription + _mRuleDescription + " " + Bundle.getMessage("LockedRouteSensorsStillAllocatedList") + " " + returnString; // NOI18N
    }
    
/**
 * IF the sensor (NOT the NBHSensor!) went inactive (unoccupied), remove it from our allocated
 * resource list:
 */    
    private void occupancyStateChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("KnownState") && (int)e.getNewValue() == Sensor.INACTIVE) {  // NOI18N  Went inactive, prune us:
            Sensor sensor = (Sensor)e.getSource();
            sensor.removePropertyChangeListener(_mSensorPropertyChangeListener);    // Not watching this one anymore.
            _mSensors.remove(sensor);           // Free this resource.
            if (_mSensors.isEmpty()) { // Notify parent that we are empty, so it can purge us completely from it's master list:
                _mLockedRoutesManager.cancelLockedRoute(this);
            }
        }
    }
}
