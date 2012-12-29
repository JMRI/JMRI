// TrainsScriptAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainsScriptFrame.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision$
 */
public class TrainsScriptAction extends AbstractAction {

	protected static final String getString(String key) {
		return ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle")
				.getString(key);
	}

	public TrainsScriptAction(String s, TrainsTableFrame frame) {
		super(s);
		this.frame = frame;
	}

	TrainsTableFrame frame; // the parent frame that is launching the TrainScriptFrame.

	TrainsScriptFrame f = null;

	public void actionPerformed(ActionEvent e) {
		// create a train scripts frame
		if (f != null && f.isVisible()) {
			f.dispose();
		}
		f = new TrainsScriptFrame();
		f.setLocation(frame.getLocation());
		f.initComponents();
		f.setExtendedState(Frame.NORMAL);
		f.setTitle(getString("MenuItemScripts"));
	}
}

/* @(#)TrainScriptAction.java */
