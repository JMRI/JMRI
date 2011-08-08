// DispatcherAction.java

package jmri.jmrit.dispatcher;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

/**
 * Swing action to create and register a
 * DispatcherFrame
 *
 * @author			Dave Duchamp    Copyright (C) 2008
 * @version			$Revision$
 */

public class DispatcherAction extends AbstractAction {

	static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");

    public DispatcherAction(String s) {
        super(s);
    }

    public DispatcherAction() {
        this(rb.getString("TitleDispatcher"));
    }
	
	DispatcherFrame f = null;

    public void actionPerformed(ActionEvent e) {
		// check that Transits have been defined and are available
		if (jmri.InstanceManager.transitManagerInstance().getSystemNameList().size()<=0) {
			// Inform the user that there are no Transits available, and don't open the window
			javax.swing.JOptionPane.showMessageDialog(null,rb.getString("NoTransitsMessage"));
			return;
		}
        // create a Dispatcher window or activate the existing one
		if (f == null) {
			f = DispatcherFrame.instance();
		}
        f.setVisible(true);
    }

}

/* @(#)DispatcherAction.java */
