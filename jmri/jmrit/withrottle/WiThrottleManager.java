package jmri.jmrit.withrottle;

import java.io.File;
import jmri.jmrit.XmlFile;

/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision: 1.3 $
 */

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
        consistController = new ConsistController();
        withrottlePreferences = new WiThrottlePreferences(XmlFile.prefsDir()+ "throttle" +File.separator+ "WiThrottlePreferences.xml");
    }

    static private WiThrottleManager instance() {
        if (root==null) root = new WiThrottleManager();
        return root;
    }
    
    
    static public TrackPowerController trackPowerControllerInstance(){
        return instance().trackPowerController;
    }

    static public TurnoutController turnoutControllerInstance(){
        return instance().turnoutController;
    }

    static public RouteController routeControllerInstance(){
        return instance().routeController;
    }

    static public ConsistController consistControllerInstance(){
        return instance().consistController;
    }

    static public WiThrottlePreferences withrottlePreferencesInstance(){
        return instance().withrottlePreferences;
    }




}
