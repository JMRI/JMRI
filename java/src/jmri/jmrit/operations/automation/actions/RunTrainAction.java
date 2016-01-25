package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCustomManifest;

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
            Train train = getAutomationItem().getTrain();
            if (train != null && train.isBuilt() && TrainCustomManifest.manifestCreatorFileExists()) {
                setRunning(true);
                TrainCustomManifest.addCVSFile(train.createCSVManifestFile());
                boolean status = TrainCustomManifest.process();
                if (status) {
                    try {
                        TrainCustomManifest.waitForProcessToComplete(60); // wait up to 60 seconds
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

}
