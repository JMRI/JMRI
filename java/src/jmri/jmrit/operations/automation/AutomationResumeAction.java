package jmri.jmrit.operations.automation;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import jmri.InstanceManager;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Action to resume all automations
 *
 * @author Daniel Boudreau Copyright (C) 2019
 */
@API(status = MAINTAINED)
public class AutomationResumeAction extends AbstractAction {

    public AutomationResumeAction() {
        super(Bundle.getMessage("MenuResumeAutomations"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InstanceManager.getDefault(AutomationManager.class).resumeAutomations();
    }
}
