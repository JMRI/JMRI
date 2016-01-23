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
            if (train != null) {
                setRunning(true);
                train.addPropertyChangeListener(this);
            } else {
                finishAction(false);
            }
        }
    }

    private void trainUpdate() {
        if (getAutomationItem() != null) {
            Train train = getAutomationItem().getTrain();
            if (train.getStatusCode() == Train.CODE_TERMINATED) {
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
        if (Control.showProperty)
            log.debug("Property change AutomationItem {}: ({}) old: ({}) new: ({})", getAutomationItem().getId(),
                    evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        if (evt.getPropertyName().equals(Train.BUILT_CHANGED_PROPERTY)) {
            trainUpdate();
        }
    }

    static Logger log = LoggerFactory.getLogger(WaitTrainTerminatedAction.class.getName());

}
