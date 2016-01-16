package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.trains.Train;

public class BuildTrainIfSelectedAction extends Action {

    private static final int _code = ActionCodes.BUILD_TRAIN_IF_SELECTED;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("BuildTrainIfSelected");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Train train = getAutomationItem().getTrain();
            if (train != null && !train.isBuilt()) {
                train.buildIfSelected();
            }
            // now show message if there's one
            finishAction();
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

}
