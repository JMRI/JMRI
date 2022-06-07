package jmri.jmrit.operations.automation.actions;

import java.util.List;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

public class ResetSwitchListsAction extends Action {

    private static final int _code = ActionCodes.RESET_SWITCHLIST;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("ResetSwitchLists");
    }

    @Override
    public void doAction() {
        List<Location> locations = InstanceManager.getDefault(LocationManager.class).getUniqueLocationsByNameList();
        for (Location location : locations) {
            if (location.isSwitchListEnabled()) {
                // new switch lists will now be created for the location
                location.setSwitchListState(Location.SW_CREATE);
                location.setStatus(Location.MODIFIED);
            }     
        }
        // set trains switch lists unknown, any built trains should remain on the switch
        // lists
        InstanceManager.getDefault(TrainManager.class).setTrainsSwitchListStatus(Train.UNKNOWN);
        finishAction(true);
    }

    @Override
    public void cancelAction() {
        // no cancel for this action
    }
}
