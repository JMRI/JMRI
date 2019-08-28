package jmri.jmrit.ctc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 *
 * This object manages all of the active routes.
 *
 */
public class LockedRoutesManager {
    private final static Logger log = LoggerFactory.getLogger(TrafficLocking.class);
    private final ArrayList<LockedRoute> _mArrayListOfLockedRoutes = new ArrayList<>();


    public void clearAllLockedRoutes() {
        _mArrayListOfLockedRoutes.clear();
    }

/**
 * Call this routine with a set of resources that need to be checked against
 * the presently allocated resources.  ONLY A CHECK IS DONE.  No resources
 * are modified!  Typically used for a O.S. section turnout check.  Get the
 * primary O.S. sensor associated with the turnout, and pass it as the only
 * entry in "sensors".  Returns true if there is no overlap of resources, else returns false.
 */
    public boolean checkRoute(HashSet<Sensor> sensors, String osSectionDescription, String ruleDescription) {
        return privateCheckRoute(sensors, osSectionDescription, ruleDescription) != null;
    }

/**
 * Call this routine with a set of resources that need to be checked against
 * the presently allocated resources.  If there is no overlap, then add the
 * set to the presently allocated resources and return the LockedRoute object.
 * If not, modify nothing and return null.
 */
    public LockedRoute checkRouteAndAllocateIfAvailable(HashSet<Sensor> sensors, String osSectionDescription, String ruleDescription) {
        LockedRoute newLockedRoute = privateCheckRoute(sensors, osSectionDescription, ruleDescription);
        if (newLockedRoute == null) return null;
//  Ran the gambit, no collisions:
        newLockedRoute.allocateRoute();
        _mArrayListOfLockedRoutes.add(newLockedRoute);
        return newLockedRoute;
    }

    private LockedRoute privateCheckRoute(HashSet<Sensor> sensors, String osSectionDescription, String ruleDescription) {
        LockedRoute newLockedRoute = new LockedRoute(this, sensors, osSectionDescription, ruleDescription);
        for (LockedRoute existingLockedRoute : _mArrayListOfLockedRoutes) { // Check against ALL others:
//  As of this moment, newLockedRoute has NOT allocated any resource(s).
//  Later, it will be locked down IF it doesn't conflict here with any other existing route:
            if (newLockedRoute.anyInCommon(existingLockedRoute)) { // Collision, invalid!
                return null;
            }
        }
        return newLockedRoute;
    }

//  The child or running time done calls us when it determines it's empty,
//  so we can delete it from our master list.
//  It's already deallocated all of its resources, but for safety call "removeAllListeners" anyways:
    public void cancelLockedRoute(LockedRoute lockedRoute) {
        if (lockedRoute != null)  lockedRoute.removeAllListeners();  // Safety:
        _mArrayListOfLockedRoutes.remove(lockedRoute);      // Even null, no throw!
    }

//  Primarily called when the CTC system is restarted from within JMRI, nothing else should call this:
    public void removeAllListeners() {
        for (LockedRoute existingLockedRoute : _mArrayListOfLockedRoutes) { // Check against ALL others:
            existingLockedRoute.removeAllListeners();
        }
    }

    void dump() {
        log.info("Locked Routes:");
        for (LockedRoute lockedRoute : _mArrayListOfLockedRoutes) {
            log.info(lockedRoute.dumpIt());
        }
        log.info("--------------");
    }
}
