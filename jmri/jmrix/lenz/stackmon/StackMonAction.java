// StackMonAction.java

package jmri.jmrix.lenz.stackmon;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a StackMonFrame object
 *
 * @author	Paul Bender    Copyright (C) 2005
 * @version     $Revision: 1.1 $
 */

public class StackMonAction extends AbstractAction {

    public StackMonAction(String s) { super(s);}
    public StackMonAction() { this("Stack Monitor");}

    public void actionPerformed(ActionEvent e) {

        // create a StackMonFrame
        StackMonFrame f = new StackMonFrame();
        f.show();

    }
}


/* @(#)SlotMonAction.java */
