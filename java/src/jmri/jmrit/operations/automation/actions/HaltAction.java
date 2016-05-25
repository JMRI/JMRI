package jmri.jmrit.operations.automation.actions;

public class HaltAction extends Action {

    private static final int _code = ActionCodes.HALT_ACTION;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("Halt");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            // can't use "finishAction(boolean)" must halt
            setRunning(true);
            getAutomationItem().setActionSuccessful(true);
            setRunning(false);
            sendMessage(getAutomationItem().getMessage(), new Object[]{Bundle.getMessage("HALT")}, true);
            firePropertyChange(ACTION_HALT_CHANGED_PROPERTY, false, true);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

    @Override
    public String getActionSuccessfulString() {
        return Bundle.getMessage("HALT");
    }
}
