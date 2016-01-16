package jmri.jmrit.operations.automation.actions;


public class HaltAction extends Action {

    private static final int _code = ActionCodes.HALT_ACTION;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String toString() {
        return Bundle.getMessage("Halt");
    }

    @Override
    public void doAction() {
        // no action, stop run or step
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

}
