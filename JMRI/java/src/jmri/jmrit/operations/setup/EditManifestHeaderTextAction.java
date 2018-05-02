package jmri.jmrit.operations.setup;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to open a window that allows a user to edit the manifest header
 * text strings.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2014
 * 
 */
public class EditManifestHeaderTextAction extends AbstractAction {

    public EditManifestHeaderTextAction() {
        this(Bundle.getMessage("TitleManifestHeaderText"));
    }

    public EditManifestHeaderTextAction(String s) {
        super(s);
    }

    EditManifestHeaderTextFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
        if (f == null || !f.isVisible()) {
            f = new EditManifestHeaderTextFrame();
            f.initComponents();
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }

//    private final static Logger log = LoggerFactory.getLogger(EditManifestHeaderTextAction.class);
}


