package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.trains.Train;

public class ResetTrainAction extends Action {

    private static final int _code = ActionCodes.RESET_TRAIN;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("ResetTrain");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Train train = getAutomationItem().getTrain();
            if (train == null || train.getRoute() == null) {
                finishAction(false); // failed, need train to reset
            } else {
                setRunning(true);
                finishAction(train.reset());
            }
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

}
