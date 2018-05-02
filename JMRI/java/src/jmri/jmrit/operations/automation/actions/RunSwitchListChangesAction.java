package jmri.jmrit.operations.automation.actions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import jmri.InstanceManager;
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
     *
     * @param isChanged if set true only locations with changes will get a
     *                  custom switch list.
     */
    @SuppressFBWarnings(
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
            if (!InstanceManager.getDefault(TrainCustomSwitchList.class).excelFileExists()) {
                log.warn("Manifest creator file not found!, directory name: {}, file name: {}", InstanceManager.getDefault(TrainCustomSwitchList.class)
                        .getDirectoryName(), InstanceManager.getDefault(TrainCustomSwitchList.class).getFileName());
                finishAction(false);
                return;
            }
            setRunning(true);
            TrainSwitchLists trainSwitchLists = new TrainSwitchLists();
            TrainCsvSwitchLists trainCsvSwitchLists = new TrainCsvSwitchLists();
            // check that both the Excel custom manifest and custom switch lists aren't busy with other work
            // We've found that on some OS only one copy of Excel can be running at a time
            // this can wait thread
            if (!InstanceManager.getDefault(TrainCustomManifest.class).checkProcessReady()) {
                log.warn(
                        "Timeout waiting for excel manifest program to complete previous operation, timeout value: {} seconds",
                        Control.excelWaitTime);
            }
            // this can wait thread
            if (!InstanceManager.getDefault(TrainCustomSwitchList.class).checkProcessReady()) {
                log.warn(
                        "Timeout waiting for excel switch list program to complete previous operation, timeout value: {} seconds",
                        Control.excelWaitTime);
            }
            if (InstanceManager.getDefault(TrainCustomSwitchList.class).doesCommonFileExist()) {
                log.warn("Switch List CSV common file exists!");
            }
            for (Location location : InstanceManager.getDefault(LocationManager.class).getLocationsByNameList()) {
                if (location.isSwitchListEnabled()
                        && (!isChanged || (isChanged && location.getStatus().equals(Location.MODIFIED)))) {
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
                    InstanceManager.getDefault(TrainCustomSwitchList.class).addCVSFile(csvFile);
                }
            }
            // Processes the CSV Manifest files using an external custom program.
            boolean status = InstanceManager.getDefault(TrainCustomSwitchList.class).process();
            if (status) {
                try {
                    status = InstanceManager.getDefault(TrainCustomSwitchList.class).waitForProcessToComplete(); // wait up to 60 seconds per file
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    log.error("Thread interrupeted while waiting", e);
                }
            } else {
                log.info("No switch list changes found");
            }
            // set trains switch lists printed
            InstanceManager.getDefault(TrainManager.class).setTrainsSwitchListStatus(Train.PRINTED);
            finishAction(status);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action
    }

    private final static Logger log = LoggerFactory.getLogger(RunSwitchListChangesAction.class);

}
