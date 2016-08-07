package jmri.jmrit.operations.automation.actions;

import java.io.File;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCsvSwitchLists;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainSwitchLists;
import jmri.jmrit.operations.trains.excel.TrainCustomManifest;
import jmri.jmrit.operations.trains.excel.TrainCustomSwitchList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunSwitchListChangesAction extends Action {

    private static final int _code = ActionCodes.RUN_SWITCHLIST_CHANGES;
    protected static final boolean IS_CHANGED = true;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("RunSwitchListChanges");
    }

    @Override
    public void doAction() {
        doAction(IS_CHANGED);
    }

    /**
     * Creates a custom switch list for each location that is selected and
     * there's new work for that location.
     * <p>
     * common code see RunSwitchListAction.java
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
            value = {"UC_USELESS_CONDITION", "RpC_REPEATED_CONDITIONAL_TEST"},
            justification = "isChanged = false when called from RunSwitchListAction")
    protected void doAction(boolean isChanged) {
        if (getAutomationItem() != null) {
            if (!Setup.isGenerateCsvSwitchListEnabled()) {
                log.warn("Generate CSV Switch List isn't enabled!");
                finishAction(false);
                return;
            }
            // we do need one of these!
            if (!TrainCustomSwitchList.manifestCreatorFileExists()) {
                log.warn("Manifest creator file not found!, directory name: {}, file name: {}", TrainCustomSwitchList
                        .getDirectoryName(), TrainCustomSwitchList.getFileName());
                finishAction(false);
                return;
            }
            setRunning(true);
            TrainSwitchLists trainSwitchLists = new TrainSwitchLists();
            TrainCsvSwitchLists trainCsvSwitchLists = new TrainCsvSwitchLists();
            // this can wait thread
            if (!new TrainCustomManifest().checkProcessReady()) {
                log.warn(
                        "Timeout waiting for excel manifest program to complete previous opeation, timeout value: {} seconds",
                        Control.excelWaitTime);
            }
            // this can wait thread
            if (!new TrainCustomSwitchList().checkProcessReady()) {
                log.warn(
                        "Timeout waiting for excel switch list program to complete previous opeation, timeout value: {} seconds",
                        Control.excelWaitTime);                
            }
            for (Location location : LocationManager.instance().getLocationsByNameList()) {
                if (location.isSwitchListEnabled() &&
                        (!isChanged || (isChanged && location.getStatus().equals(Location.MODIFIED)))) {
                    // also build the regular switch lists so they can be used
                    if (!Setup.isSwitchListRealTime()) {
                        trainSwitchLists.buildSwitchList(location);
                    }
                    File csvFile = trainCsvSwitchLists.buildSwitchList(location);
                    if (csvFile == null || !csvFile.exists()) {
                        log.error("CSV switch list file was not created for location {}", location.getName());
                        finishAction(false);
                        return;
                    }
                    TrainCustomSwitchList.addCVSFile(csvFile);
                }
            }
            // Processes the CSV Manifest files using an external custom program.
            int fileCount = TrainCustomSwitchList.getFileCount();
            boolean status = TrainCustomSwitchList.process();
            if (status) {
                try {
                    TrainCustomSwitchList.waitForProcessToComplete(Control.excelWaitTime * fileCount); // wait up to 60 seconds per file
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                log.info("No switch list changes found");
            }
            // set trains switch lists printed
            TrainManager.instance().setTrainsSwitchListStatus(Train.PRINTED);
            finishAction(status);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

    private final static Logger log = LoggerFactory.getLogger(RunSwitchListChangesAction.class.getName());

}
