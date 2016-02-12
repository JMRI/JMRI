package jmri.jmrit.operations.automation.actions;

import javax.swing.JComboBox;
import jmri.jmrit.operations.automation.Automation;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.automation.AutomationManager;

public class GotoAction extends Action {

    private static final int _code = ActionCodes.GOTO;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("Goto");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            AutomationItem automationItem = getAutomationItem().getGotoAutomationItem();
            if (automationItem != null) {
                setRunning(true);
                // the old property = null unconditional branch 
                firePropertyChange(ACTION_GOTO_CHANGED_PROPERTY, null, automationItem);
            }
            finishAction(automationItem != null);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action
    }

    @Override
    public JComboBox<AutomationItem> getComboBox() {
        if (getAutomationItem() != null) {
            Automation automation = AutomationManager.instance().getAutomationById(getAutomationItem().getId().split(Automation.REGEX)[0]);
            JComboBox<AutomationItem> cb = automation.getComboBox();
            cb.setSelectedItem(getAutomationItem().getGotoAutomationItem());
            return cb;
        }
        return null;
    }

}
