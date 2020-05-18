package jmri.jmrit.beantable.routetable;

import jmri.Sensor;

abstract class RouteElement {

    String _sysName;
    String _userName;
    boolean _included;
    int _setToState;

    RouteElement(String sysName, String userName) {
        _sysName = sysName;
        _userName = userName;
        _included = false;
        _setToState = Sensor.INACTIVE;
    }

    String getSysName() {
        return _sysName;
    }

    String getUserName() {
        return _userName;
    }

    boolean isIncluded() {
        return _included;
    }

    String getDisplayName() {
        String name = getUserName();
        if (name != null && name.length() > 0) {
            return name;
        } else {
            return getSysName();
        }

    }

    void setIncluded(boolean include) {
        _included = include;
    }

    abstract String getSetToState();

    abstract void setSetToState(String state);

    int getState() {
        return _setToState;
    }

    void setState(int state) {
        _setToState = state;
    }
}
