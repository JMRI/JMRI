// SystemInfoAction.java
package jmri.jmrix.lenz.swing.systeminfo;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a SystemInfo object.
 * <P>
 * The {@link SystemInfoFrame} is an information screen giving the hardware and
 * software versions of the Interface hardware and the command station
 *
 * @author	Paul Bender Copyright (C) 2003
 * @version	$Revision$
 */
public class SystemInfoAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 8341247362856299056L;
    jmri.jmrix.lenz.XNetSystemConnectionMemo _memo = null;

    public SystemInfoAction(String s, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
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

/* @(#)SystemInfoAction.java */
