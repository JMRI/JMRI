package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.automation.Automation;

public class StopAutomationAction extends RunAutomationAction {

    private static final int _code = ActionCodes.STOP_AUTOMATION;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("StopAutomation");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Automation automation = getAutomationItem().getAutomationToRun();
            if (automation != null) {
                setRunning(true);
                automation.stop();
            }
            finishAction(automation != null);
        }
    }
}
