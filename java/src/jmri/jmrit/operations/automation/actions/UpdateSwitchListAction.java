package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainScheduleManager;


public class UpdateSwitchListAction extends Action {

    private static final int _code = ActionCodes.UPDATE_SWITCHLIST;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        if (Setup.isSwitchListRealTime() && TrainManager.instance().isPrintPreviewEnabled())
            return Bundle.getMessage("PrintSwitchList");
        else if (Setup.isSwitchListRealTime() && !TrainManager.instance().isPrintPreviewEnabled())
            return Bundle.getMessage("PreviewSwitchList");
        else
            return Bundle.getMessage("UpdateSwitchList");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            TrainScheduleManager.instance().buildSwitchLists();
            finishAction(true);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

}
