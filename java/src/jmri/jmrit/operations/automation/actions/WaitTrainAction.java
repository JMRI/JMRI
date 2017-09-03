package jmri.jmrit.operations.automation.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitTrainAction extends Action implements PropertyChangeListener {

    private static final int _code = ActionCodes.WAIT_FOR_TRAIN;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("WaitForTrain");
    }

    @Override
    public boolean isConcurrentAction() {
        return true;
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Train train = getAutomationItem().getTrain();
            if (train != null && train.getRoute() != null) {
                setRunning(true);
                train.addPropertyChangeListener(this);
            } else {
                finishAction(false);
            }
        }
    }

    /**
     * Wait for train to build and no location, or train to arrive at location,
     * or train build to be deselected.
     *
     */
    private void trainUpdate(PropertyChangeEvent evt) {
        if (getAutomationItem() != null) {
            if (evt.getPropertyName().equals(Train.TRAIN_MOVE_COMPLETE_CHANGED_PROPERTY) ||
                    (evt.getPropertyName().equals(Train.BUILT_CHANGED_PROPERTY)
                    && (boolean) evt.getNewValue() == true)) {
                Train train = getAutomationItem().getTrain();
                RouteLocation rl = getAutomationItem().getRouteLocation();
                if (rl != null && rl != train.getCurrentLocation()) {
                    return; // haven't reached this location continue waiting
                }
                train.removePropertyChangeListener(this);
                finishAction(true);
            } else if (evt.getPropertyName().equals(Train.BUILD_CHANGED_PROPERTY)
                    && (boolean) evt.getNewValue() == false) {
                Train train = getAutomationItem().getTrain();
                train.removePropertyChangeListener(this);
                finishAction(true);
            }
        }
    }

    @Override
    public void cancelAction() {
        if (getAutomationItem() != null) {
            setRunning(false);
            Train train = getAutomationItem().getTrain();
            if (train != null) {
                train.removePropertyChangeListener(this);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Control.SHOW_PROPERTY)
            log.debug("Property change AutomationItem {}: ({}) old: ({}) new: ({})", getAutomationItem().getId(),
                    evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        trainUpdate(evt);
    }

    private final static Logger log = LoggerFactory.getLogger(WaitTrainAction.class);

}
