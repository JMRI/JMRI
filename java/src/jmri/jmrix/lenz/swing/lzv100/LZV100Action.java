// LZV100Action.java
package jmri.jmrix.lenz.swing.lzv100;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import jmri.jmrix.lenz.swing.AbstractXPressNetAction;

/**
 * Swing action to create and register an LZV100Frame object.
 * <P>
 * The {@link LZV100Frame} is a configuration tool for the LZV100 command
 * Station.
 *
 * @author	Paul Bender Copyright (C) 2003
 */
public class LZV100Action extends AbstractXPressNetAction {

    public LZV100Action(String s, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(s,memo);
    }

    public LZV100Action(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this("LZV100 Configuration Manager", memo);
    }

    public void actionPerformed(ActionEvent e) {
        // create an LZV100Frame
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.swing.lzv100.LZV100Bundle");
        LZV100Frame f = new LZV100Frame(rb.getString("LZV100Config"), _memo);
        f.setVisible(true);
    }
}
