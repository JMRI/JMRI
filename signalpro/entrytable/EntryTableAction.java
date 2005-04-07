// EntryTableAction.java

package signalpro.entrytable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.util.AbstractFrameAction;

import javax.swing.AbstractAction;
import javax.swing.JButton;

/**
 * Swing action to create and register a
 * EntryTableAction GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision: 1.1.1.1 $
 */

public class EntryTableAction extends AbstractAction {

    public EntryTableAction(String actionName) {
        super(actionName);
    }

    EntryTableDataModel m;

    EntryTableFrame f;

    public void actionPerformed(ActionEvent e) {
        // create the frame
        f = new EntryTableFrame(new EntryTableDataModel());
        f.pack();
        f.show();
    }

}
/* @(#)EntryTableAction.java */
