package jmri.jmrix.lenz.swing.liusb;

import java.awt.event.ActionEvent;
import jmri.jmrix.lenz.swing.AbstractXPressNetAction;

/**
 * Swing action to create and register an LIUSBConfigFrame object.
 * <P>
 * The {@link LIUSBConfigFrame} is a configuration tool.
 *
 * @author	Paul Bender Copyright (C) 2009
 */
public class LIUSBConfigAction extends AbstractXPressNetAction {

    public LIUSBConfigAction(String s, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(s,memo);
    }

    public LIUSBConfigAction(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this("LIUSB Configuration Manager", memo);
    }

    public void actionPerformed(ActionEvent e) {
        // create an LIUSBConfigFrame
        LIUSBConfigFrame f = new LIUSBConfigFrame(_memo);
        f.setVisible(true);
    }
}

