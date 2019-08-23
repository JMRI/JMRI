package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.automation.Automation;

public class ResumeAutomationAction extends RunAutomationAction {

    private static final int _code = ActionCodes.RESUME_AUTOMATION;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("ResumeAutomation");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Automation automation = getAutomationItem().getAutomationToRun();
            if (automation != null && !automation.isRunning()) {
                setRunning(true);
                automation.resume();
                finishAction(true);
            } else {
                finishAction(false);
            }
        }
    }
}
