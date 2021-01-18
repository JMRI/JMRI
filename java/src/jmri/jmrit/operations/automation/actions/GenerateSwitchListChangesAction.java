package jmri.jmrit.operations.automation.actions;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCsvSwitchLists;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainSwitchLists;

public class GenerateSwitchListChangesAction extends Action {

    private static final int _code = ActionCodes.GENERATE_SWITCHLIST_CHANGES;
    protected static final boolean IS_CHANGED = true;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("GenerateSwitchListChanges");
    }

    @Override
    public void doAction() {
        doAction(IS_CHANGED);
    }

    /**
     * Generates the CSV file switch list for each location that is selected and
     * there's new work for that location.
     * <p>
     * common code see GenerateSwitchListAction.java
     *
     * @param isChanged if set true only locations with changes will get a custom
     *                  switch list.
     */
    protected void doAction(boolean isChanged) {
        if (getAutomationItem() != null) {
            if (!Setup.isGenerateCsvSwitchListEnabled()) {
                log.warn("Generate CSV Switch List isn't enabled!");
                finishAction(false);
                return;
            }
            setRunning(true);
            TrainSwitchLists trainSwitchLists = new TrainSwitchLists();
            TrainCsvSwitchLists trainCsvSwitchLists = new TrainCsvSwitchLists();
            for (Location location : InstanceManager.getDefault(LocationManager.class).getLocationsByNameList()) {
                if (location.isSwitchListEnabled() &&
                        (!isChanged || location.getStatus().equals(Location.MODIFIED))) {
                    File csvFile = trainCsvSwitchLists.buildSwitchList(location);
                    // also build the regular switch lists so they can be used
                    trainSwitchLists.buildSwitchList(location);
                    if (csvFile == null || !csvFile.exists()) {
                        log.error("CSV switch list file was not created for location {}", location.getName());
                        finishAction(false);
                        return;
                    }
                }
            }
            // set trains switch lists printed
            InstanceManager.getDefault(TrainManager.class).setTrainsSwitchListStatus(Train.PRINTED);
            finishAction(true);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action
    }

    private final static Logger log = LoggerFactory.getLogger(GenerateSwitchListChangesAction.class);

}
