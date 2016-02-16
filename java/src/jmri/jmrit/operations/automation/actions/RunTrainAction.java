package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCustomManifest;
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
            if (train != null  && train.getRoute() != null && train.isBuilt() && TrainCustomManifest.manifestCreatorFileExists()) {
                setRunning(true);
                new TrainCustomManifest().checkProcessComplete(); // this will wait thread
                TrainCustomManifest.addCVSFile(train.createCSVManifestFile());
                boolean status = TrainCustomManifest.process();
                if (status) {
                    try {
                        TrainCustomManifest.waitForProcessToComplete(); // wait up to 60 seconds
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                finishAction(status);
            } else {
                finishAction(false);
            }
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }
    private final static Logger log = LoggerFactory.getLogger(RunTrainAction.class.getName());
}
