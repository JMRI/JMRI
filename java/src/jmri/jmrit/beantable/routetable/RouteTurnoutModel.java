package jmri.jmrit.beantable.routetable;

import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Table model for selecting Turnouts and Turnout State.
 */
class RouteTurnoutModel extends RouteOutputModel {

    private AbstractRouteAddEditFrame routeAddFrame;

    RouteTurnoutModel(AbstractRouteAddEditFrame routeAddFrame) {
        this.routeAddFrame = routeAddFrame;
        InstanceManager.turnoutManagerInstance().addPropertyChangeListener(this);
    }

    @Override
    public int getRowCount() {
        if (routeAddFrame.isShowAll()) {
            return routeAddFrame.get_turnoutList().size();
        } else {
            return routeAddFrame.get_includedTurnoutList().size();
        }
    }

    @Override
    public Object getValueAt(int r, int c) {
        List<RouteTurnout> turnoutList;
        if (routeAddFrame.isShowAll()) {
            turnoutList = routeAddFrame.get_turnoutList();
        } else {
            turnoutList = routeAddFrame.get_includedTurnoutList();
        }
        // some error checking
        if (r >= turnoutList.size()) {
            log.debug("row is greater than turnout list size");
            return null;
        }
        switch (c) {
            case INCLUDE_COLUMN:
                return turnoutList.get(r).isIncluded();
            case SNAME_COLUMN:  // slot number
                return turnoutList.get(r).getSysName();
            case UNAME_COLUMN:  //
                return turnoutList.get(r).getUserName();
            case STATE_COLUMN:  //
                return turnoutList.get(r).getSetToState();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object type, int r, int c) {
        List<RouteTurnout> turnoutList;
        if (routeAddFrame.isShowAll()) {
            turnoutList = routeAddFrame.get_turnoutList();
        } else {
            turnoutList = routeAddFrame.get_includedTurnoutList();
        }
        switch (c) {
            case INCLUDE_COLUMN:
                turnoutList.get(r).setIncluded(((Boolean) type));
                break;
            case STATE_COLUMN:
                turnoutList.get(r).setSetToState((String) type);
                break;
            default:
                log.error("RouteTurnoutModel.setValueAt should not be called on column {}", c);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(RouteTurnoutModel.class);

}
