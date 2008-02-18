/**
 * SimpleLightCtrlAction.java
 *
 * Description:		Swing action to create and register a
 *       			SimpleTurnoutCtrlFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version
 */

package jmri.jmrit.simplelightctrl;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class SimpleLightCtrlAction 			extends AbstractAction {

    public SimpleLightCtrlAction(String s) { 
	super(s);

	// disable ourself if there is no primary light manager available
        if (jmri.InstanceManager.lightManagerInstance()==null ||
            (((jmri.managers.AbstractProxyManager)jmri.InstanceManager
                                                 .lightManagerInstance())
                                                 .systemLetter()=='\0')) {
            setEnabled(false);
        }



    }
    public SimpleLightCtrlAction() { this("Lights");}

    public void actionPerformed(ActionEvent e) {

		SimpleLightCtrlFrame f = new SimpleLightCtrlFrame();
		f.setVisible(true);

	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SimpleLightCtrlAction.class.getName());
}


/* @(#)SimpleLightCtrlAction.java */
