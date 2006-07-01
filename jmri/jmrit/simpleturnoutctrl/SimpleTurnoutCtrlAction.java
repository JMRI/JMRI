/**
 * SimpleTurnoutCtrlAction.java
 *
 * Description:		Swing action to create and register a
 *       			SimpleTurnoutCtrlFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version
 */

package jmri.jmrit.simpleturnoutctrl;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class SimpleTurnoutCtrlAction 			extends AbstractAction {

    public SimpleTurnoutCtrlAction(String s) { super(s);}
    public SimpleTurnoutCtrlAction() { this("Turnouts");}

    public void actionPerformed(ActionEvent e) {

		SimpleTurnoutCtrlFrame f = new SimpleTurnoutCtrlFrame();
		f.show();

	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SimpleTurnoutCtrlAction.class.getName());
}


/* @(#)SimpleTurnoutCtrlAction.java */
