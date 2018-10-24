package jmri.jmrit.operations.automation.actions;

import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;

public class ActivateTrainScheduleAction extends Action {

    private static final int _code = ActionCodes.ACTIVATE_TRAIN_SCHEDULE;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("ActivateTrainSchedule");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            InstanceManager.getDefault(TrainScheduleManager.class)
                    .setTrainScheduleActiveId(getAutomationItem().getTrainScheduleId());
            finishAction(true);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action
    }

    @Override
    public JComboBox<TrainSchedule> getComboBox() {
        JComboBox<TrainSchedule> cb = InstanceManager.getDefault(TrainScheduleManager.class).getSelectComboBox();
        if (getAutomationItem() != null) {
            cb.setSelectedItem(getAutomationItem().getTrainSchedule());
        }
        return cb;
    }
}
