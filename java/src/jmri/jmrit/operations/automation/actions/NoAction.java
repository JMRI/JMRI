package jmri.jmrit.operations.automation.actions;


public class NoAction extends Action {

    private static final int _code = ActionCodes.NO_ACTION;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("NoAction");
    }

    @Override
    public void doAction() {
        finishAction(true);
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

}
