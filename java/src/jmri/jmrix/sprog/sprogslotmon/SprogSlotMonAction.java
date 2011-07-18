// SprogSlotMonAction.java

package jmri.jmrix.sprog.sprogslotmon;

import jmri.jmrix.sprog.SprogCommandStation;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * SprogSlotMonFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2001
 *              Andrew Crosland           (C) 2006 ported to SPROG
 * @version     $Revision$
 */

public class SprogSlotMonAction extends AbstractAction {

    public SprogSlotMonAction(String s) { super(s);}
    public SprogSlotMonAction() { this("SPROG Slot Monitor");}

    public void actionPerformed(ActionEvent e) {

        // create SlotManager if it doesn't exist
        SprogCommandStation.instance();

        // create a SprogSlotMonFrame
        SprogSlotMonFrame f = new SprogSlotMonFrame();
        f.setVisible(true);

    }
}


/* @(#)SprogSlotMonAction.java */
