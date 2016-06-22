package jmri.jmrix.lenz.swing.lz100;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import jmri.jmrix.lenz.swing.AbstractXPressNetAction;

/**
 * Swing action to create and register an LZ100Frame object.
 * <P>
 * The {@link LZ100Frame} is a configuration tool for the LZ100 command Station.
 *
 * @author	Paul Bender Copyright (C) 2005
 */
public class LZ100Action extends AbstractXPressNetAction {

    public LZ100Action(String s, jmri.jmrix.lenz.XNetSystemConnectionMemo memo){
        super(s,memo);
    }

    public LZ100Action(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this("LZ100 Configuration Manager", memo);
    }

    public void actionPerformed(ActionEvent e) {
        // create an LZ100Frame
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.swing.lz100.LZ100Bundle");
        LZ100Frame f = new LZ100Frame(rb.getString("LZ100Config"), _memo);
        f.setVisible(true);
    }
}

