package jmri.jmrix.lenz.swing.li101;

import java.awt.event.ActionEvent;
import jmri.jmrix.lenz.swing.AbstractXPressNetAction;

/**
 * Swing action to create and register an LI101Frame object.
 * <P>
 * The {@link LI101Frame} is a configuration tool. Note that this class does not
 * provide port services for the LI101; that's done elsewhere.
 *
 * @author	Paul Bender Copyright (C) 2003
 */
public class LI101Action extends AbstractXPressNetAction {

    public LI101Action(String s, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(s,memo);
    }

    public LI101Action(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this("LI101 Configuration Manager", memo);
    }

    public void actionPerformed(ActionEvent e) {
        // create an LI101Frame
        LI101Frame f = new LI101Frame(_memo);
        f.setVisible(true);
    }

}
