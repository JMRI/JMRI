package jmri.jmrit.operations.automation;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import jmri.InstanceManager;

/**
 * Action to resume all automations
 *
 * @author Daniel Boudreau Copyright (C) 2019
 */
public class AutomationResumeAction extends AbstractAction {

    public AutomationResumeAction() {
        super(Bundle.getMessage("MenuResumeAutomations"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InstanceManager.getDefault(AutomationManager.class).resumeAutomations();
    }
}
