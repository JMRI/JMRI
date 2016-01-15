package jmri.jmrit.operations.automation.actions;

import javax.swing.JOptionPane;
import jmri.jmrit.operations.automation.AutomationItem;
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
    public String toString() {
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
            // now show message if there's one
            if (!getAutomationItem().getMessage().equals(AutomationItem.NONE)) {
                JOptionPane.showMessageDialog(null, getAutomationItem().getMessage(),
                        getAutomationItem().getId() + " " + toString(),
                        JOptionPane.INFORMATION_MESSAGE);
            }
            firePropertyChange(ACTION_COMPLETE_CHANGED_PROPERTY, false, true);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

}
