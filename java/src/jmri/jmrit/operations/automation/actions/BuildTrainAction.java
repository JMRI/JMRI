package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.trains.Train;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

@API(status = MAINTAINED)
public class BuildTrainAction extends Action {

    private static final int _code = ActionCodes.BUILD_TRAIN;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("BuildTrain");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Train train = getAutomationItem().getTrain();
            if (train == null || train.isBuilt() || train.getRoute() == null) {
                finishAction(false); // failed to build train
            } else {
                setRunning(true);
                finishAction(train.build());
            }
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action
    }

}
