// SlotMonAction.java

package jmri.jmrix.loconet.slotmon;

import jmri.jmrix.loconet.SlotManager;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * SlotMonFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2001
 * @version     $Revision: 1.3 $
 */

public class SlotMonAction extends AbstractAction {

    public SlotMonAction(String s) { super(s);}
    public SlotMonAction() { this("Slot monitor");}

    public void actionPerformed(ActionEvent e) {

        // create SlotManager if it doesn't exist
        SlotManager.instance();

        // create a SlotMonFrame
        SlotMonFrame f = new SlotMonFrame();
        f.show();

    }
}


/* @(#)SlotMonAction.java */
