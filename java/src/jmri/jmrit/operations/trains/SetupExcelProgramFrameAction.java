// SetupExcelProgramFrameAction.java
package jmri.jmrit.operations.trains;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to launch the SetupExcelProgramFrame.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * @version $Revision: 22219 $
 */
public class SetupExcelProgramFrameAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 6600261536718738913L;

    public SetupExcelProgramFrameAction(String s) {
        super(s);
    }

    SetupExcelProgramManifestFrame f = null;

    public void actionPerformed(ActionEvent e) {
        // create a train scripts frame
        if (f != null && f.isVisible()) {
            f.dispose();
        }
        f = new SetupExcelProgramManifestFrame();
        f.initComponents();
        f.setExtendedState(Frame.NORMAL);
    }
}

/* @(#)SetupExcelProgramFrameAction.java */
