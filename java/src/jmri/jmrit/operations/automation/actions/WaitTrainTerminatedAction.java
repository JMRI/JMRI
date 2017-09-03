package jmri.jmrit.operations.automation.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitTrainTerminatedAction extends Action implements PropertyChangeListener {

    private static final int _code = ActionCodes.WAIT_FOR_TRAIN_TERMINATE;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("WaitForTrainToTerminate");
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
     * Wait for train to terminate or if user deselects the train build
     * checkbox.
     *
     */
    private void trainUpdate(PropertyChangeEvent evt) {
        if (getAutomationItem() != null) {
            Train train = getAutomationItem().getTrain();
            if ((evt.getPropertyName().equals(Train.BUILT_CHANGED_PROPERTY)
                    && train.getStatusCode() == Train.CODE_TERMINATED) ||
                    (evt.getPropertyName().equals(Train.BUILD_CHANGED_PROPERTY)
                    && (boolean) evt.getNewValue() == false)) {
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

    private final static Logger log = LoggerFactory.getLogger(WaitTrainTerminatedAction.class);

}
