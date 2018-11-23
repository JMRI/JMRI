package jmri.jmrit.operations.automation.actions;

import jmri.InstanceManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;

public class ApplyTrainScheduleAction extends Action {

    private static final int _code = ActionCodes.APPLY_TRAIN_SCHEDULE;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("ApplyTrainSchedule");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            TrainSchedule ts = InstanceManager.getDefault(TrainScheduleManager.class).getActiveSchedule();
            if (ts != null) {
                for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByIdList()) {
                    train.setBuildEnabled(ts.containsTrainId(train.getId()));
                }
            }
            finishAction(ts != null);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action
    }
}
