// LI101Action.java

package jmri.jmrix.lenz.li101;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

//import jmri.jmrix.loconet.LnTrafficController;


/**
 * LI101Action.java
 *
 * Description:		Swing action to create and register an
 *       			LI101Frame object
 *
 * @author			Paul Bender    Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
public class LI101Action 			extends AbstractAction {

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
