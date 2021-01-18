package jmri.jmrit.operations.automation.actions;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCsvSwitchLists;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainSwitchLists;

public class PrintSwitchListChangesAction extends Action {

    private static final int _code = ActionCodes.PRINT_SWITCHLIST_CHANGES;
    protected static final boolean IS_CHANGED = true;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        if (InstanceManager.getDefault(TrainManager.class).isPrintPreviewEnabled()) {
            return Bundle.getMessage("PreviewSwitchListChanges");
        } else {
            return Bundle.getMessage("PrintSwitchListChanges");
        }
    }

    @Override
    public void doAction() {
        doAction(IS_CHANGED);
    }

    protected void doAction(boolean isChanged) {
        if (getAutomationItem() != null) {
            setRunning(true);
            TrainSwitchLists trainSwitchLists = new TrainSwitchLists();
            TrainCsvSwitchLists trainCsvSwitchLists = new TrainCsvSwitchLists();
            for (Location location : InstanceManager.getDefault(LocationManager.class).getLocationsByNameList()) {
                if (location.isSwitchListEnabled() &&
                        (!isChanged || location.getStatus().equals(Location.MODIFIED))) {
                    // also build the CSV switch lists
                    trainCsvSwitchLists.buildSwitchList(location);
                    trainSwitchLists.buildSwitchList(location);
                    trainSwitchLists.printSwitchList(location,
                            InstanceManager.getDefault(TrainManager.class).isPrintPreviewEnabled());
                }
            }
            // set trains switch lists printed
            InstanceManager.getDefault(TrainManager.class).setTrainsSwitchListStatus(Train.PRINTED);
        }
        finishAction(true);
    }

    @Override
    public void cancelAction() {
        // no cancel for this action
    }
}
