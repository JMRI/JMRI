// LZV100Action.java

package jmri.jmrix.lenz.lzv100;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register an LZV100Frame object.
 * <P>
 * The {@link LZV100Frame} is a configuration tool for the LZV100 command 
 * Station.
 *
 * @author			Paul Bender    Copyright (C) 2003
 * @version			$Revision: 2.0 $
 */
public class LZV100Action extends AbstractAction {

    public LZV100Action(String s) { super(s);}
    public LZV100Action() {
        this("LZV100 Configuration Manager");
    }

    public void actionPerformed(ActionEvent e) {
        // create an LZV100Frame
        LZV100Frame f = new LZV100Frame();
        f.show();
    }
}

/* @(#)LZV100Action.java */
