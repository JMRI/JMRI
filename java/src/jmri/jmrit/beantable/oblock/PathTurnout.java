package jmri.jmrit.beantable.oblock;

import jmri.Turnout;

import static jmri.jmrit.beantable.oblock.BlockPathEditFrame.SET_TO_CLOSED;
import static jmri.jmrit.beantable.oblock.BlockPathEditFrame.SET_TO_THROWN;

/**
 * OBlock Path Table PathTurnout Element.
 *
 * Adapted from jmri.jmrit.beantable.routetable.RouteElement
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse Copyright (C) 2016, 2020
 * @author Paul Bender Copyright (C) 2020
 */
class PathTurnout {

    private final String _sysName;
    private final String _userName;
    private boolean _included;
    int _setToState;

    public PathTurnout(String sysName, String userName) {
        _sysName = sysName;
        _userName = userName;
        _included = false;
        _setToState = Turnout.CLOSED;
    }

    String getSysName() {
        return _sysName;
    }

    String getUserName() {
        return _userName;
    }

    public boolean isIncluded() {
        return _included;
    }

    public String getDisplayName() {
        String name = getUserName();
        if (name != null && name.length() > 0) {
            return name;
        } else {
            return getSysName();
        }

    }

    public void setIncluded(boolean include) {
        _included = include;
    }

    String getSetToState() {
        switch (_setToState) {
            case Turnout.CLOSED:
                return SET_TO_CLOSED;
            case Turnout.THROWN:
                return SET_TO_THROWN;
            default:
                // fall through
                break;
        }
        return "";
    }

    void setSetToState(String state) {
        if (SET_TO_CLOSED.equals(state)) {
            _setToState = Turnout.CLOSED;
        } else if (SET_TO_THROWN.equals(state)) {
            _setToState = Turnout.THROWN;
        }
    }

    public int getState() {
        return _setToState;
    }

    public void setState(int state) {
        _setToState = state;
    }

}
