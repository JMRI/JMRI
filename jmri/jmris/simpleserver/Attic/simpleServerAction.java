// simpleServerAction.java

package jmri.jmris.simpleserver;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * simpleServerControlFrame object
 *
 * @author              Paul Bender Copyright (C) 2010
 * @version             $Revision: 1.1 $
 */
 public class simpleServerAction extends AbstractAction {

    public simpleServerAction(String s) {
	super(s);
        }

    public simpleServerAction() { this("Start Simple Jmri Server");}

    public void actionPerformed(ActionEvent e) {

		simpleServerFrame f = new simpleServerFrame();
		f.setVisible(true);

	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(simpleServerAction.class.getName());
}


/* @(#)simpleServerAction.java */
