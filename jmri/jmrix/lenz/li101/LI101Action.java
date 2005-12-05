// LI101Action.java

package jmri.jmrix.lenz.li101;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register an LI101Frame object.
 * <P>
 * The {@link LI101Frame} is a configuration tool. Note that this
 * class does not provide port services for the LI101; that's
 * the {@link jmri.jmrix.lenz.li101.LI101Frame} class.
 *
 * @author			Paul Bender    Copyright (C) 2003
 * @version			$Revision: 2.1 $
 */
public class LI101Action extends AbstractAction {

    public LI101Action(String s) { super(s);}
    public LI101Action() {
        this("LI101 Configuration Manager");
    }

    public void actionPerformed(ActionEvent e) {
        // create an LI101Frame
        LI101Frame f = new LI101Frame();
        f.show();
    }
}

/* @(#)LI101Action.java */
