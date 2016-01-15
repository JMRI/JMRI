package jmri.jmrit.operations.automation.actions;

import javax.swing.JOptionPane;
import jmri.jmrit.operations.automation.Automation;
import jmri.jmrit.operations.automation.AutomationItem;

public class StopAutomationAction extends Action {

    private static final int _code = ActionCodes.STOP_AUTOMATION;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String toString() {
        return Bundle.getMessage("StopAutomation");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Automation automation = getAutomationItem().getAutomation();
            if (automation != null) {
                automation.stop();
                // now show message if there's one
                if (!getAutomationItem().getMessage().equals(AutomationItem.NONE)) {
                    JOptionPane.showMessageDialog(null, getAutomationItem().getMessage(),
                            getAutomationItem().getId() + " " + toString(),
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
