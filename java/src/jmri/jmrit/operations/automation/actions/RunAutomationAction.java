package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.automation.Automation;

public class RunAutomationAction extends Action {

    private static final int _code = ActionCodes.RUN_AUTOMATION;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String toString() {
        return Bundle.getMessage("RunAutomation");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Automation automation = getAutomationItem().getAutomation();
            automation.run();
            firePropertyChange(ACTION_COMPLETE_CHANGED_PROPERTY, false, true);
        } 
    }

    @Override
    public void cancelAction() {
        // no cancel for this action
        
    }

}
