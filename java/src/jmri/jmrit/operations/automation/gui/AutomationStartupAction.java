package jmri.jmrit.operations.automation.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Action to resume all automations
 *
 * @author Daniel Boudreau Copyright (C) 2019
 */
public class AutomationStartupAction extends AbstractAction {

    public AutomationStartupAction() {
        super(Bundle.getMessage("MenuStartupAutomation"));
    }
    
    AutomationStartupFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (f != null && f.isVisible()) {
            f.dispose();
        }
        f = new AutomationStartupFrame();
        f.initComponents();
        f.setExtendedState(Frame.NORMAL);
    }
}
