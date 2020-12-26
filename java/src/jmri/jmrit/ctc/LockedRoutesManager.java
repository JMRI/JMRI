package jmri.jmrit.ctc;

import java.util.ArrayList;
import java.util.HashSet;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This object manages all of the active routes.
 * As of 6/24/2020:
 * <p>
 * Code was added to support fleeting over "existing" routes.  Either the
 * dispatcher codes a O.S. section a second time in the same direction again for
 * a following train, or the fleeting "toggle" is on.
 * <p>
 * Background:
 * We KNOW from routine "anyInCommon", that it is IMPOSSIBLE to have overlapping
 * routes in "_mArrayListOfLockedRoutes".  That is enforced (and a requirement).
 * <p>
 * We ALSO know that when routine "checkRouteAndAllocateIfAvailable" is called,
 * its parameter "sensors" has ALL of the new sensors for that route.  We will
 * take advantage of this later on.
 * <p>
 * New code design:
 * In the routine "anyInCommon", we check to see if there is any overlap.  If
 * there is, a 2nd check is done to see if the overlapping route is in the same
 * direction.  If so, it is allowed.
 * <p>
 * IF (and only if) this is the case, we KNOW that the (old) existing overlapping
 * route in "_mArrayListOfLockedRoutes" that it was checking against is either
 * (regarding sensors) the same, or a (possible) subset.  The subset case is due
 * to occupancy sensor(s) turning off by the prior train as it advanced, and
 * being removed from the set.  Technically, it can NEVER be the same, since the
 * ABS system would prevent the signal from going non-red if the prior train
 * hasn't advanced at least one block past the O.S. section.  But the code
 * makes no assumptions regarding this.
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019, 2020
 */
public class LockedRoutesManager {
    private final static Logger log = LoggerFactory.getLogger(LockedRoutesManager.class);
    private final ArrayList<LockedRoute> _mArrayListOfLockedRoutes = new ArrayList<>();

    public void clearAllLockedRoutes() {
        _mArrayListOfLockedRoutes.clear();
    }

    /**
     * Call this routine with a set of resources that need to be checked against
     * the presently allocated resources.
     * <p>
     * ONLY A CHECK IS DONE.
     * No resources are modified!
     * <p>
     * Typically used for a O.S. section sensors check.
     * Get the primary and possibly the secondary O.S. sensor(s) associated with it, and pass it 
     * in "sensors".  NO CHECK is made of traffic direction.
     * @param sensors set of sensors.
     * @param osSectionDescription section description.
     * @param ruleDescription rule description.
     * @return true if there is no overlap of resources, else returns false.
     */
    public boolean checkRoute(HashSet<Sensor> sensors, String osSectionDescription, String ruleDescription) {
        return privateCheckRoute(sensors, osSectionDescription, ruleDescription, false, false) != null; /* Passed rightTraffic: Don't care because checkTraffic = false! */
    }

