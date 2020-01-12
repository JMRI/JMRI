package jmri.jmrit.ctc;

import jmri.Sensor;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 * 
 * The purpose of this object is to just see if the passed indicators are all
 * lit (Sensor.ACTIVE), meaning that the specified route is "selected".
 * Any unspecified indicators are ignored.
 */
public class SwitchIndicatorsRoute {
    private final NBHSensor _mSwitchIndicator1;
    private final NBHSensor _mSwitchIndicator2;
    private final NBHSensor _mSwitchIndicator3;
    private final NBHSensor _mSwitchIndicator4;
    private final NBHSensor _mSwitchIndicator5;
    private final NBHSensor _mSwitchIndicator6;
    
    public SwitchIndicatorsRoute(   String module, String userIdentifier, String parameter, 
                                    String switchIndicator1,
                                    String switchIndicator2,
                                    String switchIndicator3,
                                    String switchIndicator4,
                                    String switchIndicator5,
                                    String switchIndicator6 ) {
        _mSwitchIndicator1 = new NBHSensor(module, userIdentifier, parameter + " switchIndicator1", switchIndicator1, true);    // NOI18N
        _mSwitchIndicator2 = new NBHSensor(module, userIdentifier, parameter + " switchIndicator2", switchIndicator2, true);    // NOI18N
        _mSwitchIndicator3 = new NBHSensor(module, userIdentifier, parameter + " switchIndicator3", switchIndicator3, true);    // NOI18N   
        _mSwitchIndicator4 = new NBHSensor(module, userIdentifier, parameter + " switchIndicator4", switchIndicator4, true);    // NOI18N
        _mSwitchIndicator5 = new NBHSensor(module, userIdentifier, parameter + " switchIndicator5", switchIndicator5, true);    // NOI18N
        _mSwitchIndicator6 = new NBHSensor(module, userIdentifier, parameter + " switchIndicator6", switchIndicator6, true);    // NOI18N
    }
    
    public SwitchIndicatorsRoute(NBHSensor switchIndicator1, NBHSensor switchIndicator2, NBHSensor switchIndicator3, NBHSensor switchIndicator4, NBHSensor switchIndicator5, NBHSensor switchIndicator6) {
        _mSwitchIndicator1 = switchIndicator1;
        _mSwitchIndicator2 = switchIndicator2;
        _mSwitchIndicator3 = switchIndicator3;
        _mSwitchIndicator4 = switchIndicator4;
        _mSwitchIndicator5 = switchIndicator5;
        _mSwitchIndicator6 = switchIndicator6;
    }
    
    public boolean isRouteSelected() {
        if (!isSwitchIndicatorLit(_mSwitchIndicator1)) return false;
        if (!isSwitchIndicatorLit(_mSwitchIndicator2)) return false;
        if (!isSwitchIndicatorLit(_mSwitchIndicator3)) return false;
        if (!isSwitchIndicatorLit(_mSwitchIndicator4)) return false;
        if (!isSwitchIndicatorLit(_mSwitchIndicator5)) return false;
        if (!isSwitchIndicatorLit(_mSwitchIndicator6)) return false;
        return true;
    }
    
//  Quick and Dirty Routine: If it doesn't exist, it's considered lit.  If it exists, ACTIVE = lit.
    private boolean isSwitchIndicatorLit(NBHSensor sensor) {
        if (sensor == null) return true;
        if (sensor.valid()) return sensor.getKnownState() == Sensor.ACTIVE;
        return true;
    }
}
