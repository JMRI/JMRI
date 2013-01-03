// BuildReportOptionAction.java

package jmri.jmrit.operations.setup;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to load the print options.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2012
 * @version $Revision: 17977 $
 */
public class BuildReportOptionAction extends AbstractAction {

	public BuildReportOptionAction() {
		this(Bundle.getMessage("TitleBuildReportOptions"));
	}

	public BuildReportOptionAction(String s) {
		super(s);
	}

	BuildReportOptionFrame f = null;

	public void actionPerformed(ActionEvent e) {
		// create a settings frame
		if (f == null || !f.isVisible()) {
			f = new BuildReportOptionFrame();
			f.initComponents();
		}
		f.setExtendedState(Frame.NORMAL);
		f.setVisible(true);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(BuildReportOptionAction.class.getName());
}

/* @(#)BuildReportOptionAction.java */
