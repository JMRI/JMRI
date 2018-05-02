package jmri.jmrit.operations.automation;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action create a AutomationsTableFrame.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2016
 */
public class AutomationsTableFrameAction extends AbstractAction {

    public AutomationsTableFrameAction() {
        super(Bundle.getMessage("TitleAutomation"));
    }

    AutomationsTableFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a schedule table frame
        if (f == null || !f.isVisible()) {
            f = new AutomationsTableFrame();
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}


