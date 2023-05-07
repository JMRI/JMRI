package jmri.jmrit.operations.automation.actions;

public class CounterAction extends Action {

    private static final int _code = ActionCodes.COUNTER;
    int _counter = 0;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("Counter");
    }

    @Override
    public String getActionSuccessfulString() {
        return Integer.toString(_counter);
    }

    @Override
    public void doAction() {
        _counter += 1;
        finishAction(true);
    }

    @Override
    public void cancelAction() {
        // no cancel for this action

    }
}
