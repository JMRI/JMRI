// SprogSlotMonAction.java

package jmri.jmrix.sprog.sprogslotmon;

import jmri.jmrix.sprog.SprogSlotManager;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * SprogSlotMonFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2001
 *              Andrew Crosland           (C) 2006 ported to SPROG
 * @version     $Revision: 1.1 $
 */

public class SprogSlotMonAction extends AbstractAction {

    public SprogSlotMonAction(String s) { super(s);}
    public SprogSlotMonAction() { this("SPROG Slot Monitor");}

    public void actionPerformed(ActionEvent e) {

        // create SlotManager if it doesn't exist
        SprogSlotManager.instance();

        // create a SprogSlotMonFrame
        SprogSlotMonFrame f = new SprogSlotMonFrame();
        f.show();

    }
}


/* @(#)SprogSlotMonAction.java */
