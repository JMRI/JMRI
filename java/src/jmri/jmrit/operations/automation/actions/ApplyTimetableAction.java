package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainSchedule;
import jmri.jmrit.operations.trains.TrainScheduleManager;

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
            TrainSchedule ts = TrainScheduleManager.instance().getScheduleById(TrainManager.instance().getTrainScheduleActiveId());
            if (ts != null) {
                for (Train train : TrainManager.instance().getTrainsByIdList()) {
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
