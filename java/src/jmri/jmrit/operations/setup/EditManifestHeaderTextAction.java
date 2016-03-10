// EditManifestHeaderTextAction.java
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
 * @version $Revision: 21656 $
 */
public class EditManifestHeaderTextAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -1916894570578033881L;

    public EditManifestHeaderTextAction() {
        this(Bundle.getMessage("TitleManifestHeaderText"));
    }

    public EditManifestHeaderTextAction(String s) {
        super(s);
    }

    EditManifestHeaderTextFrame f = null;

    public void actionPerformed(ActionEvent e) {
        // create a settings frame
        if (f == null || !f.isVisible()) {
            f = new EditManifestHeaderTextFrame();
            f.initComponents();
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true);	// this also brings the frame into focus
    }

//    private final static Logger log = LoggerFactory.getLogger(EditManifestHeaderTextAction.class.getName());
}

/* @(#)EditManifestHeaderTextAction.java */
