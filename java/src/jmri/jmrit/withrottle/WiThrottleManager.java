package jmri.jmrit.withrottle;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;

/**
 * @author Brett Hoffman Copyright (C) 2010
 */
public class WiThrottleManager implements InstanceManagerAutoDefault {

    private TrackPowerController trackPowerController = null;
    private TurnoutController turnoutController = null;
    private RouteController routeController = null;
    private ConsistController consistController = null;
    private FastClockController fastClockController = null;

    public WiThrottleManager() {
    }

    public TrackPowerController getTrackPowerController() {
        if (trackPowerController == null) {
            trackPowerController = new TrackPowerController();
        }
        return trackPowerController;
    }

    public TurnoutController getTurnoutController() {
        if (turnoutController == null) {
            turnoutController = new TurnoutController();
        }
        return turnoutController;
    }

    public RouteController getRouteController() {
        if (routeController == null) {
            routeController = new RouteController();
        }
        return routeController;
    }

    public ConsistController getConsistController() {
        if (consistController == null) {
            consistController = new ConsistController();
        }
        return consistController;
    }
    
    public FastClockController getFastClockController() {
        if (fastClockController == null) {
            fastClockController = new FastClockController();
        }
        return fastClockController;
    }

}
