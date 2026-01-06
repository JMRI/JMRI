package jmri.jmrit.operations.automation.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.automation.Automation;
import jmri.jmrit.operations.setup.Control;

public class WaitAutomationAction extends RunAutomationAction implements PropertyChangeListener {

    private static final int _code = ActionCodes.WAIT_AUTOMATION;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("WaitAutomation");
    }

    @Override
    public boolean isConcurrentAction() {
        return true;
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Automation automation = getAutomationItem().getAutomationToRun();
            if (automation != null) {
                automation.addPropertyChangeListener(this);
                if (automation.isRunning()) {
                    setRunning(true);
                } else {
                    automation.removePropertyChangeListener(this);
                    finishAction(true);
                }
            } else {
                finishAction(false);
            }
        }
    }

    /**
     * Wait for automation to complete.
     *
     */
    private void automationUpdate(PropertyChangeEvent evt) {
        if (getAutomationItem() != null) {
            if (evt.getPropertyName().equals(Automation.RUNNING_CHANGED_PROPERTY)) {
                Automation automation = getAutomationItem().getAutomationToRun();
                if (automation != null) {
                    automation.removePropertyChangeListener(this);
                }
                finishAction(true);
            }
        }
    }

    @Override
    public void cancelAction() {
        if (getAutomationItem() != null) {
            setRunning(false);
            Automation automation = getAutomationItem().getAutomationToRun();
            if (automation != null) {
                automation.removePropertyChangeListener(this);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Control.SHOW_PROPERTY)
            log.debug("Property change AutomationItem {}: ({}) old: ({}) new: ({})", getAutomationItem().getId(),
                    evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        automationUpdate(evt);
    }

    private final static Logger log = LoggerFactory.getLogger(WaitAutomationAction.class);

}
