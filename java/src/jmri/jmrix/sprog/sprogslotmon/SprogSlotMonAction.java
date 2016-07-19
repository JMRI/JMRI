package jmri.jmrix.sprog.sprogslotmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.sprog.SprogCommandStation;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;

/**
 * Swing action to create and register a SprogSlotMonFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001 
 * @author      Andrew Crosland (C) 2006 ported to SPROG
 */
public class SprogSlotMonAction extends AbstractAction {

    private SprogSystemConnectionMemo _memo = null;

    public SprogSlotMonAction(String s,SprogSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public SprogSlotMonAction(SprogSystemConnectionMemo memo) {
        this("SPROG Slot Monitor",memo);
    }

    public void actionPerformed(ActionEvent e) {

        if(_memo.getCommandStation() == null) {
           // create SlotManager if it doesn't exist
           _memo.configureCommandStation();
        }

        SprogSlotMonFrame f = _memo.getCommandStation().getSprogSlotMonFrame();
        if( f == null ) { 
            // there isn't an SprogSlotMonFrame associated with
            // the command staiton for this connection, so create
            // a new SprogSlotMonFrame
            f = new SprogSlotMonFrame(_memo);
        }
        f.setVisible(true);
    }
}


/* @(#)SprogSlotMonAction.java */
