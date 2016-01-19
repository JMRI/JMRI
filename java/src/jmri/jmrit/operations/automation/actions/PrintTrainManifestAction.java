package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

public class PrintTrainManifestAction extends Action {

    private static final int _code = ActionCodes.PRINT_TRAIN_MANIFEST;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        if (TrainManager.instance().isPrintPreviewEnabled())
            return Bundle.getMessage("PreviewTrainManifest");
        else
            return Bundle.getMessage("PrintTrainManifest");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Train train = getAutomationItem().getTrain();
            if (train != null && train.isBuilt()) {
                setRunning(true);
                train.printManifest(TrainManager.instance().isPrintPreviewEnabled());
                finishAction(true);
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
