package jmri.jmrit.operations.automation.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.setup.Control;

public class DownCounterAction extends Action implements PropertyChangeListener {

    private static final int _code = ActionCodes.DOWN_COUNTER;
    int _counter = -1;
    
    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("DownCounter");
    }

    @Override
    public void doAction() {
        if (getCount() < 1) {
            setCount(getUserCount());
        }
        decrementCounter();
        if (getCount() > 0) {
            finishCounterAction(false);
        } else {
            finishCounterAction(true);
        }
    }
    
    @Override
    public void cancelAction() {
        // no cancel for this action
    }
    
    private int getCount() {
        return _counter;
    }
    
    private void setCount(int count) {
        _counter = count;
    }
    
    private void decrementCounter() {
        _counter -= 1;
    }

    private void finishCounterAction(boolean success) {
        setRunning(true);
        getAutomationItem().setActionSuccessful(success);
        setRunning(false);
        firePropertyChange(ACTION_COMPLETE_CHANGED_PROPERTY, !success, success);
    }
    
    private int getUserCount() {
        int count = -1;
        String msg = getAutomationItem().getMessage();
        if (!msg.isEmpty()) {
            try {
                count = Integer.parseInt(msg);
            } catch (NumberFormatException nfe) {
                log.error("Down count ({}) not a number", msg);
            }
        }
        return count;
    }
    
    @Override
    public String getStatus() {
        if (getCount() < 0) {
            setCount(getUserCount());
            getAutomationItem().addPropertyChangeListener(this);
        }
        if (getCount() < 0) {
            return (Bundle.getMessage("Error"));
        }
        return Integer.toString(getCount());
    }
    
    @Override
    public void reset() {
        setCount(-1);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY)
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
            if (e.getPropertyName().equals(AutomationItem.AUTOMATION_ITEM_MESSAGE_CHANGED_PROPERTY)) {
                reset(); // reset down counter value
            }
    }
    private final static Logger log = LoggerFactory.getLogger(DownCounterAction.class);
}
