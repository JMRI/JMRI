package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.trains.timetable.TrainScheduleManager;

import jmri.jmrit.operations.trains.timetable.TrainSchedule;
import javax.swing.JComboBox;
import jmri.jmrit.operations.trains.TrainManager;

public class ActivateTimetableAction extends Action {

    private static final int _code = ActionCodes.ACTIVATE_TIMETABLE;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("ActivateTimetable");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            TrainManager.instance().setTrainSecheduleActiveId(getAutomationItem().getTrainScheduleId());
            finishAction(true);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

    @Override
    public JComboBox<TrainSchedule> getComboBox() {
        JComboBox<TrainSchedule> cb = TrainScheduleManager.instance().getSelectComboBox();
        cb.setSelectedItem(getAutomationItem().getTrainSchedule());
        return cb;
    }
}
