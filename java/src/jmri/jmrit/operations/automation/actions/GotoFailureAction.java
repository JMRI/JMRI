package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.automation.AutomationItem;

public class GotoFailureAction extends Action {

    private static final int _code = ActionCodes.GOTO_IF_FALSE;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("GotoIfFailure");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            AutomationItem automationItem = getAutomationItem().getGotoAutomationItem();
            if (automationItem != null) {
                setRunning(true);
                // the old property controls conditional branch if failure
                firePropertyChange(ACTION_GOTO_CHANGED_PROPERTY, false, automationItem);
            }
            finishAction(automationItem != null);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action

    }

}
