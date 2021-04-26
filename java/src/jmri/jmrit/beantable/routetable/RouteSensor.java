package jmri.jmrit.beantable.routetable;

import jmri.Route;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Route Table RouteSensor Elements.
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
class RouteSensor extends RouteElement {
    private static final String SET_TO_ACTIVE = Bundle.getMessage("Set") + " " + Bundle.getMessage("SensorStateActive");
    private static final String SET_TO_INACTIVE = Bundle.getMessage("Set") + " " + Bundle.getMessage("SensorStateInactive");

    private static final String SET_TO_TOGGLE = Bundle.getMessage("Set") + " " + Bundle.getMessage("Toggle");

    RouteSensor(String sysName, String userName) {
        super(sysName, userName);
    }

    @Override
    String getSetToState() {
        switch (_setToState) {
            case Sensor.INACTIVE:
                return SET_TO_INACTIVE;
            case Sensor.ACTIVE:
                return SET_TO_ACTIVE;
            case Route.TOGGLE:
                return SET_TO_TOGGLE;
            default:
                log.warn("Unhandled route state: {}", _setToState);
                break;
        }
        return "";
    }

    @Override
    void setSetToState(String state) {
        if (SET_TO_INACTIVE.equals(state)) {
            _setToState = Sensor.INACTIVE;
        } else if (SET_TO_ACTIVE.equals(state)) {
            _setToState = Sensor.ACTIVE;
        } else if (SET_TO_TOGGLE.equals(state)) {
            _setToState = Route.TOGGLE;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(RouteSensor.class);
}
