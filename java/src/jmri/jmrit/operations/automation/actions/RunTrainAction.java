package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.excel.TrainCustomManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunTrainAction extends Action {

    private static final int _code = ActionCodes.RUN_TRAIN;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("RunTrain");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            if (!Setup.isGenerateCsvManifestEnabled()) {
                log.warn("Generate CSV Manifest isn't enabled!");
                finishAction(false);
                return;
            }
            if (!TrainCustomManifest.manifestCreatorFileExists()) {
                log.warn("Manifest creator file not found!, directory name: {}, file name: {}", TrainCustomManifest
                        .getDirectoryName(), TrainCustomManifest.getFileName());
                finishAction(false);
                return;
            }
            Train train = getAutomationItem().getTrain();
            if (train == null) {
                log.warn("No train selected for custom manifest");
                finishAction(false);
                return;
            }
            // a train needs a route in order to be built
            if (train.getRoute() == null || !train.isBuilt()) {
                log.warn("Train ({}) needs to be built before creating a custom manifest", train.getName());
                finishAction(false);
                return;
            }
            setRunning(true);
            new TrainCustomManifest().checkProcessComplete(); // this will wait thread
            TrainCustomManifest.addCVSFile(train.createCSVManifestFile());
            boolean status = TrainCustomManifest.process();
            if (status) {
                try {
                    status = TrainCustomManifest.waitForProcessToComplete(); // wait for process to complete or timeout
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (!status) {
                    log.warn("Timeout when creating custom manifest for train ({})", train.getName());
                }
            }
            finishAction(status);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

    private final static Logger log = LoggerFactory.getLogger(RunTrainAction.class.getName());
}
