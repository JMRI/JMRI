package jmri.jmrit.operations.automation.actions;

import javax.swing.JOptionPane;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

public class PrintTrainManifestAction extends Action {

    private static final int _code = ActionCodes.PRINT_TRAIN_MANIFEST;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String toString() {
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
                train.printManifest(TrainManager.instance().isPrintPreviewEnabled());
                // now show message if there's one
                if (!getAutomationItem().getMessage().equals(AutomationItem.NONE)) {
                    JOptionPane.showMessageDialog(null, getAutomationItem().getMessage(),
                            getAutomationItem().getId() + " " + toString() + " " + train.getName(),
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            firePropertyChange(ACTION_COMPLETE_CHANGED_PROPERTY, false, true);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

}
