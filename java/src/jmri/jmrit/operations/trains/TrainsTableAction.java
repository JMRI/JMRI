// TrainsTableAction.java
package jmri.jmrit.operations.trains;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainTableFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class TrainsTableAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -7608766876884479719L;

    public TrainsTableAction(String s) {
        super(s);
    }

    public TrainsTableAction() {
        this(Bundle.getMessage("MenuTrains"));	// NOI18N
    }

    static TrainsTableFrame f = null;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void actionPerformed(ActionEvent e) {
        // create a train table frame
        if (f == null || !f.isVisible()) {
            f = new TrainsTableFrame();
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true);	// this also brings the frame into focus
    }
}

/* @(#)TrainsTableAction.java */
