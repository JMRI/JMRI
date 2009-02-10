// LIUSBConfigAction.java

package jmri.jmrix.lenz.liusb;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register an LIUSBConfigFrame object.
 * <P>
 * The {@link LIUSBConfigFrame} is a configuration tool. 
 *
 * @author			Paul Bender    Copyright (C) 2009
 * @version			$Revision: 1.1 $
 */
public class LIUSBConfigAction extends AbstractAction {

    public LIUSBConfigAction(String s) { super(s);}
    public LIUSBConfigAction() {
        this("LIUSB Configuration Manager");
    }

    public void actionPerformed(ActionEvent e) {
        // create an LIUSBConfigFrame
        LIUSBConfigFrame f = new LIUSBConfigFrame();
        f.setVisible(true);
    }
}

/* @(#)LIUSBConfigAction.java */
