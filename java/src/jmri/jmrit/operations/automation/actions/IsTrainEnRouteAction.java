package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;

public class IsTrainEnRouteAction extends Action {

    private static final int _code = ActionCodes.IS_TRAIN_EN_ROUTE;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("IsTrainEnRoute");
    }

    /**
     * Used to determine if train is en-route. Returns true if train is built
     * and hasn't reached the selected route location. If no route location has
     * been entered, return train's en-route status.
     */
    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Train train = getAutomationItem().getTrain();
            if (train == null || !train.isBuilt() || train.getRoute() == null) {
                finishAction(false);
            } else {
                RouteLocation rl = getAutomationItem().getRouteLocation();
                if (rl == null) {
                    finishAction(train.isTrainEnRoute());
                    return;
                }
                for (RouteLocation routeLocation : train.getRoute().getLocationsBySequenceList()) {
                    if (routeLocation == rl) {
                        finishAction(false);
                        break;
                    }
                    if (train.getCurrentLocation() == routeLocation) {
                        finishAction(true);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

}
