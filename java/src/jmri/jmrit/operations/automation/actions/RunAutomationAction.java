package jmri.jmrit.operations.automation.actions;

import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrit.operations.automation.Automation;
import jmri.jmrit.operations.automation.AutomationManager;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

@API(status = MAINTAINED)
public class RunAutomationAction extends Action {

    private static final int _code = ActionCodes.RUN_AUTOMATION;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("RunAutomation");
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            Automation automation = getAutomationItem().getAutomationToRun();
            if (automation != null) {
                setRunning(true);
                automation.run();
            }
            finishAction(automation != null);
        }
    }

    @Override
    public void cancelAction() {
        // no cancel for this action
    }

    @Override
    public JComboBox<Automation> getComboBox() {
        if (getAutomationItem() != null) {
            JComboBox<Automation> cb = InstanceManager.getDefault(AutomationManager.class).getComboBox();
            cb.setSelectedItem(getAutomationItem().getAutomationToRun());
            return cb;
        }
        return null;
    }
}
