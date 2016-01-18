// ResetAutomationAction.java
package jmri.jmrit.operations.automation;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Action to reset an automation
 *
 * @author Daniel Boudreau Copyright (C) 2016
 * @version $Revision$
 */
public class ResetAutomationAction extends AbstractAction {

    private AutomationEditFrame _frame;

    public ResetAutomationAction(AutomationEditFrame frame) {
        super(Bundle.getMessage("MenuResetAutomation"));
        _frame = frame;
    }

    public void actionPerformed(ActionEvent e) {
        if (_frame._automation != null) {
            _frame._automation.reset();
        }
    }
}
