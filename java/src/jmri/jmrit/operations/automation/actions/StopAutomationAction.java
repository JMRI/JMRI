package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.automation.Automation;

public class StopAutomationAction extends Action {

    private static final int _code = ActionCodes.STOP_AUTOMATION;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String toString() {
        return Bundle.getMessage("StopAutomation");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Automation automation = getAutomationItem().getAutomation();
            if (automation != null) {
                automation.stop();
            }
            // now show message if there's one
            finishAction();
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action

    }

}
