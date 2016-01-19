package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.automation.Automation;

public class RunAutomationAction extends Action {

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
            Automation automation = getAutomationItem().getAutomation();
            if (automation != null) {
                automation.run();
            }
            finishAction(automation != null);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action

    }

}
