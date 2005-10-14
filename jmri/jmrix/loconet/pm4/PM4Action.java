// PM4Action.java

package jmri.jmrix.loconet.pm4;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * Create and register a PM4Frame object.
 *
 * @author			Bob Jacobsen    Copyright (C) 2002
 * @version			$Revision: 1.4 $
 */
public class PM4Action 	extends AbstractAction {

    public PM4Action(String s) { super(s);}

    public PM4Action() { this("PM4 programmer");}

    public void actionPerformed(ActionEvent e) {
        // create a PM4Frame
        PM4Frame f = new PM4Frame();
        f.setVisible(true);
    }
}


/* @(#)PM4Action.java */
