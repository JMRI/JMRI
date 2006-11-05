// AutomatTableAction.java

package jmri.jmrit.automat.monitor;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * AutomatTable GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision: 1.3 $
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
        f.setVisible(true);
    }

}
/* @(#)AutomatTableAction.java */
