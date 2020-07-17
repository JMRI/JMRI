package jmri.jmrit.ctc.topology;

import java.util.LinkedList;

import jmri.Sensor;

/**
 * This contains all of the information needed for the higher level
 * "TRL_Rules" to generate all of the entries in
 * "_mTRL_TrafficLockingRulesSSVList"
 * 
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019, 2020
 */
public class TopologyInfo {
    private static class SwitchInfo {
        
    }
    private final LinkedList<Sensor> _mSensors = new LinkedList<>();
    private boolean _mValid = true;
    public void addSensor(Sensor sensor) {
        _mSensors.add(sensor);
    }
    public LinkedList<Sensor> getSensors() { return _mSensors; }
    public void setInvalid() { _mValid = false; }
    public boolean isValid() { return _mValid; }
}
