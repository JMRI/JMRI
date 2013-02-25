// JmriSRCPServerAction.java

package jmri.jmris.srcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * JmriSRCPServerControlFrame object
 *
 * @author              Paul Bender Copyright (C) 2009
 * @version             $Revision$
 */
 public class JmriSRCPServerAction extends AbstractAction {

    public JmriSRCPServerAction(String s) {
	super(s);
        }

    public JmriSRCPServerAction() { this("Start SRCP Jmri Server");}

    public void actionPerformed(ActionEvent e) {

		//JmriSRCPServerFrame f = new JmriSRCPServerFrame();
		//f.setVisible(true);
                JmriSRCPServer.instance().start();
	}
   static Logger log = LoggerFactory.getLogger(JmriSRCPServerAction.class.getName());
}


/* @(#)JmriSRCPServerAction.java */
