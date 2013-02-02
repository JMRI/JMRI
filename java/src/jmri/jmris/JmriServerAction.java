// JmriServerAction.java

package jmri.jmris;

import org.apache.log4j.Logger;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * JmriServerControlFrame object
 *
 * @author              Paul Bender Copyright (C) 2010
 * @version             $Revision$
 */
 public class JmriServerAction extends AbstractAction {

    public JmriServerAction(String s) {
	super(s);
        }

    public JmriServerAction() { this("Start Jmri Server");}

    public void actionPerformed(ActionEvent e) {

		JmriServerFrame f = new JmriServerFrame();
		f.setVisible(true);

	}
   static Logger log = Logger.getLogger(JmriServerAction.class.getName());
}


/* @(#)JmriServerAction.java */
