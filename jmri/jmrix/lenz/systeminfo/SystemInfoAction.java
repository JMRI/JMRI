// SystemInfoAction.java

package jmri.jmrix.lenz.systeminfo;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a SystemInfo object.
 * <P>
 * The {@link SystemInfoFrame} is an information screen giving
 * the hardware and software versions of the Interface hardware
 * and the command station
 *
 * @author			Paul Bender    Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
public class SystemInfoAction extends AbstractAction {

    public SystemInfoAction(String s) { super(s);}
    public SystemInfoAction() {
        this("Xpressnet System Information");
    }

    public void actionPerformed(ActionEvent e) {
        // create an SystemInfoFrame
        SystemInfoFrame f = new SystemInfoFrame();
        f.show();
    }
}

/* @(#)SystemInfoAction.java */
