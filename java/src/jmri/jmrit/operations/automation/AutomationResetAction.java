package jmri.jmrit.operations.automation;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Action to reset an automation
 *
 * @author Daniel Boudreau Copyright (C) 2016
 */
@API(status = MAINTAINED)
public class AutomationResetAction extends AbstractAction {

    private AutomationTableFrame _frame;

    public AutomationResetAction(AutomationTableFrame frame) {
        super(Bundle.getMessage("MenuResetAutomation"));
        _frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_frame._automation != null) {
            _frame._automation.reset();
        }
    }
}
