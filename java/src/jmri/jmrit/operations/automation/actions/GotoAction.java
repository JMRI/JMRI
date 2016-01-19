package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.automation.AutomationItem;

public class GotoAction extends Action {

    private static final int _code = ActionCodes.GOTO;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("Goto");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            AutomationItem automationItem = getAutomationItem().getGotoAutomationItem();
            if (automationItem != null) {
                setRunning(true);
                firePropertyChange(ACTION_GOTO_CHANGED_PROPERTY, null, automationItem);
            }
            finishAction(automationItem != null);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action

    }

}
