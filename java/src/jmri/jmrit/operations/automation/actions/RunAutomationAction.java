package jmri.jmrit.operations.automation.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.automation.Automation;
import jmri.jmrit.operations.automation.AutomationManager;
import jmri.jmrit.operations.setup.Control;

public class RunAutomationAction extends Action implements PropertyChangeListener {

    private static final int _code = ActionCodes.RUN_AUTOMATION;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("RunAutomation");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Automation automation = getAutomationItem().getAutomationToRun();
            if (automation != null) {
                automation.removePropertyChangeListener(this);
                automation.addPropertyChangeListener(this);
                setRunning(true);
                automation.run();
            }
            finishAction(automation != null && automation.getSize() > 0);
        }
    }

    @Override
    public String getStatus() {
        Automation automation = getAutomationItem().getAutomationToRun();
        if (getAutomationItem().isActionRan() && getAutomationItem().isActionSuccessful() && automation != null) {
            // return status of the automation that was selected
            return automation.getActionStatus();
        }
        return super.getStatus();
    }

    @Override
    public void cancelAction() {
        // no cancel for this action
    }

    @Override
    public JComboBox<Automation> getComboBox() {
        if (getAutomationItem() != null) {
            JComboBox<Automation> cb = InstanceManager.getDefault(AutomationManager.class).getComboBox();
            cb.setSelectedItem(getAutomationItem().getAutomationToRun());
            return cb;
        }
        return null;
    }

    private void automationUpdate(PropertyChangeEvent evt) {
        if (getAutomationItem() != null) {
            getAutomationItem().firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Control.SHOW_PROPERTY)
            log.debug("Property change AutomationItem {}: ({}) old: ({}) new: ({})", getAutomationItem().getId(),
                    evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        automationUpdate(evt);
    }

    private static final Logger log = LoggerFactory.getLogger(RunAutomationAction.class);
}
