package jmri.jmrit.withrottle;

import java.io.File;
import jmri.util.FileUtil;

/**
 * @author Brett Hoffman Copyright (C) 2010
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="ISC_INSTANTIATE_STATIC_CLASS", justification="False Positive (April 2016)")
        // FindBugs is flagging "This class allocates an object that is based on a class that only supplies static methods. This object does not need to be created, just access the static methods directly using the class name as a qualifier."
        // so it's confused about the access to the instance() variable.
public class WiThrottleManager {

    static private WiThrottleManager root;

    private TrackPowerController trackPowerController = null;
    private TurnoutController turnoutController = null;
    private RouteController routeController = null;
    private ConsistController consistController = null;

    private WiThrottlePreferences withrottlePreferences = null;

    public WiThrottleManager() {
        trackPowerController = new TrackPowerController();
        turnoutController = new TurnoutController();
        routeController = new RouteController();
        if (jmri.InstanceManager.getOptionalDefault(jmri.jmrit.withrottle.WiThrottlePreferences.class) == null) {
            jmri.InstanceManager.store(new jmri.jmrit.withrottle.WiThrottlePreferences(FileUtil.getUserFilesPath() + "throttle" + File.separator + "WiThrottlePreferences.xml"), jmri.jmrit.withrottle.WiThrottlePreferences.class);
        }
        withrottlePreferences = jmri.InstanceManager.getDefault(jmri.jmrit.withrottle.WiThrottlePreferences.class);
    }

    static private WiThrottleManager instance() {
        if (root == null) {
            root = new WiThrottleManager();
        }
        return root;
    }

    static public TrackPowerController trackPowerControllerInstance() {
        return instance().trackPowerController;
    }

    static public TurnoutController turnoutControllerInstance() {
        return instance().turnoutController;
    }

    static public RouteController routeControllerInstance() {
        return instance().routeController;
    }

    static public ConsistController consistControllerInstance() {
        if (instance().consistController == null) {
            instance().consistController = new ConsistController();
        }
        return instance().consistController;
    }

    static public WiThrottlePreferences withrottlePreferencesInstance() {
        return instance().withrottlePreferences;
    }

}
