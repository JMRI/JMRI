package jmri.jmrit.beantable.routetable;

import jmri.Sensor;

/**
 * Base class Route Table RouteElements.
 *
 * Split from {@link jmri.jmrit.beantable.RouteTableAction}
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse Copyright (C) 2016
 * @author Paul Bender Copyright (C) 2020
 */
abstract class RouteElement {

    private final String _sysName;
    private final String _userName;
    private boolean _included;
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
