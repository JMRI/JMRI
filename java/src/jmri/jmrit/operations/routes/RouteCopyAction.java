// RouteCopyAction.java

package jmri.jmrit.operations.routes;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a RouteCopyFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class RouteCopyAction extends AbstractAction {

	public RouteCopyAction(String s) {
		super(s);
	}

	String routeName;

	public RouteCopyAction(String s, String routeName) {
		super(s);
		this.routeName = routeName;
	}

	RouteCopyFrame f = null;

	public void actionPerformed(ActionEvent e) {
		// create a copy route frame
		if (f == null || !f.isVisible()) {
			f = new RouteCopyFrame();
		}
		if (routeName != null)
			f.setRouteName(routeName);
		f.setExtendedState(Frame.NORMAL);
		f.setVisible(true);
	}
}

/* @(#)RouteCopyAction.java */
