// SE8Action.java

package jmri.jmrix.loconet.se8;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.loconet.LnTrafficController;


/**
 * Swing action to create and register a
 *       			SE8Frame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2002
 * @version	$Revision: 1.2 $
 */
public class SE8Action  extends AbstractAction {

    public SE8Action(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
        // create a PM4Frame
        SE8Frame f = new SE8Frame();
    }
}


/* @(#)SE8Action.java */
