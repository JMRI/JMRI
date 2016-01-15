package jmri.jmrit.operations.automation.actions;

import javax.swing.JOptionPane;
import jmri.jmrit.operations.automation.AutomationItem;

public class NoAction extends Action {

    private static final int _code = ActionCodes.NO_ACTION;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String toString() {
        return Bundle.getMessage("NoAction");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            if (!getAutomationItem().getMessage().equals(AutomationItem.NONE)) {
                JOptionPane.showMessageDialog(null, getAutomationItem().getMessage(),
                        getAutomationItem().getId() + " " + toString(),
                        JOptionPane.INFORMATION_MESSAGE);
            }
            firePropertyChange(ACTION_COMPLETE_CHANGED_PROPERTY, false, true);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action     
    }

}
