// SprogSlotMonAction.java
package jmri.jmrix.sprog.sprogslotmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.sprog.SprogCommandStation;

/**
 * Swing action to create and register a SprogSlotMonFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001 Andrew Crosland (C) 2006 ported to
 * SPROG
 * @version $Revision$
 */
public class SprogSlotMonAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 7965879364483000275L;

    public SprogSlotMonAction(String s) {
        super(s);
    }

    public SprogSlotMonAction() {
        this("SPROG Slot Monitor");
    }

    public void actionPerformed(ActionEvent e) {

        // create SlotManager if it doesn't exist
        SprogCommandStation.instance();

        // create a SprogSlotMonFrame
        SprogSlotMonFrame f = new SprogSlotMonFrame();
        f.setVisible(true);

    }
}


/* @(#)SprogSlotMonAction.java */
