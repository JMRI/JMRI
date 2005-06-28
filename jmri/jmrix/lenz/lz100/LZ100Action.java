// LZ100Action.java

package jmri.jmrix.lenz.lz100;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import java.util.ResourceBundle;

/**
 * Swing action to create and register an LZ100Frame object.
 * <P>
 * The {@link LZ100Frame} is a configuration tool for the LZ100 command 
 * Station.
 *
 * @author			Paul Bender    Copyright (C) 2005
 * @version			$Revision: 1.1 $
 */
public class LZ100Action extends AbstractAction {

    public LZ100Action(String s) { super(s);}
    public LZ100Action() {
        this("LZ100 Configuration Manager");
    }

    public void actionPerformed(ActionEvent e) {
        // create an LZ100Frame
 	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.lz100.LZ100Bundle");
        LZ100Frame f = new LZ100Frame(rb.getString("LZ100Config"));
        f.show();
    }
}

/* @(#)LZ100Action.java */
