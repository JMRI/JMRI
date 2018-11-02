package jmri.jmrit.operations.automation.actions;

import javax.swing.JComboBox;
import jmri.InstanceManager;
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
            AutomationItem gotoAutomationItem = getAutomationItem().getGotoAutomationItem();
            if (gotoAutomationItem != null) {
                setRunning(true);
                // the old property = null unconditional branch
                firePropertyChange(ACTION_GOTO_CHANGED_PROPERTY, null, gotoAutomationItem);
            }
            finishAction(gotoAutomationItem != null);
        }
    }

    @Override
    public void cancelAction() {
        setRunning(false);
    }
    
    @Override
    public String getActionSuccessfulString() {
        return Bundle.getMessage("ButtonOK") + getGotoAutomationItemId();
    }
    
    private String getGotoAutomationItemId() {
        String id = "";
        if (getAutomationItem() != null) {
            AutomationItem gotoAutomationItem = getAutomationItem().getGotoAutomationItem();
            if (gotoAutomationItem != null && getAutomationItem().isGotoBranched()) {
                id = " -> " + gotoAutomationItem.getId();
            }
        }
        return id;
    }

    @Override
    public JComboBox<AutomationItem> getComboBox() {
        if (getAutomationItem() != null) {
            Automation automation = InstanceManager.getDefault(AutomationManager.class).getAutomationById(getAutomationItem().getId().split(Automation.REGEX)[0]);
            JComboBox<AutomationItem> cb = automation.getComboBox();
            cb.removeItem(getAutomationItem());
            cb.setSelectedItem(getAutomationItem().getGotoAutomationItem());
            return cb;
        }
        return null;
    }

}
