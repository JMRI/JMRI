package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.trains.Train;

public class SelectTrainAction extends Action {

    private static final int _code = ActionCodes.SELECT_TRAIN;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("SelectTrain");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Train train = getAutomationItem().getTrain();
            if (train != null) {
                train.setBuildEnabled(true);
            }
            finishAction(train != null);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

}
