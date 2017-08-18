package jmri.jmrit.operations.automation.actions;

import jmri.InstanceManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.timetable.TrainSchedule;
import jmri.jmrit.operations.trains.timetable.TrainScheduleManager;

public class ApplyTimetableAction extends Action {

    private static final int _code = ActionCodes.APPLY_TIMETABLE;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("ApplyTimetable");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            TrainSchedule ts = InstanceManager.getDefault(TrainScheduleManager.class).getScheduleById(InstanceManager.getDefault(TrainManager.class).getTrainScheduleActiveId());
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
