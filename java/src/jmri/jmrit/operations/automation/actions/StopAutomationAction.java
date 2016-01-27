package jmri.jmrit.operations.automation.actions;

import javax.swing.JComboBox;
import jmri.jmrit.operations.automation.Automation;

public class StopAutomationAction extends Action {

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

    @Override
    public void cancelAction() {
        // no cancel for this action
    }
    
    @Override
    public JComboBox<Automation> getComboBox() {
        return getAutomationComboBox();
    }

}
