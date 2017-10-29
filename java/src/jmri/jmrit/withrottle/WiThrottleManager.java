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

    /**
     *
     * @return the default instance
     * @deprecated since 4.9.5; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} with
     * WiThrottlePreferences.class directly
     */
    @Deprecated
    public WiThrottlePreferences getWiThrottlePreferences() {
        return InstanceManager.getDefault(WiThrottlePreferences.class);
    }

    /**
     *
     * @return the TrackPowerController managed by this WiThrottleManager
     * @deprecated since 4.9.5; use {@link #getTrackPowerController() } instead
     */
    @Deprecated
    static public TrackPowerController trackPowerControllerInstance() {
        return InstanceManager.getDefault(WiThrottleManager.class).getTrackPowerController();
    }

    /**
     *
     * @return the TurnoutController managed by this WiThrottleManager
     * @deprecated since 4.9.5; use {@link #getTurnoutController() } instead
     */
    @Deprecated
    static public TurnoutController turnoutControllerInstance() {
        return InstanceManager.getDefault(WiThrottleManager.class).getTurnoutController();
    }

    /**
     *
     * @return the RouteController managed by this WiThrottleManager
     * @deprecated since 4.9.5; use {@link #getRouteController() } instead
     */
    @Deprecated
    static public RouteController routeControllerInstance() {
        return InstanceManager.getDefault(WiThrottleManager.class).getRouteController();
    }

    /**
     *
     * @return the ConsistController managed by this WiThrottleManager
     * @deprecated since 4.9.5; use {@link #getConsistController() } instead
     */
    @Deprecated
    static public ConsistController consistControllerInstance() {
        return InstanceManager.getDefault(WiThrottleManager.class).getConsistController();
    }

    /**
     *
     * @return the default instance of the WiThrottlePreferences
     * @deprecated since 4.9.5; use {@link #getWiThrottlePreferences()} instead
     */
    @Deprecated
    static public WiThrottlePreferences withrottlePreferencesInstance() {
        return InstanceManager.getDefault(WiThrottlePreferences.class);
    }
}
