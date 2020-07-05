package jmri.jmrit.operations.automation;

import java.awt.Frame;
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
public class AutomationCopyAction extends AbstractAction {

    private Automation _automation;
    
    public AutomationCopyAction() {
        super(Bundle.getMessage("MenuCopyAutomation"));
    }

    public AutomationCopyAction(Automation automation) {
        super(Bundle.getMessage("MenuCopyAutomation"));
        _automation = automation;
    }

    AutomationCopyFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy train frame
        if (f == null || !f.isVisible()) {
            f = new AutomationCopyFrame(_automation);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}
