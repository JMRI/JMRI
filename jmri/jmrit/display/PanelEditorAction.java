package jmri.jmrit.display;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.*;

import javax.swing.*;

/**
 * Start a PanelEditor.  Creates a JFrame to contain it, and another to
 * connect it to.
 *
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Id: PanelEditorAction.java,v 1.1 2002-03-31 19:02:46 jacobsen Exp $
 * @see             jmri.jmrit.display.PanelEditorAction
 */
public class PanelEditorAction extends AbstractAction {

	public PanelEditorAction(String s) {
		super(s);
	}

    public void actionPerformed(ActionEvent e) {
        JFrame targetFrame = new JFrame("Panel");
        JPanel targetPanel = new JPanel();
        targetFrame.getContentPane().add(targetPanel);
        targetPanel.setLayout(null);

        targetFrame.show();

        PanelEditor panel = new PanelEditor();
        panel.setTarget(targetPanel);
        JFrame editFrame = new JFrame("PanelEditor");
        editFrame.getContentPane().add(panel);
        editFrame.pack();
        editFrame.show();

	}

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelEditorAction.class.getName());

}
