// AutomatTableAction.java

package jmri.jmrit.automat.monitor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.util.AbstractFrameAction;

import javax.swing.AbstractAction;
import javax.swing.JButton;

/**
 * Swing action to create and register a
 * AutomatTable GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision: 1.1 $
 */

public class AutomatTableAction extends AbstractAction {

    public AutomatTableAction(String actionName) {
        super(actionName);
    }

    AutomatTableDataModel m;

    AutomatTableFrame f;

    public void actionPerformed(ActionEvent e) {
        // create the frame
        f = new AutomatTableFrame(new AutomatTableDataModel());
        f.pack();
        f.show();
    }

}
/* @(#)AutomatTableAction.java */
