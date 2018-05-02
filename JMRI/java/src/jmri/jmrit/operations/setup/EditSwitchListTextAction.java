package jmri.jmrit.operations.setup;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to open a window that allows a user to edit the switch list text
 * strings.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 */
public class EditSwitchListTextAction extends AbstractAction {

    public EditSwitchListTextAction() {
        this(Bundle.getMessage("TitleSwitchListText"));
    }

    public EditSwitchListTextAction(String s) {
        super(s);
    }

    EditSwitchListTextFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
        if (f == null || !f.isVisible()) {
            f = new EditSwitchListTextFrame();
            f.initComponents();
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }

//    private final static Logger log = LoggerFactory.getLogger(EditSwitchListTextAction.class);
}


