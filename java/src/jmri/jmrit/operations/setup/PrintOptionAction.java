// PrintOptionAction.java

package jmri.jmrit.operations.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to load the print options.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2009
 * @version $Revision$
 */
public class PrintOptionAction extends AbstractAction {

	public PrintOptionAction() {
		this(Bundle.getMessage("TitlePrintOptions"));
	}

	public PrintOptionAction(String s) {
		super(s);
	}

	PrintOptionFrame f = null;

	public void actionPerformed(ActionEvent e) {
		// create a settings frame
		if (f == null || !f.isVisible()) {
			f = new PrintOptionFrame();
			f.initComponents();
		}
		f.setExtendedState(Frame.NORMAL);
	   	f.setVisible(true);	// this also brings the frame into focus
	}

	static Logger log = LoggerFactory.getLogger(PrintOptionAction.class
			.getName());
}

/* @(#)PrintOptionAction.java */
