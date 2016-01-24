package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.automation.AutomationItem;

public class GotoSuccessAction extends Action {

    private static final int _code = ActionCodes.GOTO_IF_TRUE;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("GotoIfSuccess");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            AutomationItem automationItem = getAutomationItem().getGotoAutomationItem();
            if (automationItem != null) {
                setRunning(true);
                // the old property controls conditional branch if successful
                firePropertyChange(ACTION_GOTO_CHANGED_PROPERTY, true, automationItem);
            }
            finishAction(automationItem != null);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action

    }

}
