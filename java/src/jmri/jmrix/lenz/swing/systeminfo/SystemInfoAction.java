package jmri.jmrix.lenz.swing.systeminfo;

import java.awt.event.ActionEvent;
import jmri.jmrix.lenz.swing.AbstractXPressNetAction;

/**
 * Swing action to create and register a SystemInfo object.
 * <P>
 * The {@link SystemInfoFrame} is an information screen giving the hardware and
 * software versions of the Interface hardware and the command station
 *
 * @author	Paul Bender Copyright (C) 2003
 */
public class SystemInfoAction extends AbstractXPressNetAction {

    public SystemInfoAction(String s, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(s,memo);
    }

    public SystemInfoAction(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this("Xpressnet System Information", memo);
    }

    public void actionPerformed(ActionEvent e) {
        // create an SystemInfoFrame
        SystemInfoFrame f = new SystemInfoFrame(_memo);
        f.setVisible(true);
    }
}

