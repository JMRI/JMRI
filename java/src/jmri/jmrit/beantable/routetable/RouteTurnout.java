package jmri.jmrit.beantable.routetable;

import jmri.Route;
import jmri.Turnout;

class RouteTurnout extends RouteElement {
    private final String SET_TO_CLOSED;
    private final  String SET_TO_THROWN;
    private final String SET_TO_TOGGLE;

    RouteTurnout(String sysName, String userName) {
        super(sysName, userName);
        SET_TO_CLOSED = Bundle.getMessage("Set") + " " + Bundle.getMessage("TurnoutStateClosed");
        SET_TO_THROWN = Bundle.getMessage("Set") + " " + Bundle.getMessage("TurnoutStateThrown");
        SET_TO_TOGGLE = Bundle.getMessage("Set") + " " + Bundle.getMessage("Toggle");
    }

    @Override
    String getSetToState() {
        switch (_setToState) {
            case Turnout.CLOSED:
                return SET_TO_CLOSED;
            case Turnout.THROWN:
                return SET_TO_THROWN;
            case Route.TOGGLE:
                return SET_TO_TOGGLE;
            default:
                // fall through
                break;
        }
        return "";
    }

    @Override
    void setSetToState(String state) {
        if (SET_TO_CLOSED.equals(state)) {
            _setToState = Turnout.CLOSED;
        } else if (SET_TO_THROWN.equals(state)) {
            _setToState = Turnout.THROWN;
        } else if (SET_TO_TOGGLE.equals(state)) {
            _setToState = Route.TOGGLE;
        }
    }
}
