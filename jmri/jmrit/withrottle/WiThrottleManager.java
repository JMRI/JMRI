package jmri.jmrit.withrottle;

import java.io.File;
import jmri.jmrit.XmlFile;

/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision: 1.1 $
 */

public class WiThrottleManager {
    
    static private WiThrottleManager root;

    private TrackPowerController trackPowerController = null;
    private WiThrottlePreferences withrottlePreferences = null;
    
    public WiThrottleManager() {
        trackPowerController = new TrackPowerController();
        withrottlePreferences = new WiThrottlePreferences(XmlFile.prefsDir()+ "throttle" +File.separator+ "WiThrottlePreferences.xml");
    }

    static private WiThrottleManager instance() {
        if (root==null) root = new WiThrottleManager();
        return root;
    }
    
    
    static public TrackPowerController trackPowerControllerInstance(){
        return instance().trackPowerController;
    }

    static public WiThrottlePreferences withrottlePreferencesInstance(){
        return instance().withrottlePreferences;
    }




}
