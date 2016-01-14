package jmri.jmrit.operations.automation.actions;


public class BlankAction extends Action {

    private static final int _code = ActionCodes.NOP_ACTION;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String toString() {
        return "";
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