    /**
     * Call this routine with a set of resources that need to be checked against
     * the presently allocated resources.  IF the traffic direction is the same
     * as presently allocated, then it is ALWAYS allowed, as some form of fleeting
     * operation has been requested by the dispatcher.
     * <p>
     * If there is no overlap, then add the
     * set to the presently allocated resources and return the LockedRoute object.
     * If there is overlap, modify nothing and return null.
     * 
     * See explanation at the start of this source code about support put in here
     * for Fleeting.
     * 
     * @param sensors set of sensors.
     * @param osSectionDescription section description.
     * @param ruleDescription rule description.
     * @param rightTraffic true if right traffic, else false if left traffic
     * @return locked route if success, null if failed.
     */
    public LockedRoute checkRouteAndAllocateIfAvailable(HashSet<Sensor> sensors, String osSectionDescription, String ruleDescription, boolean rightTraffic) {
        LockedRoute newLockedRoute = privateCheckRoute(sensors, osSectionDescription, ruleDescription, true, rightTraffic);
        if (newLockedRoute == null) return null;
//  Ran the gambit, no collision.  However, we may need to merge if there are common elements, since this may be a fleeting request of some kind:
        if (existingLockedRouteThatHasCommonSensors == null) { // No conflict, not fleeting:
            newLockedRoute.allocateRoute();
            _mArrayListOfLockedRoutes.add(newLockedRoute);
            return newLockedRoute;
        } else { // Merge here:
/*          Background:
                "newLockedRoute" is NOT in "_mArrayListOfLockedRoutes" at this time, and "newLockedRoute" has NOT had "allocateRoute" called on it.
                "existingLockedRouteThatHadFleeting" has some sensor(s) that have registered notification of sensor events already.
            Shortcuts: 
                It would be easiest to merge "newLockedRoute" into "existingLockedRouteThatHadFleeting", since that is the least work.
                That is what "mergeRoutes" ASSUMES!
*/
            existingLockedRouteThatHasCommonSensors.mergeRoutes(newLockedRoute);
            return existingLockedRouteThatHasCommonSensors;
        }
    }

/*  We leave a breadcrumb trail for "checkRouteAndAllocateIfAvailable", if we see a LockedRoute.AnyInCommonReturn.FLEETING,
    we note which entry created it, and since there can be no overlap, that is fine.
*/
    private LockedRoute existingLockedRouteThatHasCommonSensors;
    private LockedRoute privateCheckRoute(HashSet<Sensor> sensors, String osSectionDescription, String ruleDescription, boolean checkTraffic, boolean rightTraffic) {
        existingLockedRouteThatHasCommonSensors = null;  // Flag none found.
        LockedRoute newLockedRoute = new LockedRoute(this, sensors, osSectionDescription, ruleDescription, rightTraffic);
        for (LockedRoute existingLockedRoute : _mArrayListOfLockedRoutes) { // Check against ALL others:
//  As of this moment, newLockedRoute has NOT allocated any resource(s).
//  Later, it will be locked down IF it doesn't conflict here with any other existing route:
            LockedRoute.AnyInCommonReturn anyInCommonReturn = newLockedRoute.anyInCommon(existingLockedRoute, checkTraffic, rightTraffic);
            if (anyInCommonReturn == LockedRoute.AnyInCommonReturn.YES) { // Collision, invalid!
                return null;
            } else if (anyInCommonReturn == LockedRoute.AnyInCommonReturn.FLEETING) { // Note which one:
                existingLockedRouteThatHasCommonSensors = existingLockedRoute;
                break;  // Can only be one!
            }
        }
        return newLockedRoute;
    }

    /**
     * This routine frees all resources allocated by the passed lockedRoute (listeners primarily)
     * and then removes it from our "_mArrayListOfLockedRoutes".  Now the route does NOT exist anywhere.
     * 
     * @param lockedRoute The route to cancel.
     * <p>
     * NOTE:
     * The child (LockedRoute object) or running time done calls us when it determines it's empty,
     * so we can delete it from our master list.
     * It's already de-allocated all of its resources, but for safety call "removeAllListeners" anyways.
     */
    public void cancelLockedRoute(LockedRoute lockedRoute) {
        if (lockedRoute != null)  lockedRoute.removeAllListeners();  // Safety
        _mArrayListOfLockedRoutes.remove(lockedRoute);      // Even null, no throw!
    }

    /**
     * Primarily called when the CTC system is restarted from within JMRI,
     * nothing else external to this module should call this.
     */
    public void removeAllListeners() {
        _mArrayListOfLockedRoutes.forEach((existingLockedRoute) -> {
            existingLockedRoute.removeAllListeners();
        });
    }

    /**
     * Simple routine to dump all locked routes information.  Called from
     * CTCMain when the debug sensor goes active.
     */
    void dumpAllRoutes() {
        log.info("Locked Routes:");
        for (LockedRoute lockedRoute : _mArrayListOfLockedRoutes) {
            log.info(lockedRoute.dumpRoute());
        }
        log.info("--------------");
    }
}
