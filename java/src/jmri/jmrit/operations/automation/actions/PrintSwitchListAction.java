package jmri.jmrit.operations.automation.actions;

import jmri.InstanceManager;
import jmri.jmrit.operations.trains.TrainManager;

public class PrintSwitchListAction extends PrintSwitchListChangesAction {

    private static final int _code = ActionCodes.PRINT_SWITCHLIST;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        if (InstanceManager.getDefault(TrainManager.class).isPrintPreviewEnabled()) {
            return Bundle.getMessage("PreviewSwitchList");
        } else {
            return Bundle.getMessage("PrintSwitchList");
        }
    }

    @Override
    public void doAction() {
        doAction(!IS_CHANGED);
    }

    @Override
    public void cancelAction() {
        // no cancel for this action
    }
}
