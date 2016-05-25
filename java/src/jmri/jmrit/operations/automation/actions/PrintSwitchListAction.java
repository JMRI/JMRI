package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainSwitchLists;

public class PrintSwitchListAction extends Action {

    private static final int _code = ActionCodes.PRINT_SWITCHLIST;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        if (TrainManager.instance().isPrintPreviewEnabled()) {
            return Bundle.getMessage("PreviewSwitchList");
        } else {
            return Bundle.getMessage("PrintSwitchList");
        }
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            setRunning(true);
            TrainSwitchLists trainSwitchLists = new TrainSwitchLists();
            for (Location location : LocationManager.instance().getLocationsByNameList()) {
                if (location.isSwitchListEnabled()) {
                    trainSwitchLists.buildSwitchList(location);
                    trainSwitchLists.printSwitchList(location, TrainManager.instance().isPrintPreviewEnabled());
                }
            }
            // set trains switch lists printed
            TrainManager.instance().setTrainsSwitchListStatus(Train.PRINTED);
        }
        finishAction(true);
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }
}
