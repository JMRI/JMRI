package jmri.jmrit.operations.automation.actions;

import javax.swing.JOptionPane;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.trains.Train;

public class BuildTrainIfSelectedAction extends Action {

    private static final int _code = ActionCodes.BUILD_TRAIN_IF_SELECTED;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String toString() {
        return Bundle.getMessage("BuildTrainIfSelected");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Train train = getAutomationItem().getTrain();
            if (train != null && !train.isBuilt()) {
                train.buildIfSelected();
                // now show message if there's one
                if (!getAutomationItem().getMessage().equals(AutomationItem.NONE)) {
                    JOptionPane.showMessageDialog(null, getAutomationItem().getMessage(),
                            toString() + " " + train.getName(),
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
