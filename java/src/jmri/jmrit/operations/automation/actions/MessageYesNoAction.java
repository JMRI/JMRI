package jmri.jmrit.operations.automation.actions;

public class MessageYesNoAction extends Action {

    private static final int _code = ActionCodes.MESSAGE_YES_NO;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("MessageYesNo");
    }

    @Override
    public String getActionSuccessfulString() {
        return Bundle.getMessage("ButtonYes");
    }

    @Override
    public String getActionFailedString() {
        return Bundle.getMessage("ButtonNo");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            setRunning(true);
            int response = sendMessage(getAutomationItem().getMessage(),
                    new Object[]{Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonNo")}, true);
            getAutomationItem().setActionSuccessful(response != 1);
            setRunning(false);
            firePropertyChange(ACTION_COMPLETE_CHANGED_PROPERTY, false, true);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

}
